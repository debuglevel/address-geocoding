package de.debuglevel.addressgeocoding.geocode

import java.util.*

data class GetGeocodeResponse(
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