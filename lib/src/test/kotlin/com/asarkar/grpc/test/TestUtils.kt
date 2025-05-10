package com.asarkar.grpc.test

import io.grpc.ManagedChannel
import io.grpc.inprocess.InProcessChannelBuilder
import kotlin.random.Random

internal object TestUtils {
    internal fun randomChannel(): ManagedChannel =
        InProcessChannelBuilder
            .forName((1..4).map { ('a' + Random.nextInt(0, 26)) }.joinToString(""))
            .build()
}
