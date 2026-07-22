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

package off.szymon.vmessage

import com.google.gson.JsonParser
import com.google.inject.Inject
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent
import com.velocitypowered.api.plugin.Dependency
import com.velocitypowered.api.plugin.Plugin
import com.velocitypowered.api.plugin.PluginContainer
import com.velocitypowered.api.plugin.annotation.DataDirectory
import com.velocitypowered.api.proxy.ProxyServer
import off.szymon.fishy.api.FishyAPI
import off.szymon.vmessage.command.CommandManager
import off.szymon.vmessage.config.Config
import off.szymon.vmessage.generated.Version
import off.szymon.vmessage.integration.IntegrationManager
import off.szymon.vmessage.message.HandlerManager
import off.szymon.vmessage.message.ServerAliases
import org.bstats.velocity.Metrics
import org.slf4j.Logger
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.file.Path
import java.time.Duration
import java.util.concurrent.CompletableFuture
import kotlin.jvm.optionals.getOrDefault
import kotlin.math.max

// TODO update README

@Plugin(
    id = "vmessage",
    name = "vMessage",
    description = "vMessage is the best Velocity plugin for synchronizing chat and player events across your entire proxy network",
    version = Version.VERSION, // Your IDE may show an error here, but it will compile fine. This is generated during build. Run the generateVersion task if needed.
    authors = ["SzymON/OFF"],
    url = "https://szymonoff.me/projects/vmessage.html",
    dependencies = [
        Dependency(id = "signedvelocity", optional = true),
        Dependency(id = "luckperms", optional = true),
        Dependency(id = "papiproxybridge", optional = true),
    ]
)
class VMessage @Inject constructor(
    val proxy: ProxyServer,
    val logger: Logger,
    @param:DataDirectory val dataDir: Path,
    val plugin: PluginContainer,
    val metrics: Metrics.Factory // TODO
) {

    companion object {
        private lateinit var instance: VMessage

        @JvmStatic
        fun get(): VMessage = instance
    }

    init {
        instance = this
    }

    @Subscribe
    fun onProxyInitialization(event: ProxyInitializeEvent) {
        val description = plugin.description
        logger.info("Initializing ${description.name} v${description.version.getOrDefault("0.0.0-UNKNOWN")} by ${description.authors.joinToString(", ")}")
        logger.info("Powered by: FishyAPI v${FishyAPI.VERSION} by SzymON/OFF")
        Config()
        detectSignedVelocity()
        initializeVMessage()
        logger.info("Initialization completed! Ready to serve chat messages!")
        checkForUpdates()
    }

    private fun detectSignedVelocity() {
        val signedVelocityEnabled = proxy.pluginManager.isLoaded("signedvelocity")
        val chatMessagesEnabled = Config.get().tree.messages.chat.enabled
        if (!signedVelocityEnabled && chatMessagesEnabled) {
            logger.warn("SignedVelocity not detected! vMessage chat messages will most likely have issues.")
            logger.warn("Please consider installing SignedVelocity on the proxy and backends from https://modrinth.com/plugin/signedvelocity")
            logger.warn("Do not ignore this if you don't know what you're doing.")
            logger.warn("Proceeding without it...")
        }
    }

    fun initializeVMessage() {
        ServerAliases()
        IntegrationManager()
        HandlerManager()
        CommandManager()
    }

    fun reloadVMessage() {
        Config.get().load()
//        detectSignedVelocity()
        ServerAliases.get().loadAliases()
        IntegrationManager.get().reloadIntegrations()
        HandlerManager.get().reloadHandlers()
    }

    fun checkForUpdates() {
        if (!Config.get().tree.settings.checkForUpdates) {
            logger.info("Skipping update check. Consider enabling this in the config.")
            return
        }

        val currentVersionString = plugin.description.version.getOrDefault("0.0.0-UNKNOWN")
        if (currentVersionString.endsWith("dev")) {
            logger.info("You are running a development build of vMessage ($currentVersionString). Update checking is disabled for dev builds.")
            logger.info("Consider switching to a stable release from https://modrinth.com/plugin/vmessage")
            return
        }

        // I don't really wanna add kotlin coroutines for this once usecase

        CompletableFuture.runAsync {
            val newestVersionString = fetchLatestReleaseVersion() ?: run {
                logger.warn("Cannot check for updates. Could not fetch latest release version.")
                return@runAsync
            }

            val currentVersion = currentVersionString.split(".").map { i ->
                i.toIntOrNull() ?: run {
                    logger.warn("Cannot check for updates. Invalid current version format: $currentVersionString")
                    return@runAsync
                }
            }
            val newestVersion = newestVersionString.split(".").map { i ->
                i.toIntOrNull() ?: run {
                    logger.warn("Cannot check for updates. Invalid newest version format: $newestVersionString")
                    return@runAsync
                }
            }

            val length = max(currentVersion.size, newestVersion.size)
            for (i in 0 until length) {
                val currentPart = currentVersion.getOrNull(i) ?: 0
                val newestPart = newestVersion.getOrNull(i) ?: 0
                when {
                    currentPart > newestPart -> {
                        logger.info("You are running an unreleased build ahead of the newest version (newest: $newestVersionString)")
                        return@runAsync
                    }
                    currentPart < newestPart -> {
                        logger.info("A new version of vMessage is available: $newestVersionString (current: $currentVersionString)")
                        logger.info("Please update to the latest version from https://modrinth.com/plugin/vmessage")
                        return@runAsync
                    }
                }
            }
            logger.info("You are running the latest version of vMessage ($currentVersionString)")
        }
    }

    private val httpClient = HttpClient.newHttpClient()

    fun fetchLatestReleaseVersion(): String? {
        val request = HttpRequest.newBuilder()
            .uri(URI.create("https://api.modrinth.com/v2/project/vmessage/version"))
            .header("User-Agent", "vMessage-UpdateChecker")
            .timeout(Duration.ofSeconds(5))
            .build()
        return try {
            val body = httpClient.send(request, HttpResponse.BodyHandlers.ofString()).body()
            val versions = JsonParser.parseString(body).asJsonArray
            versions.firstOrNull { it.asJsonObject.get("version_type").asString == "release" } // Assumes Modrinth returns in order (which it does)
                ?.asJsonObject?.get("version_number")?.asString
        } catch (e: Exception) {
            logger.warn("Failed to fetch latest version from Modrinth: ${e.message}")
            null
        }
    }

}