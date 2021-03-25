package de.debuglevel.addressgeocoding.geocoding.nominatim

import io.micronaut.context.annotation.ConfigurationProperties

@ConfigurationProperties("app.address-geocoding.geocoders.nominatim")
class NominatimProperties {
    /**
     * How long should be waited between two requests
     */
    var waitBetweenRequests: Long = 1_000_000_000

    /**
     * Base URL for the Nominatim service to use
     */
    var url: String = "https://nominatim.openstreetmap.org/"

    /**
     * Email address to include in requests according to the Nominatim usage policy (https://nominatim.org/release-docs/latest/api/Search/#other)
     */
    var email: String = "github.com/debuglevel/address-geocoding"
}
