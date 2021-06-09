package com.asarkar.grpc.test

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(GrpcCleanupExtension::class)
class GrpcCleanupExtensionTests {
    private lateinit var resources: Resources

    @Test
    fun testParameterInjection(resources: Resources) {
        Assertions.assertThat(this.resources).isNotNull
        Assertions.assertThat(resources).isNotNull
        Assertions.assertThat(resources).isNotSameAs(this.resources)
    }
}
