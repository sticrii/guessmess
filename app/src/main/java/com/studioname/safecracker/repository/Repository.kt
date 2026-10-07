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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import com.google.firebase.database.MutableData
import com.google.firebase.database.Transaction
import com.studioname.safecracker.model.Hint

@Singleton
class Repository @Inject constructor() {
    private val db = Firebase.database.reference

    private companion object {
        const val LOBBY_CODE_CHARS = "23456789EHKNRWXYZ"
        const val LOBBY_CODE_LENGTH = 6
    }

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

    fun getLobby(lobbyCode: String): DatabaseReference {
        val code = lobbyCode.trim().uppercase()

        if (
            code.length != LOBBY_CODE_LENGTH ||
            !code.all { it in LOBBY_CODE_CHARS }
        ) {
            error("Code must consist of digits and characters.")
        }

        return db.child("lobbies").child(code)
    }


    // retrieves current lobby host, its a flow function because upon leaving a host can change
    fun getLobbyHost(lobbyCode: String): Flow<Player> = callbackFlow {
        val lobbyReference = this@Repository.getLobby(lobbyCode)

        val listener = lobbyReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val hostId = snapshot.child("hostId").getValue(String::class.java)

                if (
                    hostId == null ||
                    !snapshot.child("players").child(hostId).exists()
                ) {
                    this@Repository.promoteHostIfMissing(lobbyCode)
                    return
                }

                if (hostId != null) {
                    launch { // we have to launch a coroutine so we can call a suspend function
                        try {
                            val host = this@Repository.getPlayer(PlayerId(hostId))

                            if (host != null) {
                                trySend(host)
                            }
                        } catch (e: Exception) {
                            close(e)
                        }
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        awaitClose { lobbyReference.removeEventListener(listener) }
    }


    private fun promoteHostIfMissing(lobbyCode: String) {
        val lobbyReference = getLobby(lobbyCode)

        lobbyReference.runTransaction(object : Transaction.Handler {
            override fun doTransaction(currentData: MutableData): Transaction.Result {
                if (currentData.value == null) return Transaction.abort()

                val players = currentData.child("players")
                val currentHostId =
                    currentData.child("hostId").getValue(String::class.java)

                if (
                    currentHostId != null &&
                    players.child(currentHostId).value != null
                ) {
                    return Transaction.abort()
                }

                // host missing - another player takees the role
                val nextHostId = players.children.firstOrNull()?.key
                    ?: return Transaction.abort()

                currentData.child("hostId").value = nextHostId
                return Transaction.success(currentData)
            }

            override fun onComplete(
                error: DatabaseError?,
                committed: Boolean,
                currentData: DataSnapshot?
            ) {
                if (error != null) {
                    println("[ERROR]: Failed to promote lobby host: ${error.message}")
                }
            }
        })
    }

    suspend fun createLobby(player: Player): String {
        var lobbyCode: String

        do {
            lobbyCode = (1..LOBBY_CODE_LENGTH)
                .map { LOBBY_CODE_CHARS.random() }
                .joinToString("")

            val lobbyExists = db.child("lobbies").child(lobbyCode)
                .get().await().exists()
        } while (lobbyExists)

        val lobby = mapOf(
            "phase" to "LOBBY",
            "round" to 1,
            "hostId" to player.id.value,
            "players" to mapOf(
                player.id.value to createPlayerData(player)
            )
        )
        val lobbyReference = db.child("lobbies").child(lobbyCode)
        val playerReference =
            lobbyReference.child("players").child(player.id.value)

        //deleting players from lobby if they close app

        playerReference.onDisconnect().removeValue().await()
        lobbyReference.setValue(lobby).await()
        return lobbyCode
    }

    suspend fun joinLobby(lobbyCode: String, player: Player) {
        val lobbyReference = this@Repository.getLobby(lobbyCode)
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
            .onDisconnect().removeValue().await()

        //firebase registers disconnection/closing app and deletes player

        lobbyReference.updateChildren(
            mapOf(
                "players/${player.id.value}" to createPlayerData(player),
                "emptySince" to null
                //assigns player to the lobby and deletes empty since
            )
        ).await()
    }

    suspend fun leaveLobby(lobbyCode: String, playerId: String) {
        val lobbyReference = this@Repository.getLobby(lobbyCode)
        val playersReference = lobbyReference.child("players")

        val currentHost = this@Repository.getLobbyHost(lobbyCode).first() // .first() so we get only 1 value, since Flow<Player> is a stream of data

        playersReference.child(playerId).removeValue().await()

        val playersSnapshot = playersReference.get().await()
        val nextHostId = playersSnapshot.children.firstOrNull()?.key

        if (nextHostId == null) {
            lobbyReference
                .child("emptySince")
                .setValue(com.google.firebase.database.ServerValue.TIMESTAMP)
                .await()
                //not deleting the lobby, marks the start time for timeout
            return
        }

        if (currentHost.id.value == playerId) {
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


    suspend fun cleanupExpiredEmptyLobbies() {
        val lobbiesReference = db.child("lobbies")
        val lobbiesSnapshot = lobbiesReference.get().await()
        val now = System.currentTimeMillis()
        val timeoutMillis = 15 * 60 * 1000L

        lobbiesSnapshot.children.forEach { lobby ->
            val lobbyCode = lobby.key ?: return@forEach

            // no deletion if lobby has players
            if (lobby.child("players").hasChildren()) return@forEach

            val lobbyReference = lobbiesReference.child(lobbyCode)
            val emptySince =
                lobby.child("emptySince").getValue(Long::class.java)

            if (emptySince == null) {
                // if everyone closes app, mark the time
                lobbyReference
                    .child("emptySince")
                    .setValue(com.google.firebase.database.ServerValue.TIMESTAMP)
                    .await()
                return@forEach
            }

            if (now - emptySince >= timeoutMillis) {
                // second check before deletion
                val currentLobby = lobbyReference.get().await()
                val currentEmptySince =
                    currentLobby.child("emptySince").getValue(Long::class.java)

                if (
                    !currentLobby.child("players").hasChildren() &&
                    currentEmptySince == emptySince
                ) {
                    lobbyReference.removeValue().await()
                }
            }
        }
    }

    fun startGame(lobbyCode: String) {
        val lobbyReference = this@Repository.getLobby(lobbyCode)

        lobbyReference.child("phase").setValue("PICK_ROOM")
    }

    // continously listen to lobby phase change
    fun getLobbyPhase(lobbyCode: String): Flow<String?> = callbackFlow {
        val lobbyReference = this@Repository.getLobby(lobbyCode)
        val ref = lobbyReference.child("phase")

        val listener = ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                trySend(snapshot.getValue(String::class.java))
            }
            override fun onCancelled(error: DatabaseError) {}
        })
        awaitClose { ref.removeEventListener(listener) }
    }

    fun getRoomHints(
        lobbyCode: String,
        roomName: String
    ): Flow<List<Hint>> {
        val roomKey = roomName
            .trim()
            .lowercase()
            .replace(" ", "_")

        require(roomKey.matches(Regex("^[a-z0-9_]+$"))) {
            "Room name can only contain lowercase letters, numbers, and underscores."
        }

        val hintsReference = getLobby(lobbyCode)
            .child("rooms")
            .child(roomKey)
            .child("hints")

        return observeHints(hintsReference)

        //Emits the hints currently available in the specified room.
    }


    fun getPlayerHints(
        lobbyCode: String,
        playerId: PlayerId
    ): Flow<List<Hint>> {
        val hintsReference = getLobby(lobbyCode)
            .child("players")
            .child(playerId.value)
            .child("hints")

        return observeHints(hintsReference)
        //Emits the current player's private hints and updates when they change.
    }

    private fun observeHints(
        hintsReference: DatabaseReference
    ): Flow<List<Hint>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val hints = snapshot.children.mapNotNull { hintSnapshot ->
                    val hintId = hintSnapshot.key ?: return@mapNotNull null
                    val type = hintSnapshot.child("type")
                        .getValue(String::class.java)
                        ?: return@mapNotNull null
                    val content = hintSnapshot.child("content")
                        .getValue(String::class.java)
                        ?: return@mapNotNull null

                    Hint(
                        id = hintId,
                        type = type,
                        content = content
                    )
                }

                trySend(hints)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        hintsReference.addValueEventListener(listener)

        awaitClose {
            hintsReference.removeEventListener(listener)
        }
    }
    //Converts Firebase value changes into a Flow and removes the listener
    //when collection stops.

}
