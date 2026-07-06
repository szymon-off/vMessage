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

import com.velocitypowered.api.event.PostOrder
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.player.PlayerChatEvent
import net.kyori.adventure.text.minimessage.MiniMessage
import off.szymon.vmessage.VMessage
import off.szymon.vmessage.config.Config
import off.szymon.vmessage.message.DefaultParser
import off.szymon.vmessage.message.MessagesHandler
import kotlin.jvm.optionals.getOrNull

class ChatHandler : MessagesHandler("chat") {

    var order: PostOrder

    init {
        try {
            order = PostOrder.valueOf(Config.get().tree.messages.chat.order)
        } catch (e: IllegalArgumentException) {
            VMessage.get().logger.warn("${e.message}: Invalid order value in config for chat messages: ${Config.get().tree.messages.chat.order}. Defaulting to LAST.")
            order = PostOrder.LAST
        }
    }

    fun onChat(event: PlayerChatEvent) {
        if (event.result == PlayerChatEvent.ChatResult.denied()) return

        // Cancel possible because of Signed Velocity Dependency
        event.result = PlayerChatEvent.ChatResult.denied()
        val format = Config.get().tree.messages.chat.format
        sendMessage(VMessage.get().server, format, DefaultParser(getPlaceholders(event), event.player))
    }

    fun getPlaceholders(event: PlayerChatEvent): Map<String, String> {
        val placeholders = mutableMapOf<String, String>()
        placeholders[$$"$player$"] = event.player.username
        placeholders[$$"$message$"] = if (Config.get().tree.messages.chat.allowMiniMessage) event.message else MiniMessage.miniMessage().escapeTags(event.message)
        placeholders[$$"$server$"] = event.player.currentServer.getOrNull()?.serverInfo?.name ?: "Unknown" // TODO configurable default value
        return placeholders.toMap()
    }

    // I had to do it this way because annotation args must be "compile-time static"

    @Subscribe(order = PostOrder.FIRST)
    fun onChatFirst(event: PlayerChatEvent) {
        if (order == PostOrder.FIRST) { onChat(event) }
    }

    @Subscribe(order = PostOrder.EARLY)
    fun onChatEarly(event: PlayerChatEvent) {
        if (order == PostOrder.EARLY) { onChat(event) }
    }

    @Subscribe(order = PostOrder.NORMAL)
    fun onChatNormal(event: PlayerChatEvent) {
        if (order == PostOrder.NORMAL) { onChat(event) }
    }

    @Subscribe(order = PostOrder.LATE)
    fun onChatLate(event: PlayerChatEvent) {
        if (order == PostOrder.LATE) { onChat(event) }
    }

    @Subscribe(order = PostOrder.LAST)
    fun onChatLast(event: PlayerChatEvent) {
        if (order == PostOrder.LAST) { onChat(event) }
    }

}