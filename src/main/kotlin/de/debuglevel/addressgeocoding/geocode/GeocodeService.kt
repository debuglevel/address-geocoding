package de.debuglevel.addressgeocoding.geocode

import de.debuglevel.addressgeocoding.Application.applicationContext
import de.debuglevel.addressgeocoding.geocoder.AddressNotFoundException
import de.debuglevel.addressgeocoding.geocoder.Geocoder
import de.debuglevel.addressgeocoding.geocoder.Statistics
import de.debuglevel.addressgeocoding.geocoder.UnreachableServiceException
import de.debuglevel.microservicecommons.backoff.LinearBackoff
import de.debuglevel.microservicecommons.outdate.OutdateUtils
import io.micronaut.data.exceptions.EmptyResultException
import io.micronaut.scheduling.annotation.Scheduled
import mu.KotlinLogging
import java.time.LocalDateTime
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.Future
import javax.inject.Singleton
import kotlin.time.ExperimentalTime

@Singleton
class GeocodeService(
    private val geocodeRepository: GeocodeRepository,
    private val serviceProperties: ServiceProperties,
    private val outdatingProperties: OutdatingProperties,
    private val failureBackoffProperties: FailureBackoffProperties,
) {
    private val logger = KotlinLogging.logger {}

    private val geocodingQueueMonitor = mutableMapOf<Geocode, Future<*>>()
    private val geocodingExecutor = Executors.newFixedThreadPool(serviceProperties.maximumThreads)

    private val geocoders: List<Geocoder>
        get() {
            return applicationContext
                .getBeansOfType(Geocoder::class.java)
                .toList()
        }

    /**
     * Gets a geocoder from all geocoders to perform the next geocoding action.
     * @implNote Returns the geocoder with fewest enqueued tasks; return random geocoder if no tasks are enqueued.
     */
    private val availableGeocoder: Geocoder
        get() {
            return if (this.geocoders.all { it.executorQueueSize == 0 }) {
                val randomGeocoder = this.geocoders.random()
                logger.trace { "Returning random geocoder (as all have an empty executor queue): ${randomGeocoder.javaClass.simpleName}..." }
                randomGeocoder
            } else {
                val geocoder = this.geocoders.minByOrNull { it.executorQueueSize }!!
                logger.trace { "Returning geocoder with smallest executor queue: ${geocoder.javaClass.simpleName}..." }
                geocoder
            }
        }

    val statistics: Map<String, Statistics>
        get() {
            logger.debug { "Getting statistics..." }

            val statistics = geocoders
                .associateBy({ it.javaClass.simpleName }, { it.statistics })

            logger.debug { "Got statistics: $statistics" }
            return statistics
        }

    fun get(id: UUID): Geocode {
        logger.debug { "Getting geocode with ID '$id'..." }

        val geocode: Geocode = geocodeRepository.findById(id).orElseThrow {
            logger.debug { "Getting geocode with ID '$id' failed" }
            EntityNotFoundException(id)
        }

        logger.debug { "Got geocode with ID '$id': $geocode" }
        return geocode
    }

    fun getAll(): Set<Geocode> {
        logger.debug { "Getting all geocodes ..." }

        val geocodes = geocodeRepository.findAll().toSet()

        logger.debug { "Got all geocodes" }
        return geocodes
    }

    @ExperimentalTime
    fun add(geocode: Geocode): Geocode {
        logger.debug { "Adding geocode '$geocode'..." }

        val savedGeocode = try {
            logger.debug { "Searching if geocode '$geocode' already exists..." }
            val foundGeocode = geocodeRepository.find(geocode.address)
            logger.debug { "Using already existing geocode '$geocode'..." }
            foundGeocode
        } catch (e: EmptyResultException) {
            logger.debug { "Saving not-already existing geocode '$geocode'..." }
            val savedGeocode = geocodeRepository.save(geocode)
            enqueueGeocoding(savedGeocode)
            savedGeocode
        }

        logger.debug { "Added geocode: $savedGeocode" }
        return savedGeocode
    }

    fun update(id: UUID, geocode: Geocode): Geocode {
        logger.debug { "Updating geocode '$geocode' with ID '$id'..." }

        // an object must be known to Hibernate (i.e. retrieved first) to get updated;
        // it would be a "detached entity" otherwise.
        val updateGeocode = this.get(id).apply {
            status = geocode.status
            address = geocode.address
            longitude = geocode.longitude
            latitude = geocode.latitude
            failedAttempts = geocode.failedAttempts
            lastGeocodingOn = geocode.lastGeocodingOn
            geocoder = geocode.geocoder
        }

        val updatedGeocode = geocodeRepository.update(updateGeocode)

        logger.debug { "Updated geocode: $updatedGeocode with ID '$id'" }
        return updatedGeocode
    }

    @ExperimentalTime
    @Scheduled(
        fixedDelay = "\${app.address-geocoding.scheduler.interval:60s}",
        initialDelay = "\${app.address-geocoding.scheduler.initial-delay:60s}"
    )
    fun updateGeocodes() {
        logger.debug { "Updating outdated or missing-data geocodes..." }

        cleanupQueueMonitor()

        // TODO: get only those with lon==null or/and lat==null from database
        // TODO:  that's not useful since isOutdated() is also checked
        geocodeRepository.findAll()
            .filter { isMissingData(it) || isOutdated(it) }
            .filter { isBackedOff(it) }
            .forEach { enqueueGeocoding(it) }
    }

    /**
     * Removes all done geocoding tasks from queue monitor.
     */
    private fun cleanupQueueMonitor() {
        logger.debug { "Cleaning up queue monitor (${geocodingQueueMonitor.count()} entries)..." }
        geocodingQueueMonitor
            .filter { it.value.isDone }
            .forEach { geocodingQueueMonitor.remove(it.key) }
        logger.debug { "Cleaned up queue monitor (${geocodingQueueMonitor.count()} entries)" }
    }

    private fun isMissingData(geocode: Geocode): Boolean {
        logger.trace { "Checking if geocode $geocode has missing data..." }
        val isMissingData = geocode.latitude == null || geocode.longitude == null
        logger.trace { "Checked if geocode $geocode has missing data: $isMissingData" }
        return isMissingData
    }

    private fun isOutdated(geocode: Geocode): Boolean {
        logger.trace { "Checking if geocode $geocode is outdated (outdating-interval=${outdatingProperties.duration})..." }
        val outdated = OutdateUtils.isOutdated(geocode.lastGeocodingOn, outdatingProperties.duration)
        logger.trace { "Checked if geocode $geocode is outdated (outdating-interval=${outdatingProperties.duration}): $outdated" }
        return outdated
    }

    private fun isBackedOff(geocode: Geocode): Boolean {
        logger.trace { "Checking if geocode $geocode is backed off..." }
        val isBackedOff = LinearBackoff.isBackedOff(
            geocode.lastGeocodingOn,
            geocode.failedAttempts.toLong(),
            failureBackoffProperties.multiplierDuration,
            failureBackoffProperties.maximumDuration,
            geocode.hashCode()
        )
        logger.trace { "Checked if geocode $geocode is backed off: $isBackedOff" }
        return isBackedOff
    }

    @ExperimentalTime
    private fun enqueueGeocoding(geocode: Geocode) {
        logger.debug { "Enqueuing $geocode for geocoding..." }

        if (!geocodingQueueMonitor.contains(geocode)) {
            val future = geocodingExecutor.submit {
                logger.debug { "Starting enqueued task for $geocode..." }
                if (isBackedOff(geocode)) {
                    geocode(geocode)
                }
                logger.debug { "Ended enqueued task for $geocode" }
            }
            logger.debug { "Enqueued $geocode for geocoding" }
            geocodingQueueMonitor[geocode] = future
        } else {
            logger.debug { "Not enqueued $geocode as it was already on queue" }
        }
    }

    @ExperimentalTime
    fun geocode(geocode: Geocode) {
        logger.debug { "Geocoding $geocode..." }

        geocode.lastGeocodingOn = LocalDateTime.now()

        try {
            val geocoder = availableGeocoder
            val coordinates = geocoder.getCoordinates(geocode.address)
            geocode.apply {
                latitude = coordinates.latitude
                longitude = coordinates.longitude
                status = Status.Succeeded
                failedAttempts = 0
                this.geocoder = geocoder.name
            }

            update(geocode.id!!, geocode)

            logger.debug { "Geocoded $geocode" }
        } catch (e: AddressNotFoundException) {
            logger.debug { "Geocoding $geocode failed as address is unknown to geocoder" }

            geocode.apply {
                status = Status.AddressNotFound
                failedAttempts += 1
            }

            update(geocode.id!!, geocode)
        } catch (e: UnreachableServiceException) {
            logger.warn(e) { "Geocoding $geocode failed as backend could not be reached" }

            geocode.apply {
                status = Status.FailedDueToUnreachableService
                failedAttempts += 1
            }

            update(geocode.id!!, geocode)
        } catch (e: Exception) {
            logger.warn(e) { "Geocoding $geocode failed with unknown exception" }

            geocode.apply {
                status = Status.FailedDueToUnexpectedError
                failedAttempts += 1
            }

            update(geocode.id!!, geocode)
        }
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