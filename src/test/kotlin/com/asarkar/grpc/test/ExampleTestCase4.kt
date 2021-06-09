package com.asarkar.grpc.test

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(GrpcCleanupExtension::class)
class ExampleTestCase4 {
    @Test
    fun test1() {
        setOfResources.add(resources)
    }

    @Test
    fun test2() {
        setOfResources.add(resources)
    }

    @Test
    fun test3() {
        setOfResources.add(resources)
    }

    companion object {
        @JvmStatic
        internal val setOfResources: MutableSet<Resources> = mutableSetOf()

        @JvmStatic
        private lateinit var resources: Resources
    }
}
