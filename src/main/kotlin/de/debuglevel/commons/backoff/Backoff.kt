package de.debuglevel.commons.backoff

import mu.KotlinLogging
import java.time.Duration
import java.time.LocalDateTime
import kotlin.math.roundToLong
import kotlin.random.Random

abstract class Backoff {
    private val logger = KotlinLogging.logger {}

    /**
     * The maximum (multiplied) difference from the backoff duration when adding a random part
     */
    private val maximumRandomDifference = 0.25

    /**
     * Checks whether the backoff duration is reached.
     * @param lastAttemptOn When the last attempt was made; null if never.
     * @param failedAttempts How many failed attempts were made so far.
     * @param multiplierDuration Which duration should be added for each failed attempt.
     * @param maximumBackoffDuration The maximum duration to backoff (to prevent very large backoff durations) or null to allow infinite backoff durations.
     * @param randomSeed A seed for calculating a random part of the duration (should be tied to an item and only change when the item is modified, i.e. hashcode is a good choice); null to disable random part in duration.
     */
    fun isBackedOff(
        lastAttemptOn: LocalDateTime?,
        failedAttempts: Long,
        multiplierDuration: Duration,
        maximumBackoffDuration: Duration? = null,
        randomSeed: Int? = null
    ): Boolean {
        require(lastAttemptOn == null || lastAttemptOn < LocalDateTime.now()) { "Last attempt must be in the past or null." }
        require(failedAttempts >= 0) { "Failed attempts must be non-negative." }
        require(!multiplierDuration.isNegative) { "Multiplier duration must be non-negative." }
        require(maximumBackoffDuration == null || !maximumBackoffDuration.isNegative) { "Maximum backoff duration must be non-negative or null." }
        logger.trace { "Checking if backed off..." }

        val isBackedOff = if (lastAttemptOn == null) {
            logger.trace { "Empty last attempt; it's backed off therefore." }
            true
        } else {
            val backoffDuration =
                getBackoffDuration(failedAttempts, multiplierDuration, maximumBackoffDuration, randomSeed)
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
     * @param randomSeed A seed for calculating a random part of the duration (should be tied to an item and only change when the item is modified, i.e. hashcode is a good choice); null to disable random part in duration.
     */
    fun getBackoffDuration(
        failedAttempts: Long,
        multiplierDuration: Duration,
        maximumBackoffDuration: Duration?,
        randomSeed: Int? = null,
    ): Duration {
        require(failedAttempts >= 0) { "Failed attempts must be non-negative." }
        require(!multiplierDuration.isNegative) { "Multiplier duration must be non-negative." }
        require(maximumBackoffDuration == null || !maximumBackoffDuration.isNegative) { "Maximum backoff duration must be non-negative or null." }
        logger.trace { "Getting backoff duration for failedAttempts=$failedAttempts, multiplierDuration=$multiplierDuration, maximumBackoffInterval=$maximumBackoffDuration..." }

        var backoffDuration = calculateBackoffDuration(failedAttempts, multiplierDuration)
        logger.trace { "Backoff duration: $backoffDuration" }

        if (randomSeed != null) {
            backoffDuration = randomizeDuration(randomSeed, backoffDuration, maximumRandomDifference)
        }

        backoffDuration = if (maximumBackoffDuration != null && backoffDuration > maximumBackoffDuration) {
            logger.trace { "Shorted backoff duration $backoffDuration to $maximumBackoffDuration" }
            maximumBackoffDuration
        } else {
            backoffDuration
        }

        logger.trace { "Got backoff duration for failedAttempts=$failedAttempts, multiplierDuration=$multiplierDuration, maximumBackoffInterval=$maximumBackoffDuration: $backoffDuration" }
        return backoffDuration
    }

    /**
     * Get a random duration
     * @param randomSeed A seed for calculating a random part of the duration (should be tied to an item and only change when the item is modified, i.e. hashcode is a good choice); null to disable random part in duration.
     * @param duration The original backoff duration to add some randomness to
     * @param maximumDifference The maximum difference to multiply to the backoff duration
     *
     * maximumDifference = 0.25 and duration = 1m result in a range from 0.75m to 1.25m
     */
    private fun randomizeDuration(randomSeed: Int, duration: Duration, maximumDifference: Double): Duration {
        logger.trace { "Randomizing duration $duration..." }
        val random = Random(randomSeed)

        val durationMilliseconds = duration.toMillis()
        val differenceMultiplier = random.nextDouble(1 - maximumDifference, 1 + maximumDifference)
        val randomizedDurationSeconds = durationMilliseconds * differenceMultiplier
        val randomizedDuration = Duration.ofMillis(randomizedDurationSeconds.roundToLong())

        logger.trace { "Randomized duration $duration: $randomizedDuration" }
        return randomizedDuration
    }

    /**
     * Gets the duration until the next attempt, based on the number of previous failed attempts.
     * @param failedAttempts How many failed attempts were made so far.
     * @param multiplierDuration Which duration should be added for each failed attempt.
     */
    abstract fun calculateBackoffDuration(
        failedAttempts: Long,
        multiplierDuration: Duration,
    ): Duration
}