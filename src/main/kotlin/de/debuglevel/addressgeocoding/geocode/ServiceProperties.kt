package de.debuglevel.addressgeocoding.geocode

import io.micronaut.context.annotation.ConfigurationProperties

@ConfigurationProperties("app.address-geocoding.geocode")
class ServiceProperties {
    /**
     * Maximum parallel geocoding executions.
     *
     * Note that the Nominatim and Photon geocoders will effectively
     * reduce this to 1 parallel request to their service.
     */
    var maximumThreads: Int = 5
}