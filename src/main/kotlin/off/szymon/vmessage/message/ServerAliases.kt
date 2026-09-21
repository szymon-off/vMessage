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

package off.szymon.vmessage.message

import com.velocitypowered.api.proxy.ServerConnection
import com.velocitypowered.api.proxy.server.RegisteredServer
import off.szymon.vmessage.config.Config
import java.util.*
import kotlin.jvm.optionals.getOrNull

class ServerAliases {

    companion object {
        private lateinit var instance: ServerAliases

        @JvmStatic
        fun get() = instance
    }

    var aliases: Map<String, String> = emptyMap()
        private set

    init {
        instance = this
        loadAliases()
    }

    @JvmName("getServerNameFromConnection") // otherwise type erasure makes it so the methods are exactly the same
    fun getServerName(server: Optional<ServerConnection>): String {
        return getServerName(server.getOrNull()?.server)
    }

    @JvmName("getServerNameFromRegistered") // otherwise type erasure makes it so the methods are exactly the same
    fun getServerName(server: Optional<RegisteredServer>): String {
        return getServerName(server.getOrNull())
    }

    fun getServerName(server: RegisteredServer?): String {
        val name = server?.serverInfo?.name ?: return Config.get().tree.settings.defaultServerName
        return aliases[name] ?: name
    }

    fun loadAliases() {
        // build a new map and swap it in, so a reload drops aliases removed from the config
        // and never exposes a half filled map to the handlers reading it
        val loaded = mutableMapOf<String, String>()
        Config.get().root.node("settings","server-aliases").childrenMap().forEach { (key, value) ->
            loaded[key.toString()] = value.string ?: return@forEach
        }
        aliases = loaded
    }

}
