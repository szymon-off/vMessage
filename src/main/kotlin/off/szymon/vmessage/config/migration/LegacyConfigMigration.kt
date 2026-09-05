/*
 * vMessage
 * Copyright (c) 2026.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * See the LICENSE file in the project root for details.
 */

package off.szymon.vmessage.config.migration

import off.szymon.vmessage.VMessage
import off.szymon.vmessage.config.Config
import org.spongepowered.configurate.CommentedConfigurationNode
import org.spongepowered.configurate.loader.HeaderMode
import org.spongepowered.configurate.serialize.SerializationException
import org.spongepowered.configurate.yaml.NodeStyle
import org.spongepowered.configurate.yaml.YamlConfigurationLoader

class LegacyConfigMigration {

    var legacyConfigRoot: CommentedConfigurationNode? = getLegacyConfigRoot()
    var newConfigRoot: CommentedConfigurationNode = Config.get().root

    fun runMigrationIfNeeded() {
        if (needsMigration()) {
            // TODO: backup
            // TODO: readme: config moved

            try {
                migrateLegacyConfig()
            } catch (e: java.lang.UnsupportedOperationException) {
                VMessage.get().logger.warn("Legacy config migration failed: ${e.message}")
            }
        }
    }

    fun getLegacyConfigRoot(): CommentedConfigurationNode? {
        val path = VMessage.get().dataDir.parent.resolve("vMessage").resolve("config.yml")
        if (!path.toFile().exists()) {
            return null
        }
        val loader: YamlConfigurationLoader = YamlConfigurationLoader.builder()
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
        return loader.load()
    }

    fun needsMigration(): Boolean {
        return legacyConfigRoot != null && newConfigRoot.node("config-version").isNull // TODO check if migration has already been done before (does this work?)
    }

    @Throws(UnsupportedOperationException::class)
    private fun migrateLegacyConfig() {
        val legacyConfig: LegacyMainConfig? = null
        try {
            val legacyConfig = legacyConfigRoot?.get(LegacyMainConfig::class.java) ?: throw UnsupportedOperationException("Legacy config is invalid, cannot migrate")
        } catch (e: SerializationException) {
            throw UnsupportedOperationException("Legacy config is invalid, cannot migrate")
        }
        // TODO: implement migration logic
    }

}