package com.asarkar.grpc.test.ignore

import com.asarkar.grpc.test.GrpcCleanupExtension
import com.asarkar.grpc.test.Resources
import io.grpc.ManagedChannel
import io.grpc.Server
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import java.util.concurrent.TimeUnit

/**
 * Reproduces the success-aggregation masking bug in [GrpcCleanupExtension.afterAll].
 *
 * afterAll releases the instance-field [Resources] first, then the per-class (`once`) [Resources].
 * Each release overwrites `successful` instead of accumulating it, so an `once` release that
 * succeeds masks a field release that failed. The field resource here fails to release while the
 * `once` resource succeeds; afterAll must still fail the container.
 *
 * Run via [com.asarkar.grpc.test.GrpcCleanupExtensionIntegrationTests.testMaskedFailureInAfterAll];
 * excluded from the Gradle test task via the `ignore` package filter.
 */
@ExtendWith(GrpcCleanupExtension::class)
@TestInstance(PER_CLASS)
class ExampleTestCase13 {
    private lateinit var resources: Resources

    @BeforeAll
    fun beforeAll(once: Resources) {
        val channel = Mockito.mock(ManagedChannel::class.java)
        Mockito
            .`when`(
                channel.awaitTermination(
                    ArgumentMatchers.anyLong(),
                    ArgumentMatchers.any(TimeUnit::class.java),
                ),
            ).thenReturn(true)
        once.register(channel)

        val server = Mockito.mock(Server::class.java)
        Mockito
            .`when`(
                server.awaitTermination(
                    ArgumentMatchers.anyLong(),
                    ArgumentMatchers.any(TimeUnit::class.java),
                ),
            ).thenReturn(false)
        resources.register(server)
    }

    @Test
    fun test1() {
    }
}
