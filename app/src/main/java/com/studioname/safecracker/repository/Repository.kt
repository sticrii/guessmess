package com.studioname.safecracker.repository

import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.database.database
import com.google.firebase.database.getValue
import com.studioname.safecracker.model.DisplayName
import com.studioname.safecracker.model.Player
import com.studioname.safecracker.model.PlayerId
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

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

    suspend fun createLobby(player: Player): String{
        var roomCode: String

        do {
            roomCode = (100000..999999).random().toString()
            val roomExists = db.child("rooms").child(roomCode)
                .get().await().exists()
            //to do: napraviti pouzdaniju proveru

        } while(roomExists)

        val lobby = mapOf(
            "phase" to "LOBBY",
            "round" to 1,
            "hostId" to player.id.value,
            "players" to mapOf(
                player.id.value to mapOf(
                    "name" to player.displayName.value,
                    "failedAttempts" to 0,
                    "isEliminated" to false
                )
            )
        )
        db.child("rooms").child(roomCode).setValue(lobby).await()
        return roomCode
    }
}