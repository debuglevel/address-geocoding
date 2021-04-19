package de.debuglevel.addressgeocoding.geocoder.photon.client


import com.fasterxml.jackson.annotation.JsonProperty

data class Feature(
    @JsonProperty("geometry")
    val geometry: Geometry,
    @JsonProperty("properties")
    val properties: Properties,
    @JsonProperty("type")
    val type: String
)