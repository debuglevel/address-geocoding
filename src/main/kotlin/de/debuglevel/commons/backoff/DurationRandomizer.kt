package de.debuglevel.commons.backoff

import mu.KotlinLogging
import java.time.Duration
import kotlin.math.roundToLong
import kotlin.random.Random

object DurationRandomizer {
    private val logger = KotlinLogging.logger {}

    /**
     * Get a random duration
     * @param duration The original duration to add some randomness to
     * @param maximumDifference The maximum difference to multiply to the duration
     *
     * maximumDifference = 0.25 and duration = 1m result in a range from 0.75m to 1.25m
     *
     * @param randomSeed A seed to base the randomness on (should be tied to an item and only change when the item is modified, i.e. hashcode is a good choice); null to use a non-deterministic random number generator.
     */
    fun randomizeDuration(duration: Duration, maximumDifference: Double, randomSeed: Int? = null): Duration {
        logger.trace { "Randomizing duration $duration (maximumDifference=$maximumDifference)..." }
        val random = if (randomSeed != null) {
            logger.trace { "Using seed=$randomSeed..." }
            Random(randomSeed)
        } else {
            logger.trace { "Using no seed..." }
            Random.Default
        }

        val durationMilliseconds = duration.toMillis()
        val differenceMultiplier = random.nextDouble(1 - maximumDifference, 1 + maximumDifference)
        val randomizedDurationSeconds = durationMilliseconds * differenceMultiplier
        val randomizedDuration = Duration.ofMillis(randomizedDurationSeconds.roundToLong())

        logger.trace { "Randomized duration $duration (maximumDifference=$maximumDifference): $randomizedDuration" }
        return randomizedDuration
    }
}