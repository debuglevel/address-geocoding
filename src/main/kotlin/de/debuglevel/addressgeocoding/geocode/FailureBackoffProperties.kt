package de.debuglevel.addressgeocoding.geocode

import io.micronaut.context.annotation.ConfigurationProperties
import java.time.Duration

/**
 * Failed attempts to geocoding are retried again after a backoff duration.
 */
@ConfigurationProperties("app.address-geocoding.failure-backoff")
class FailureBackoffProperties {
    /**
     * The count of failed attempts is multiplied with this duration.
     *
     * After the third attempt, the next try is after 3 * 1day = 3days
     */
    var multiplierDuration: Duration = Duration.ofDays(1)

    /**
     * A new attempt is made at least after this duration,
     * even if the regular backoff duration calculation would be greater.
     */
    var maximumDuration: Duration = Duration.ofDays(30)
}