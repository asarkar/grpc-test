package com.asarkar.grpc.test.ignore

import com.asarkar.grpc.test.GrpcCleanupExtension
import com.asarkar.grpc.test.Resources
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito

open class BaseTestCase {
    protected lateinit var resources: Resources
}

@ExtendWith(GrpcCleanupExtension::class)
class ExampleTestCase5 : BaseTestCase() {
    private val setOfResources: MutableSet<Resources> = mutableSetOf()
    private val set = Mockito.spy(setOfResources)

    @Test
    fun testInheritResource() {
        assertThat(setOfResources.add(super.resources)).isTrue
    }
}
