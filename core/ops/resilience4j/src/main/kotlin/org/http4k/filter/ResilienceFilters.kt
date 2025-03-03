package org.http4k.filter

import io.github.resilience4j.bulkhead.Bulkhead
import io.github.resilience4j.bulkhead.BulkheadFullException
import io.github.resilience4j.circuitbreaker.CallNotPermittedException
import io.github.resilience4j.circuitbreaker.CircuitBreaker
import io.github.resilience4j.ratelimiter.RateLimiter
import io.github.resilience4j.ratelimiter.RequestNotPermitted
import io.github.resilience4j.retry.Retry
import io.github.resilience4j.retry.Retry.ofDefaults
import io.github.resilience4j.timelimiter.TimeLimiter
import org.http4k.core.Filter
import org.http4k.core.Response
import org.http4k.core.Status.Companion.CLIENT_TIMEOUT
import org.http4k.core.Status.Companion.SERVICE_UNAVAILABLE
import org.http4k.core.Status.Companion.TOO_MANY_REQUESTS
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit.MILLISECONDS
import java.util.concurrent.TimeoutException

object ResilienceFilters {

    /**
     * Provide simple Circuit Breaker. Returns ServiceUnavailable when the circuit is open.
     * By default, uses a % failure rate of 50% detection and an Circuit Open period of 1minute
     */
    object CircuitBreak {
        private object CircuitError : RuntimeException()

        operator fun invoke(
            cb: CircuitBreaker = CircuitBreaker.ofDefaults("Circuit"),
            isError: (Response) -> Boolean = { it.status.serverError },
            onError: () -> Response = { Response(SERVICE_UNAVAILABLE.description("Circuit is open")) }
        ) = Filter { next ->
            {
                try {
                    cb.acquirePermission()
                    val start = cb.currentTimestamp
                    next(it).apply {
                        if (isError(this)) cb.onError(0, MILLISECONDS, CircuitError)
                        else cb.onSuccess(cb.currentTimestamp - start, cb.timestampUnit)
                    }
                } catch (_: CallNotPermittedException) {
                    onError()
                }
            }
        }
    }

    /**
     * Provide simple Retrying functionality. Returns the last response when retries expire.
     * By default, retries 3 times with a delay of 500ms between attempts, backing off at a 1.5x multiplier.
     */
    object RetryFailures {

        private class RetryError(val response: Response) : RuntimeException()

        operator fun invoke(
            retry: Retry = ofDefaults("Retrying"),
            isError: (Response) -> Boolean = { it.status.serverError }
        ) = Filter { next ->
            {
                try {
                    retry.executeCallable {
                        next(it).apply {
                            if (isError(this)) throw RetryError(this)
                        }
                    }
                } catch (e: RetryError) {
                    e.response
                }
            }
        }
    }

    /**
     * Provide simple Rate Limiter functionality.
     * By default, handles maximum of 50 requests per 5 seconds.
     */
    object RateLimit {
        operator fun invoke(
            rateLimit: RateLimiter = RateLimiter.ofDefaults("RateLimit"),
            onError: () -> Response = { Response(TOO_MANY_REQUESTS.description("Rate limit exceeded")) }
        ) = Filter { next ->
            {
                try {
                    rateLimit.executeCallable { next(it) }
                } catch (_: RequestNotPermitted) {
                    onError()
                }
            }
        }
    }

    /**
     * Provide simple Bulkhead functionality.
     * By default, handles 25 parallel requests, with zero wait time.
     */
    object Bulkheading {
        operator fun invoke(
            bulkhead: Bulkhead = Bulkhead.ofDefaults("Bulkhead"),
            onError: () -> Response = { Response(TOO_MANY_REQUESTS.description("Bulkhead limit exceeded")) }
        ) = Filter { next ->
            {
                try {
                    bulkhead.executeCallable { next(it) }
                } catch (_: BulkheadFullException) {
                    onError()
                }
            }
        }
    }

    /**
     * Provide simple timeout functionality.
     * By default, reject with HTTP 504 after 1 second.
     */
    object TimeLimit {
        operator fun invoke(
            timeLimiter: TimeLimiter = TimeLimiter.ofDefaults("TimeLimit"),
            onError: () -> Response = { Response(CLIENT_TIMEOUT.description("Time limit exceeded")) },
            futureSupplier: (() -> Response) -> Future<Response> = { CompletableFuture.supplyAsync(it) }
        ) = Filter { next ->
            {
                try {
                    timeLimiter.executeFutureSupplier {
                        futureSupplier {
                            next(it)
                        }
                    }
                } catch (_: TimeoutException) {
                    onError()
                }
            }
        }
    }
}
