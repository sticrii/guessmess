package com.studioname.safecracker.model

enum class HintType {
    RED,
    BLACK,
    BLUE
}

data class Hint(
    val id: String,
    val templateId: String,
    val type: HintType,
    val content: String,
    val roomKey: String
) {
    init {
        require(id.isNotBlank()) { "Hint ID cannot be blank" }
        require(templateId.isNotBlank()) { "Template ID cannot be blank" }
        require(content.isNotBlank()) { "Hint content cannot be blank" }
        require(roomKey.isNotBlank()) { "Room key cannot be blank" }
    }
}

data class HintTemplate(
    val id: String,
    val type: HintType,
    val render: (String) -> String?
)