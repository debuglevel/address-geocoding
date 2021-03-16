package de.debuglevel.addressgeocoding.geocoding.photon

import de.debuglevel.addressgeocoding.geocoding.Coordinate
import de.debuglevel.addressgeocoding.geocoding.Geocoder
import io.micronaut.context.annotation.Requires
import javax.inject.Singleton

@Singleton
@Requires(property = "app.address-geocoding.geocoders.photon.enabled", value = "true")
class PhotonGeocoder : Geocoder {
    override fun getCoordinates(address: String): Coordinate {
        TODO("Not yet implemented")
    }
}