package de.debuglevel.addressgeocoding.geocoding.photon

import de.debuglevel.addressgeocoding.geocode.GeocoderConfiguration
import de.debuglevel.addressgeocoding.geocoding.GeocoderProperties

class PhotonProperties() : GeocoderProperties() {
    constructor(geocoderProperties: GeocoderConfiguration) : this() {
        this.enabled = geocoderProperties.enabled
        this.maximumThreads = geocoderProperties.maximumThreads
        this.waitBetweenRequests = geocoderProperties.waitBetweenRequests
        this.url = geocoderProperties.url
    }
}
