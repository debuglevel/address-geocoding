package de.debuglevel.addressgeocoding.geocode

import java.util.*

data class AddGeocodeResponse(
    val id: UUID,
    val address: String,
    val longitude: Double?,
    val latitude: Double?,
) {
    constructor(geocode: Geocode) : this(
        geocode.id!!,
        geocode.address,
        geocode.longitude,
        geocode.latitude,
    )
}