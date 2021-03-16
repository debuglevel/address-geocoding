package de.debuglevel.addressgeocoding.geocoding

interface Geocoder {
    fun getCoordinates(address: String): Coordinate
}