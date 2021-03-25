package de.debuglevel.addressgeocoding.geocoding.photon

import de.debuglevel.addressgeocoding.geocoding.AddressNotFoundException
import de.debuglevel.addressgeocoding.geocoding.Coordinate
import de.debuglevel.addressgeocoding.geocoding.Geocoder
import io.micronaut.context.annotation.Requires
import mu.KotlinLogging
import javax.inject.Singleton
import kotlin.concurrent.withLock

@Singleton
@Requires(property = "app.address-geocoding.geocoders.photon.enabled", value = "true")
class PhotonGeocoder(
    private val photonProperties: PhotonProperties,
    private val photonClient: PhotonClient
) : Geocoder(photonProperties) {
    private val logger = KotlinLogging.logger {}

    override fun getCoordinatesImpl(address: String): Coordinate {
        logger.debug { "Getting coordinates for address '$address'..." }

        val feature = getPhotonFeature(address)

        val coordinate = Coordinate(
            feature.geometry.coordinates[0],
            feature.geometry.coordinates[1]
        )

        logger.debug { "Got coordinates for address '$address': $coordinate" }
        return coordinate
    }

    private val singleRequestLock = java.util.concurrent.locks.ReentrantLock()

    private fun getPhotonFeature(address: String): Feature {
        logger.debug("Searching address '$address'...")

        // Photon API should be used sequentially (i.e. with 1 concurrent connection).
        logger.debug("Waiting for lock to call PhotonClient for address '$address'...")
        val resultset = singleRequestLock.withLock {
            waitForNextRequestAllowed()
            setLastRequestDateTime()

            logger.debug("Calling PhotonClient for address '$address'...")
            val resultset = photonClient.geocode(address)
            logger.debug("Called PhotonClient for address '$address': ${resultset.features.size} results.")
            resultset
        }

        if (resultset.features.isEmpty()) {
            logger.info { "Could not geocode '$address'; address is unknown" }
            throw AddressNotFoundException(address)
        }

        val feature = resultset.features.first()
        // TODO: if available, prefer class="building"

        logger.debug("Searched address '$address': $feature")
        return feature
    }
}