package com.studioname.safecracker.usecase

import com.studioname.safecracker.model.Hint
import com.studioname.safecracker.model.HintTemplate
import com.studioname.safecracker.model.HintTemplates
import java.util.UUID
import javax.inject.Inject
import kotlin.random.Random

class HintGenerator @Inject constructor() {

    fun generate(
        safeCode: String,
        roomKeys: List<String>,
        minHintsPerRoom: Int = 1,
        maxHintsPerRoom: Int = 3,
        random: Random = Random.Default
    ): List<Hint> {
        require(safeCode.length == 6 && safeCode.all { it in '0'..'9' }) {
            "Safe code must contain exactly six digits."
        }
        require(roomKeys.isNotEmpty()) {
            "At least one room is required."
        }
        require(roomKeys.all { it.isNotBlank() }) {
            "Room keys cannot be blank."
        }
        require(minHintsPerRoom in 1..maxHintsPerRoom) {
            "Invalid hint count range."
        }

        return roomKeys.flatMap { roomKey ->
            val hintsPerRoom = random.nextInt(
                from = minHintsPerRoom,
                until = maxHintsPerRoom + 1
            )

            val candidates = HintTemplates.all
                .mapNotNull { template ->
                    template.render(safeCode)?.let { content ->
                        template to content
                    }
                }
                .shuffled(random)

            require(candidates.size >= hintsPerRoom) {
                "Not enough valid hint templates for room '$roomKey'."
            }

            candidates
                .take(hintsPerRoom)
                .map { (template, content) ->
                    template.toHint(
                        content = content,
                        roomKey = roomKey
                    )
                }
        }
    }

    private fun HintTemplate.toHint(
        content: String,
        roomKey: String
    ): Hint = Hint(
        id = UUID.randomUUID().toString(),
        templateId = id,
        type = type,
        content = content,
        roomKey = roomKey
    )
}