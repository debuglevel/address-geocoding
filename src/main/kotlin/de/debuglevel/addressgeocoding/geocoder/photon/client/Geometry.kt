package de.debuglevel.addressgeocoding.geocoder.photon.client


import com.fasterxml.jackson.annotation.JsonProperty

data class Geometry(
    @JsonProperty("coordinates")
    val coordinates: List<Double>,
    @JsonProperty("type")
    val type: String
)