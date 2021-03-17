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
    var address: String,
    var longitude: Double?,
    var latitude: Double?,
    var failedAttempts: Int = 0,
    @DateCreated
    var createdOn: LocalDateTime = LocalDateTime.now(),
    @DateUpdated
    var lastModifiedOn: LocalDateTime = LocalDateTime.now(),
)