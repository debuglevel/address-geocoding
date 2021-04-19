package de.debuglevel.addressgeocoding.geocoder

/**
 * Statistics about the Geocoder
 */
data class Statistics(
    var unreachable: Int = 0,
    var unknownAddress: Int = 0,
    var success: Int = 0,
    var averageRequestDuration: Double? = null,
    var queueSize: Int = 0,
) {
    val all: Int
        get() {
            return unreachable + unknownAddress + success
        }
}
