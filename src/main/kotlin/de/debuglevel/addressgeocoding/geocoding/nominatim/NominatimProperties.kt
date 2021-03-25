package de.debuglevel.addressgeocoding.geocoding.nominatim

import de.debuglevel.addressgeocoding.geocoding.GeocoderProperties
import io.micronaut.context.annotation.ConfigurationProperties

@ConfigurationProperties("app.address-geocoding.geocoders.nominatim")
class NominatimProperties : GeocoderProperties() {
    /**
     * Base URL for the Nominatim service to use
     */
    var url: String = "https://nominatim.openstreetmap.org/"

    /**
     * Email address to include in requests according to the Nominatim usage policy (https://nominatim.org/release-docs/latest/api/Search/#other)
     */
    var email: String = "github.com/debuglevel/address-geocoding"
}
