plugins {
  id("java")
  kotlin("jvm") version "2.1.20"
  kotlin("plugin.serialization") version "1.9.23"
  id("org.jetbrains.intellij.platform") version "2.5.0"
}

group = "com.totrit"
version = "0.6.3"

repositories {
  mavenCentral()

  intellijPlatform {
    defaultRepositories()
  }
}

tasks {
  // Set the JVM compatibility versions
  withType<JavaCompile> {
    sourceCompatibility = "17"
    targetCompatibility = "17"
  }
  withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    compilerOptions {
      jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
  }

  patchPluginXml {
    sinceBuild.set("232")
    untilBuild.set("252.*")
  }

  signPlugin {
    certificateChainFile.set(file("~/.jetbrains/chain.crt"))
    privateKeyFile.set(file("~/.jetbrains/private.pem"))
    password.set(System.getenv("JETBRAINS_PRIVATE_KEY_PASSWORD"))
  }

  publishPlugin {
    token.set(System.getenv("JETBRAINS_PUBLISH_TOKEN"))
  }
}

val jacksonVersion = "2.17.0"

dependencies {
  intellijPlatform {
    intellijIdeaCommunity("2025.1")
  }
  implementation("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")
  implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")
  implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:$jacksonVersion")
}
