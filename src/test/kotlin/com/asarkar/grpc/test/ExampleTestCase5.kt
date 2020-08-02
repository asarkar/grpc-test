package com.asarkar.grpc.test

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito

open class BaseTestCase {
    protected var resources: Resources? = null
}

@ExtendWith(GrpcCleanupExtension::class)
class ExampleTestCase5 : BaseTestCase() {
    private val setOfResources: Set<Resources?> = mutableSetOf()
    private val set = Mockito.spy(setOfResources)

    @Test
    fun testInheritResource() {
        setOfResources.plus(super.resources)
    }
}
