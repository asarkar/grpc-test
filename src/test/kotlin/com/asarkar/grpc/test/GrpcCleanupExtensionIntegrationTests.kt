package com.asarkar.grpc.test

import io.grpc.ManagedChannel
import io.grpc.Server
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.platform.commons.PreconditionViolationException
import org.junit.platform.engine.discovery.DiscoverySelectors.selectClass
import org.junit.platform.engine.discovery.DiscoverySelectors.selectMethod
import org.junit.platform.testkit.engine.EngineTestKit
import org.junit.platform.testkit.engine.EventConditions.event
import org.junit.platform.testkit.engine.EventConditions.finishedSuccessfully
import org.junit.platform.testkit.engine.EventConditions.finishedWithFailure
import org.junit.platform.testkit.engine.TestExecutionResultConditions.instanceOf
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import org.mockito.listeners.MockCreationListener
import java.util.concurrent.TimeUnit

class GrpcCleanupExtensionIntegrationTests {
    @Test
    fun testSuccessful() {
        val mocks = mutableListOf<Any>()
        val listener = MockCreationListener { mock, _ -> mocks.add(mock) }
        Mockito.framework().addListener(listener)
        EngineTestKit.engine("junit-jupiter")
            .selectors(
                selectMethod(
                    ExampleTestCase::class.java,
                    ExampleTestCase::class.java.getDeclaredMethod("testSuccessful", Resources::class.java)
                )
            )
            .execute()
            .testEvents()
            .assertThatEvents()
            .haveExactly(
                1,
                event(finishedSuccessfully())
            )

        assertThat(mocks).hasSize(2)

        val server = mocks.find { it is Server } as Server?
        assertThat(server).isNotNull
        Mockito.verify(server)!!.shutdown()
        Mockito.verify(server)!!
            .awaitTermination(ArgumentMatchers.anyLong(), ArgumentMatchers.any(TimeUnit::class.java))
        val channel = mocks.find { it is ManagedChannel } as ManagedChannel?
        assertThat(channel).isNotNull
        Mockito.verify(channel)!!.shutdown()
        Mockito.verify(channel)!!
            .awaitTermination(ArgumentMatchers.anyLong(), ArgumentMatchers.any(TimeUnit::class.java))
    }

    @Test
    fun testFailedShutdown() {
        val mocks = mutableListOf<Any>()
        val listener = MockCreationListener { mock, _ -> mocks.add(mock) }
        Mockito.framework().addListener(listener)
        EngineTestKit.engine("junit-jupiter")
            .selectors(
                selectMethod(
                    ExampleTestCase::class.java,
                    ExampleTestCase::class.java.getDeclaredMethod("testFailedShutdown", Resources::class.java)
                )
            )
            .execute()
            .testEvents()
            .assertThatEvents()
            .haveExactly(
                1,
                event(finishedWithFailure(instanceOf(PostconditionViolationException::class.java)))
            )

        assertThat(mocks).hasSize(2)

        val server = mocks.find { it is Server } as Server?
        assertThat(server).isNotNull
        Mockito.verify(server)!!.shutdown()
        Mockito.verify(server)!!
            .awaitTermination(ArgumentMatchers.anyLong(), ArgumentMatchers.any(TimeUnit::class.java))
        val channel = mocks.find { it is ManagedChannel } as ManagedChannel?
        assertThat(channel).isNotNull
        Mockito.verify(channel)!!.shutdown()
        Mockito.verify(channel)!!
            .awaitTermination(ArgumentMatchers.anyLong(), ArgumentMatchers.any(TimeUnit::class.java))
    }

    @Test
    fun testException() {
        val mocks = mutableListOf<Any>()
        val listener = MockCreationListener { mock, _ -> mocks.add(mock) }
        Mockito.framework().addListener(listener)
        EngineTestKit.engine("junit-jupiter")
            .selectors(
                selectMethod(
                    ExampleTestCase::class.java,
                    ExampleTestCase::class.java.getDeclaredMethod("testException", Resources::class.java)
                )
            )
            .execute()
            .testEvents()
            .assertThatEvents()
            .haveExactly(
                1,
                event(finishedWithFailure(instanceOf(RuntimeException::class.java)))
            )

        assertThat(mocks).hasSize(2)

        val server = mocks.find { it is Server } as Server?
        assertThat(server).isNotNull
        Mockito.verify(server)!!.shutdownNow()
        Mockito.verify(server)!!
            .awaitTermination(ArgumentMatchers.anyLong(), ArgumentMatchers.any(TimeUnit::class.java))
        val channel = mocks.find { it is ManagedChannel } as ManagedChannel?
        assertThat(channel).isNotNull
        Mockito.verify(channel)!!.shutdownNow()
        Mockito.verify(channel)!!
            .awaitTermination(ArgumentMatchers.anyLong(), ArgumentMatchers.any(TimeUnit::class.java))
    }

    @Test
    fun testTimeout() {
        val mocks = mutableListOf<Any>()
        val listener = MockCreationListener { mock, _ -> mocks.add(mock) }
        Mockito.framework().addListener(listener)
        EngineTestKit.engine("junit-jupiter")
            .selectors(
                selectMethod(
                    ExampleTestCase::class.java,
                    ExampleTestCase::class.java.getDeclaredMethod("testTimeout", Resources::class.java)
                )
            )
            .execute()
            .testEvents()
            .assertThatEvents()
            .haveExactly(
                1,
                event(finishedWithFailure(instanceOf(PostconditionViolationException::class.java)))
            )
        assertThat(mocks).hasSize(2)

        val server = mocks.find { it is Server } as Server?
        assertThat(server).isNotNull
        Mockito.verify(server)!!.shutdown()
        Mockito.verify(server)!!
            .awaitTermination(ArgumentMatchers.anyLong(), ArgumentMatchers.any(TimeUnit::class.java))
        val channel = mocks.find { it is ManagedChannel } as ManagedChannel?
        assertThat(channel).isNotNull
        Mockito.verify(channel)!!.shutdown()
        Mockito.verify(channel)!!
            .awaitTermination(ArgumentMatchers.anyLong(), ArgumentMatchers.any(TimeUnit::class.java))
    }

    @Test
    fun testReinitializeInstanceField() {
        val mocks = mutableListOf<Any>()
        val listener = MockCreationListener { mock, _ -> mocks.add(mock) }
        Mockito.framework().addListener(listener)
        EngineTestKit.engine("junit-jupiter")
            .selectors(
                selectClass(ExampleTestCase2::class.java)
            )
            .execute()
            .testEvents()
            .assertThatEvents()
            .haveExactly(
                3,
                event(finishedSuccessfully())
            )

        assertThat(mocks).allMatch { it is Set<*> }
        assertThat(mocks.flatMap { it as Set<*> }.toSet()).hasSize(3)
    }

    @Test
    fun testInitializeInstanceFieldOnlyOnce() {
        val mocks = mutableListOf<Any>()
        val listener = MockCreationListener { mock, _ -> mocks.add(mock) }
        Mockito.framework().addListener(listener)
        EngineTestKit.engine("junit-jupiter")
            .selectors(
                selectClass(ExampleTestCase3::class.java)
            )
            .execute()
            .testEvents()
            .assertThatEvents()
            .haveExactly(
                2,
                event(finishedSuccessfully())
            )

        assertThat(mocks).allMatch { it is Set<*> }
        assertThat(mocks.flatMap { it as Set<*> }.toSet()).hasSize(1)
    }

    @Test
    fun testInitializeStaticFieldOnlyOnce() {
        EngineTestKit.engine("junit-jupiter")
            .selectors(
                selectClass(ExampleTestCase4::class.java)
            )
            .execute()
            .testEvents()
            .assertThatEvents()
            .haveExactly(
                3,
                event(finishedSuccessfully())
            )

        assertThat(ExampleTestCase4.setOfResources).hasSize(1)
    }

    @Test
    fun testInheritResource() {
        val mocks = mutableListOf<Any>()
        val listener = MockCreationListener { mock, _ -> mocks.add(mock) }
        Mockito.framework().addListener(listener)
        EngineTestKit.engine("junit-jupiter")
            .selectors(
                selectClass(ExampleTestCase5::class.java)
            )
            .execute()
            .testEvents()
            .assertThatEvents()
            .haveExactly(
                1,
                event(finishedSuccessfully())
            )

        assertThat(mocks).allMatch { it is Set<*> }
        assertThat(mocks.flatMap { it as Set<*> }.toSet()).hasSize(1)
    }

    @Test
    fun testMultipleParametersError() {
        EngineTestKit.engine("junit-jupiter")
            .selectors(
                selectMethod(
                    ExampleTestCase::class.java,
                    ExampleTestCase::class.java.getDeclaredMethod(
                        "testMultipleParameters", Resources::class.java, Resources::class.java
                    )
                )
            )
            .execute()
            .testEvents()
            .assertThatEvents()
            .haveExactly(
                1,
                event(finishedWithFailure(instanceOf(PreconditionViolationException::class.java)))
            )
    }

    @Test
    fun testMultipleInstanceFieldsError() {
        EngineTestKit.engine("junit-jupiter")
            .selectors(
                selectClass(
                    ExampleTestCase6::class.java
                )
            )
            .execute()
            .allEvents()
            .assertThatEvents()
            .haveExactly(
                1,
                event(finishedWithFailure(instanceOf(PreconditionViolationException::class.java)))
            )
    }

    @Test
    fun testAlreadyInitializedField() {
        val mocks = mutableListOf<Any>()
        val listener = MockCreationListener { mock, _ -> mocks.add(mock) }
        Mockito.framework().addListener(listener)
        EngineTestKit.engine("junit-jupiter")
            .selectors(
                selectClass(ExampleTestCase7::class.java)
            )
            .execute()
            .testEvents()
            .assertThatEvents()
            .haveExactly(
                2,
                event(finishedSuccessfully())
            )

        assertThat(mocks).allMatch { it is Set<*> }
        assertThat(mocks.flatMap { it as Set<*> }.toSet()).hasSize(1)
    }

    @Test
    fun testAlreadyInitializedFieldError() {
        EngineTestKit.engine("junit-jupiter")
            .selectors(
                selectClass(ExampleTestCase8::class.java)
            )
            .execute()
            .testEvents()
            .assertThatEvents()
            .haveExactly(
                1,
                event(finishedWithFailure(instanceOf(PreconditionViolationException::class.java)))
            )
    }

    @Test
    fun testInitializeParameterOnlyOnce() {
        val mocks = mutableListOf<Any>()
        val listener = MockCreationListener { mock, _ -> mocks.add(mock) }
        Mockito.framework().addListener(listener)
        EngineTestKit.engine("junit-jupiter")
            .selectors(
                selectClass(ExampleTestCase9::class.java)
            )
            .execute()
            .testEvents()
            .assertThatEvents()
            .haveExactly(
                2,
                event(
                    finishedSuccessfully()
                )
            )

        assertThat(mocks).allMatch { it is Set<*> }
        assertThat(mocks.flatMap { it as Set<*> }.toSet()).hasSize(3)
    }

    @Test
    fun testNested() {
        EngineTestKit.engine("junit-jupiter")
            .selectors(
                selectClass(ExampleTestCase10::class.java)
            )
            .execute()
            .testEvents()
            .assertThatEvents()
            .haveExactly(
                1,
                event(finishedSuccessfully())
            )

        assertThat(ExampleTestCase10.setOfResources).hasSize(2)
    }
}
