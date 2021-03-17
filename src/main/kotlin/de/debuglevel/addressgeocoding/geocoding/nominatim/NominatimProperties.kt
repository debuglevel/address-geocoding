package de.debuglevel.addressgeocoding.geocoding.nominatim

import io.micronaut.context.annotation.ConfigurationProperties

@ConfigurationProperties("app.address-geocoding.geocoders.nominatim")
class NominatimProperties {
    /**
     * How long should be waited between two requests
     */
    var waitBetweenRequests: Long = 1_000_000_000
}
