package de.debuglevel.addressgeocoding.geocode

import io.micronaut.data.annotation.Repository
import io.micronaut.data.repository.CrudRepository
import java.util.*

@Repository
interface GeocodeRepository : CrudRepository<Geocode, UUID> {
    fun find(address: String): Geocode
}