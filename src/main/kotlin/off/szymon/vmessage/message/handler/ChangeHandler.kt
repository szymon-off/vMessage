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
import com.velocitypowered.api.proxy.Player
import com.velocitypowered.api.proxy.server.RegisteredServer
import off.szymon.vmessage.VMessage
import off.szymon.vmessage.config.Config
import off.szymon.vmessage.message.MessagesHandler
import off.szymon.vmessage.message.ServerAliases
import off.szymon.vmessage.message.parser.DefaultParser

class ChangeHandler : MessagesHandler("change") {

    private val serverAliases = ServerAliases.get()

    @Subscribe
    fun onJoin(event: ServerPostConnectEvent): EventTask? {
        if (event.previousServer == null) return null

        return EventTask.async { broadcast(event.player, event.previousServer!!) }
    }

    fun broadcast(player: Player, previousServer: RegisteredServer) {
        val format = Config.get().tree.messages.change.format
        sendMessage(
            VMessage.get().proxy,
            format,
            DefaultParser(mapOf(
                $$"$player$" to player.username,
                $$"$old_server$" to serverAliases.getServerName(previousServer),
                $$"$new_server$" to serverAliases.getServerName(player.currentServer)
            ), player)
        )
    }

}