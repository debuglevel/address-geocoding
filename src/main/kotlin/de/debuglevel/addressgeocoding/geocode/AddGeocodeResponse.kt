package de.debuglevel.addressgeocoding.geocode

import java.time.LocalDateTime
import java.util.*

data class AddGeocodeResponse(
    val id: UUID,
    val address: String,
    val status: Status,
    val longitude: Double?,
    val latitude: Double?,
    val lastGeocodingOn: LocalDateTime?,
    val failedAttempts: Int,
    val createdOn: LocalDateTime?,
    val lastModifiedOn: LocalDateTime?,
) {
    constructor(geocode: Geocode) : this(
        geocode.id!!,
        geocode.address,
        geocode.status,
        geocode.longitude,
        geocode.latitude,
        geocode.lastGeocodingOn,
        geocode.failedAttempts,
        geocode.createdOn,
        geocode.lastModifiedOn,
    )
}