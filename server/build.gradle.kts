import io.ktor.plugin.*
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
            "-Xcontext-parameters"
        )
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
    implementation("org.pkl-lang:pkl-codegen-kotlin:0.30.2")
    implementation("org.pkl-lang:pkl-config-kotlin:0.30.2") {
        exclude(group = "org.pkl-lang", module = "pkl-config-java-all")
    }
    implementation("org.pkl-lang:pkl-config-java:0.30.2") {
        exclude(group = "org.pkl-lang", module = "pkl-config-java-all")
    }
    testImplementation(libs.ktx.coroutines.test)
    testImplementation(kotlin("test"))
}

tasks {
    jar {
        manifest {
            attributes["Main-Class"] = application.mainClass.get()
            attributes["Implementation-Version"] = project.version
        }
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
    binaries {
        named("main") {
            fallback.set(false)
            verbose.set(true)

            buildArgs.add("--initialize-at-build-time=kotlin")
            buildArgs.add("--initialize-at-build-time=org.pkl.core")
            buildArgs.add("--initialize-at-build-time=com.oracle.truffle")
            buildArgs.add("--initialize-at-build-time=org.graalvm.polyglot")

            buildArgs.add("--initialize-at-run-time=kotlin.uuid.SecureRandomHolder")
            buildArgs.add("--initialize-at-run-time=org.bouncycastle")

            buildArgs.add("--initialize-at-run-time=io.netty.channel.epoll.Epoll")
            buildArgs.add("--initialize-at-run-time=io.netty.channel.epoll.Native")
            buildArgs.add("--initialize-at-run-time=io.netty.channel.epoll.EpollEventLoop")
            buildArgs.add("--initialize-at-run-time=io.netty.channel.unix.Errors")
            buildArgs.add("--initialize-at-run-time=io.netty.channel.unix.IovArray")
            buildArgs.add("--initialize-at-run-time=io.netty.channel.unix.Limits")

            buildArgs.add("--add-modules=jdk.unsupported")
            buildArgs.add("-J--add-exports=java.base/jdk.internal.misc=ALL-UNNAMED")
            buildArgs.add("-H:+AllowIncompleteClasspath")

            buildArgs.add("--install-exit-handlers")
            buildArgs.add("-H:+ReportUnsupportedElementsAtRuntime")
            buildArgs.add("-H:+ReportExceptionStackTraces")

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
            remoteUrl("https://github.com/devnatan/kuken/blob/main/server/src/main/kotlin")
            remoteLineSuffix.set("#L")
        }
    }
}
