/*
 * vMessage
 * Copyright (c) 2025.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * See the LICENSE file in the project root for details.
 */

plugins {
    kotlin("jvm") version "2.2.21"
    id("com.gradleup.shadow") version "9.3.0"
}

group = "off.szymon"
// Replaced if built with GitHub Actions or Gradle CLI -PpluginVersion=...
val pluginVersion = project.findProperty("pluginVersion") as String? ?: "0.0.0-UNKNOWN"
version = pluginVersion

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://jitpack.io/")
    maven("https://repo.william278.net/releases/")
    maven("https://repo.szymonoff.me/repository/maven-public/")
}

dependencies {
    /* Fishy API */
    implementation("off.szymon:fishy-api:0.9.5")

    /* Velocity API */
    compileOnly("com.velocitypowered:velocity-api:3.5.1") {
        exclude(group = "org.spongepowered", module = "configurate-core")
        exclude(group = "org.spongepowered", module = "configurate-yaml")
    }
    annotationProcessor("com.velocitypowered:velocity-api:3.5.1") {
        exclude(group = "org.spongepowered", module = "configurate-core")
        exclude(group = "org.spongepowered", module = "configurate-yaml")
    }

    /* Plugin Integration APIs */
    // Placeholder Plugins
    compileOnly("net.luckperms:api:5.5")
    compileOnly("net.william278:papiproxybridge:1.8")

    /* Usage Statistics */
    implementation("org.bstats:bstats-velocity:3.1.0")
}

/* Generate Version.kt: build/generated/source/version/kotlin/off/szymon/vmessage/generated/Version.kt -> Treated as a source */
val generateVersion by tasks.registering {
    description = "Generates a Version.kt file containing the plugin version."
    val outputDir = layout.buildDirectory.dir("generated/source/version/kotlin")

    outputs.dir(outputDir)

    doLast {
        val versionFile = outputDir.get()
            .file("off/szymon/vmessage/generated/Version.kt")
            .asFile

        versionFile.parentFile.mkdirs()
        versionFile.writeText(
            """
            package off.szymon.vmessage.generated

            object Version {
                const val VERSION: String = "$version"
            }
            """.trimIndent()
        )
    }
}


tasks.compileJava {
    dependsOn(generateVersion)
}


/* ShadowJAR */
tasks.shadowJar {
    archiveClassifier.set("")
    relocate("kotlin", "off.szymon.vmessage.libs.kotlin")
    relocate("org.spongepowered.configurate", "off.szymon.vmessage.libs.configurate")
    relocate("org.bstats", "off.szymon.vmessage.libs.bstats")
    relocate("off.szymon.fishy.api", "off.szymon.vmessage.libs.fishyapi")
}

tasks.build {
    dependsOn("shadowJar")
}

/* Sources */
sourceSets {
    main {
        java.setSrcDirs(emptyList<String>()) // Disables src/main/java
        kotlin.srcDir("src/main/kotlin")
        kotlin.srcDir(generateVersion.map { it.outputs.files.singleFile })
        resources.srcDir("src/main/resources")
    }
}