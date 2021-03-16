package de.debuglevel.addressgeocoding.geocoding.nominatim

import de.debuglevel.addressgeocoding.geocoding.Coordinate
import de.debuglevel.addressgeocoding.geocoding.Geocoder
import fr.dudie.nominatim.client.JsonNominatimClient
import fr.dudie.nominatim.client.request.NominatimSearchRequest
import fr.dudie.nominatim.model.Address
import io.micronaut.context.annotation.Requires
import mu.KotlinLogging
import org.apache.http.impl.client.HttpClientBuilder
import javax.inject.Singleton
import kotlin.concurrent.withLock

@Singleton
@Requires(property = "app.address-geocoding.geocoders.nominatim.enabled", value = "true")
class NominatimGeocoder : Geocoder {
    private val logger = KotlinLogging.logger {}

    override fun getCoordinates(address: String): Coordinate {
        logger.debug { "Getting location for address '$address'..." }

        val result = getNominatimAddress(address)

        val location = Coordinate(
            result.longitude,
            result.latitude
        )

        logger.debug { "Got location for address '$address': $location" }
        return location
    }

    private val singleRequestLock = java.util.concurrent.locks.ReentrantLock()

    private val nominatimClient: JsonNominatimClient
        get() = buildNominatimClient()

    private fun buildNominatimClient(): JsonNominatimClient {
        logger.trace { "Building Nominatim client..." }

        val httpClient = HttpClientBuilder.create().build()

        val baseUrl = "https://nominatim.openstreetmap.org/"
        val email = "debuglevel.de"
        val jsonNominatimClient = JsonNominatimClient(baseUrl, httpClient, email)

        logger.trace { "Built Nominatim client" }
        return jsonNominatimClient
    }

    private fun getNominatimAddress(address: String): Address {
        logger.debug("Searching address '$address'...")

        val searchRequest = NominatimSearchRequest()
        searchRequest.setQuery(address)

        // OpenStreetMaps Nominatim API allows only 1 concurrent connection. Ensure this with a lock.
        logger.debug("Waiting for lock to call NominatimClient for address '$address'...")
        val addresses = singleRequestLock.withLock {
            logger.debug("Calling NominatimClient for address '$address'...")
            val addresses = nominatimClient.search(searchRequest)
            logger.debug("Called NominatimClient for address '$address': ${addresses.size} results.")
            addresses
        }

        if (addresses.isEmpty()) {
            logger.warn { "No address found for '$address'" }
            throw NoAddressesFoundException(address)
        }

        val result = addresses[0]
        // TODO: if available, prefer class="building"

        logger.debug("Searched address '$address': ${result.displayName}")
        return result
    }

    class NoAddressesFoundException(address: String) :
        Exception("No Nominatim API results found for address '$address'")
}