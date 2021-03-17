package de.debuglevel.addressgeocoding.geocode

import de.debuglevel.addressgeocoding.geocoding.AddressNotFoundException
import de.debuglevel.addressgeocoding.geocoding.Geocoder
import io.micronaut.data.exceptions.EmptyResultException
import io.micronaut.scheduling.annotation.Scheduled
import mu.KotlinLogging
import java.util.*
import javax.inject.Singleton


@Singleton
class GeocodeService(
    private val geocodeRepository: GeocodeRepository,
    private val geocoder: Geocoder,
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

        val savedGeocode = try {
            logger.debug { "Searching if geocode '$geocode' already exists..." }
            val savedGeocode = geocodeRepository.find(geocode.address)
            logger.debug { "Using already existing geocode '$geocode'..." }
            savedGeocode
        } catch (e: EmptyResultException) {
            logger.debug { "Saving not-already existing geocode '$geocode'..." }
            geocodeRepository.save(geocode)
        }

        // TODO: should trigger an async geocode() call

        logger.debug { "Added geocode: $savedGeocode" }
        return savedGeocode
    }

    fun update(id: UUID, geocode: Geocode): Geocode {
        logger.debug { "Updating geocode '$geocode' with ID '$id'..." }

        // an object must be known to Hibernate (i.e. retrieved first) to get updated;
        // it would be a "detached entity" otherwise.
        val updateGeocode = this.get(id).apply {
            address = geocode.address
            longitude = geocode.longitude
            latitude = geocode.latitude
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

    @Scheduled(
        fixedDelay = "\${app.address-geocoding.missing-geocode-update.interval:60s}",
        initialDelay = "\${app.address-geocoding.missing-geocode-update.initial-delay:60s}"
    )
    fun updateMissingGeocodes() {
        logger.debug { "Geocoding items with missing longitude and latitude..." }

        // TODO: get only those with lon==null or/and lat==null from database
        geocodeRepository.findAll()
            .filter { it.latitude == null || it.longitude == null }
            .forEach { geocode(it) }
    }

    fun geocode(geocode: Geocode) {
        logger.debug { "Geocoding $geocode..." }

        try {
            val coordinates = geocoder.getCoordinates(geocode.address)
            geocode.apply {
                latitude = coordinates.latitude
                longitude = coordinates.longitude
            }

            update(geocode.id!!, geocode)

            logger.debug { "Geocoded $geocode" }
        } catch (e: AddressNotFoundException) {
            logger.debug { "Geocoding $geocode failed as address is unknown to geocoder" }
        }
    }

    class EntityNotFoundException(criteria: Any) : Exception("Entity '$criteria' does not exist.")
}