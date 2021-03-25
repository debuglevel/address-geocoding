package de.debuglevel.addressgeocoding.geocoding.nominatim

import de.debuglevel.addressgeocoding.geocoding.AddressNotFoundException
import de.debuglevel.addressgeocoding.geocoding.Coordinate
import de.debuglevel.addressgeocoding.geocoding.Geocoder
import fr.dudie.nominatim.client.JsonNominatimClient
import fr.dudie.nominatim.client.request.NominatimSearchRequest
import fr.dudie.nominatim.model.Address
import io.micronaut.context.annotation.Requires
import mu.KotlinLogging
import org.apache.http.impl.client.HttpClientBuilder
import javax.inject.Singleton


@Singleton
@Requires(property = "app.address-geocoding.geocoders.nominatim.enabled", value = "true")
class NominatimGeocoder(
    private val nominatimProperties: NominatimProperties,
) : Geocoder(nominatimProperties) {
    private val logger = KotlinLogging.logger {}

    override fun getCoordinatesImpl(address: String): Coordinate {
        logger.debug { "Getting coordinates for address '$address'..." }

        val result = getNominatimAddress(address)

        val location = Coordinate(
            result.longitude,
            result.latitude
        )

        logger.debug { "Got coordinates for address '$address': $location" }
        return location
    }

    private val nominatimClient: JsonNominatimClient = buildNominatimClient()

    private fun buildNominatimClient(): JsonNominatimClient {
        logger.trace { "Building Nominatim client..." }

        val httpClient = HttpClientBuilder.create().build()
        val jsonNominatimClient = JsonNominatimClient(nominatimProperties.url, httpClient, nominatimProperties.email)

        logger.trace { "Built Nominatim client" }
        return jsonNominatimClient
    }

    private fun getNominatimAddress(address: String): Address {
        logger.debug("Searching address '$address'...")

        // OpenStreetMaps Nominatim API allows only 1 parallel connection. Ensure this with a lock.
        val addresses = withDelayedLock {
            logger.debug("Calling NominatimClient for address '$address'...")
            val searchRequest = NominatimSearchRequest()
            searchRequest.setQuery(address)
            val addresses = nominatimClient.search(searchRequest)
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