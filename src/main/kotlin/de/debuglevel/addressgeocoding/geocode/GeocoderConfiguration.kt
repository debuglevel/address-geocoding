package de.debuglevel.addressgeocoding.geocode

import io.micronaut.context.annotation.EachProperty
import io.micronaut.context.annotation.Parameter

@EachProperty("app.address-geocoding.geocoders")
class GeocoderConfiguration
constructor(@param:Parameter val name: String) {
    var type: String = "none"

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
     * Base URL for the Nominatim service to use
     */
    var url: String = "invalid"

    /**
     * Email address to include in requests according to the Nominatim usage policy (https://nominatim.org/release-docs/latest/api/Search/#other)
     */
    var email: String = "invalid"
}