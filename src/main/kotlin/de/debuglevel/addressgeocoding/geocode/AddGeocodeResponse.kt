package de.debuglevel.addressgeocoding.geocode

import java.util.*

data class AddGeocodeResponse(
    val id: UUID,
    val address: String,
    val status: Status,
    val longitude: Double?,
    val latitude: Double?,
) {
    constructor(geocode: Geocode) : this(
        geocode.id!!,
        geocode.address,
        geocode.status,
        geocode.longitude,
        geocode.latitude,
    )
}