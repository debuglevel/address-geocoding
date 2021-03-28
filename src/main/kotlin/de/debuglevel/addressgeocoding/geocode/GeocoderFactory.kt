package de.debuglevel.addressgeocoding.geocode

import de.debuglevel.addressgeocoding.geocoding.Geocoder
import de.debuglevel.addressgeocoding.geocoding.GeocoderProperties
import de.debuglevel.addressgeocoding.geocoding.nominatim.NominatimGeocoder
import de.debuglevel.addressgeocoding.geocoding.nominatim.NominatimProperties
import de.debuglevel.addressgeocoding.geocoding.photon.PhotonGeocoder
import de.debuglevel.addressgeocoding.geocoding.photon.PhotonProperties
import io.micronaut.context.annotation.EachBean
import io.micronaut.context.annotation.Factory
import mu.KotlinLogging

@Factory
class GeocoderFactory {
    private val logger = KotlinLogging.logger {}

    @EachBean(GeocoderConfiguration::class)
    internal fun geocoder(configuration: GeocoderConfiguration): Geocoder {
        logger.trace { "Building geocoder for configuration '${configuration.name}'..." }

        val property = when (configuration.type) {
            "photon" -> PhotonProperties(configuration)
            "nominatim" -> NominatimProperties(configuration).apply { email = configuration.email }
            else -> throw InvalidGeocoderType(configuration.type)
        }

        val geocoder = when (property) {
            is NominatimProperties -> NominatimGeocoder(property)
            is PhotonProperties -> PhotonGeocoder(property)
            else -> throw InvalidPropertyType(property)
        }

        logger.trace { "Built geocoder for configuration '${configuration.name}'" }
        return geocoder
    }

    class InvalidGeocoderType(type: String) : Exception("Invalid backend type '$type'")
    class InvalidPropertyType(property: GeocoderProperties) :
        Exception("Unsupported property type ${property.javaClass.name}")
}