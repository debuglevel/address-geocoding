package de.debuglevel.addressgeocoding.geocoding.photon


import com.fasterxml.jackson.annotation.JsonProperty

data class Properties(
    @JsonProperty("city")
    val city: String?,
    @JsonProperty("country")
    val country: String?,
    @JsonProperty("countrycode")
    val countrycode: String?,
    @JsonProperty("district")
    val district: String?,
    @JsonProperty("extent")
    val extent: List<Double>?,
    @JsonProperty("housenumber")
    val housenumber: String?,
    @JsonProperty("locality")
    val locality: String?,
    @JsonProperty("name")
    val name: String?,
    @JsonProperty("osm_id")
    val osmId: Long?,
    @JsonProperty("osm_key")
    val osmKey: String?,
    @JsonProperty("osm_type")
    val osmType: String?,
    @JsonProperty("osm_value")
    val osmValue: String?,
    @JsonProperty("postcode")
    val postcode: String?,
    @JsonProperty("state")
    val state: String?,
    @JsonProperty("street")
    val street: String?,
    @JsonProperty("type")
    val type: String?
)