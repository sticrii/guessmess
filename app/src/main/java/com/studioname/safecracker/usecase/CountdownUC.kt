package com.studioname.safecracker.usecase

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class CountdownUC @Inject constructor() {
    operator fun invoke(
        phaseStartTimeMillis: Long,
        durationSeconds: Int
    ): Flow<Int> = flow {
        val endTimeMillis =
            phaseStartTimeMillis + durationSeconds * 1_000L

        while (true) {
            val remainingMillis = endTimeMillis - System.currentTimeMillis()
            val secondsLeft = ((remainingMillis + 999) / 1_000)
                .coerceAtLeast(0)
                .toInt()

            emit(secondsLeft)

            if (secondsLeft == 0) break

            delay(250)
        }
    }
}