package com.asarkar.grpc.test

import org.junit.platform.commons.JUnitException

/**
 * Thrown if a [Resource] cannot be released.
 *
 * @author Abhijit Sarkar
 * @since 1.0.0
 */
internal class PostconditionViolationException(
    message: String,
    cause: Throwable? = null,
) : JUnitException(message, cause)
