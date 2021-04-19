package de.debuglevel.addressgeocoding.geocoder

import de.debuglevel.microservicecommons.statistics.RequestDurationUtils
import de.debuglevel.microservicecommons.wait.WaitUtils
import mu.KotlinLogging
import java.io.IOException
import java.net.UnknownHostException
import java.util.concurrent.Executors
import java.util.concurrent.ThreadPoolExecutor
import kotlin.time.ExperimentalTime
import kotlin.time.measureTimedValue

abstract class Geocoder(
    private val geocoderProperties: GeocoderProperties,
) {
    private val logger = KotlinLogging.logger {}

    private val _statistics = Statistics()
    val statistics: Statistics
        get() {
            return _statistics.apply { queueSize = executorQueueSize }
        }

    /**
     * Executor to manage parallel (or serial, with 1 thread) requests.
     */
    private val executor = Executors.newFixedThreadPool(geocoderProperties.maximumThreads)

    /**
     * Count of the tasks in the executor queue.
     */
    val executorQueueSize: Int
        get() {
            return (executor as ThreadPoolExecutor).queue.size
        }

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

        this.statistics.averageRequestDuration = RequestDurationUtils.calculateAverageRequestDuration(
            this,
            "default",
            timedValue.duration
        )

        logger.trace { "Recorded duration for action: ${timedValue.duration}" }
        return timedValue.value
    }

    /**
     * Waits until the next request is allowed and then executes the given [action] in this executor.
     * @return the return value of the action.
     */
    fun <T> withDelayedExecution(action: () -> T): T {
        logger.trace("Submitting task to executor...")
        val future = executor.submit<T> {
            WaitUtils.waitForNextRequestAllowed(this, this.geocoderProperties.waitBetweenRequests)
            WaitUtils.setLastRequestDateTime(this)
            return@submit action()
        }
        logger.trace("Submitted task to executor; waiting task to finish...")
        val value = future.get()
        logger.trace("Got task result")
        return value
    }
}