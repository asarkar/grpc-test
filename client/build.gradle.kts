import com.github.spotbugs.snom.SpotBugsTask

plugins {
    `java-library`
    alias(libs.plugins.spotbugs)
    alias(libs.plugins.spotless)
    alias(libs.plugins.errorprone)

}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform(libs.grpc.bom))
    testImplementation(platform(libs.junit.bom))
    testImplementation(platform(libs.mockito.bom))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("io.grpc:grpc-api")
    testImplementation("org.mockito:mockito-core")
    testImplementation(project(":lib"))
    testRuntimeOnly("io.grpc:grpc-core")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    errorprone(libs.errorprone)
}

val javaVersion =
    providers
        .provider { rootDir.resolve(".java-version").readText(Charsets.UTF_8).trim() }
        .map(JavaLanguageVersion::of)

java {
    toolchain {
        languageVersion = javaVersion
    }
}

spotbugs {
    toolVersion = libs.versions.spotbugs.get()
}

spotless {
    java {
        palantirJavaFormat(libs.versions.palantirJavaFmt.get()).style("GOOGLE")
        toggleOffOn()
    }
}

val ci: Boolean by lazy { System.getenv("CI") != null }

/*
DO NOT use existing(Task::class) for configuring tasks.
existing() if not referenced from another task is dropped on the floor!
Use withTask() instead.
*/
tasks {
    test {
        useJUnitPlatform()
        testLogging {
            showStandardStreams = true
        }
        // Suppress warning: Sharing is only supported for boot loader classes...
        // https://stackoverflow.com/q/54205486/839733
        jvmArgs("-Xshare:off")
    }

    withType<SpotBugsTask> {
        reports.create("html") {
            required = !ci
        }
    }

    val spotlessApply by existing {
        enabled = !ci
    }

    named("spotlessCheck") {
        dependsOn(spotlessApply)
    }
}
