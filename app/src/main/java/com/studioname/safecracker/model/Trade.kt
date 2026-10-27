package com.studioname.safecracker.model

import com.studioname.safecracker.enums.TradeState

data class Trade(
    val id: String,
    val senderId: PlayerId,
    val senderOffer: MutableList<String>? = null,
    val receiverId: PlayerId,
    val receiverOffer: MutableList<String>? = null,
    val state: TradeState
)
