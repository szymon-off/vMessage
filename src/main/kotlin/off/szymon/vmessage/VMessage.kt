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

package off.szymon.vmessage/*
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
import java.nio.file.Path

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

    // TODO update checker
    @Subscribe
    fun onProxyInitialization(event: ProxyInitializeEvent) {
        val description = plugin.description
        logger.info("Initializing ${description.name} v${description.version} by ${description.authors.joinToString(", ")}")
        logger.info("Powered by: FishyAPI v${FishyAPI.VERSION} by SzymON/OFF")
        Config()
        detectSignedVelocity()
        initializeVMessage()
        logger.info("Initialization complete! Ready to serve chat messages!")
    }

    private fun detectSignedVelocity() {
        val signedVelocityEnabled = proxy.pluginManager.isLoaded("SignedVelocity")
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

}