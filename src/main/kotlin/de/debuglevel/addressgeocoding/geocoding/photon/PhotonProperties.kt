package de.debuglevel.addressgeocoding.geocoding.photon

import de.debuglevel.addressgeocoding.geocoding.GeocoderProperties
import io.micronaut.context.annotation.ConfigurationProperties

@ConfigurationProperties("app.address-geocoding.geocoders.photon")
class PhotonProperties : GeocoderProperties()
