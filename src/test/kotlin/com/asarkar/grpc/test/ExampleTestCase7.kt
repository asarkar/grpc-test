package com.asarkar.grpc.test

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito

@ExtendWith(GrpcCleanupExtension::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ExampleTestCase7 {
    private var resources = Resources()
    private val setOfResources: Set<Resources?> = mutableSetOf()
    private val set = Mockito.spy(setOfResources)

    @Test
    fun test1() {
        setOfResources.plus(resources)
    }

    @Test
    fun test2() {
        setOfResources.plus(resources)
    }
}
