package de.debuglevel.addressgeocoding.geocoder.nominatim

import de.debuglevel.addressgeocoding.geocode.GeocoderConfiguration
import de.debuglevel.addressgeocoding.geocoder.GeocoderProperties

class NominatimProperties() : GeocoderProperties() {
    constructor(geocoderProperties: GeocoderConfiguration) : this() {
        this.enabled = geocoderProperties.enabled
        this.maximumThreads = geocoderProperties.maximumThreads
        this.waitBetweenRequests = geocoderProperties.waitBetweenRequests
        this.url = geocoderProperties.url
    }

    /**
     * Email address to include in requests according to the Nominatim usage policy (https://nominatim.org/release-docs/latest/api/Search/#other)
     */
    var email: String = "github.com/debuglevel/address-geocoding"

    override fun toString(): String {
        return "NominatimProperties(" +
                "enabled=$enabled, " +
                "maximumThreads=$maximumThreads, " +
                "waitBetweenRequests=$waitBetweenRequests, " +
                "url='$url', " +
                "email='$email'" +
                ")"
    }
}
