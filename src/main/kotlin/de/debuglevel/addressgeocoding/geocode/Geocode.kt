package de.debuglevel.addressgeocoding.geocode

import io.micronaut.data.annotation.DateCreated
import io.micronaut.data.annotation.DateUpdated
import java.time.LocalDateTime
import java.util.*
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id

@Entity
data class Geocode(
    @Id
    @GeneratedValue
    var id: UUID?,
    /**
     * Address which should be geocoded
     */
    var address: String,
    /**
     * Longitude, if geocoding was successful; null if failed or not yet attempted.
     */
    var longitude: Double?,
    /**
     * Latitude, if geocoding was successful; null if failed or not yet attempted.
     */
    var latitude: Double?,
    /**
     * DateTime when the last attempt to geocoding was made; null if not yet attempted.
     */
    var lastGeocodingOn: LocalDateTime? = null,
    /**
     * How many times geocoding failed. Reset to 0 on success.
     */
    var failedAttempts: Int = 0,
    /**
     * When created in the database
     */
    @DateCreated
    var createdOn: LocalDateTime = LocalDateTime.now(),
    /**
     * When last modified in the database
     */
    @DateUpdated
    var lastModifiedOn: LocalDateTime = LocalDateTime.now(),
)