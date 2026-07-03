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

import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.player.PlayerChatEvent
import net.kyori.adventure.text.minimessage.MiniMessage
import off.szymon.fishy.api.messenger.parser.PlaceholderParser
import off.szymon.vmessage.VMessage
import off.szymon.vmessage.config.Config
import off.szymon.vmessage.message.MessagesHandler
import kotlin.jvm.optionals.getOrNull

class ChatHandler : MessagesHandler("chat") {

    @Subscribe // TODO figure out priorities because they have to be fucking compile time static shits
    fun onChat(event: PlayerChatEvent) {
        val format = Config.get().tree.messages.chat.format
        sendMessage(VMessage.get().server, format, PlaceholderParser(getPlaceholders(event))) // TODO + integration placeholders
    }

    fun getPlaceholders(event: PlayerChatEvent): Map<String, String> {
        val placeholders = mutableMapOf<String, String>()
        placeholders[$$"$player$"] = event.player.username
        placeholders[$$"$message$"] = if (Config.get().tree.messages.chat.allowMiniMessage) event.message else MiniMessage.miniMessage().escapeTags(event.message)
        placeholders[$$"$server$"] = event.player.currentServer.getOrNull()?.serverInfo?.name ?: "Unknown" // TODO configurable default value
        return placeholders.toMap()
    }

}