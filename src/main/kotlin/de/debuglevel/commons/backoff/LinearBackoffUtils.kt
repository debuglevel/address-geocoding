package de.debuglevel.commons.backoff

import mu.KotlinLogging
import java.time.Duration
import java.time.LocalDateTime

object LinearBackoffUtils {
    private val logger = KotlinLogging.logger {}

    /**
     * Checks whether the backoff duration is reached.
     * @param lastAttemptOn When the last attempt was made; null if never.
     * @param failedAttempts How many failed attempts were made so far.
     * @param multiplierDuration Which duration should be added for each failed attempt.
     * @param maximumBackoffDuration The maximum duration to backoff (to prevent very large backoff durations) or null to allow infinite backoff durations.
     */
    fun isBackedOff(
        lastAttemptOn: LocalDateTime?,
        failedAttempts: Long,
        multiplierDuration: Duration,
        maximumBackoffDuration: Duration? = null
    ): Boolean {
        require(lastAttemptOn == null || lastAttemptOn < LocalDateTime.now()) { "Last attempt must be in the past or null." }
        require(failedAttempts >= 0) { "Failed attempts must be non-negative." }
        require(!multiplierDuration.isNegative) { "Duration multiplier must be non-negative." }
        require(maximumBackoffDuration == null || !maximumBackoffDuration.isNegative) { "Maximum backoff duration must be non-negative or null." }
        logger.trace { "Checking if backed off..." }

        val isBackedOff = if (lastAttemptOn == null) {
            logger.trace { "Empty last attempt; it's backed off therefore." }
            true
        } else {
            val backoffDuration = getBackoffDuration(failedAttempts, multiplierDuration, maximumBackoffDuration)
            val nextAttemptOn = lastAttemptOn.plus(backoffDuration)
            logger.trace { "Next attempt is after $nextAttemptOn" }

            val isBackedOff = nextAttemptOn < LocalDateTime.now()
            isBackedOff
        }

        logger.trace { "Checked if backed off: $isBackedOff" }
        return isBackedOff
    }

    /**
     * Gets the duration until the next attempt, based on the number of previous failed attempts.
     * @param failedAttempts How many failed attempts were made so far.
     * @param multiplierDuration Which duration should be added for each failed attempt.
     * @param maximumBackoffDuration The maximum duration to backoff (to prevent very large backoff durations) or null to allow infinite backoff durations.
     */
    fun getBackoffDuration(
        failedAttempts: Long,
        multiplierDuration: Duration,
        maximumBackoffDuration: Duration? = null
    ): Duration {
        require(failedAttempts >= 0) { "Failed attempts must be non-negative." }
        require(!multiplierDuration.isNegative) { "Duration multiplier must be non-negative." }
        require(maximumBackoffDuration == null || !maximumBackoffDuration.isNegative) { "Maximum backoff duration must be non-negative or null." }
        logger.trace { "Getting backoff duration for failedAttempts=$failedAttempts, multiplierDuration=$multiplierDuration, maximumBackoffInterval=$maximumBackoffDuration..." }

        var backoffDuration = multiplierDuration.multipliedBy(failedAttempts)
        logger.trace { "Backoff duration for $failedAttempts*$multiplierDuration=$backoffDuration" }

        backoffDuration = if (maximumBackoffDuration == null && backoffDuration > maximumBackoffDuration) {
            logger.trace { "Shorted backoff duration $backoffDuration to $maximumBackoffDuration" }
            maximumBackoffDuration
        } else {
            backoffDuration
        }

        logger.trace { "Got backoff duration for failedAttempts=$failedAttempts, multiplierDuration=$multiplierDuration, maximumBackoffInterval=$maximumBackoffDuration: $backoffDuration" }
        return backoffDuration
    }
}