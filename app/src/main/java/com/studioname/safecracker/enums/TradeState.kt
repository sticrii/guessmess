package com.studioname.safecracker.enums

enum class TradeState {
    PENDING, // for trades that have been sent and are awaiting accept/decline
    IN_PROGRESS, // for trades that are currently in progress
    ACCEPTED, // for accepted trades (the offers were accepted, not just the invitation)
    DECLINED, // trade was declined (either the invitation itself, or the offer, both use it)
}