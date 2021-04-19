package de.debuglevel.addressgeocoding.geocoder

open class GeocoderProperties {
    /**
     * Whether the Geocoder is enabled
     */
    var enabled: Boolean = false

    /**
     * Maximum parallel geocoding requests.
     */
    var maximumThreads: Int = 1

    /**
     * How long should be waited between two requests (in nanoseconds)
     */
    var waitBetweenRequests: Long = 1_000_000_000

    /**
     * Base URL of the service
     */
    var url: String = "invalid"

    override fun toString(): String {
        return "GeocoderProperties(" +
                "enabled=$enabled, " +
                "maximumThreads=$maximumThreads, " +
                "waitBetweenRequests=$waitBetweenRequests, " +
                "url='$url'" +
                ")"
    }
}