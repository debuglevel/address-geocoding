package de.debuglevel.addressgeocoding.geocoding.photon

import de.debuglevel.addressgeocoding.geocoding.AddressNotFoundException
import de.debuglevel.addressgeocoding.geocoding.Coordinate
import de.debuglevel.addressgeocoding.geocoding.Geocoder
import mu.KotlinLogging
import kotlin.time.ExperimentalTime

class PhotonGeocoder(
    private val photonProperties: PhotonProperties,
) : Geocoder(photonProperties) {
    private val logger = KotlinLogging.logger {}

    private val photonClient: PhotonClient = buildPhotonClient()

    @ExperimentalTime
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

    private fun buildPhotonClient(): PhotonClient {
        logger.trace { "Building Photon client..." }

        val photonClient = PhotonClientImpl(photonProperties.url)

        logger.trace { "Built Photon client" }
        return photonClient
    }

    @ExperimentalTime
    private fun getPhotonFeature(address: String): Feature {
        logger.debug("Searching address '$address'...")

        val resultset = withDelayedExecution {
            logger.debug("Calling PhotonClient for address '$address'...")
            val resultset = withRecordedDuration {
                photonClient.geocode(address)
            }
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