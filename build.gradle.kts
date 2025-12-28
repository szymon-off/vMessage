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
    id("com.gradleup.shadow") version "8.3.8"
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
    maven("https://mvn-repo.arim.space/lesser-gpl3/")
    maven("https://mvn-repo.arim.space/gpl3/")
    maven("https://mvn-repo.arim.space/affero-gpl3/")
    maven("https://repo.szymonoff.me/repository/fishy-dependencies/")
}

dependencies {
    /* Configurate */
    /*
    - The 4.2.0+YAML_COMMENTS version is a custom build that includes YAML comments support.
    - It is hosted in my personal Maven repository at https://repo.szymonoff.me/repository/fishy-dependencies/
    - It is based on this PR: https://github.com/SpongePowered/Configurate/pull/410 by @Tim203
     */
    implementation("org.spongepowered:configurate-core:4.2.0+YAML_COMMENTS")
    implementation("org.spongepowered:configurate-yaml:4.2.0+YAML_COMMENTS") {
        exclude(group = "org.spongepowered", module = "configurate-core")
    }
    implementation("org.spongepowered:configurate-extra-kotlin:4.2.0+YAML_COMMENTS") {
        exclude(group = "org.spongepowered", module = "configurate-core")
    }

    /* Velocity API */
    compileOnly("com.velocitypowered:velocity-api:3.4.0-SNAPSHOT") {
        exclude(group = "org.spongepowered", module = "configurate-core")
        exclude(group = "org.spongepowered", module = "configurate-yaml")
    }

    /* Plugin Compatibility APIs */
    compileOnly("net.luckperms:api:5.4")
    compileOnly("space.arim.libertybans:bans-api:1.1.0")
    compileOnly("com.gitlab.ruany:LiteBansAPI:0.6.1")

    /* Usage Statistics */
    implementation("org.bstats:bstats-velocity:3.1.0")
}

/* Generate Version.java */

val generateVersion by tasks.registering {
    val outputDir = layout.buildDirectory.dir("generated/source/version/java")

    outputs.dir(outputDir)

    doLast {
        val versionFile = outputDir.get()
            .file("off/szymon/vmessage/generated/Version.java")
            .asFile

        versionFile.parentFile.mkdirs()
        versionFile.writeText(
            """
            package off.szymon.vmessage.generated;

            public final class Version {
                public static final String VERSION = "$version";
            }
            """.trimIndent()
        )
    }
}


sourceSets {
    named("main") {
        java.srcDir(generateVersion.map { it.outputs.files.singleFile })
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
}

tasks.build {
    dependsOn("shadowJar")
}

/* Java Sources */
sourceSets {
    main {
        java.srcDirs("src/main/java")
        resources.srcDirs("src/main/resources")
    }
}