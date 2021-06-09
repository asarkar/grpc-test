package com.asarkar.grpc.test

import io.grpc.ManagedChannel
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito

@ExtendWith(GrpcCleanupExtension::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ExampleTestCase9 {
    private lateinit var channel: ManagedChannel
    private val setOfResources: MutableSet<Resources> = mutableSetOf()
    private val set = Mockito.spy(setOfResources)

    @BeforeAll
    fun beforeAll(resources: Resources) {
        channel = TestUtils.randomChannel()
        resources.register(channel)
        assertThat(setOfResources.add(resources)).isTrue
    }

    @Test
    fun test1(resources: Resources) {
        assertThat(channel.isShutdown).isFalse
        assertThat(channel.isTerminated).isFalse
        resources.register(TestUtils.randomChannel())
        assertThat(setOfResources.add(resources)).isTrue
    }

    @Test
    fun test2(resources: Resources) {
        assertThat(channel.isShutdown).isFalse
        assertThat(channel.isTerminated).isFalse
        resources.register(TestUtils.randomChannel())
        assertThat(setOfResources.add(resources)).isTrue
    }
}
