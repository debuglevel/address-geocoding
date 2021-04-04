package de.debuglevel.commons.statistics

data class RequestDuration(
    val requester: Any,
    val scope: Any,
    var calls: Int,
    var averageDuration: Double? = null,
) {
    val durationSum: Double
        get() {
            return calls * (averageDuration ?: 0.0)
        }
}
