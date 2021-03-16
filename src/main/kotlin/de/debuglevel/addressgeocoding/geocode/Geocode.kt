package de.debuglevel.addressgeocoding.geocode

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
)