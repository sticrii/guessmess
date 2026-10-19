package com.studioname.safecracker.model

data class Hint(
    val id: String,
    val type: String,
    val content: String,
    val roomKey: String
) {
    init {
        require(id.isNotBlank()) { "Hint ID cannot be blank" }
        require(type.isNotBlank()) { "Hint type cannot be blank" }
        require(content.isNotBlank()) { "Hint content cannot be blank" }
    }
}