package de.debuglevel.addressgeocoding.geocoding

/**
 * Statistics about the Geocoder
 */
data class Statistics(
    var unreachable: Int = 0,
    var unknownAddress: Int = 0,
    var success: Int = 0,
    var averageRequestDuration: Double? = null,
)
