package de.debuglevel.addressgeocoding.geocoder.nominatim

import de.debuglevel.addressgeocoding.geocoder.AddressNotFoundException
import de.debuglevel.addressgeocoding.geocoder.Coordinate
import de.debuglevel.addressgeocoding.geocoder.Geocoder
import fr.dudie.nominatim.client.JsonNominatimClient
import fr.dudie.nominatim.client.request.NominatimSearchRequest
import fr.dudie.nominatim.model.Address
import mu.KotlinLogging
import org.apache.http.impl.client.HttpClientBuilder
import kotlin.time.ExperimentalTime

class NominatimGeocoder(
    private val nominatimProperties: NominatimProperties,
) : Geocoder(nominatimProperties) {
    private val logger = KotlinLogging.logger {}

    private val nominatimClient: JsonNominatimClient = buildNominatimClient()

    @ExperimentalTime
    override fun getCoordinatesImpl(address: String): Coordinate {
        logger.debug { "Getting coordinates for address '$address'..." }

        val result = getNominatimAddress(address)

        val coordinate = Coordinate(
            result.longitude,
            result.latitude
        )

        logger.debug { "Got coordinates for address '$address': $coordinate" }
        return coordinate
    }

    private fun buildNominatimClient(): JsonNominatimClient {
        logger.trace { "Building Nominatim client..." }

        val httpClient = HttpClientBuilder.create().build()
        val jsonNominatimClient = JsonNominatimClient(nominatimProperties.url, httpClient, nominatimProperties.email)

        logger.trace { "Built Nominatim client" }
        return jsonNominatimClient
    }

    @ExperimentalTime
    private fun getNominatimAddress(address: String): Address {
        logger.debug("Searching address '$address'...")

        val addresses = withDelayedExecution {
            logger.debug("Calling NominatimClient for address '$address'...")
            val searchRequest = NominatimSearchRequest()
            searchRequest.setQuery(address)
            val addresses = withRecordedDuration {
                nominatimClient.search(searchRequest)
            }
            logger.debug("Called NominatimClient for address '$address': ${addresses.size} results.")
            addresses
        }

        if (addresses.isEmpty()) {
            logger.info { "Could not geocode '$address'; address is unknown" }
            throw AddressNotFoundException(address)
        }

        val result = addresses[0]
        // TODO: if available, prefer class="building"

        logger.debug("Searched address '$address': ${result.displayName}")
        return result
    }
}