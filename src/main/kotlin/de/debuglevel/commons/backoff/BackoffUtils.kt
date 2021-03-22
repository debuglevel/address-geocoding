package de.debuglevel.commons.backoff

import mu.KotlinLogging
import java.time.Duration
import java.time.LocalDateTime

object BackoffUtils {
    private val logger = KotlinLogging.logger {}

    fun isBackedOff(
        lastSuccessfulAttemptOn: LocalDateTime?,
        failedAttempts: Long,
        intervalMultiplicator: Duration,
        maximumBackoffInterval: Duration
    ): Boolean {
        logger.trace { "Checking if is backed off..." }

        val isBackedOff = if (lastSuccessfulAttemptOn == null) {
            logger.trace { "Empty last successful attempt; it's backed off therefore." }
            true
        } else {
            val backoffDuration = getBackoffDuration(failedAttempts, intervalMultiplicator, maximumBackoffInterval)
            val nextAttemptOn = lastSuccessfulAttemptOn.plus(backoffDuration)
            logger.trace { "Next attempt is after $nextAttemptOn" }

            val isBackedOff = nextAttemptOn < LocalDateTime.now()
            isBackedOff
        }

        logger.trace { "Checked if is backed off: $isBackedOff" }
        return isBackedOff
    }

    fun getBackoffDuration(
        failedAttempts: Long,
        intervalMultiplicator: Duration,
        maximumBackoffInterval: Duration
    ): Duration {
        logger.trace { "Getting backoff duration for failedAttempts=$failedAttempts, intervalMultiplicator=$intervalMultiplicator, maximumBackoffInterval=$maximumBackoffInterval..." }

        var backoffDuration = intervalMultiplicator.multipliedBy(failedAttempts)
        logger.trace { "Backoff duration for failedAttempts=$failedAttempts, intervalMultiplicator=$intervalMultiplicator, maximumBackoffInterval=$maximumBackoffInterval is $backoffDuration" }

        backoffDuration = if (backoffDuration > maximumBackoffInterval) {
            logger.trace { "Shorted backoff duration to $maximumBackoffInterval" }
            maximumBackoffInterval
        } else {
            backoffDuration
        }

        logger.trace { "Got backoff duration for failedAttempts=$failedAttempts, intervalMultiplicator=$intervalMultiplicator, maximumBackoffInterval=$maximumBackoffInterval: $backoffDuration" }
        return backoffDuration
    }
}