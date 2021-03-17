package de.debuglevel.addressgeocoding.geocoding.nominatim

// Tests commented out because @Requires interferes
//@MicronautTest
//@TestInstance(TestInstance.Lifecycle.PER_CLASS)
//class NominatimGeocoderTests {
//    @Inject
//    lateinit var geocoder: NominatimGeocoder
//
//    fun validAddresses() = TestDataProvider.`valid addresses`()
//
//    @ParameterizedTest
//    @MethodSource("validAddresses")
//    fun `get coordinates for address`(testData: TestDataProvider.GeocodeTestData) {
//        // Arrange
//
//        // Act
//        val location = geocoder.getCoordinates(testData.address)
//
//        // Assert
//        assertThat(location.latitude).isEqualTo(testData.latitude, within(TestDataProvider.about7meters*2))
//        assertThat(location.longitude).isEqualTo(testData.longitude, within(TestDataProvider.about7meters*2))
//    }
//}