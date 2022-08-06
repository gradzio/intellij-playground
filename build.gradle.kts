plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.6.20"
    id("org.jetbrains.intellij") version "1.5.2"
}

group = "com.lowgular.intellij.plugin"
version = "0.3.0-SNAPSHOT"
val ktorVersion = "2.0.3"

dependencies {
  implementation("io.ktor:ktor-client-core:$ktorVersion")
  implementation("io.ktor:ktor-client-cio:$ktorVersion")
}

repositories {
    mavenCentral()
}

sourceSets {
    create("unpacked") {
        resources {
            setSrcDirs(listOf("gen/main/unpacked"))
        }
    }
}

// Configure Gradle IntelliJ Plugin - read more: https://github.com/JetBrains/gradle-intellij-plugin
intellij {
    version.set("2022.1")
    type.set("IU") // Target IDE Platform

    plugins.set(listOf("JavaScriptLanguage"))
}

tasks {
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "11"
        targetCompatibility = "11"
    }
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "11"
    }

    buildPlugin {
        from(sourceSets.named("unpacked").get().allSource) {
            into("")
        }
      duplicatesStrategy = DuplicatesStrategy.INCLUDE
    }

    prepareSandbox {
        from(sourceSets.named("unpacked").get().allSource) {
            into(pluginName.get())
        }
    }

    patchPluginXml {
        sinceBuild.set("212")
        untilBuild.set("222.*")
    }

    signPlugin {
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
        privateKey.set(System.getenv("PRIVATE_KEY"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
        token.set(System.getenv("PUBLISH_TOKEN"))
    }
}
