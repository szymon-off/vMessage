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

    val legacyConfigRoot: CommentedConfigurationNode? = buildLegacyConfigRoot()
    val newConfigRoot: CommentedConfigurationNode = Config.get().root

    fun runMigrationIfNeeded() {
        if (needsMigration()) {
            // TODO: backup
            // TODO: readme: config moved

            try {
                migrateLegacyConfig()
            } catch (e: UnsupportedOperationException) {
                VMessage.get().logger.error("Legacy config migration failed: ${e.message}", e)
            }
        }
    }

    fun buildLegacyConfigRoot(): CommentedConfigurationNode? {
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
                    vMessage LEGACY Configuration File
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
        return legacyConfigRoot != null && newConfigRoot.node("config-version").isNull // TODO check if migration has already been done before (does this work? NOOOO)
    }

    @Suppress("DuplicatedCode")
    @Throws(UnsupportedOperationException::class)
    private fun migrateLegacyConfig() {
        var legacyConfig: LegacyMainConfig?
        try {
            legacyConfig = legacyConfigRoot?.get(LegacyMainConfig::class.java) ?: throw UnsupportedOperationException("Legacy config is invalid, cannot migrate")
        } catch (e: SerializationException) {
            throw UnsupportedOperationException("Legacy config is invalid, cannot migrate")
        }
        val newConfig = Config.get().tree

        newConfig.commands.message.enabled = legacyConfig.commands.message.enabled
        newConfig.commands.message.allowMiniMessage = legacyConfig.commands.message.allowMiniMessage
        newConfig.commands.message.allowByDefault = legacyConfig.commands.message.allowByDefault
        newConfig.commands.message.format.sender = legacyConfig.commands.message.format.sender
            .replace("%sender%", $$"$sender$")
            .replace("%receiver%", $$"$receiver$")
            .replace("%message%", $$"$message$")
            .replace("%sender-server%", "")
            .replace("%receiver-server%", "")
            .replace("%sender-prefix%", "")
            .replace("%receiver-prefix%", $$"$prefix$")
            .replace("%sender-suffix%", "")
            .replace("%receiver-suffix%", $$"$suffix$")
        newConfig.commands.message.format.receiver = legacyConfig.commands.message.format.receiver
            .replace("%sender%", $$"$sender$")
            .replace("%receiver%", $$"$receiver$")
            .replace("%message%", $$"$message$")
            .replace("%sender-server%", "")
            .replace("%receiver-server%", "")
            .replace("%sender-prefix%", $$"$prefix$")
            .replace("%receiver-prefix%", "")
            .replace("%sender-suffix%", $$"$suffix$")
            .replace("%receiver-suffix%", "")

        newConfig.commands.reply.enabled = legacyConfig.commands.message.enableReplyCommand
        newConfig.commands.reply.allowByDefault = legacyConfig.commands.message.allowByDefault

        newConfig.commands.broadcast.enabled = legacyConfig.commands.broadcast.enabled
        newConfig.commands.broadcast.allowMiniMessage = legacyConfig.commands.broadcast.allowMiniMessage
        newConfig.commands.broadcast.allowByDefault = legacyConfig.commands.broadcast.allowByDefault
        newConfig.commands.broadcast.format.player = legacyConfig.commands.broadcast.format
            .replace("%player%", $$"$player$")
            .replace("%message%", $$"$message$")
            .replace("%server%", $$"$server$")
            .replace("%prefix%", $$"$prefix$")
            .replace("%suffix%", $$"$suffix$")
        newConfig.commands.broadcast.format.console = legacyConfig.commands.broadcast.format
            .replace("%player%", "")
            .replace("%message%", $$"$message$")
            .replace("%server%", "")
            .replace("%prefix%", "")
            .replace("%suffix%", "")

        newConfig.messages.chat.enabled = legacyConfig.messages.chat.enabled
        newConfig.messages.chat.allowMiniMessage = legacyConfig.messages.chat.allowMiniMessage
        newConfig.messages.chat.format = legacyConfig.messages.chat.format
            .replace("%player%", $$"$player$")
            .replace("%message%", $$"$message$")
            .replace("%server%", $$"$server$")
            .replace("%prefix%", $$"$prefix$")
            .replace("%suffix%", $$"$suffix$")

        newConfig.messages.join.enabled = legacyConfig.messages.join.enabled
        newConfig.messages.join.format = legacyConfig.messages.join.format
            .replace("%player%", $$"$player$")
            .replace("%server%", $$"$server$")
            .replace("%prefix%", $$"$prefix$")
            .replace("%suffix%", $$"$suffix$")

        newConfig.messages.leave.enabled = legacyConfig.messages.leave.enabled
        newConfig.messages.leave.format = legacyConfig.messages.leave.format
            .replace("%player%", $$"$player$")
            .replace("%server%", $$"$server$")
            .replace("%prefix%", $$"$prefix$")
            .replace("%suffix%", $$"$suffix$")

        newConfig.messages.change.enabled = legacyConfig.messages.change.enabled
        newConfig.messages.change.format = legacyConfig.messages.change.format
            .replace("%player%", $$"$player$")
            .replace("%server%", $$"$server$")
            .replace("%prefix%", $$"$prefix$")
            .replace("%suffix%", $$"$suffix$")

        newConfigRoot.node("settings", "server-aliases").from(legacyConfigRoot.node("server-aliases"))
        newConfigRoot.node("placeholders", "luck-perms", "custom-meta").from(legacyConfigRoot.node("luck-perms-meta"))
    }

}