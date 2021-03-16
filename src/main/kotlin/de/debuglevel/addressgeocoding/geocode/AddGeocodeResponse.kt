package de.debuglevel.addressgeocoding.geocode

import java.util.*

data class AddGeocodeResponse(
    val id: UUID,
    val address: String,
) {
    constructor(geocode: Geocode) : this(
        geocode.id!!,
        geocode.address,
    )
}