package com.asarkar.grpc.test

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
import java.lang.reflect.Field
import java.lang.reflect.Modifier
import kotlin.reflect.KFunction1

/**
 * A JUnit 5 [Extension](https://junit.org/junit5/docs/current/api/org.junit.jupiter.api/org/junit/jupiter/api/extension/Extension.html) that can register gRPC resources and manages their automatic release at
 * the end of the test. If any of the registered resources can not be successfully released, fails the test.
 * Keeping in line with the [GrpcCleanupRule](https://grpc.github.io/grpc-java/javadoc/io/grpc/testing/GrpcCleanupRule.html),
 * tries to force release if the test has already failed.
 *
 * @author Abhijit Sarkar
 * @since 1.0.0
 *
 * @throws [PreconditionViolationException] if a test method or class declares more than one [Resources].
 * @throws [PostconditionViolationException] if one or more registered resources can not be successfully released.
 * @throws [JUnitException] if there is an unexpected problem.
 */
class GrpcCleanupExtension :
    BeforeEachCallback, AfterEachCallback, ParameterResolver, BeforeAllCallback, AfterAllCallback {
    private var resources: Resources? = null
    private var resourcesField: Field? = null

    override fun beforeEach(ctx: ExtensionContext) {
        val resources = ctx.requiredTestMethod.parameters
            .filter { it.type == Resources::class.java }
        if (resources.size > 1) {
            throw PreconditionViolationException("At most one parameter of type Resources may be declared by a method")
        }

        if (shouldAccessResourcesField(ctx)) {
            if (tryGet(ctx.requiredTestInstance) != null) {
                throw PreconditionViolationException(
                    "Either set lifecycle PER_CLASS or don't initialize Resources field"
                )
            }
            trySet(ctx.requiredTestInstance)
        }
    }

    override fun afterEach(ctx: ExtensionContext) {
        var successful = true
        this.resources?.also {
            getCleanUpMethod(ctx)(it)
            successful = it.awaitReleased()
        }

        if (shouldAccessResourcesField(ctx)) {
            tryGet(ctx.requiredTestInstance)?.also {
                getCleanUpMethod(ctx)(it)
                if (!it.awaitReleased()) {
                    if (!successful) throw PostconditionViolationException("$resources and $it couldn't be released")
                    else throw PostconditionViolationException("$it couldn't be released")
                }
                trySet(ctx.requiredTestInstance, null)
            }
        }
        if (!successful) throw PostconditionViolationException("$resources couldn't be released")
    }

    override fun supportsParameter(parameterContext: ParameterContext, extensionContext: ExtensionContext): Boolean {
        return parameterContext.parameter.type == Resources::class.java
    }

    override fun resolveParameter(parameterContext: ParameterContext, extensionContext: ExtensionContext): Any {
        return Resources().also { resources = it }
    }

    override fun beforeAll(ctx: ExtensionContext) {
        val field = findField(ctx)

        if (field != null) {
            try {
                field.isAccessible = true
            } catch (e: ReflectiveOperationException) {
                throw JUnitException("Illegal state: Cannot access Resources field", e)
            }
            resourcesField = field
            if (tryGet(ctx.testInstance.orElse(null)) == null) {
                trySet(ctx.testInstance.orElse(null))
            }
        }
    }

    override fun afterAll(ctx: ExtensionContext) {
        tryGet(ctx.testInstance.orElse(null))?.also {
            getCleanUpMethod(ctx)(it)
            if (!it.awaitReleased()) throw PostconditionViolationException("$it couldn't be released")
        }
    }

    private fun shouldAccessResourcesField(ctx: ExtensionContext): Boolean {
        return resourcesField != null &&
            ctx.testInstanceLifecycle.orElse(null) != TestInstance.Lifecycle.PER_CLASS &&
            !resourcesField.isStatic()
    }

    private fun trySet(target: Any?, value: Resources? = Resources()) {
        try {
            resourcesField?.takeIf { target != null || it.isStatic() }?.set(target, value)
        } catch (e: ReflectiveOperationException) {
            throw JUnitException("Illegal state: Cannot set Resources field", e)
        }
    }

    private fun tryGet(target: Any?): Resources? {
        return try {
            resourcesField?.takeIf { target != null || it.isStatic() }?.get(target) as Resources?
        } catch (e: ReflectiveOperationException) {
            throw JUnitException("Illegal state: Cannot get Resources field", e)
        }
    }

    private fun getCleanUpMethod(ctx: ExtensionContext): KFunction1<Resources, Unit> {
        return if (ctx.executionException.isPresent) Resources::forceCleanUp else Resources::cleanUp
    }

    private fun Field?.isStatic() = this != null && Modifier.isStatic(this.modifiers)

    private fun findField(ctx: ExtensionContext): Field? {
        return generateSequence<Pair<Class<*>?, Field?>>((ctx.requiredTestClass to null)) { (clazz, field) ->
            val fields = try {
                clazz!!.declaredFields.filter { it.type == Resources::class.java }
            } catch (e: ReflectiveOperationException) {
                throw JUnitException("Illegal state: Cannot find Resources field", e)
            }
            if (fields.size > 1) {
                throw PreconditionViolationException("At most one field of type Resources may be declared by a class")
            }
            val fld = fields.firstOrNull()
            when {
                fld != null -> (clazz to fld)
                clazz.superclass != null -> (clazz.superclass to field)
                else -> (null to field)
            }
        }
            .dropWhile { it.first != null && it.second == null }
            .take(1)
            .iterator()
            .next()
            .second
    }
}
