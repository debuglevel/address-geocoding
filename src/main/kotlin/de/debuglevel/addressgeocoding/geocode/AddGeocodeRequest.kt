package de.debuglevel.addressgeocoding.geocode

data class AddGeocodeRequest(
    val address: String,
) {
    constructor(geocode: Geocode) : this(
        address = geocode.address
    )

    fun toGeocode(): Geocode {
        return Geocode(
            id = null,
            status = Status.Pending,
            address = this.address,
            longitude = null,
            latitude = null
        )
    }
}