package com.studioname.safecracker.repository

import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.database.database
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
        return db.child("players/${playerId}")
            .get()
            .await()
            .getValue(Player::class.java) // converts from JSON to Player type
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
}