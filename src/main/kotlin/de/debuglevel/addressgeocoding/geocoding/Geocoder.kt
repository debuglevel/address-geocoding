package de.debuglevel.addressgeocoding.geocoding

import mu.KotlinLogging
import java.io.IOException
import java.net.UnknownHostException
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

abstract class Geocoder(private val geocoderProperties: GeocoderProperties) {
    private val logger = KotlinLogging.logger {}

    abstract fun getCoordinatesImpl(address: String): Coordinate

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
     * When the last request to the service was made
     */
    private var lastRequestOn: LocalDateTime? = null

    /**
     * Waits until the next request is permitted to be made.
     */
    fun waitForNextRequestAllowed() {
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
    fun setLastRequestDateTime() {
        this.lastRequestOn = LocalDateTime.now()
    }
}