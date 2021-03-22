package de.debuglevel.commons.backoff

import mu.KotlinLogging
import java.time.Duration
import java.time.LocalDateTime

object LinearBackoffUtils {
    private val logger = KotlinLogging.logger {}

    /**
     * Checks whether the backoff duration is reached.
     * @param lastAttemptOn When the last attempt was made; null if never
     * @param failedAttempts How many failed attempts were made so far
     * @param intervalMultiplicator Which duration should be added for each failed attempt
     * @param maximumBackoffDuration The maximum duration to backoff (to prevent very large backoff durations)
     */
    fun isBackedOff(
        lastAttemptOn: LocalDateTime?,
        failedAttempts: Long,
        intervalMultiplicator: Duration,
        maximumBackoffDuration: Duration
    ): Boolean {
        require(lastAttemptOn == null || lastAttemptOn < LocalDateTime.now()) { "Last attempt must not be in the future." }
        require(failedAttempts >= 0) { "Failed attempts must be non-negative." }
        require(!intervalMultiplicator.isNegative) { "Interval multiplicator must be non-negative." }
        require(!maximumBackoffDuration.isNegative) { "Maximum backoff duration must be non-negative." }
        logger.trace { "Checking if backed off..." }

        val isBackedOff = if (lastAttemptOn == null) {
            logger.trace { "Empty last attempt; it's backed off therefore." }
            true
        } else {
            val backoffDuration = getBackoffDuration(failedAttempts, intervalMultiplicator, maximumBackoffDuration)
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
     * @param failedAttempts How many failed attempts were made so far
     * @param intervalMultiplicator Which duration should be added for each failed attempt
     * @param maximumBackoffDuration The maximum duration to backoff (to prevent very large backoff durations)
     */
    fun getBackoffDuration(
        failedAttempts: Long,
        intervalMultiplicator: Duration,
        maximumBackoffDuration: Duration // TODO: allow to be null to grow backoff duration infinite
    ): Duration {
        require(failedAttempts >= 0) { "Failed attempts must be non-negative." }
        require(!intervalMultiplicator.isNegative) { "Interval multiplicator must be non-negative." }
        require(!maximumBackoffDuration.isNegative) { "Maximum backoff duration must be non-negative." }

        logger.trace { "Getting backoff duration for failedAttempts=$failedAttempts, intervalMultiplicator=$intervalMultiplicator, maximumBackoffInterval=$maximumBackoffDuration..." }

        var backoffDuration = intervalMultiplicator.multipliedBy(failedAttempts)
        logger.trace { "Backoff duration for failedAttempts=$failedAttempts, intervalMultiplicator=$intervalMultiplicator, maximumBackoffInterval=$maximumBackoffDuration is $backoffDuration" }

        backoffDuration = if (backoffDuration > maximumBackoffDuration) {
            logger.trace { "Shorted backoff duration to $maximumBackoffDuration" }
            maximumBackoffDuration
        } else {
            backoffDuration
        }

        logger.trace { "Got backoff duration for failedAttempts=$failedAttempts, intervalMultiplicator=$intervalMultiplicator, maximumBackoffInterval=$maximumBackoffDuration: $backoffDuration" }
        return backoffDuration
    }
}