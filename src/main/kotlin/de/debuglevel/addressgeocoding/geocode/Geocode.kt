package de.debuglevel.addressgeocoding.geocode

import io.micronaut.data.annotation.DateCreated
import io.micronaut.data.annotation.DateUpdated
import java.time.LocalDateTime
import java.util.*
import javax.persistence.*

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
     * Geocoding status
     */
    @Enumerated(EnumType.STRING)
    var status: Status,
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
) {
    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Geocode

        if (id != other.id) return false
        if (address != other.address) return false
        if (longitude != other.longitude) return false
        if (latitude != other.latitude) return false
        if (lastGeocodingOn != other.lastGeocodingOn) return false
        if (failedAttempts != other.failedAttempts) return false
        if (createdOn != other.createdOn) return false
        if (lastModifiedOn != other.lastModifiedOn) return false

        return true
    }
}