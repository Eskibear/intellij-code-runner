import org.jetbrains.intellij.platform.gradle.TestFrameworkType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  java
  idea
  kotlin("jvm") version "2.1.10"
  id("org.jetbrains.intellij.platform") version "2.1.0"
}

group = "io.github.eskibear"
version = "1.0.0-SNAPSHOT.1"

val javaVersion = "17"

repositories {
  maven("https://packages.jetbrains.team/maven/p/ij/intellij-dependencies")
  mavenLocal()
  mavenCentral()
  intellijPlatform {
    defaultRepositories()
    marketplace()
  }
}

dependencies {
  intellijPlatform {
    create("IC", "2023.2.8")
    bundledPlugins(
        "org.jetbrains.plugins.terminal"
    )
    instrumentationTools()
    testFramework(TestFrameworkType.Platform)
    pluginVerifier()
  }
}

tasks {

  compileJava {
    sourceCompatibility = javaVersion
    targetCompatibility = javaVersion
  }

  compileKotlin {
    compilerOptions {
      jvmTarget.set(JvmTarget.fromTarget(javaVersion))
    }
  }

  patchPluginXml {
    sinceBuild.set("232")
    untilBuild.set("251.*")
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
