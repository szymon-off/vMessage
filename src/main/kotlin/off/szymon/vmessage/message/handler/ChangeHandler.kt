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

package off.szymon.vmessage.message.handler

import com.velocitypowered.api.event.EventTask
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.player.ServerPostConnectEvent
import off.szymon.vmessage.VMessage
import off.szymon.vmessage.config.Config
import off.szymon.vmessage.message.DefaultParser
import off.szymon.vmessage.message.MessagesHandler
import off.szymon.vmessage.message.ServerAliasesHolder

class ChangeHandler : MessagesHandler("change") {

    @Subscribe
    fun onJoin(event: ServerPostConnectEvent): EventTask? {
        if (event.previousServer != null) return null

        return EventTask.async {
            val format = Config.get().tree.messages.change.format
            sendMessage(
                VMessage.get().server,
                format,
                DefaultParser(getPlaceholders(event), event.player)
            )
        }
    }

    fun getPlaceholders(event: ServerPostConnectEvent): Map<String, String> {
        val placeholders = mutableMapOf<String, String>()
        placeholders[$$"$player$"] = event.player.username
        placeholders[$$"$old_server$"] = ServerAliasesHolder.getServerName(event.previousServer)
        placeholders[$$"$new_server$"] = ServerAliasesHolder.getServerName(event.player.currentServer)
        return placeholders.toMap()
    }

}