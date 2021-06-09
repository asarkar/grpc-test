package com.asarkar.grpc.test

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(GrpcCleanupExtension::class)
class ExampleTestCase6 {
    private lateinit var resources: Resources
    private lateinit var resources2: Resources

    @Test
    fun testMultipleInstances() {
    }
}
