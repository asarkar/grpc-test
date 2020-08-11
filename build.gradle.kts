plugins {
    kotlin("jvm")
    `maven-publish`
    id("org.jetbrains.dokka")
    id("org.jlleitschuh.gradle.ktlint")
    id("com.jfrog.bintray")
}

val projectGroup: String by project
val projectVersion: String by project
val projectDescription: String by project
group = projectGroup
version = projectVersion
description = projectDescription

repositories {
    jcenter()
}

val jUnitVersion: String by project
val gRPCVersion: String by project
val mockitoVersion: String by project
val jUnitTestkitVersion: String by project

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(platform("io.grpc:grpc-bom:$gRPCVersion"))
    implementation(platform("org.junit:junit-bom:$jUnitVersion"))
    implementation("org.junit.jupiter:junit-jupiter-api")
    implementation("io.grpc:grpc-api")
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("io.grpc:grpc-core")
    testImplementation("org.mockito:mockito-core:$mockitoVersion")
    testImplementation("org.mockito:mockito-junit-jupiter:$mockitoVersion")
    testImplementation("org.junit.platform:junit-platform-testkit:$jUnitTestkitVersion")
}

plugins.withType<JavaPlugin> {
    extensions.configure<JavaPluginExtension> {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "1.8"
    }
}

tasks.withType<org.jetbrains.dokka.gradle.DokkaTask> {
    outputFormat = "html"
    outputDirectory = "$buildDir/javadoc"
}

val sourcesJar by tasks.creating(Jar::class) {
    group = JavaBasePlugin.DOCUMENTATION_GROUP
    description = "Creates a sources JAR"
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allSource)
}

val kdocJar by tasks.creating(Jar::class) {
    group = JavaBasePlugin.DOCUMENTATION_GROUP
    description = "Creates KDoc"
    archiveClassifier.set("javadoc")
    from(tasks.dokka)
}

tasks.jar.configure {
    finalizedBy(sourcesJar, kdocJar)
}

val licenseName: String by project
val licenseUrl: String by project
val developerName: String by project
val developerEmail: String by project
val gitHubUsername: String by project

val gitHubUrl: String by lazy { "github.com/$gitHubUsername/${project.name}" }

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            artifact(kdocJar)
            artifact(sourcesJar)
            pom {
                name.set("${project.group}:${project.name}")
                description.set(project.description)
                url.set("https://$gitHubUrl")
                licenses {
                    license {
                        name.set(licenseName)
                        url.set(licenseUrl)
                    }
                }
                developers {
                    developer {
                        name.set(developerName)
                        email.set(developerEmail)
                    }
                }
                scm {
                    connection.set("scm:git:git://$gitHubUrl.git")
                    developerConnection.set("scm:git:ssh://github.com:$gitHubUsername/${project.name}.git")
                    url.set("https://$gitHubUrl")
                }
            }
        }
    }
}

val bintrayRepo: String by project
bintray {
    user = (findProperty("bintrayUser") ?: System.getenv("BINTRAY_USER"))?.toString()
    key = (findProperty("bintrayKey") ?: System.getenv("BINTRAY_KEY"))?.toString()
    setPublications(*publishing.publications.names.toTypedArray())
    with(pkg) {
        repo = bintrayRepo
        name = "${project.group}:${project.name}"
        desc = project.description
        websiteUrl = "https://$gitHubUrl"
        vcsUrl = "https://$gitHubUrl.git"
        setLabels("grpc", "protobuf", "test", "junit5", "junit")
        setLicenses(licenseName)
        with(version) {
            name = project.version.toString()
            with(gpg) {
                sign = true
            }
            with(mavenCentralSync) {
                sync = true
                user = (findProperty("sonatypeUser") ?: System.getenv("SONATYPE_USER"))?.toString()
                password = (findProperty("sonatypePwd") ?: System.getenv("SONATYPE_PWD"))?.toString()
            }
        }
    }
    publish = true
    override = false
    dryRun = false
}
