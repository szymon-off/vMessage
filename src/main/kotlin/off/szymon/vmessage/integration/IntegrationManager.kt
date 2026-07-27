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
import off.szymon.fishy.api.messenger.parser.MessageParser
import off.szymon.vmessage.VMessage
import off.szymon.vmessage.config.Config

class IntegrationManager : IntegrationParser {

    val vMessage = VMessage.get()
    val config = Config.get()

    val integrations = mutableMapOf<Class<out Integration>, Integration>()

    companion object {
        private lateinit var instance: IntegrationManager

        @JvmStatic
        fun get(): IntegrationManager = instance
    }

    init {
        instance = this
        loadIntegrations()
    }

    fun loadIntegrations() {
        loadIntegrationIfEnabled(LuckPermsIntegration::class.java, "luck-perms", "luckperms")
        loadIntegrationIfEnabled(PlaceholderApiIntegration::class.java, "placeholder-api", "papiproxybridge")
    }

    fun unloadIntegrations() {
        for (integrationClass in integrations.keys) {
            unloadIntegration(integrationClass)
        }
    }

    fun reloadIntegrations() {
        unloadIntegrations()
        loadIntegrations()
    }

    fun loadIntegrationIfEnabled(clazz: Class<out Integration>, id: String, pluginId: String) {
        if (!vMessage.proxy.pluginManager.isLoaded(pluginId)) {
            vMessage.logger.info("Skipping '$id' integration...")
            return
        }
        if (!config.root.node("integrations","placeholder",id,"enabled").getBoolean(true)) {
            vMessage.logger.info("Skipping '$id' integration...")
            return
        }
        val integration = clazz.getDeclaredConstructor().newInstance() // only initialize if plugin is loaded (import not found exception or something otherwise)
        if (integration.id != id || integration.pluginId != pluginId) {
            vMessage.logger.info("Integration '$id' was loaded improperly. Skipping...")
            return
        }
        vMessage.logger.info("Loading '$id' integration...")
        integrations[clazz] = integration
    }

    fun unloadIntegration(clazz: Class<out Integration>) {
        val integration = integrations[clazz] ?: return
        vMessage.logger.info("Unloading '${integration.id}' integration...")
        integrations.remove(clazz)
    }

    @Suppress("UNCHECKED_CAST")
    fun <I : Integration> getIntegration(clazz: Class<I>): I? {
        val integration: Integration = integrations[clazz] ?: return null
        return integration as I
    }

    override fun parse(string: String, player: Player): String {
        var output = string
        for (i in integrations.values) {
            output = i.parse(output, player)
        }
        return output
    }

    fun asMessageParser(player: Player): MessageParser {
        return object : MessageParser {
            override fun parse(string: String): String {
                return this@IntegrationManager.parse(string, player)
            }
        }
    }

}
