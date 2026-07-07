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

        fun get() = instance
    }

    val aliases: MutableMap<String, String> = mutableMapOf()

    init {
        Config.get().root.node("server-aliases").childrenMap().forEach { (key, value) ->
            aliases[key.toString()] = value.string ?: return@forEach
        }
    }

    fun getServerName(server: Optional<ServerConnection>): String {
        return getServerName(server.getOrNull()?.server)
    }

    fun getServerName(server: RegisteredServer?): String {
        return aliases[server?.serverInfo?.name] ?: "Unknown"
    }

}
