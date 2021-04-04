package de.debuglevel.commons.statistics

import mu.KotlinLogging
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

object RequestDurationUtils {
    private val logger = KotlinLogging.logger {}

    // TODO: might better be a Map
    private val requestDurations = mutableListOf<RequestDuration>()

    /**
     * Calculates the new average duration based on this new duration.
     * Must be called before incrementing the request counter for correct calculation.
     * @param requester The object which executes the request
     * @param scope Any object to define a scope within the requester; e.g. "POST /foobar" or "default" or null
     * @param duration How long the request took
     */
    @ExperimentalTime
    fun calculateAverageRequestDuration(requester: Any, scope: Any, duration: Duration): Double {
        logger.trace { "Calculating new average request duration..." }

        val requesterScope = requester to scope
        val requestDuration = requestDurations.firstOrNull { it.requester == requester && it.scope == scope }
            ?: RequestDuration(requester, scope, 0)

        val oldCalls = requestDuration.calls
        val oldDurationSum = requestDuration.durationSum
        val oldAverageDuration = requestDuration.averageDuration

        val newAverageDuration = if (oldAverageDuration == null) {
            duration.inSeconds
        } else {
            val newCalls = oldCalls + 1
            val newDurationSum = oldDurationSum + duration.inSeconds
            val newAverageDuration = newDurationSum / newCalls
            newAverageDuration
        }

        requestDuration.calls += 1
        requestDuration.averageDuration = newAverageDuration

        logger.trace { "Calculated new average request duration: ${newAverageDuration}s" }
        return newAverageDuration
    }
}