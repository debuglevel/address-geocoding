package de.debuglevel.commons.wait

import mu.KotlinLogging
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

object WaitUtils {
    private val logger = KotlinLogging.logger {}

    private val lastRequests = mutableMapOf<Any, LocalDateTime>()

    /**
     * Waits until the next request is permitted to be made.
     * @param requester The object which executes the request
     * @param waitBetweenRequestsNanoseconds How long to wait between to requests.
     */
    fun waitForNextRequestAllowed(requester: Any, waitBetweenRequestsNanoseconds: Long) {
        logger.trace { "Waiting until next request to '$requester' is allowed..." }

        val lastRequest = lastRequests[requester]
        waitForNextRequestAllowed(lastRequest, waitBetweenRequestsNanoseconds)

        logger.trace { "Waited until next request to '$requester' is allowed" }
    }

    /**
     * Waits until the next request is permitted to be made.
     * @param lastRequestOn When the last request was made.
     * @param waitBetweenRequestsNanoseconds How long to wait between to requests.
     */
    fun waitForNextRequestAllowed(lastRequestOn: LocalDateTime?, waitBetweenRequestsNanoseconds: Long) {
        logger.trace { "Waiting until next request is allowed..." }

        if (lastRequestOn != null) {
            val nextRequestDateTime = lastRequestOn.plusNanos(waitBetweenRequestsNanoseconds)
            val waitingTimeMilliseconds = ChronoUnit.MILLIS.between(LocalDateTime.now(), nextRequestDateTime)

            logger.trace { "Last request was on $lastRequestOn, waiting duration between requests is ${waitBetweenRequestsNanoseconds}ns, next request is on $nextRequestDateTime, waiting time is ${waitingTimeMilliseconds}ms" }

            if (waitingTimeMilliseconds > 0) {
                logger.debug { "Sleeping ${waitingTimeMilliseconds}ms until the next request is allowed..." }
                Thread.sleep(waitingTimeMilliseconds)
            }
        }

        logger.trace { "Waited until next request is allowed" }
    }

    /**
     * Sets the last request DateTime for an object to now()
     * @param requester The object which executes the request
     */
    fun setLastRequestDateTime(requester: Any) {
        lastRequests[requester] = LocalDateTime.now()
    }
}