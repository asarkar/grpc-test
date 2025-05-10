package com.asarkar.grpc.test

import io.grpc.ManagedChannel
import io.grpc.Server
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.reset
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import java.time.Duration
import java.util.concurrent.TimeUnit

class ResourcesTest {
    private val server = mock(Server::class.java)
    private val channel = mock(ManagedChannel::class.java)

    @AfterEach
    fun afterEach() {
        reset(server, channel)
    }

    @Test
    fun testEquality() {
        val resources1 = Resources().apply { register(server) }
        val resources2 = Resources().apply { register(server) }

        assertThat(resources1).isEqualTo(resources2)
    }

    @Test
    fun testHashCode() {
        val resources = Resources().apply { register(server) }

        assertThat(resources.hashCode()).isEqualTo(server.hashCode())
    }

    @Test
    fun testToString() {
        val server = mock(Server::class.java, "mock")
        val resources = Resources().apply { register(server) }

        assertThat(resources.toString()).isEqualTo("Resources[mock]")
        assertThat(Resources().toString()).isEqualTo("Resources[]")
    }

    @Test
    fun testCleanUp() {
        Resources().apply {
            register(server).register(channel)
        }
            .cleanUp()

        verify(server, times(1)).shutdown()
        verify(channel, times(1)).shutdown()
    }

    @Test
    fun testForceCleanUp() {
        Resources().apply {
            register(server).register(channel)
        }
            .forceCleanUp()

        verify(server, times(1)).shutdownNow()
        verify(channel, times(1)).shutdownNow()
    }

    @Test
    fun testAwaitReleaseSuccessful() {
        val resources =
            Resources().apply {
                register(server).register(channel)
            }

        `when`(server.awaitTermination(anyLong(), eq(TimeUnit.NANOSECONDS)))
            .thenReturn(true)
        `when`(channel.awaitTermination(anyLong(), eq(TimeUnit.NANOSECONDS)))
            .thenReturn(true)

        assertThat(resources.awaitRelease()).isTrue

        verify(server, never()).shutdownNow()
        verify(channel, never()).shutdownNow()
    }

    @Test
    fun testAwaitReleaseFailsWhenAnyResourceIsNotReleased() {
        val resources =
            Resources().apply {
                register(server).register(channel)
            }

        `when`(server.awaitTermination(anyLong(), eq(TimeUnit.NANOSECONDS)))
            .thenReturn(false)
        `when`(channel.awaitTermination(anyLong(), eq(TimeUnit.NANOSECONDS)))
            .thenReturn(true)

        assertThat(resources.awaitRelease()).isFalse

        verify(server, times(1)).shutdownNow()
        verify(channel, never()).shutdownNow()
    }

    @Test
    fun testAwaitReleaseFailsWhenAnyResourceTimeoutElapses() {
        val resources =
            Resources().apply {
                register(server, Duration.ofNanos(1)).register(channel)
            }

        `when`(server.awaitTermination(anyLong(), eq(TimeUnit.NANOSECONDS)))
            .thenReturn(true)
        `when`(channel.awaitTermination(anyLong(), eq(TimeUnit.NANOSECONDS)))
            .thenReturn(true)

        assertThat(resources.awaitRelease()).isFalse

        verify(server, times(1)).shutdownNow()
        verify(channel, never()).shutdownNow()
    }
}
