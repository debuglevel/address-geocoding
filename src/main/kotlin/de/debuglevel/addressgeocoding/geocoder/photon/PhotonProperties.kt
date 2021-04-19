package de.debuglevel.addressgeocoding.geocoder.photon

import de.debuglevel.addressgeocoding.geocode.GeocoderConfiguration
import de.debuglevel.addressgeocoding.geocoder.GeocoderProperties

class PhotonProperties() : GeocoderProperties() {
    constructor(geocoderProperties: GeocoderConfiguration) : this() {
        this.enabled = geocoderProperties.enabled
        this.maximumThreads = geocoderProperties.maximumThreads
        this.waitBetweenRequests = geocoderProperties.waitBetweenRequests
        this.url = geocoderProperties.url
    }

    override fun toString(): String {
        return "PhotonProperties(" +
                "enabled=$enabled, " +
                "maximumThreads=$maximumThreads, " +
                "waitBetweenRequests=$waitBetweenRequests, " +
                "url='$url'" +
                ")"
    }
}
