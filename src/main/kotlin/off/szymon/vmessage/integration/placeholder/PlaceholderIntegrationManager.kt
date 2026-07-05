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

package off.szymon.vmessage.integration.placeholder

import off.szymon.fishy.api.messenger.parser.MultiParser
import off.szymon.vmessage.VMessage
import off.szymon.vmessage.config.Config
import off.szymon.vmessage.integration.IntegrationManager

class PlaceholderIntegrationManager : IntegrationManager<PlaceholderIntegration> {

    val vMessage = VMessage.get()
    val config = Config.get()

    val integrations = mutableMapOf<Class<out PlaceholderIntegration>, PlaceholderIntegration>()
    // TODO mute integrations

    companion object {
        @JvmStatic
        private lateinit var instance: PlaceholderIntegrationManager

        @JvmStatic
        fun get(): PlaceholderIntegrationManager = instance
    }

    init {
        instance = this
        loadIntegrations()
    }

    override fun loadIntegrations() {
        loadIntegrationIfEnabled(ServerAliasesIntegration::class.java)
    }

    override fun unloadIntegrations() {
        for (integrationClass in integrations.keys) {
            unloadIntegration(integrationClass)
        }
    }

    override fun loadIntegrationIfEnabled(clazz: Class<out PlaceholderIntegration>) {
        val integration = clazz.getDeclaredConstructor().newInstance()
        val pluginEnabled = vMessage.server.pluginManager.isLoaded(integration.pluginId)
        if (pluginEnabled && config.root.node("integrations","placeholder",integration.id,"enabled").getBoolean(true)) {
            vMessage.logger.info("Loading '${integration.id}' integration...")
            integrations[clazz] = integration
            integration.onEnable()
        } else {
            vMessage.logger.info("Skipping '${integration.id}' integration...")
        }
    }

    override fun unloadIntegration(clazz: Class<out PlaceholderIntegration>) {
        val integration = integrations[clazz] ?: return
        vMessage.logger.info("Unloading '${integration.id}' integration...")
        integrations.remove(clazz)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <I : PlaceholderIntegration> getIntegration(clazz: Class<I>): I? {
        val handler: PlaceholderIntegration = integrations[clazz] ?: return null
        return handler as? I
    }

    fun getMultiParser(): MultiParser {
        return MultiParser(integrations.values)
    }

}