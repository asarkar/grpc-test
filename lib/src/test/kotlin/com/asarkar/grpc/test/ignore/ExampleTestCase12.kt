package com.asarkar.grpc.test.ignore

import com.asarkar.grpc.test.GrpcCleanupExtension
import com.asarkar.grpc.test.Resources
import io.grpc.ManagedChannel
import io.grpc.Server
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import java.util.concurrent.TimeUnit

/**
 * Reproduces the success-aggregation masking bug in [GrpcCleanupExtension.afterEach].
 *
 * afterEach releases the method parameter [Resources] first, then the instance-field
 * [Resources]. Each release overwrites `successful` instead of accumulating it, so a field
 * release that succeeds masks a parameter release that failed. The parameter resource here
 * fails to release while the field resource succeeds; afterEach must still fail the test.
 *
 * Run via [com.asarkar.grpc.test.GrpcCleanupExtensionIntegrationTests.testMaskedFailureInAfterEach];
 * excluded from the Gradle test task via the `ignore` package filter.
 */
@ExtendWith(GrpcCleanupExtension::class)
class ExampleTestCase12 {
    private lateinit var resources: Resources

    @Test
    fun testMaskedFailure(params: Resources) {
        val server = Mockito.mock(Server::class.java)
        Mockito.`when`(
            server.awaitTermination(
                ArgumentMatchers.anyLong(),
                ArgumentMatchers.any(TimeUnit::class.java),
            ),
        ).thenReturn(false)
        params.register(server)

        val channel = Mockito.mock(ManagedChannel::class.java)
        Mockito.`when`(
            channel.awaitTermination(
                ArgumentMatchers.anyLong(),
                ArgumentMatchers.any(TimeUnit::class.java),
            ),
        ).thenReturn(true)
        resources.register(channel)
    }
}
