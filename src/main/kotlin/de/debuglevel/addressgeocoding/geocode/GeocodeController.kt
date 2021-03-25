package de.debuglevel.addressgeocoding.geocode

import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Delete
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import io.micronaut.security.annotation.Secured
import io.micronaut.security.rules.SecurityRule
import io.swagger.v3.oas.annotations.tags.Tag
import mu.KotlinLogging
import java.util.*
import kotlin.time.ExperimentalTime

@Secured(SecurityRule.IS_ANONYMOUS)
@Controller("/geocodes")
@Tag(name = "geocodes")
class GeocodeController(private val geocodeService: GeocodeService) {
    private val logger = KotlinLogging.logger {}

    /**
     * Get all geocodes
     * @return All geocodes
     */
    @Get("/")
    fun getAllGeocodes(): HttpResponse<List<GetGeocodeResponse>> {
        logger.debug("Called getAllGeocodes()")
        return try {
            val geocodes = geocodeService.list()
            val getGeocodeResponses = geocodes
                .map { GetGeocodeResponse(it) }

            HttpResponse.ok(getGeocodeResponses)
        } catch (e: Exception) {
            logger.error(e) { "Unhandled exception" }
            HttpResponse.serverError()
        }
    }

    /**
     * Get a geocode
     * @param id ID of the geocode
     * @return A geocode
     */
    @Get("/{id}")
    fun getOneGeocode(id: UUID): HttpResponse<GetGeocodeResponse> {
        logger.debug("Called getOneGeocode($id)")
        return try {
            val geocode = geocodeService.get(id)

            val getGeocodeResponse = GetGeocodeResponse(geocode)
            HttpResponse.ok(getGeocodeResponse)
        } catch (e: GeocodeService.EntityNotFoundException) {
            logger.debug { "Getting geocode $id failed: ${e.message}" }
            HttpResponse.notFound()
        } catch (e: Exception) {
            logger.error(e) { "Unhandled exception" }
            HttpResponse.serverError()
        }
    }

    /**
     * Create a geocode.
     * @return A geocode with their ID
     */
    @ExperimentalTime
    @Post("/")
    fun postOneGeocode(addGeocodeRequest: AddGeocodeRequest): HttpResponse<AddGeocodeResponse> {
        logger.debug("Called postOneGeocode($addGeocodeRequest)")

        return try {
            val geocode = addGeocodeRequest.toGeocode()
            val addedGeocode = geocodeService.add(geocode)

            val addGeocodeResponse = AddGeocodeResponse(addedGeocode)
            HttpResponse.created(addGeocodeResponse)
        } catch (e: Exception) {
            logger.error(e) { "Unhandled exception" }
            HttpResponse.serverError()
        }
    }

    /**
     * Delete a geocode.
     * @param id ID of the geocode
     */
    @Delete("/{id}")
    fun deleteOneGeocode(id: UUID): HttpResponse<Unit> {
        logger.debug("Called deleteOneGeocode($id)")
        return try {
            geocodeService.delete(id)

            HttpResponse.noContent()
        } catch (e: GeocodeService.EntityNotFoundException) {
            HttpResponse.notFound()
        } catch (e: Exception) {
            logger.error(e) { "Unhandled exception" }
            HttpResponse.serverError()
        }
    }

    /**
     * Delete all geocode.
     */
    @Delete("/")
    fun deleteAllGeocodes(): HttpResponse<Unit> {
        logger.debug("Called deleteAllGeocodes()")
        return try {
            geocodeService.deleteAll()

            HttpResponse.noContent()
        } catch (e: Exception) {
            logger.error(e) { "Unhandled exception" }
            HttpResponse.serverError()
        }
    }
}