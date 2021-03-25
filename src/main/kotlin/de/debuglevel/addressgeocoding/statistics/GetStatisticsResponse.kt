package de.debuglevel.addressgeocoding.statistics

import de.debuglevel.addressgeocoding.geocoding.Statistics

data class GetStatisticsResponse(
    val unreachable: Int,
    val unknownAddress: Int,
    val success: Int,
    val averageRequestDuration: Double? = null,
) {
    constructor(statistics: Statistics) : this(
        statistics.unreachable,
        statistics.unknownAddress,
        statistics.success,
        statistics.averageRequestDuration,
    )
}