package com.studioname.safecracker.model

object HintTemplates {
    val all: List<HintTemplate> = listOf(
        // RED: konkretne informacije o pozicijama
        HintTemplate(
            id = "red.position_1",
            type = HintType.RED,
            render = { code ->
                if (!isValidCode(code)) {
                    null
                } else {
                    "Prva cifra je ${code[0]}."
                }
            }
        ),
        HintTemplate(
            id = "red.position_3",
            type = HintType.RED,
            render = { code ->
                if (!isValidCode(code)) {
                    null
                } else {
                    "Na trećoj poziciji je cifra ${code[2]}."
                }
            }
        ),
        HintTemplate(
            id = "red.position_4",
            type = HintType.RED,
            render = { code ->
                if (!isValidCode(code)) {
                    null
                } else {
                    "Na četvrtoj poziciji je cifra ${code[3]}."
                }
            }
        ),
        HintTemplate(
            id = "red.position_6",
            type = HintType.RED,
            render = { code ->
                if (!isValidCode(code)) {
                    null
                } else {
                    "Poslednja cifra je ${code[5]}."
                }
            }
        ),

        // BLACK: opšte osobine šifre
        HintTemplate(
            id = "black.odd_count",
            type = HintType.BLACK,
            render = { code ->
                if (!isValidCode(code)) {
                    null
                } else {
                    val count = code.count { it.digitToInt() % 2 != 0 }
                    "U šifri se pojavljuje $count neparnih cifara."
                }
            }
        ),
        HintTemplate(
            id = "black.even_count",
            type = HintType.BLACK,
            render = { code ->
                if (!isValidCode(code)) {
                    null
                } else {
                    val count = code.count { it.digitToInt() % 2 == 0 }
                    "U šifri se pojavljuje $count parnih cifara."
                }
            }
        ),
        HintTemplate(
            id = "black.digit_sum",
            type = HintType.BLACK,
            render = { code ->
                if (!isValidCode(code)) {
                    null
                } else {
                    val sum = code.sumOf { it.digitToInt() }
                    "Zbir svih cifara u šifri je $sum."
                }
            }
        ),
        HintTemplate(
            id = "black.largest_digit",
            type = HintType.BLACK,
            render = { code ->
                if (!isValidCode(code)) {
                    null
                } else {
                    "Najveća cifra u šifri je ${code.maxOrNull()}."
                }
            }
        ),
        HintTemplate(
            id = "black.digit_count",
            type = HintType.BLACK,
            render = { code ->
                if (!isValidCode(code)) {
                    null
                } else {
                    val digit = code.first()
                    val count = code.count { it == digit }
                    "Cifra $digit pojavljuje se $count puta."
                }
            }
        ),

        // BLUE: isključivanje mogućnosti
        HintTemplate(
            id = "blue.absent_digit",
            type = HintType.BLUE,
            render = { code ->
                if (!isValidCode(code)) {
                    null
                } else {
                    val absentDigit = ('0'..'9').firstOrNull { it !in code }
                    absentDigit?.let { "Cifra $it se ne pojavljuje u šifri." }
                }
            }
        ),
        HintTemplate(
            id = "blue.first_digit_not",
            type = HintType.BLUE,
            render = { code ->
                if (!isValidCode(code)) {
                    null
                } else {
                    val excludedDigit = ('0'..'9').firstOrNull { it != code[0] }
                        ?: return@HintTemplate null

                    "Prva cifra nije $excludedDigit."
                }
            }
        ),
        HintTemplate(
            id = "blue.absent_pair",
            type = HintType.BLUE,
            render = { code ->
                if (!isValidCode(code)) {
                    null
                } else {
                    val absentDigits = ('0'..'9')
                        .filter { it !in code }
                        .take(2)

                    if (absentDigits.size < 2) {
                        null
                    } else {
                        "Cifre ${absentDigits[0]} i ${absentDigits[1]} se ne pojavljuju u šifri."
                    }
                }
            }
        )
    )

    private fun isValidCode(code: String): Boolean =
        code.length == 6 && code.all { it in '0'..'9' }
}