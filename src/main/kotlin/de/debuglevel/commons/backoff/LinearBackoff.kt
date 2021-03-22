package de.debuglevel.commons.backoff

import mu.KotlinLogging
import java.time.Duration

object LinearBackoff : Backoff() {
    private val logger = KotlinLogging.logger {}

    /**
     * Gets the duration until the next attempt, based on the number of previous failed attempts.
     * @param failedAttempts How many failed attempts were made so far.
     * @param multiplierDuration Which duration should be added for each failed attempt.
     * @param maximumBackoffDuration The maximum duration to backoff (to prevent very large backoff durations) or null to allow infinite backoff durations.
     */
    override fun getBackoffDuration(
        failedAttempts: Long,
        multiplierDuration: Duration,
        maximumBackoffDuration: Duration?
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