# grpc-test

Includes a JUnit 5 [Extension](https://junit.org/junit5/docs/current/api/org.junit.jupiter.api/org/junit/jupiter/api/extension/Extension.html) 
that can automatically release gRPC resources at the end of the test. Like [GrpcCleanupRule](https://grpc.github.io/grpc-java/javadoc/io/grpc/testing/GrpcCleanupRule.html), 
but built for JUnit 5 and actively maintained.

Requires Java 8 or later. Verified working with the latest JUnit 5 version, which you can find in the [gradle.properties](gradle.properties).

[![](https://github.com/asarkar/grpc-test/workflows/CI%20Pipeline/badge.svg)](https://github.com/asarkar/grpc-test/actions?query=workflow%3A%22CI+Pipeline%22)

## Installation

You can find the latest version on Bintray. [ ![Download](https://api.bintray.com/packages/asarkar/mvn/com.asarkar.grpc%3Agrpc-test/images/download.svg) ](https://bintray.com/asarkar/mvn/com.asarkar.grpc%3Agrpc-test/_latestVersion)

It is also on Maven Central and jcenter.

## Usage

Declare a `Resources` in one of the three following ways, and register `Server` and/or `ManagedChannel` instances with it.

Get a `Resources` from:
1. A test method parameter injection, or
2. An instance field, or
3. A static field.

The difference is in the lifecycle of the `Resources` object. For `#1`, a new instance is created for every test method. 
`#2` is the same as `#1` unless the test class declares `@TestInstance(TestInstance.Lifecycle.PER_CLASS)`, in which case 
one instance is shared among all the tests. `#3` is obviously one instance shared among all the tests.

Note that for `#2` and `#3`, if the variable is already been assigned a value by the user, the extension will not 
reinitialize it.

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

## Contribute

This project is a volunteer effort. You are welcome to send pull requests, ask questions, or create issues.
If you like it, you can help by spreading the word!

## License

Copyright 2020 Abhijit Sarkar - Released under [Apache License v2.0](LICENSE).
