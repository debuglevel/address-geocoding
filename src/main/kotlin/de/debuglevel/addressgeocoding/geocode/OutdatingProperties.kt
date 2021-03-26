package de.debuglevel.addressgeocoding.geocode

import io.micronaut.context.annotation.ConfigurationProperties
import java.time.Duration

/**
 * Geocodes can be marked as outdated after a duration to update their data.
 */
@ConfigurationProperties("app.address-geocoding.outdating")
class OutdatingProperties {
    /**
     * Duration after which a geocode is assumed to be outdated and should be requested again.
     */
    var duration: Duration = Duration.ofDays(90)
}