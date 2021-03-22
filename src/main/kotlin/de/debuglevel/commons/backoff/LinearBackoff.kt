package de.debuglevel.commons.backoff

import mu.KotlinLogging
import java.time.Duration

object LinearBackoff : Backoff() {
    private val logger = KotlinLogging.logger {}

    /**
     * Gets the duration until the next attempt, based on the number of previous failed attempts.
     * @param failedAttempts How many failed attempts were made so far.
     * @param multiplierDuration Which duration should be added for each failed attempt.
     */
    override fun calculateBackoffDuration(
        failedAttempts: Long,
        multiplierDuration: Duration,
    ): Duration {
        require(failedAttempts >= 0) { "Failed attempts must be non-negative." }
        require(!multiplierDuration.isNegative) { "Multiplier duration must be non-negative." }

        val backoffDuration = multiplierDuration.multipliedBy(failedAttempts)
        logger.trace { "Backoff duration: $failedAttempts*$multiplierDuration=$backoffDuration" }

        return backoffDuration
    }
}