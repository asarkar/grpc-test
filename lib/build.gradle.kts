import com.vanniktech.maven.publish.KotlinJvm
import com.vanniktech.maven.publish.SonatypeHost
import com.vanniktech.maven.publish.tasks.JavadocJar
import org.jlleitschuh.gradle.ktlint.reporter.ReporterType
import org.jlleitschuh.gradle.ktlint.tasks.KtLintCheckTask
import org.jlleitschuh.gradle.ktlint.tasks.KtLintFormatTask
import com.vanniktech.maven.publish.JavadocJar as PublishJavadocJar

plugins {
    alias(libs.plugins.kotlin)
    alias(libs.plugins.dokka)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.mavenPublish)
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation(platform(libs.grpc.bom))
    implementation(platform(libs.junit.bom))
    implementation("org.junit.jupiter:junit-jupiter-api")
    implementation("io.grpc:grpc-api")
    testImplementation("io.grpc:grpc-inprocess")
    testImplementation("org.junit.platform:junit-platform-testkit")
    testImplementation(platform(libs.mockito.bom))
    testImplementation("org.mockito:mockito-core")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("io.grpc:grpc-core")
}

val javaVersion =
    JavaLanguageVersion.of(
        rootDir.resolve(".java-version").readText(Charsets.UTF_8).trim(),
    )

kotlin {
    jvmToolchain {
        languageVersion = javaVersion
    }
    compilerOptions {
        freeCompilerArgs = listOf("-Werror")
    }
}

tasks.withType<Jar> {
    manifest {
        attributes("Automatic-Module-Name" to "com.asarkar.grpc.test")
    }
    archiveBaseName = rootProject.name
}

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        showStandardStreams = true
    }
    exclude("**/ignore/**")
    // Suppress warning: Sharing is only supported for boot loader classes...
    // https://stackoverflow.com/q/54205486/839733
    jvmArgs("-Xshare:off")
}

val ci: Boolean by lazy { System.getenv("CI") != null }

ktlint {
    outputToConsole = ci
    reporters {
        reporter(ReporterType.HTML)
    }
    additionalEditorconfig = mapOf("max_line_length" to "100")
}

tasks.withType<KtLintFormatTask> {
    enabled = !ci
}

tasks.withType<KtLintCheckTask> {
    val fmtTaskName = name.replace("Check", "Format")
    dependsOn(tasks.named(fmtTaskName))
}

val gitHubUsername: String by project
val gitHubRepo: String by lazy {
    System.getenv("GITHUB_REPOSITORY") ?: "$gitHubUsername/${rootProject.name}"
}
val gitHubRepoUrl: String by lazy { "github.com/$gitHubRepo" }

dokka {
    moduleName = rootProject.name
    dokkaPublications.html {
        failOnWarning = true
    }
    dokkaSourceSets.main {
        jdkVersion = javaVersion.asInt()
        sourceLink {
            localDirectory = file(sourceRoots.asPath)
            remoteUrl("https://$gitHubRepoUrl")
        }
    }
}

val projectGroup: String by project
val projectVersion: String by project
val projectDescription: String by project
val licenseName: String by project
val licenseUrl: String by project

version = projectVersion

// https://github.com/vanniktech/gradle-maven-publish-plugin/issues/966
tasks.withType<JavadocJar> {
    archiveFileName = "${rootProject.name}-javadoc.jar"
}

mavenPublishing {
    configure(
        KotlinJvm(
            javadocJar = PublishJavadocJar.Dokka(tasks.dokkaGeneratePublicationHtml.name),
            sourcesJar = true,
        ),
    )

    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL, automaticRelease = false)
    signAllPublications()
    coordinates(projectGroup, rootProject.name, projectVersion)

    pom {
        name = "$projectGroup:${rootProject.name}"
        description = projectDescription
        url = "https://$gitHubRepoUrl"
        licenses {
            license {
                name = licenseName
                url = licenseUrl
            }
        }
        developers {
            developer {
                id = gitHubUsername
                url = "https://github.com/$gitHubUsername"
            }
        }
        scm {
            connection = "scm:git:git://$gitHubRepoUrl.git"
            developerConnection = "scm:git:ssh://github.com:$gitHubRepo.git"
            url = "https://$gitHubRepoUrl"
        }
    }
}
