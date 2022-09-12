package com.asarkar.grpc.test.ignore

import com.asarkar.grpc.test.GrpcCleanupExtension
import com.asarkar.grpc.test.Resources
import com.asarkar.grpc.test.TestUtils
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(GrpcCleanupExtension::class)
class ExampleTestCase10 {
    @BeforeEach
    fun beforeEach(resources: Resources) {
        resources.register(TestUtils.randomChannel())
        assertThat(setOfResources.add(resources)).isTrue
    }

    @Nested
    inner class NestedClass {
        @Test
        fun test() {
        }
    }

    companion object {
        @JvmStatic
        internal val setOfResources: MutableSet<Resources> = mutableSetOf()

        @JvmStatic
        private lateinit var resources: Resources

        @JvmStatic
        @BeforeAll
        fun beforeAll() {
            resources.register(TestUtils.randomChannel())
            assertThat(setOfResources.add(resources)).isTrue
        }
    }
}
