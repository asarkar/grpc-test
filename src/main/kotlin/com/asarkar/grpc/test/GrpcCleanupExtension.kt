package com.asarkar.grpc.test

import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.ParameterContext
import org.junit.jupiter.api.extension.ParameterResolver
import org.junit.platform.commons.JUnitException
import org.junit.platform.commons.PreconditionViolationException

/**
 * A JUnit 5 [Extension](https://junit.org/junit5/docs/current/api/org.junit.jupiter.api/org/junit/jupiter/api/extension/Extension.html)
 * that can register gRPC resources and manages their automatic release at
 * the end of the test. If any of the registered resources can not be successfully released, fails the test.
 * Keeping in line with the [GrpcCleanupRule](https://grpc.github.io/grpc-java/javadoc/io/grpc/testing/GrpcCleanupRule.html),
 * tries to force release if the test has already failed.
 *
 * @author Abhijit Sarkar
 * @since 1.0.0
 */
class GrpcCleanupExtension :
    BeforeEachCallback, AfterEachCallback, ParameterResolver, BeforeAllCallback, AfterAllCallback {
    override fun beforeEach(ctx: ExtensionContext) {
        val resources = ctx.requiredTestMethod.parameters
            .filter { it.type == Resources::class.java }
        if (resources.size > 1) {
            throw PreconditionViolationException("At most one parameter of type Resources may be declared by a method")
        }

        if (ctx.isAccessResourcesField) {
            if (ctx.resourcesInstance != null) {
                throw PreconditionViolationException(
                    "Either set lifecycle PER_CLASS or don't initialize Resources field"
                )
            }
            ctx.resourcesInstance = Resources()
        }
    }

    override fun afterEach(ctx: ExtensionContext) {
        var successful = true
        ctx.resources[false]?.forEach {
            ctx.cleanUp(it)
            successful = it.awaitReleased()
        }

        if (ctx.isAccessResourcesField) {
            ctx.resourcesInstance?.also {
                ctx.cleanUp(it)
                successful = it.awaitReleased()

                ctx.resourcesInstance = null
            }
        }
        if (!successful) throw PostconditionViolationException("One or more Resources couldn't be released")
    }

    override fun supportsParameter(parameterContext: ParameterContext, extensionContext: ExtensionContext): Boolean {
        return parameterContext.parameter.type == Resources::class.java
    }

    override fun resolveParameter(parameterCtx: ParameterContext, extensionCtx: ExtensionContext): Any {
        val once = !parameterCtx.target.isPresent ||
            (
                extensionCtx.testInstanceLifecycle.orElse(null) == TestInstance.Lifecycle.PER_CLASS &&
                    parameterCtx.declaringExecutable.isAnnotationPresent(BeforeAll::class.java)
                )

        return Resources().also { extensionCtx.resources.getOrPut(once, { mutableListOf() }).add(it) }
    }

    override fun beforeAll(ctx: ExtensionContext) {
        val field = ctx.findResourcesField()

        if (field != null) {
            try {
                field.isAccessible = true
            } catch (e: ReflectiveOperationException) {
                throw JUnitException("Illegal state: Cannot access Resources field", e)
            }
            ctx.resourcesField = field
            if (ctx.resourcesInstance == null) {
                ctx.resourcesInstance = Resources()
            }
        }
    }

    override fun afterAll(ctx: ExtensionContext) {
        var successful = true
        ctx.resourcesInstance?.also {
            ctx.cleanUp(it)
            successful = it.awaitReleased()
        }
        ctx.resources[true]?.forEach {
            ctx.cleanUp(it)
            successful = it.awaitReleased()
        }
        if (!successful) throw PostconditionViolationException("One or more Resources couldn't be released")
    }
}
