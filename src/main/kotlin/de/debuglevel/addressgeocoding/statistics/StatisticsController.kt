package de.debuglevel.addressgeocoding.statistics

import de.debuglevel.addressgeocoding.geocode.GeocodeService
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.security.annotation.Secured
import io.micronaut.security.rules.SecurityRule
import io.swagger.v3.oas.annotations.tags.Tag
import mu.KotlinLogging

@Secured(SecurityRule.IS_ANONYMOUS)
@Controller("/statistics")
@Tag(name = "statistics")
class StatisticsController(private val geocodeService: GeocodeService) {
    private val logger = KotlinLogging.logger {}

    @Get("/")
    fun getStatistics(): HttpResponse<List<GetStatisticsResponse>> {
        logger.debug("Called getStatistics()")
        return try {
            val statistics = geocodeService.getStatistics()
            val getStatisticsResponse = statistics.map {
                GetStatisticsResponse(it.key, it.value)
            }

            HttpResponse.ok(getStatisticsResponse)
        } catch (e: Exception) {
            logger.error(e) { "Unhandled exception" }
            HttpResponse.serverError()
        }
    }
}