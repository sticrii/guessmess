package com.studioname.safecracker.repository

import android.R
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.database
import com.google.firebase.database.getValue
import com.studioname.safecracker.model.DisplayName
import com.studioname.safecracker.model.Player
import com.studioname.safecracker.model.PlayerId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose

@Singleton
class Repository @Inject constructor() {
    private val db = Firebase.database.reference

    // creates a new player if one doesnt exit, else overrides the player data
    suspend fun setPlayer(player: Player) {
        val playerMap = mapOf(
            "id" to player.id.value,
            "displayName" to player.displayName.value
        )

        db.child("players/${player.id.value}").setValue(playerMap).await()

        println("[SUCCESS]: Successfully added/overwritten player '${player.displayName.value}' to the database")
    }

    suspend fun getPlayer(playerId: PlayerId): Player? {
        val snapshot = db.child("players").child(playerId.value)
            .get().await()

        //pre se koristio ceo PlayerId objekat, sad value

        val displayName = snapshot.child("displayName")
            .getValue(String::class.java) //converts from JSON to Player type
            ?: return null

        return Player(id = playerId, displayName = DisplayName(displayName))
    }

    // TODO: migrate from firebase anonymous auth to google play service auth
    suspend fun getCurrentPlayerId(): String {
        val user = Firebase.auth.currentUser ?: Firebase.auth.signInAnonymously().await().user
        println("THIS IS A TEST: ${user?.uid}")
        return user?.uid ?: error("[ERROR]: Could not retrieve PlayerID after anonymous auth")
    }

    suspend fun getCurrentPlayer(): Player? {
        val uid: String = this.getCurrentPlayerId()
        val playerId: PlayerId = PlayerId(uid)
        return this.getPlayer(playerId)
    }

    private fun createPlayerData(player: Player): Map<String, Any> {
        return mapOf(
            "name" to player.displayName.value,
            "failedAttempts" to 0,
            "isEliminated" to false
        )
    }

    suspend fun createLobby(player: Player): String {
        var lobbyCode: String

        do {
            lobbyCode = (100000..999999).random().toString()
            val lobbyExists = db.child("lobbies").child(lobbyCode)
                .get().await().exists()
            // TODO: napraviti pouzdaniju proveru

        } while (lobbyExists)

        val lobby = mapOf(
            "phase" to "LOBBY",
            "round" to 1,
            "hostId" to player.id.value,
            "players" to mapOf(
                player.id.value to createPlayerData(player)
            )
        )
        db.child("lobbies").child(lobbyCode).setValue(lobby).await()
        return lobbyCode
    }

    suspend fun joinLobby(lobbyCode: String, player: Player) {
        val code = lobbyCode.trim()

        if (code.length != 6 || !code.all { it.isDigit() }) {
            error("Code must have 6 digits.")
        }

        val lobbyReference = db.child("lobbies").child(code)
        val lobbySnapshot = lobbyReference.get().await()

        if (!lobbySnapshot.exists()) {
            error("A lobby with that code doesn't exist.")
        }

        val phase = lobbySnapshot.child("phase")
            .getValue(String::class.java)

        if (phase != "LOBBY") {
            error("This lobby is no longer accepting players.")
        }

        lobbyReference
            .child("players").child(player.id.value)
            .setValue(createPlayerData(player)).await()
    }

    suspend fun leaveLobby(lobbyCode: String, playerId: String) {
        val lobbyReference = db.child("lobbies").child(lobbyCode.trim())
        val playersReference = lobbyReference.child("players")

        val currentHostId = lobbyReference
            .child("hostId")
            .get().await().getValue(String::class.java)

        playersReference.child(playerId).removeValue().await()

        val playersSnapshot = playersReference.get().await()
        val nextHostId = playersSnapshot.children.firstOrNull()?.key

        if (nextHostId == null) {
            lobbyReference.removeValue().await()
            return
        }

        if (currentHostId == playerId) {
            lobbyReference.child("hostId").setValue(nextHostId).await()
        }
    }

    // callbackFlow bridges the gap between callback style (firebase, old)
    // and flow style (what we are using, similar to react, think useEffect())
    fun getLobbyPlayers(lobbyCode: String): Flow<List<Player>> = callbackFlow {
        val ref = db.child("lobbies").child(lobbyCode.trim()).child("players")

        // firebase uses callback style, every time a value is changed (a player joins/leaves)
        // it will call onDataChange, we define it here
        val listener = ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // snapshot.children is the list of all players in the lobby (JSON)
                val players = snapshot.children.mapNotNull { child ->
                    val id = child.key ?: return@mapNotNull null
                    val name = child.child("name").getValue(String::class.java) ?: return@mapNotNull null
                    try {
                        Player(id = PlayerId(id), displayName = DisplayName(name))
                    } catch (e: Exception) {
                        null
                    }
                }
                trySend(players) // translates to flow style
            }
            override fun onCancelled(error: DatabaseError) {} // in case of error
        })

        // when flow stops listening, this closes teh firebase listener
        awaitClose { ref.removeEventListener(listener) }
    }
}
