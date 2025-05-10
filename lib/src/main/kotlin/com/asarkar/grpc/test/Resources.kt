package com.asarkar.grpc.test

import io.grpc.ManagedChannel
import io.grpc.Server
import java.time.Duration
import java.time.Instant
import java.util.Objects

/**
 * A collection of [Resource]s used by the test that will be released at the end.
 *
 * @author Abhijit Sarkar
 * @since 1.0.0
 */
class Resources {
    private val resources = mutableMapOf<Resource, Duration>()
    private var timeout = Duration.ofSeconds(5)

    /**
     * Registers the given server with the extension. Once registered, the server will be automatically
     * shutdown at the end of the test.
     *
     * @param server server that will be registered.
     * @param timeout a positive time limit for the server to shutdown. If it fails to shutdown
     * in time, the test will fail. Defaults to 5 seconds.
     *
     * @return this
     */
    @JvmOverloads
    fun <T : Server> register(
        server: T,
        timeout: Duration = this.timeout,
    ): Resources {
        return this.apply { this@Resources.resources[ServerResource(server)] = timeout }
    }

    /**
     * Registers the given channel with the extension. Once registered, the channel will be automatically
     * closed at the end of the test.
     *
     * @param channel channel that will be registered.
     * @param timeout a positive time limit for the channel to close. If it fails to close
     * in time, the test will fail. Defaults to 5 seconds.
     *
     * @return this
     */
    @JvmOverloads
    fun <T : ManagedChannel> register(
        channel: T,
        timeout: Duration = this.timeout,
    ): Resources {
        return this.apply {
            this@Resources.resources[
                ManagedChannelResource(channel),
            ] = timeout
        }
    }

    /**
     * A positive time limit for the registered resources to be released. If the resources fail to
     * release in time, the test will fail. Defaults to 5 seconds. Calling this method does not change
     * the timeout for the resources previously registered.
     *
     * @param timeout
     *
     * @return this
     */
    fun timeout(timeout: Duration): Resources {
        return this.apply { this@Resources.timeout = timeout }
    }

    internal fun cleanUp() {
        resources.keys.forEach { it.cleanUp() }
    }

    internal fun forceCleanUp() {
        resources.keys.forEach { it.forceCleanUp() }
    }

    internal fun awaitRelease(): Boolean {
        val start = Instant.now()
        var successful = true
        resources.forEach { (r, t) ->
            try {
                val timeLapsed = Duration.between(start, Instant.now()).abs()
                val timeout = t.minus(timeLapsed)
                if (timeout.isNegative || !r.awaitRelease(timeout)) {
                    successful = false
                    r.forceCleanUp()
                }
            } catch (e: InterruptedException) {
                successful = false
                Thread.currentThread().interrupt()
                r.forceCleanUp()
            }
        }
        return successful
    }

    override fun toString(): String {
        return if (resources.isEmpty()) {
            "Resources[]"
        } else {
            "Resources${resources.keys.map { it.toString() }}"
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Resources

        return resources.keys == other.resources.keys
    }

    override fun hashCode(): Int {
        return Objects.hashCode(resources.keys)
    }
}
