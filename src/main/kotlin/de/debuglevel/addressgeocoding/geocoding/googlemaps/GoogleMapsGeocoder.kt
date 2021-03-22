package de.debuglevel.addressgeocoding.geocoding.googlemaps

import de.debuglevel.addressgeocoding.geocoding.Coordinate
import de.debuglevel.addressgeocoding.geocoding.Geocoder
import io.micronaut.context.annotation.Requires
import javax.inject.Singleton

@Singleton
@Requires(property = "app.address-geocoding.geocoders.googlemaps.enabled", value = "true")
class GoogleMapsGeocoder : Geocoder {
    init {
        TODO("Not yet implemented")
    }

    override fun getCoordinates(address: String): Coordinate {
        TODO("Not yet implemented")
    }
}