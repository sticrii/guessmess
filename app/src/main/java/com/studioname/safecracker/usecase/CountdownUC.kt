package com.studioname.safecracker.usecase

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds


class CountdownUC @Inject constructor() {
    operator fun invoke(initialTime: Int): Flow<Int> = flow {
        var timeLeft = initialTime
        while (timeLeft >= 0) {
            emit(timeLeft)
            delay(1000.milliseconds)
            timeLeft--
        }
    }
}