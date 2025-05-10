package com.asarkar.grpc.test.ignore

import com.asarkar.grpc.test.GrpcCleanupExtension
import com.asarkar.grpc.test.Resources
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito

@ExtendWith(GrpcCleanupExtension::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ExampleTestCase3 {
    private lateinit var resources: Resources
    private val setOfResources: MutableSet<Resources> = mutableSetOf()
    private val set = Mockito.spy(setOfResources)

    @Test
    fun test1() {
        setOfResources.add(resources)
    }

    @Test
    fun test2() {
        setOfResources.add(resources)
    }
}
