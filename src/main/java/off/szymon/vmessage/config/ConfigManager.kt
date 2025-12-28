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

package off.szymon.vmessage.config

import com.google.common.io.Files
import off.szymon.vmessage.VMessagePlugin
import off.szymon.vmessage.config.tree.MainConfig
import org.spongepowered.configurate.CommentedConfigurationNode
import org.spongepowered.configurate.loader.HeaderMode
import org.spongepowered.configurate.yaml.NodeStyle
import org.spongepowered.configurate.yaml.YamlConfigurationLoader
import java.io.File
import java.nio.file.Path

/**
 * ConfigManager
 *
 * This file is written in Kotlin for ease of use with the Configurate library.
 * I find Kotlin's data classes and concise syntax make it easier to define configuration schemas
 * and manage configuration files compared to Java.
 */
class ConfigManager {
    companion object {
        @JvmStatic
        private lateinit var instance: ConfigManager

        @JvmStatic
        fun get(): ConfigManager = instance
    }

    private val fileName = "config.yml"
    private val path: Path = VMessagePlugin.get().dataFolder.toPath().resolve(fileName)
    private val file: File = path.toFile()

    private val loader: YamlConfigurationLoader = YamlConfigurationLoader.builder()
        .path(path)
        .defaultOptions { opts ->
            opts.shouldCopyDefaults(true)
                .header(
                    """
                    vMessage Configuration File
                    Thanks for downloading my plugin! I hope you like it!
                    MiniMessage is supported for formatting in all messages.
                    Placeholders are parsed before MiniMessage so you can use them in your format.
                    For in-depth explanation of the configuration options, visit: https://github.com/szymon-off/vMessage/wiki/Configuration-(config.yml)
                    
                    ⚠️ If you have used vMessage before v1.8.0, the contents of this file may be malformed ⚠️
                    """.trimIndent()
                )
                .implicitInitialization(true)
        }
        .indent(2)
        .nodeStyle(NodeStyle.BLOCK)
        .headerMode(HeaderMode.PRESERVE)
        .build()

    lateinit var config: MainConfig
        private set

    private lateinit var root: CommentedConfigurationNode

    init {
        instance = this
        load()
    }

    fun load() {
        var fileCreated = false
        if (!file.exists()) {
            fileCreated = true
            file.parentFile.mkdirs()
            file.createNewFile()
        }
        root = loader.load()
        config = root.get(MainConfig::class.java) ?: MainConfig()
        if (!fileCreated && config.backupConfig) {
            @Suppress("UnstableApiUsage")
            Files.copy(file, File(file.parentFile, "$fileName.bak"))
        }
        config.backupConfig = false
        save()
    }

    fun save() {
        root.set(MainConfig::class.java, config)
        loader.save(root)
    }

    fun getNode(path: String): CommentedConfigurationNode =
        root.node(*path.split('.').toTypedArray())
}