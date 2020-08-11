package com.asarkar.grpc.test

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito

@ExtendWith(GrpcCleanupExtension::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ExampleTestCase3 {
    private var resources: Resources? = null
    private val setOfResources: MutableSet<Resources?> = mutableSetOf()
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
