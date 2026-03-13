package com.asarkar.grpc.test.ignore

import com.asarkar.grpc.test.GrpcCleanupExtension
import com.asarkar.grpc.test.Resources
import com.asarkar.grpc.test.TestUtils
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import java.util.concurrent.CyclicBarrier
import java.util.concurrent.TimeUnit

/**
 * Reproduces the parallel-execution resource-isolation bug.
 *
 * The bug: [GrpcCleanupExtension]'s [ExtensionContext.Store] uses [getOrDefault], which walks
 * the context hierarchy. When [beforeAll] stores its [Resources] under the class-level context,
 * concurrent [resolveParameter] calls for [testA] and [testB] both find that same mutable map
 * via the hierarchy walk and append their own resources to the shared `false`-keyed list.
 *
 * Consequence: [afterEach] for [testB] cleans the shared list — which now contains [testA]'s
 * channel — while [testA] is still running. [testA] then observes `channelA.isShutdown == true`
 * and fails.
 *
 * Run via [com.asarkar.grpc.test.GrpcCleanupExtensionIntegrationTests.testConcurrentResourceIsolation]
 * with parallel execution enabled; never run directly by the Gradle test task (excluded via the
 * `ignore` package filter in build.gradle.kts).
 */
@ExtendWith(GrpcCleanupExtension::class)
@TestInstance(PER_CLASS)
@Execution(ExecutionMode.CONCURRENT)
class ExampleTestCase11 {
    companion object {
        @JvmStatic
        val barrier = CyclicBarrier(2)
    }

    @BeforeAll
    fun beforeAll(resources: Resources) {
        // Storing any resource here causes the class-level ExtensionContext.Store to hold the
        // shared MutableMap<Boolean, MutableList<Resources>> under the "resources" key.
        // Concurrent method-level resolveParameter calls then retrieve that same map object
        // via getOrDefault's hierarchy walk, creating the shared-state race.
        resources.register(TestUtils.randomChannel())
    }

    @Test
    fun testA(resources: Resources) {
        val ch = TestUtils.randomChannel()
        resources.register(ch)
        // Both tests must have registered before either exits, ensuring both are in the
        // shared resource list before testB's afterEach fires.
        barrier.await(5, TimeUnit.SECONDS)
        // Give testB time to return and its afterEach to run. With the bug, afterEach for
        // testB drains the shared list which includes testA's channel, shutting it down.
        Thread.sleep(300)
        assertThat(ch.isShutdown).isFalse()
    }

    @Test
    fun testB(resources: Resources) {
        resources.register(TestUtils.randomChannel())
        barrier.await(5, TimeUnit.SECONDS)
        // Return immediately so afterEach fires while testA is still sleeping.
    }
}
