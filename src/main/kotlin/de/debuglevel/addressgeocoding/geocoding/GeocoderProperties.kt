package de.debuglevel.addressgeocoding.geocoding

open class GeocoderProperties {
    /**
     * Whether the Geocoder is enabled
     */
    var enabled: Boolean = false

    /**
     * How long should be waited between two requests
     */
    var waitBetweenRequests: Long = 1_000_000_000
}