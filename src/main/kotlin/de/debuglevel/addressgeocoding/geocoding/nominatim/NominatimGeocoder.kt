package de.debuglevel.addressgeocoding.geocoding.nominatim

import de.debuglevel.addressgeocoding.geocoding.AddressNotFoundException
import de.debuglevel.addressgeocoding.geocoding.Coordinate
import de.debuglevel.addressgeocoding.geocoding.Geocoder
import de.debuglevel.addressgeocoding.geocoding.UnreachableServiceException
import fr.dudie.nominatim.client.JsonNominatimClient
import fr.dudie.nominatim.client.request.NominatimSearchRequest
import fr.dudie.nominatim.model.Address
import io.micronaut.context.annotation.Requires
import mu.KotlinLogging
import org.apache.http.impl.client.HttpClientBuilder
import java.io.IOException
import java.net.UnknownHostException
import javax.inject.Singleton
import kotlin.concurrent.withLock


@Singleton
@Requires(property = "app.address-geocoding.geocoders.nominatim.enabled", value = "true")
class NominatimGeocoder(
    private val nominatimProperties: NominatimProperties,
) : Geocoder(nominatimProperties) {
    private val logger = KotlinLogging.logger {}

    override fun getCoordinates(address: String): Coordinate {
        logger.debug { "Getting coordinates for address '$address'..." }

        val result = getNominatimAddress(address)

        val location = Coordinate(
            result.longitude,
            result.latitude
        )

        logger.debug { "Got coordinates for address '$address': $location" }
        return location
    }

    private val singleRequestLock = java.util.concurrent.locks.ReentrantLock()

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

        // OpenStreetMaps Nominatim API allows only 1 concurrent connection. Ensure this with a lock.
        logger.debug("Waiting for lock to call NominatimClient for address '$address'...")
        val addresses = singleRequestLock.withLock {
            waitForNextRequestAllowed()
            setLastRequestDateTime()

            val addresses = try {
                logger.debug("Calling NominatimClient for address '$address'...")
                val searchRequest = NominatimSearchRequest()
                searchRequest.setQuery(address)
                val addresses = nominatimClient.search(searchRequest)
                logger.debug("Called NominatimClient for address '$address': ${addresses.size} results.")
                addresses
            } catch (e: UnknownHostException) {
                throw UnreachableServiceException(e)
            } catch (e: IOException) {
                throw UnreachableServiceException(e)
            }

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