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

val javaVersion = JavaLanguageVersion.of(rootDir.resolve(".java-version").readText(Charsets.UTF_8).trim())

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

val ci: Boolean by lazy { listOf("CI").any { System.getenv(it) != null } }
val spotlessTasks = arrayOf("spotlessApply", "spotlessCheck")
val spotlessTask = spotlessTasks[true.compareTo(ci)]  // true > false

tasks.named(spotlessTask) {
    enabled = false
}

tasks.named("check") {
    dependsOn(spotlessTasks[0])
}

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        showStandardStreams = true
    }
    // Suppress warning: Sharing is only supported for boot loader classes...
    // https://stackoverflow.com/q/54205486/839733
    jvmArgs("-Xshare:off")
}

tasks.withType<SpotBugsTask> {
    reports.create("html") {
        required = !ci
    }
}
