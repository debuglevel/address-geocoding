package de.debuglevel.addressgeocoding.geocode

import io.micronaut.context.annotation.ConfigurationProperties
import java.time.Duration

/**
 * Data pending to geocode (missing, failure or outdated data) are processed within a scheduler.
 */
@ConfigurationProperties("app.address-geocoding.scheduler")
class SchedulerProperties {
    /**
     * Wait this duration after startup to start the scheduler the first time.
     */
    var initialDelay: Duration = Duration.ofSeconds(60)

    /**
     * Wait this duration after last schedule termination to process again.
     */
    var interval: Duration = Duration.ofSeconds(60)
}