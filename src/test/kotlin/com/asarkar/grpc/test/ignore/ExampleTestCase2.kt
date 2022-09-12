package com.asarkar.grpc.test.ignore

import com.asarkar.grpc.test.GrpcCleanupExtension
import com.asarkar.grpc.test.Resources
import com.asarkar.grpc.test.TestUtils
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito

@ExtendWith(GrpcCleanupExtension::class)
class ExampleTestCase2 {
    private lateinit var resources: Resources
    private val setOfResources: MutableSet<Resources> = mutableSetOf()
    private val set = Mockito.spy(setOfResources)

    @Test
    fun test1() {
        resources.register(TestUtils.randomChannel())
        assertThat(setOfResources.add(resources)).isTrue
    }

    @Test
    fun test2() {
        resources.register(TestUtils.randomChannel())
        assertThat(setOfResources.add(resources)).isTrue
    }

    @Test
    fun test3() {
        resources.register(TestUtils.randomChannel())
        assertThat(setOfResources.add(resources)).isTrue
    }
}
