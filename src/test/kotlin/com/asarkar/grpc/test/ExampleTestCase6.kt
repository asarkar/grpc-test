package com.asarkar.grpc.test

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(GrpcCleanupExtension::class)
class ExampleTestCase6 {
    private var resources: Resources? = null
    private var resources2: Resources? = null

    @Test
    fun testMultipleInstances() {
    }
}
