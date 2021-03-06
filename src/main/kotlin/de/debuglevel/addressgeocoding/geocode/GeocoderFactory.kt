package de.debuglevel.addressgeocoding.geocode

import de.debuglevel.addressgeocoding.geocoder.Geocoder
import de.debuglevel.addressgeocoding.geocoder.GeocoderProperties
import de.debuglevel.addressgeocoding.geocoder.nominatim.NominatimGeocoder
import de.debuglevel.addressgeocoding.geocoder.nominatim.NominatimProperties
import de.debuglevel.addressgeocoding.geocoder.photon.PhotonGeocoder
import de.debuglevel.addressgeocoding.geocoder.photon.PhotonProperties
import io.micronaut.context.annotation.EachBean
import io.micronaut.context.annotation.Factory
import mu.KotlinLogging

@Factory
class GeocoderFactory {
    private val logger = KotlinLogging.logger {}

    @EachBean(GeocoderConfiguration::class)
    internal fun geocoder(geocoderConfiguration: GeocoderConfiguration): Geocoder {
        logger.trace { "Building geocoder for configuration '${geocoderConfiguration.name}'..." }

        val geocoderProperties = buildGeocoderProperties(geocoderConfiguration)
        val geocoder = buildGeocoder(geocoderProperties)

        logger.trace { "Built geocoder for configuration '${geocoderConfiguration.name}'" }
        return geocoder
    }

    private fun buildGeocoder(geocoderProperties: GeocoderProperties): Geocoder {
        return when (geocoderProperties) {
            is NominatimProperties -> NominatimGeocoder(geocoderProperties)
            is PhotonProperties -> PhotonGeocoder(geocoderProperties)
            else -> throw InvalidPropertyType(geocoderProperties)
        }
    }

    private fun buildGeocoderProperties(geocoderConfiguration: GeocoderConfiguration): GeocoderProperties {
        logger.trace { "Building GeocoderProperties for $geocoderConfiguration..." }

        val geocoderProperties = when (geocoderConfiguration.type) {
            "photon" -> PhotonProperties(geocoderConfiguration)
            "nominatim" -> NominatimProperties(geocoderConfiguration).apply { email = geocoderConfiguration.email }
            else -> throw InvalidGeocoderType(geocoderConfiguration.type)
        }

        logger.trace { "Built GeocoderProperties for $geocoderConfiguration: $geocoderProperties" }
        return geocoderProperties
    }

    class InvalidGeocoderType(type: String) : Exception("Invalid backend type '$type'")
    class InvalidPropertyType(property: GeocoderProperties) :
        Exception("Unsupported property type ${property.javaClass.name}")
}