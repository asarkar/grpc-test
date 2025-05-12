# grpc-test

Includes a JUnit 5 [Extension](https://junit.org/junit5/docs/current/api/org.junit.jupiter.api/org/junit/jupiter/api/extension/Extension.html)
that can automatically release gRPC resources at the end of the test.
Like [GrpcCleanupRule](https://grpc.github.io/grpc-java/javadoc/io/grpc/testing/GrpcCleanupRule.html), but built for
JUnit 5 and actively maintained.

[![Sponsor](https://img.shields.io/badge/Sponsor-%E2%9D%A4-black?style=for-the-badge&logo=github&color=%23fe8e86)](https://github.com/sponsors/asarkar)
[![CI](<https://img.shields.io/github/actions/workflow/status/asarkar/grpc-test/ci.yml?branch=main&style=for-the-badge&logo=github>)](https://github.com/asarkar/grpc-test/actions?query=workflow%3A%22CI%22)
[![Maven](https://img.shields.io/maven-central/v/com.asarkar.grpc/grpc-test?style=for-the-badge&logo=apachemaven)](https://central.sonatype.com/artifact/com.asarkar.grpc/grpc-test?smo=true)
[![Javadoc](https://javadoc.io/badge2/com.asarkar.grpc/grpc-test/javadoc.svg?style=for-the-badge&logo=readthedocs&logoColor=white)](https://javadoc.io/doc/com.asarkar.grpc/grpc-test)
[![License](https://img.shields.io/github/license/asarkar/grpc-test?style=for-the-badge&logo=apache&color=blue)](https://www.apache.org/licenses/LICENSE-2.0)
[![COC](https://img.shields.io/badge/COC-Code%20Of%20Conduct-brightgreen?style=for-the-badge&logo=asterisk&logoColor=white)](https://github.com/asarkar/.github/blob/main/CODE_OF_CONDUCT.md)
[![JVM](https://img.shields.io/badge/dynamic/regex?style=for-the-badge&logo=openjdk&color=blue&label=JVM&url=https%3A%2F%2Fraw.githubusercontent.com%2Fasarkar%2Fgrpc-test%2Frefs%2Fheads%2Fmain%2F.java-version&search=%5Cd%2B)](https://github.com/asarkar/grpc-test/blob/main/.java-version)

## Usage

Declare a `Resources` in one of the three following ways, and register `Server` and/or `ManagedChannel` instances with
it.

Get a `Resources` from:

1. A test method parameter injection, or
2. An instance field, or
3. A static field.

The difference is in the lifecycle of the `Resources` object. For `#1`, a new instance is created for every test method.
`#2` is the same as `#1` unless the test class declares `@TestInstance(TestInstance.Lifecycle.PER_CLASS)`, in which case
one instance is shared among all the tests. `#3` is obviously one instance shared among all the tests.

```
@ExtendWith(GrpcCleanupExtension.class)
class ExampleTestCase {
    @Test
    void testSuccessful(Resources resources) {
        resources.register(server); // use default timeout
        resources.register(channel, Duration.ofSeconds(1)); // override default timeout
        resources.timeout(Duration.ofSeconds(3)); // change default timeout to 3 seconds
        resources.register(channel2) // channel2 timeout is 3 seconds; server and channel timeouts didn't change
    }
}

```

:information_source: Note that for `#2` and `#3`, if the variable is already been assigned a value by the user, the
extension will not reinitialize it.

:information_source: If you're writing `@Nested` tests, see [issues/8](https://github.com/asarkar/grpc-test/issues/8).

The test class in [client](client/src/test) project uses the `GrpcCleanupExtension` from Java code.
