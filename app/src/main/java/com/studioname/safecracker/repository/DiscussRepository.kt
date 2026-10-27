package com.studioname.safecracker.repository

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.studioname.safecracker.enums.TradeState
import com.studioname.safecracker.model.Player
import com.studioname.safecracker.model.PlayerId
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton
import com.studioname.safecracker.model.Trade
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow

@Singleton
class DiscussRepository @Inject constructor(
    private val mainRepository: MainRepository
) {
    suspend fun getCurrentPlayerId(): String = mainRepository.getCurrentPlayerId()
    fun getLobbyPlayers(lobbyCode: String): Flow<List<Player>> = mainRepository.getLobbyPlayers(lobbyCode)

    private fun generateTradeId(lobbyCode: String, senderId: String, receiverId: String): String {
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

        val tradeId = generateTradeId(lobbyCode, senderId, receiverId)

        val tradeData = mapOf(
            "senderId" to senderId,
            "receiverId" to receiverId,
            "status" to TradeState.PENDING
        )

        tradesReference.child(tradeId).setValue(tradeData)
        println("[TRADES]: Trade request created with ID $tradeId.")
    }

    fun getTradeRequests(lobbyCode: String): Flow<List<Trade>> = callbackFlow {
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

                    val stateStr = trade.child("state").getValue(String::class.java) ?: continue
                    val state = stateStr.let {
                        runCatching { TradeState.valueOf(it.uppercase()) }.getOrNull()
                    } ?: TradeState.PENDING

                    val thisTrade = Trade(
                        id = tradeId,
                        senderId = senderId,
                        senderOffer = senderOffer,
                        receiverId = receiverId,
                        receiverOffer = receiverOffer,
                        state = state,
                    )

                    trades.add(thisTrade)
                }

                trySend(trades)
            }

            override fun onCancelled(error: DatabaseError) {}
        })

        awaitClose { tradesReference.removeEventListener(listener) }
    }
}