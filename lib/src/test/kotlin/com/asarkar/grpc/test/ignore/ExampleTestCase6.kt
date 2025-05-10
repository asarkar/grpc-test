package com.asarkar.grpc.test.ignore

import com.asarkar.grpc.test.GrpcCleanupExtension
import com.asarkar.grpc.test.Resources
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
