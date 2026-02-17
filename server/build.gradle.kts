import io.ktor.plugin.OpenApiPreview
import org.jetbrains.dokka.gradle.engine.parameters.VisibilityModifier

plugins {
    application
    alias(libs.plugins.kotlin)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.atomicfu)
    alias(libs.plugins.ktor)
    alias(libs.plugins.graalvm.native)
    alias(libs.plugins.kotlinter)
    alias(libs.plugins.dokka)
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll(
            "-opt-in=kotlin.uuid.ExperimentalUuidApi,kotlin.time.ExperimentalTime",
            "-Xcontext-parameters",
        )
        allWarningsAsErrors = true
    }
}

application {
    mainClass.set("gg.kuken.LauncherKt")
}

dependencies {
    annotationProcessor(libs.validator.processor)
    implementation(libs.validator)
    implementation(libs.ktx.coroutines.core)
    implementation(libs.ktx.coroutines.reactor)
    implementation(libs.ktx.atomicfu)
    implementation(libs.ktx.serialization.hocon)
    implementation(libs.ktx.serialization.json)
    implementation(libs.dockerKotlin)
    implementation(libs.bcprov)
    implementation(libs.log4j.core)
    implementation(libs.log4j.slf4j2)
    implementation(libs.hocon)
    implementation(libs.bundles.ktor)
    implementation(libs.koin.core)
    implementation(libs.koin.ktor)
    implementation(libs.bundles.exposed)
    implementation(libs.postgresql)
    implementation(libs.h2)
    implementation(libs.lettuce)
    implementation(libs.pkl)
    implementation(libs.tika)
    testImplementation(libs.ktx.coroutines.test)
    testImplementation(kotlin("test"))
    testImplementation(libs.ktor.client.mock)
}

tasks {
    jar {
        manifest {
            attributes["Main-Class"] = application.mainClass.get()
            attributes["Implementation-Version"] = project.version
        }
    }

    check {
        dependsOn("installKotlinterPrePushHook")
    }
}

ktor {
    @OptIn(OpenApiPreview::class)
    openApi {
        title = "Kuken"
        contact = "support@kuken.io"
    }

    development = providers.environmentVariable("PRODUCTION").isPresent
}

graalvmNative {
    agent {
        defaultMode.set("standard")
    }

    binaries {
        named("main") {
            fallback.set(false)
            verbose.set(true)

            buildArgs.addAll(
                "--install-exit-handlers",
                "--add-modules=jdk.unsupported",
                "--initialize-at-build-time=kotlin",
                "--initialize-at-build-time=org.pkl.core.runtime.VmLanguageProvider",
                "--initialize-at-build-time=kotlinx.serialization",
                "--initialize-at-build-time=org.hibernate.validator.internal.engine.ConfigurationImpl",
                "--initialize-at-run-time=kotlin.uuid.SecureRandomHolder",
                "--initialize-at-run-time=org.bouncycastle",
                "--initialize-at-run-time=io.netty.channel.epoll.Epoll",
                "--initialize-at-run-time=io.netty.channel.epoll.Native",
                "--initialize-at-run-time=io.netty.channel.epoll.EpollEventLoop",
                "--initialize-at-run-time=io.netty.channel.unix.Errors",
                "--initialize-at-run-time=io.netty.channel.unix.IovArray",
                "--initialize-at-run-time=io.netty.channel.unix.Limits",
                "--initialize-at-run-time=kotlin.uuid.SecureRandomHolder",
                "--initialize-at-run-time=org.bouncycastle",
                "--initialize-at-run-time=org.hibernate.validator",
                "--initialize-at-run-time=org.hibernate.validator.internal",
                "--initialize-at-build-time=org.hibernate.validator.internal.util.logging.Log_\$logger",
                "--initialize-at-build-time=org.jboss.logging",
                "--initialize-at-build-time=org.apache.logging.log4j",
                "-J--add-exports=java.base/jdk.internal.misc=ALL-UNNAMED",
                "-H:+ReportUnsupportedElementsAtRuntime",
                "-H:+ReportExceptionStackTraces",
                "-H:+UnlockExperimentalVMOptions",
                "-H:+AddAllCharsets",
            )

            imageName.set("kuken-server")
            mainClass.set(application.mainClass)
        }
    }
}

dokka {
    dokkaPublications.html {
        moduleName.set("Küken")
        moduleVersion.set(project.version.toString())
    }

    dokkaSourceSets.configureEach {
        documentedVisibilities.set(setOf(VisibilityModifier.Public))
        displayName.set("API")

        sourceLink {
            localDirectory.set(file("src/main/kotlin"))
            remoteUrl("https://github.com/kuken-project/kuken/blob/main/server/src/main/kotlin")
            remoteLineSuffix.set("#L")
        }
    }
}
