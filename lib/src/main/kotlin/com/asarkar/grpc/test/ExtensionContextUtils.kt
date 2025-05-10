package com.asarkar.grpc.test

import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.platform.commons.JUnitException
import org.junit.platform.commons.PreconditionViolationException
import java.lang.reflect.Field
import java.lang.reflect.Modifier
import kotlin.reflect.KFunction1

private object ExtensionContextUtils {
    internal val NAMESPACE: ExtensionContext.Namespace =
        ExtensionContext.Namespace
            .create(*GrpcCleanupExtension::class.java.name.split(".").toTypedArray())
    internal const val RESOURCES = "resources"
    internal const val RESOURCES_FIELD = "resources-field"
}

@Suppress("UNCHECKED_CAST")
internal var ExtensionContext.resources: MutableMap<Boolean, MutableList<Resources>>
    get() =
        getStore(ExtensionContextUtils.NAMESPACE)
            .getOrDefault(
                ExtensionContextUtils.RESOURCES,
                MutableMap::class.java,
                mutableMapOf<Boolean, MutableList<Resources>>(),
            ) as MutableMap<Boolean, MutableList<Resources>>
    set(value) {
        getStore(ExtensionContextUtils.NAMESPACE)
            .put(ExtensionContextUtils.RESOURCES, value)
    }

internal var ExtensionContext.resourcesField: Field?
    get() =
        getStore(ExtensionContextUtils.NAMESPACE)
            .get(
                ExtensionContextUtils.RESOURCES_FIELD,
                Field::class.java,
            )
    set(value) {
        getStore(ExtensionContextUtils.NAMESPACE)
            .put(ExtensionContextUtils.RESOURCES_FIELD, value)
    }

internal var ExtensionContext.resourcesInstance: Resources?
    get() {
        return try {
            val target = testInstance.orElse(null)
            resourcesField?.takeIf { target != null || isStaticField }?.get(target) as Resources?
        } catch (e: ReflectiveOperationException) {
            throw JUnitException("Illegal state: Cannot get Resources field", e)
        }
    }
    set(value) {
        try {
            val target = testInstance.orElse(null)
            resourcesField?.takeIf { target != null || isStaticField }?.set(target, value)
        } catch (e: ReflectiveOperationException) {
            throw JUnitException("Illegal state: Cannot set Resources field", e)
        }
    }

private val ExtensionContext.isStaticField: Boolean
    get() = resourcesField != null && Modifier.isStatic(resourcesField!!.modifiers)

internal val ExtensionContext.isAccessResourcesField: Boolean
    get() =
        resourcesField != null &&
            testInstanceLifecycle.orElse(null) != TestInstance.Lifecycle.PER_CLASS &&
            !isStaticField

internal val ExtensionContext.cleanUp: KFunction1<Resources, Unit>
    get() = if (executionException.isPresent) Resources::forceCleanUp else Resources::cleanUp

internal fun ExtensionContext.findResourcesField(): Field? {
    return generateSequence<Pair<Class<*>?, Field?>>(
        (requiredTestClass to null),
    ) { (clazz, field) ->
        val fields =
            try {
                clazz!!.declaredFields.filter { it.type == Resources::class.java }
            } catch (e: ReflectiveOperationException) {
                throw JUnitException("Illegal state: Cannot find Resources field", e)
            }
        if (fields.size > 1) {
            throw PreconditionViolationException(
                "At most one field of type Resources may be declared by a class",
            )
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
