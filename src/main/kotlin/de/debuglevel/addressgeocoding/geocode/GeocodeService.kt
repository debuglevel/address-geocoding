package de.debuglevel.addressgeocoding.geocode

import mu.KotlinLogging
import java.io.InputStream
import java.io.PipedInputStream
import java.io.PipedOutputStream
import java.util.*
import javax.inject.Singleton
import kotlin.concurrent.thread

@Singleton
class GeocodeService(
    private val geocodeRepository: GeocodeRepository,
) {
    private val logger = KotlinLogging.logger {}

    fun get(id: UUID): Geocode {
        logger.debug { "Getting geocode with ID '$id'..." }

        val geocode: Geocode = geocodeRepository.findById(id).orElseThrow {
            logger.debug { "Getting geocode with ID '$id' failed" }
            EntityNotFoundException(id)
        }

        logger.debug { "Got geocode with ID '$id': $geocode" }
        return geocode
    }

    fun add(geocode: Geocode): Geocode {
        logger.debug { "Adding geocode '$geocode'..." }

        val savedGeocode = geocodeRepository.save(geocode)

        logger.debug { "Added geocode: $savedGeocode" }
        return savedGeocode
    }

    fun update(id: UUID, geocode: Geocode): Geocode {
        logger.debug { "Updating geocode '$geocode' with ID '$id'..." }

        // an object must be known to Hibernate (i.e. retrieved first) to get updated;
        // it would be a "detached entity" otherwise.
        val updateGeocode = this.get(id).apply {
            address = geocode.address
        }

        val updatedGeocode = geocodeRepository.update(updateGeocode)

        logger.debug { "Updated geocode: $updatedGeocode with ID '$id'" }
        return updatedGeocode
    }

    fun list(): Set<Geocode> {
        logger.debug { "Getting all geocodes ..." }

        val geocodes = geocodeRepository.findAll().toSet()

        logger.debug { "Got all geocodes" }
        return geocodes
    }

    fun delete(id: UUID) {
        logger.debug { "Deleting geocode with ID '$id'..." }

        if (geocodeRepository.existsById(id)) {
            geocodeRepository.deleteById(id)
        } else {
            throw EntityNotFoundException(id)
        }

        logger.debug { "Deleted geocode with ID '$id'" }
    }

    fun deleteAll() {
        logger.debug { "Deleting all geocodes..." }

        val countBefore = geocodeRepository.count()
        geocodeRepository.deleteAll() // CAVEAT: does not delete dependent entities; use this instead: geocodeRepository.findAll().forEach { geocodeRepository.delete(it) }
        val countAfter = geocodeRepository.count()
        val countDeleted = countBefore - countAfter

        logger.debug { "Deleted $countDeleted of $countBefore geocodes, $countAfter remaining" }
    }

    class EntityNotFoundException(criteria: Any) : Exception("Entity '$criteria' does not exist.")
}