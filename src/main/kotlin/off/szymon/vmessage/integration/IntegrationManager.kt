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

package off.szymon.vmessage.integration

import com.velocitypowered.api.proxy.Player
import off.szymon.fishy.api.messenger.parser.MultiParser
import off.szymon.vmessage.VMessage
import off.szymon.vmessage.config.Config

class IntegrationManager {

    val vMessage = VMessage.get()
    val config = Config.get()

    val integrations = mutableListOf<Class<out Integration>>()

    companion object {
        @JvmStatic
        private lateinit var instance: IntegrationManager

        @JvmStatic
        fun get(): IntegrationManager = instance
    }

    init {
        instance = this
        loadIntegrations()
    }

    fun loadIntegrations() {
        loadIntegrationIfEnabled(ServerAliasesIntegration::class.java)
        loadIntegrationIfEnabled(LuckPermsIntegration::class.java)
        loadIntegrationIfEnabled(PlaceholderApiIntegration::class.java)
    }

    fun unloadIntegrations() {
        for (integrationClass in integrations) {
            unloadIntegration(integrationClass)
        }
    }

    fun loadIntegrationIfEnabled(clazz: Class<out Integration>) {
        val integration = clazz.getDeclaredConstructor().newInstance()
        val pluginEnabled = vMessage.server.pluginManager.isLoaded(integration.pluginId)
        if (pluginEnabled && config.root.node("integrations","placeholder",integration.id,"enabled").getBoolean(true)) {
            vMessage.logger.info("Loading '${integration.id}' integration...")
            integrations.add(clazz)
        } else {
            vMessage.logger.info("Skipping '${integration.id}' integration...")
        }
    }

    fun unloadIntegration(clazz: Class<out Integration>) {
        val integration = clazz.getDeclaredConstructor().newInstance() ?: return
        vMessage.logger.info("Unloading '${integration.id}' integration...")
        integrations.remove(clazz)
    }

    @Suppress("UNCHECKED_CAST")
    fun <I : Integration> getIntegration(clazz: Class<I>): I? {
        val handler: Integration = clazz.getDeclaredConstructor().newInstance() ?: return null
        return handler as? I
    }

    fun getMultiParser(player: Player): MultiParser {
        return MultiParser(integrations.map { it.getDeclaredConstructor().newInstance(player) })
    }

}