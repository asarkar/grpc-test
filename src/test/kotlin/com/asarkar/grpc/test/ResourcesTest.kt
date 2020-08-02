package com.asarkar.grpc.test

import io.grpc.Server
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.Mockito

class ResourcesTest {
    @Test
    fun testEquality() {
        val server = Mockito.mock(Server::class.java)
        val resources1 = Resources().apply { register(server) }
        val resources2 = Resources().apply { register(server) }

        assertThat(resources1).isEqualTo(resources2)
    }

    @Test
    fun testHashCode() {
        val server = Mockito.mock(Server::class.java)
        val resources = Resources().apply { register(server) }

        assertThat(resources.hashCode()).isEqualTo(server.hashCode())
    }

    @Test
    fun testToString() {
        val server = Mockito.mock(Server::class.java, "mock")
        val resources = Resources().apply { register(server) }

        assertThat(resources.toString()).isEqualTo("Resources[mock]")
        assertThat(Resources().toString()).isEqualTo("Resources[]")
    }
}
