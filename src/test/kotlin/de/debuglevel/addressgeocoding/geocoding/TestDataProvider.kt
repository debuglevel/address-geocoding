package de.debuglevel.addressgeocoding.geocoding

import java.util.stream.Stream

object TestDataProvider {
    data class GeocodeTestData(
        val address: String,
        val latitude: Double?,
        val longitude: Double?,
    )

    /**
     * longitude and (?) latitude +/- 0.0001 is about +/- 7m (in Germany)
     */
    val about7meters = 0.0001

    fun `valid addresses`() = Stream.of(
        GeocodeTestData("96047 Bamberg Markusplatz 3", 49.8956824, 10.883799413534243),
        GeocodeTestData("76185 Karlsruhe, Pfannkuchstraße 14", 49.010723, 8.3476224),
        GeocodeTestData("Kapuzinerstraße 17, 96047 Bamberg", 49.8938, 10.8855819),
        GeocodeTestData("Am Kranen 3, 96047 Bamberg", 49.8929635, 10.88620782490312),
        GeocodeTestData("Pödeldorfer Str. 174, 96050 Bamberg", 49.8997338, 10.923941186913893),
        GeocodeTestData("Kapuzinerstraße 15, 96047 Bamberg", 49.8936776, 10.8857587),
        GeocodeTestData("Altenburg 1, 96049 Bamberg", 49.880534, 10.8687914),
        GeocodeTestData("Geisfelder Str. 4, 96050 Bamberg", 49.8891068, 10.914465330203114),
    )
}