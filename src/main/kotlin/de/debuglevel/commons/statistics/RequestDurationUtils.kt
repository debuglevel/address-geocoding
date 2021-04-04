package de.debuglevel.commons.statistics

import mu.KotlinLogging
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

object RequestDurationUtils {
    private val logger = KotlinLogging.logger {}

    private val averageDurations = mutableMapOf<Pair<Any, Any>, Double>()

    /**
     * Calculates the new average duration based on this new duration.
     * Must be called before incrementing the request counter for correct calculation.
     * @param requester The object which executes the request
     * @param scope Any object to define a scope within the requester; e.g. "POST /foobar" or "default" or null
     * @param calls How many calls have been made so far (TODO: this could also be managed in here)
     * @param duration How long the request took
     */
    @ExperimentalTime
    fun calculateAverageRequestDuration(requester: Any, scope: Any, calls: Int, duration: Duration): Double {
        logger.trace { "Calculating new average request duration..." }

        val requesterScope = requester to scope
        val oldAverageDuration = averageDurations[requesterScope]

        val newAverageDuration = if (oldAverageDuration == null) {
            duration.inSeconds
        } else {
            val durationSum = oldAverageDuration * calls
            val newAverageDuration = (durationSum + duration.inSeconds) / (calls + 1)
            newAverageDuration
        }

        averageDurations[requesterScope] = newAverageDuration

        logger.trace { "Calculated new average request duration: ${newAverageDuration}s" }
        return newAverageDuration
    }
}