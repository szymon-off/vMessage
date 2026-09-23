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
import org.spongepowered.configurate.yaml.NodeStyle
import org.spongepowered.configurate.yaml.YamlConfigurationLoader
import java.nio.file.Files
import java.nio.file.StandardCopyOption

class LegacyConfigMigration {

    private val legacyConfigDir = VMessage.get().dataDir.parent.resolve("vMessage")
    private val legacyConfigFile = legacyConfigDir.resolve("config.yml")

    val newConfigRoot: CommentedConfigurationNode = Config.get().root

    fun runMigrationIfNeeded() {
        if (needsMigration()) {
            // any exception here comes from a legacy config we don't control, so none of them may stop the plugin from loading
            try {
                migrateLegacyConfig()
            } catch (e: Exception) {
                VMessage.get().logger.error("Legacy config migration failed: ${e.message}", e)
                Config.get().load() // drop whatever was already copied over before it failed
                return
            }

            try {
                finalizeLegacyConfigFolder()
            } catch (e: Exception) {
                VMessage.get().logger.warn("Legacy config was migrated, but the old plugins/vMessage folder could not be cleaned up: ${e.message}", e)
            }
        }
    }

    private fun finalizeLegacyConfigFolder() {
        val migratedFile = legacyConfigDir.resolve("MIGRATED-config.yml")
        Files.move(legacyConfigFile, migratedFile, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE)

        Files.writeString(
            legacyConfigDir.resolve("README.txt"),
            """
                This folder is no longer used.

                vMessage's configuration has moved to the plugins/vmessage folder (lowercase).

                Your old configuration has already been migrated automatically and is kept here,
                renamed to MIGRATED-config.yml, only for reference. You can safely delete this
                entire folder once you've confirmed everything migrated correctly.
            """.trimIndent()
        )
    }

    fun buildLegacyConfigRoot(): CommentedConfigurationNode {
        val loader: YamlConfigurationLoader = YamlConfigurationLoader.builder()
            .path(legacyConfigFile)
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
        // Once migration succeeds, the legacy config.yml is renamed to MIGRATED-config.yml,
        // so its absence on the next boot is itself proof migration already ran.
        return Files.exists(legacyConfigFile)
    }

    @Suppress("DuplicatedCode")
    private fun migrateLegacyConfig() {
        val legacyConfigRoot = buildLegacyConfigRoot()
        val legacyConfig = legacyConfigRoot.get(LegacyMainConfig::class.java) ?: throw IllegalStateException("Legacy config is invalid, cannot migrate")
        val newConfig = Config.get().tree

        newConfig.commands.message.enabled = legacyConfig.commands.message.enabled
        newConfig.commands.message.allowMiniMessage = legacyConfig.commands.message.allowMiniMessage
        newConfig.commands.message.allowByDefault = legacyConfig.commands.message.allowByDefault
        newConfig.commands.message.format.sender = legacyConfig.commands.message.format.sender
            .replace("%sender%", $$"$sender$")
            .replace("%receiver%", $$"$receiver$")
            .replace("%message%", $$"$message$")
            .replace("%sender-server%", $$"$sender_server$")
            .replace("%receiver-server%", $$"$receiver_server$")
            .replace("%sender-prefix%", "")
            .replace("%receiver-prefix%", $$"$prefix$")
            .replace("%sender-suffix%", "")
            .replace("%receiver-suffix%", $$"$suffix$")
        newConfig.commands.message.format.receiver = legacyConfig.commands.message.format.receiver
            .replace("%sender%", $$"$sender$")
            .replace("%receiver%", $$"$receiver$")
            .replace("%message%", $$"$message$")
            .replace("%sender-server%", $$"$sender_server$")
            .replace("%receiver-server%", $$"$receiver_server$")
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
            .replace("%old_server%", $$"$old_server$")
            .replace("%new_server%", $$"$new_server$")
            .replace("%prefix%", $$"$prefix$")
            .replace("%suffix%", $$"$suffix$")

        Config.get().save()

        newConfigRoot.node("settings", "server-aliases").from(legacyConfigRoot.node("server-aliases"))
        newConfigRoot.node("placeholders", "luck-perms", "custom-meta").from(legacyConfigRoot.node("luck-perms-meta"))
        Config.get().saveRoot()
    }

}