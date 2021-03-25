package de.debuglevel.addressgeocoding.geocoding

import mu.KotlinLogging
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

abstract class Geocoder(private val geocoderProperties: GeocoderProperties) {
    private val logger = KotlinLogging.logger {}

    abstract fun getCoordinates(address: String): Coordinate

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

    fun setLastRequestDateTime() {
        this.lastRequestOn = LocalDateTime.now()
    }
}