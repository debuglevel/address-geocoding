package de.debuglevel.addressgeocoding.geocoding

import mu.KotlinLogging
import java.io.IOException
import java.net.UnknownHostException
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import kotlin.concurrent.withLock

abstract class Geocoder(private val geocoderProperties: GeocoderProperties) {
    private val logger = KotlinLogging.logger {}

    /**
     * Lock object for implementation that only allow 1 parallel request.
     */
    private val singleRequestLock = java.util.concurrent.locks.ReentrantLock()

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
    fun getCoordinates(address: String): Coordinate {
        logger.debug { "Getting coordinates for address '$address'..." }

        val coordinate = try {
            getCoordinatesImpl(address)
        } catch (e: UnknownHostException) {
            throw UnreachableServiceException(e)
        } catch (e: IOException) {
            throw UnreachableServiceException(e)
        }

        logger.debug { "Got coordinates for address '$address': $coordinate" }
        return coordinate
    }

    /**
     * Waits until the next request is permitted to be made.
     */
    private fun waitForNextRequestAllowed() {
        val lastRequestOn = this.lastRequestOn
        if (lastRequestOn != null) {
            val nextRequestDateTime = lastRequestOn.plusNanos(geocoderProperties.waitBetweenRequests)
            val waitingTimeMilliseconds = ChronoUnit.MILLIS.between(LocalDateTime.now(), nextRequestDateTime)
            if (waitingTimeMilliseconds > 0) {
                logger.debug { "Waiting ${waitingTimeMilliseconds}ms until the next request to Nominatim is allowed..." }
                Thread.sleep(waitingTimeMilliseconds)
            }
        }
    }

    /**
     * Set the last request DateTime to now()
     */
    private fun setLastRequestDateTime() {
        this.lastRequestOn = LocalDateTime.now()
    }

    /**
     * Waits until the next request is allowed and then executes the given [action] under this lock.
     * @return the return value of the action.
     */
    fun <T> withDelayedLock(action: () -> T): T {
        waitForNextRequestAllowed()
        setLastRequestDateTime()

        logger.debug("Waiting for lock...")
        singleRequestLock.withLock {
            return action()
        }
    }
}