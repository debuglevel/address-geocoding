package de.debuglevel.addressgeocoding.geocode

import java.time.LocalDateTime
import java.util.*

data class GetGeocodeResponse(
    val id: UUID,
    val address: String,
    val longitude: Double?,
    val latitude: Double?,
    val failedAttempts: Int,
    val createdOn: LocalDateTime?,
    val lastModifiedOn: LocalDateTime?,
) {
    constructor(geocode: Geocode) : this(
        geocode.id!!,
        geocode.address,
        geocode.longitude,
        geocode.latitude,
        geocode.failedAttempts,
        geocode.createdOn,
        geocode.lastModifiedOn,
    )
}