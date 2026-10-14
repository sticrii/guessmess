package com.studioname.safecracker.model

import com.studioname.safecracker.R

data class Room(
    val name: String,
    val playersIn: List<String> = emptyList(),
    val referenceImage: Int = R.drawable.room,
    val hints: List<String> = emptyList()
)
