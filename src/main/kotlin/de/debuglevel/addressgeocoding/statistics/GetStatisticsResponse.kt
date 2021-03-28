package de.debuglevel.addressgeocoding.statistics

import de.debuglevel.addressgeocoding.geocoding.Statistics

data class GetStatisticsResponse(
    var name: String,
    val unreachable: Int,
    val unknownAddress: Int,
    val success: Int,
    val averageRequestDuration: Double? = null,
) {
    constructor(name: String, statistics: Statistics) : this(
        name,
        statistics.unreachable,
        statistics.unknownAddress,
        statistics.success,
        statistics.averageRequestDuration
    )
}