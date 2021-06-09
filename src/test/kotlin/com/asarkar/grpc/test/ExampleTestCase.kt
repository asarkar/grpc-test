package com.asarkar.grpc.test

import io.grpc.ManagedChannel
import io.grpc.Server
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import java.lang.Thread.sleep
import java.time.Duration
import java.util.concurrent.TimeUnit

@ExtendWith(GrpcCleanupExtension::class)
class ExampleTestCase {
    private val server = Mockito.mock(Server::class.java)
    private val channel = Mockito.mock(ManagedChannel::class.java)

    @Test
    fun testSuccessful(resources: Resources) {
        Mockito.`when`(server.awaitTermination(ArgumentMatchers.anyLong(), ArgumentMatchers.any(TimeUnit::class.java)))
            .thenReturn(true)
        Mockito.`when`(channel.awaitTermination(ArgumentMatchers.anyLong(), ArgumentMatchers.any(TimeUnit::class.java)))
            .thenReturn(true)
        resources.register(server).register(channel)
    }

    @Test
    fun testFailedShutdown(resources: Resources) {
        Mockito.`when`(server.awaitTermination(ArgumentMatchers.anyLong(), ArgumentMatchers.any(TimeUnit::class.java)))
            .thenReturn(true)
        resources.register(server).register(channel)
    }

    @Test
    fun testException(resources: Resources) {
        Mockito.`when`(server.awaitTermination(ArgumentMatchers.anyLong(), ArgumentMatchers.any(TimeUnit::class.java)))
            .thenReturn(true)
        Mockito.`when`(channel.awaitTermination(ArgumentMatchers.anyLong(), ArgumentMatchers.any(TimeUnit::class.java)))
            .thenReturn(true)
        resources.register(server).register(channel)
        throw RuntimeException("Boom")
    }

    @Test
    fun testTimeout(resources: Resources) {
        Mockito.`when`(server.awaitTermination(ArgumentMatchers.anyLong(), ArgumentMatchers.any(TimeUnit::class.java)))
            .thenAnswer { sleep(1000); true }
        resources.register(server).register(channel, Duration.ofMillis(500))
    }

    @Test
    fun testMultipleParameters(resources: Resources, resources2: Resources) {
        resources.register(server)
        resources2.register(server)
    }
}
