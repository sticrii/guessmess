package com.studioname.safecracker.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.MutableData
import com.google.firebase.database.ServerValue
import com.google.firebase.database.Transaction
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import com.google.firebase.database.getValue
import com.studioname.safecracker.model.DisplayName
import com.studioname.safecracker.model.Player
import com.studioname.safecracker.model.PlayerId
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MainRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    private val db = Firebase.database.reference

    companion object {
        const val LOBBY_CODE_CHARS = "23456789EHKNRWXYZ"
        const val LOBBY_CODE_LENGTH = 6
        private val DISPLAY_NAME_KEY = stringPreferencesKey("display_name")
    }

    val savedDisplayName: Flow<String> = dataStore.data.map { preferences ->
        preferences[DISPLAY_NAME_KEY] ?: ""
    }

    suspend fun saveDisplayName(name: String) {
        dataStore.edit { preferences ->
            preferences[DISPLAY_NAME_KEY] = name
        }
    }

    suspend fun setPlayer(player: Player) {
        val playerMap = mapOf(
            "id" to player.id.value,
            "displayName" to player.displayName.value
        )
        db.child("players/${player.id.value}").setValue(playerMap).await()
        println("[SUCCESS]: Successfully added/overwritten player '${player.displayName.value}' to the database")
    }

    suspend fun getPlayer(playerId: PlayerId): Player? {
        val snapshot = db.child("players").child(playerId.value).get().await()
        val displayName = snapshot.child("displayName").getValue(String::class.java) ?: return null
        return Player(id = playerId, displayName = DisplayName(displayName))
    }

    suspend fun getCurrentPlayerId(): String {
        val user = Firebase.auth.currentUser ?: Firebase.auth.signInAnonymously().await().user
        return user?.uid ?: error("[ERROR]: Could not retrieve PlayerID after anonymous auth")
    }

    suspend fun getCurrentPlayer(): Player? {
        val uid = getCurrentPlayerId()
        return getPlayer(PlayerId(uid))
    }

    fun getLobby(lobbyCode: String): DatabaseReference {
        val code = lobbyCode.trim().uppercase()
        if (code.length != LOBBY_CODE_LENGTH || !code.all { it in LOBBY_CODE_CHARS }) {
            error("Code must consist of digits and characters.")
        }
        return db.child("lobbies").child(code)
    }

    fun getLobbyHost(lobbyCode: String): Flow<Player> = callbackFlow {
        val lobbyReference = getLobby(lobbyCode)
        val listener = lobbyReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val hostId = snapshot.child("hostId").getValue(String::class.java)
                if (hostId == null || !snapshot.child("players").child(hostId).exists()) {
                    promoteHostIfMissing(lobbyCode)
                    return
                }
                launch {
                    try {
                        val host = getPlayer(PlayerId(hostId))
                        if (host != null) {
                            trySend(host)
                        }
                    } catch (e: Exception) {
                        close(e)
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
        awaitClose { lobbyReference.removeEventListener(listener) }
    }

    fun promoteHostIfMissing(lobbyCode: String) {
        val lobbyReference = getLobby(lobbyCode)
        lobbyReference.runTransaction(object : Transaction.Handler {
            override fun doTransaction(currentData: MutableData): Transaction.Result {
                if (currentData.value == null) return Transaction.abort()
                val players = currentData.child("players")
                val currentHostId = currentData.child("hostId").getValue(String::class.java)
                if (currentHostId != null && players.child(currentHostId).value != null) {
                    return Transaction.abort()
                }
                val nextHostId = players.children.firstOrNull()?.key ?: return Transaction.abort()
                currentData.child("hostId").value = nextHostId
                return Transaction.success(currentData)
            }

            override fun onComplete(error: DatabaseError?, committed: Boolean, currentData: DataSnapshot?) {
                if (error != null) {
                    println("[ERROR]: Failed to promote lobby host: ${error.message}")
                }
            }
        })
    }

    fun getLobbyPlayers(lobbyCode: String): Flow<List<Player>> = callbackFlow {
        val ref = getLobby(lobbyCode).child("players")
        val listener = ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val players = snapshot.children.mapNotNull { child ->
                    val id = child.key ?: return@mapNotNull null
                    val name = child.child("name").getValue(String::class.java) ?: return@mapNotNull null
                    try {
                        Player(id = PlayerId(id), displayName = DisplayName(name))
                    } catch (e: Exception) {
                        null
                    }
                }
                trySend(players)
            }
            override fun onCancelled(error: DatabaseError) {}
        })
        awaitClose { ref.removeEventListener(listener) }
    }

    fun getLobbyPhase(lobbyCode: String): Flow<String?> = callbackFlow {
        val ref = getLobby(lobbyCode).child("phase")
        val listener = ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                trySend(snapshot.getValue(String::class.java))
            }
            override fun onCancelled(error: DatabaseError) {}
        })
        awaitClose { ref.removeEventListener(listener) }
    }

    suspend fun leaveLobby(lobbyCode: String, playerId: String) {
        val lobbyReference = this@MainRepository.getLobby(lobbyCode)
        val playersReference = lobbyReference.child("players")

        val currentHost = this@MainRepository.getLobbyHost(lobbyCode).first()

        playersReference.child(playerId).removeValue().await()

        val playersSnapshot = playersReference.get().await()
        val nextHostId = playersSnapshot.children.firstOrNull()?.key

        if (nextHostId == null) {
            lobbyReference.child("emptySince").setValue(ServerValue.TIMESTAMP).await()
            return
        }

        if (currentHost.id.value == playerId) {
            lobbyReference.child("hostId").setValue(nextHostId).await()
        }
    }

    suspend fun getRoomHintIds(roomKey: String): List<String> {
        val snapshot = db.child("hints").child(roomKey).get().await()
        return snapshot.children.mapNotNull { it.key }
    }

}
