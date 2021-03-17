package de.debuglevel.addressgeocoding.geocoding.photon


import com.fasterxml.jackson.annotation.JsonProperty

data class Feature(
    @JsonProperty("geometry")
    val geometry: Geometry,
    @JsonProperty("properties")
    val properties: Properties,
    @JsonProperty("type")
    val type: String
)