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
import com.velocitypowered.api.event.connection.DisconnectEvent
import com.velocitypowered.api.proxy.Player
import off.szymon.vmessage.VMessage
import off.szymon.vmessage.config.Config
import off.szymon.vmessage.message.MessagesHandler
import off.szymon.vmessage.message.ServerAliases
import off.szymon.vmessage.message.parser.DefaultParser

class LeaveHandler : MessagesHandler("leave") {

    private val serverAliases = ServerAliases.get()

    @Subscribe
    fun onLeave(event: DisconnectEvent): EventTask {
        @Suppress("DuplicatedCode")
        return EventTask.async {
            broadcast(event.player)
        }
    }

    fun broadcast(player: Player) {
        val format = Config.get().tree.messages.leave.format
        sendMessage(
            VMessage.get().proxy,
            format,
            DefaultParser(mapOf(
                $$"$player$" to player.username,
                $$"$server$" to serverAliases.getServerName(player.currentServer)
            ), player)
        )
    }

}