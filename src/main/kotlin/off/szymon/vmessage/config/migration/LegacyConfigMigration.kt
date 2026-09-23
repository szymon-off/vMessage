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
import java.nio.file.Path
import java.nio.file.StandardCopyOption

// 1.x kept its config in plugins/vMessage, 2.0 uses plugins/vmessage. On case-insensitive filesystems
// (Windows, macOS) those are the same folder, so a legacy config is recognized by its contents instead:
// every 1.x config has a messages section and none of them has config-version, which 2.0 always writes.
//
// this class's code was thouroughly tested and fixed by an AI agent. The base logic is authored by me.
class LegacyConfigMigration {

    private val dataDir = VMessage.get().dataDir
    private val legacyConfigDir = dataDir.parent.resolve("vMessage")

    // where prepareMigration() moved the legacy config to, null when there is nothing to migrate
    private var migratedFile: Path? = null

    // must run before Config() is created, otherwise Config() would load a legacy config sitting in
    // plugins/vmessage as its own and merge the 2.0 defaults into it
    fun prepareMigration() {
        // any exception here comes from a legacy config we don't control, so none of them may stop the plugin from loading
        try {
            val legacyConfigFile = findLegacyConfigFile() ?: return
            val target = findFreeMigratedFile(legacyConfigFile.parent)
            Files.move(legacyConfigFile, target, StandardCopyOption.ATOMIC_MOVE)
            migratedFile = target
        } catch (e: Exception) {
            VMessage.get().logger.error("Could not prepare the legacy config migration: ${e.message}", e)
        }
    }

    fun runMigrationIfNeeded() {
        val migratedFile = migratedFile ?: return

        try {
            migrateLegacyConfig(migratedFile)
        } catch (e: Exception) {
            VMessage.get().logger.error("Legacy config migration failed: ${e.message}. Your old config is kept at $migratedFile", e)
            Config.get().load() // drop whatever was already copied over before it failed
            return
        }
        VMessage.get().logger.info("Migrated the vMessage 1.x config. The old one is kept at $migratedFile")

        // on case-insensitive filesystems the legacy folder is the live one, which must not be called unused
        if (Files.isSameFile(migratedFile.parent, dataDir)) return
        try {
            writeLegacyFolderReadme(migratedFile)
        } catch (e: Exception) {
            VMessage.get().logger.warn("Legacy config was migrated, but the old plugins/vMessage folder could not be cleaned up: ${e.message}", e)
        }
    }

    private fun findLegacyConfigFile(): Path? {
        // on case-insensitive filesystems this is also where the legacy config lives
        val configFile = dataDir.resolve("config.yml")
        if (isLegacyConfig(configFile)) return configFile

        val legacyConfigFile = legacyConfigDir.resolve("config.yml")
        if (!isLegacyConfig(legacyConfigFile)) return null

        // migrating now would overwrite a 2.0 config that may already have been set up by hand
        if (Files.exists(configFile)) {
            VMessage.get().logger.warn("Found a vMessage 1.x config in $legacyConfigDir, but $configFile already exists. Skipping the migration, delete $configFile to migrate the old config instead.")
            return null
        }
        return legacyConfigFile
    }

    private fun isLegacyConfig(file: Path): Boolean {
        if (!Files.exists(file)) return false
        val root = buildLegacyConfigRoot(file)
        return root.node("config-version").virtual() && !root.node("messages").virtual()
    }

    // never replace an earlier MIGRATED-config.yml, it may be the only copy of someone's 1.x settings
    private fun findFreeMigratedFile(dir: Path): Path {
        var target = dir.resolve("MIGRATED-config.yml")
        var i = 1
        while (Files.exists(target)) {
            target = dir.resolve("MIGRATED-config-${i++}.yml")
        }
        return target
    }

    private fun writeLegacyFolderReadme(migratedFile: Path) {
        Files.writeString(
            migratedFile.resolveSibling("README.txt"),
            """
                This folder is no longer used.

                vMessage's configuration has moved to the plugins/vmessage folder (lowercase).

                Your old configuration has already been migrated automatically and is kept here,
                renamed to ${migratedFile.fileName}, only for reference. You can safely delete this
                entire folder once you've confirmed everything migrated correctly.
            """.trimIndent()
        )
    }

    fun buildLegacyConfigRoot(file: Path): CommentedConfigurationNode {
        val loader: YamlConfigurationLoader = YamlConfigurationLoader.builder()
            .path(file)
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

    @Suppress("DuplicatedCode")
    private fun migrateLegacyConfig(migratedFile: Path) {
        val legacyConfigRoot = buildLegacyConfigRoot(migratedFile)
        val newConfigRoot = Config.get().root
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