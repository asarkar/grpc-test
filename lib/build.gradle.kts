import com.vanniktech.maven.publish.KotlinJvm
import com.vanniktech.maven.publish.SonatypeHost
import com.vanniktech.maven.publish.tasks.JavadocJar
import org.jlleitschuh.gradle.ktlint.reporter.ReporterType
import org.jlleitschuh.gradle.ktlint.tasks.KtLintCheckTask
import org.jlleitschuh.gradle.ktlint.tasks.KtLintFormatTask
import java.net.URI
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
    providers
        .provider { rootDir.resolve(".java-version").readText(Charsets.UTF_8).trim() }
        .map(JavaLanguageVersion::of)

kotlin {
    jvmToolchain {
        languageVersion = javaVersion
    }
    compilerOptions {
        freeCompilerArgs = listOf("-Werror")
    }
}

val ci = providers.environmentVariable("CI")

ktlint {
    outputToConsole = ci.isPresent
    reporters {
        reporter(ReporterType.HTML)
    }
    additionalEditorconfig = mapOf("max_line_length" to "100")
}

val gitHubUsername = providers.environmentVariable("GITHUB_REPOSITORY_OWNER")
val gitHubRepo = providers.environmentVariable("GITHUB_REPOSITORY")
val gitHubUrl = providers.environmentVariable("GITHUB_SERVER_URL")
val gitHubDomain = gitHubUrl.map { URI(it).host }
val gitHubRepoUrl = gitHubUrl.zip(gitHubRepo, { x, y -> "$x/$y" })

dokka {
    moduleName = rootProject.name
    dokkaPublications.html {
        failOnWarning = true
    }
    dokkaSourceSets.main {
        jdkVersion = javaVersion.map { it.asInt() }
    }
}

val projectGroup: String by project
val projectVersion: String by project
val projectDescription: String by project
val licenseName: String by project
val licenseUrl: String by project

version = projectVersion

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
        url = gitHubRepoUrl
        licenses {
            license {
                name = licenseName
                url = licenseUrl
            }
        }
        developers {
            developer {
                id = gitHubUsername
                url = gitHubUrl.zip(gitHubUsername, { x, y -> "$x/$y" })
            }
        }
        scm {
            connection = gitHubDomain.zip(gitHubRepo, { x, y -> "scm:git:git://$x/$y.git" })
            developerConnection =
                gitHubDomain.zip(
                    gitHubRepo,
                    { x, y -> "scm:git:ssh://$x:$y.git" },
                )
            url = gitHubRepoUrl
        }
    }
}

/*
DO NOT use existing(Task::class) for configuring tasks.
existing() if not referenced from another task is dropped on the floor!
Use withTask() instead.
*/
tasks {
    withType<Jar> {
        manifest {
            attributes("Automatic-Module-Name" to "com.asarkar.grpc.test")
        }
        archiveBaseName = rootProject.name
    }

    test {
        useJUnitPlatform()
        testLogging {
            showStandardStreams = true
        }
        exclude("**/ignore/**")
        // Suppress warning: Sharing is only supported for boot loader classes...
        // https://stackoverflow.com/q/54205486/839733
        jvmArgs("-Xshare:off")
    }

    // https://github.com/vanniktech/gradle-maven-publish-plugin/issues/966
    withType<JavadocJar> {
        archiveFileName = "${rootProject.name}-javadoc.jar"
    }

    withType<KtLintFormatTask> {
        enabled = !ci.isPresent
    }

    // https://github.com/JLLeitschuh/ktlint-gradle/issues/886
    withType<KtLintCheckTask> {
        val fmtTaskName = name.replace("Check", "Format")
        val fmtTask by named(fmtTaskName)
        dependsOn(fmtTask)
    }
}
