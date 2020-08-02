package com.asarkar.grpc.test

import io.grpc.ManagedChannel
import io.grpc.Server
import java.time.Duration
import java.util.concurrent.TimeUnit

/**
 * An abstraction of a resource used by the test that will be released at the end.
 *
 * @author Abhijit Sarkar
 * @since 1.0.0
 */
interface Resource {
    fun cleanUp()

    fun forceCleanUp()

    fun awaitReleased(duration: Duration): Boolean
}

sealed class AbstractResource<E : Any>(private val e: E) : Resource {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AbstractResource<*>

        return e == other.e
    }

    override fun hashCode(): Int {
        return e.hashCode()
    }

    override fun toString(): String {
        return e.toString()
    }
}

internal class ManagedChannelResource internal constructor(private val channel: ManagedChannel) :
    AbstractResource<ManagedChannel>(channel) {
    override fun cleanUp() {
        channel.shutdown()
    }

    override fun forceCleanUp() {
        channel.shutdownNow()
    }

    override fun awaitReleased(duration: Duration): Boolean {
        return channel.awaitTermination(duration.toNanos(), TimeUnit.NANOSECONDS)
    }
}

internal class ServerResource internal constructor(private val server: Server) : AbstractResource<Server>(server) {
    override fun cleanUp() {
        server.shutdown()
    }

    override fun forceCleanUp() {
        server.shutdownNow()
    }

    override fun awaitReleased(duration: Duration): Boolean {
        return server.awaitTermination(duration.toNanos(), TimeUnit.NANOSECONDS)
    }
}
