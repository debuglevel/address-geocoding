package de.debuglevel.addressgeocoding.geocoding

import mu.KotlinLogging
import java.io.IOException
import java.net.UnknownHostException
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.concurrent.Executors
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.measureTimedValue

abstract class Geocoder(
    private val geocoderProperties: GeocoderProperties,
) {
    private val logger = KotlinLogging.logger {}

    val statistics = Statistics()

    /**
     * Executor to manage parallel (or serial, with 1 thread) requests.
     */
    private val executor = Executors.newFixedThreadPool(geocoderProperties.maximumThreads)

    /**
     * When the last request to the service was made
     */
    private var lastRequestOn: LocalDateTime? = null

    /**
     * Get coordinates for an address, dependent on the actual implementation.
     */
    abstract fun getCoordinatesImpl(address: String): Coordinate

    /**
     * Get coordinates for an address.
     */
    @ExperimentalTime
    fun getCoordinates(address: String): Coordinate {
        logger.debug { "Getting coordinates for address '$address'..." }

        val coordinate = try {
            val coordinate = getCoordinatesImpl(address)
            statistics.success += 1
            coordinate
        } catch (e: UnknownHostException) {
            statistics.unreachable += 1
            throw UnreachableServiceException(e)
        } catch (e: IOException) {
            statistics.unreachable += 1
            throw UnreachableServiceException(e)
        } catch (e: AddressNotFoundException) {
            statistics.unknownAddress += 1
            throw e
        }

        logger.debug { "Got coordinates for address '$address': $coordinate" }
        return coordinate
    }

    /**
     * Calculates the new average duration based on this new duration.
     * Must be called before incrementing the request counter for correct calculation.
     */
    @ExperimentalTime
    private fun calculateAverageRequestDuration(duration: Duration) {
        logger.trace { "Calculating new average request duration..." }

        val averageDuration = statistics.averageRequestDuration
        statistics.averageRequestDuration = if (averageDuration == null) {
            duration.inSeconds
        } else {
            val calls = statistics.success + statistics.unknownAddress + statistics.unreachable
            val durationSum = averageDuration * calls
            val newAverageDuration = (durationSum + duration.inSeconds) / (calls + 1)
            newAverageDuration
        }

        logger.trace { "Calculated new average request duration: ${statistics.averageRequestDuration}" }
    }

    /**
     * Waits until the next request is permitted to be made.
     */
    private fun waitForNextRequestAllowed() {
        logger.trace { "Waiting until next request is allowed..." }

        val lastRequestOn = this.lastRequestOn
        if (lastRequestOn != null) {
            val nextRequestDateTime = lastRequestOn.plusNanos(geocoderProperties.waitBetweenRequests)
            val waitingTimeMilliseconds = ChronoUnit.MILLIS.between(LocalDateTime.now(), nextRequestDateTime)

            logger.trace { "Last request was on $lastRequestOn, waiting duration between requests is ${geocoderProperties.waitBetweenRequests}ns, next request is on $nextRequestDateTime, waiting time is ${waitingTimeMilliseconds}ms" }

            if (waitingTimeMilliseconds > 0) {
                logger.debug { "Sleeping ${waitingTimeMilliseconds}ms until the next request is allowed..." }
                Thread.sleep(waitingTimeMilliseconds)
            }
        }

        logger.trace { "Waited until next request is allowed" }
    }

    /**
     * Set the last request DateTime to now()
     */
    private fun setLastRequestDateTime() {
        this.lastRequestOn = LocalDateTime.now()
    }

    /**
     * Records the duration of the execution and
     * @return the return value of the action.
     */
    @ExperimentalTime
    fun <T> withRecordedDuration(action: () -> T): T {
        logger.trace { "Recording duration for action..." }

        val timedValue = measureTimedValue {
            action()
        }
        logger.trace { "Action took ${timedValue.duration}" }

        calculateAverageRequestDuration(timedValue.duration)

        logger.trace { "Recorded duration for action: ${timedValue.duration}" }
        return timedValue.value
    }

    /**
     * Waits until the next request is allowed and then executes the given [action] in this executor.
     * @return the return value of the action.
     */
    fun <T> withDelayedExecution(action: () -> T): T {
        waitForNextRequestAllowed()
        setLastRequestDateTime()

        logger.debug("Submitting task to executor...")
        val future = executor.submit<T> {
            return@submit action()
        }
        logger.debug("Submitted task to executor; waiting task to finish...")
        val value = future.get()
        logger.debug("Got task result")
        return value
    }
}