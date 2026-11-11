package com.studioname.safecracker.repository

import coil3.memory.MemoryCache
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue
import com.studioname.safecracker.enums.TradeState
import com.studioname.safecracker.model.Player
import com.studioname.safecracker.model.PlayerId
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton
import com.studioname.safecracker.model.Trade
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

@Singleton
class DiscussRepository @Inject constructor(
    private val mainRepository: MainRepository
) {
    suspend fun getCurrentPlayerId(): String = mainRepository.getCurrentPlayerId()
    fun getLobbyPlayers(lobbyCode: String): Flow<List<Player>> = mainRepository.getLobbyPlayers(lobbyCode)

    fun getLobbyHost(lobbyCode: String): Flow<Player> = mainRepository.getLobbyHost(lobbyCode)

    private fun generateTradeId(senderId: String, receiverId: String): String {
        val charPool = ('A'..'Z') + ('0'..'9')
        val randomChars = (1..3).map { charPool.random() }.joinToString("")

        val id = "TS" + senderId.take(3).uppercase() +
                "R" + receiverId.take(3).uppercase() +
                randomChars

        return id
    }

    fun createRequest(lobbyCode: String, senderId: String, receiverId: String) {
        val lobbyReference = mainRepository.getLobby(lobbyCode)
        val tradesReference = lobbyReference.child("trades")

        val tradeId = generateTradeId(senderId, receiverId)

        val tradeData = mapOf(
            "senderId" to senderId,
            "receiverId" to receiverId,
            "state" to TradeState.PENDING,
            "sentAt" to ServerValue.TIMESTAMP
        )

        tradesReference.child(tradeId).setValue(tradeData)
        println("[TRADES]: Trade request created with ID $tradeId.")
    }

    fun deleteRequest(lobbyCode: String, tradeId: String) {
        val lobbyReference = mainRepository.getLobby(lobbyCode)
        lobbyReference.child("trades")
            .child(tradeId)
            .removeValue()
            .addOnSuccessListener { println("[TRADES]: Trade request with ID $tradeId successfully deleted.") }
            .addOnFailureListener { error -> println("[TRADES]: Couldn't delete trade request with ID $tradeId\n${error.message}") }
    }

    fun deleteAllRequests(lobbyCode: String) {
        val lobbyReference = mainRepository.getLobby(lobbyCode)
        lobbyReference.child("tradeRequests").removeValue()
            .addOnSuccessListener { println("[TRADES]: Deleted all trades in lobby '$lobbyCode'")}
            .addOnFailureListener { error -> println("[TRADES]: Couldn't delete all trades in lobby '$lobbyCode'\n${error.message}") }
    }

    /**
     * Returns the ID of the trade between 2 players in the same lobby if one exists. Else, returns null
     * @param lobbyCode The code of the lobby we are looking in
     * @param id1 String form ID of the 1st player (doesn't matter if its sender or receiver)
     * @param id2 String form ID of the 2nd player (doesn't matter if its sender or receiver)
     */
    suspend fun getTradeBetween(lobbyCode: String, id1: String, id2: String): String? {
        val lobbyReference = mainRepository.getLobby(lobbyCode)
        val tradesReference = lobbyReference.child("trades")

        try {
            val snapshot = tradesReference.get().await()

            println("[TRADES_DEBUG] Got snapshot. It's children: ${snapshot.childrenCount}")

            for (tradeSnapshot in snapshot.children) {
                println("[TRADES_DEBUG] Trade node key: ${tradeSnapshot.key}, value: ${tradeSnapshot.value}")
                val senderId = tradeSnapshot.child("senderId").getValue(String::class.java) ?: continue
                val receiverId = tradeSnapshot.child("receiverId").getValue(String::class.java) ?: continue
                val stateString = tradeSnapshot.child("state").getValue(String::class.java)

                val state = TradeState.fromString(stateString)

                println("[TRADES_DEBUG]: SID - $senderId, RID - $receiverId, STATE - $state")

                if ((id1 == senderId || id1 == receiverId) &&
                    (id2 == senderId || id2 == receiverId) &&
                    state == TradeState.PENDING
                ) {
                    return tradeSnapshot.key // return the tradeId
                }
            }
        } catch (e: Exception) { println("[TRADES]: Something went wrong... ${e.message}") }

        return null
    }

    fun getMyTradeRequests(lobbyCode: String, playerId: String): Flow<List<Trade>> = callbackFlow {
        val lobbyReference = mainRepository.getLobby(lobbyCode)
        val tradesReference = lobbyReference.child("trades")

        val listener = tradesReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val trades = mutableListOf<Trade>()
                for (trade in snapshot.children) {
                    val tradeId = trade.key ?: continue

                    val senderIdStr = trade.child("senderId").getValue(String::class.java) ?: continue
                    val senderId = PlayerId(senderIdStr)

                    val receiverIdStr = trade.child("receiverId").getValue(String::class.java) ?: continue
                    val receiverId = PlayerId(receiverIdStr)

                    // skip trades which we are not meant for us
                    if (receiverIdStr != playerId) { continue }

                    val senderOffer = mutableListOf<String>()
                    for (hintSnapshot in trade.child("senderOffer").children) {
                        val hintId = hintSnapshot.key ?: continue
                        senderOffer.add(hintId)
                    }

                    val receiverOffer = mutableListOf<String>()
                    for (hintSnapshot in trade.child("receiverOffer").children) {
                        val hintId = hintSnapshot.key ?: continue
                        receiverOffer.add(hintId)
                    }

                    val state = trade.child("state").getValue(TradeState::class.java) ?: TradeState.PENDING

                    // skip trades that are not in a pending state
                    if (state != TradeState.PENDING) { continue }

                    val sentAt = trade.child("sentAt").getValue(Long::class.java) ?: 0L

                    val thisTrade = Trade(
                        id = tradeId,
                        senderId = senderId,
                        senderOffer = senderOffer,
                        receiverId = receiverId,
                        receiverOffer = receiverOffer,
                        state = state,
                        sentAt = sentAt
                    )

                    trades.add(thisTrade)
                }

                trySend(trades)
            }

            override fun onCancelled(error: DatabaseError) {}
        })

        awaitClose { tradesReference.removeEventListener(listener) }
    }

    suspend fun expireOldTrades(lobbyCode: String, expirySeconds: Long = 15) {
        val ref = mainRepository.getLobby(lobbyCode).child("trades")
        val snapshot = ref.get().await()

        val now = System.currentTimeMillis()

        for (tradeSnapshot in snapshot.children) {
            val trade = tradeSnapshot.getValue(Trade::class.java) ?: continue

            if (trade.state == TradeState.PENDING) {
                val elapsed = (now - trade.sentAt) / 1000
                if (elapsed >= expirySeconds) {
                    tradeSnapshot.ref.child("state").setValue(TradeState.DECLINED)
                }
            }
        }
    }
}