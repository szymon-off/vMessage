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
import com.velocitypowered.api.event.PostOrder
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.player.PlayerChatEvent
import net.kyori.adventure.text.minimessage.MiniMessage
import off.szymon.vmessage.VMessage
import off.szymon.vmessage.config.Config
import off.szymon.vmessage.message.DefaultParser
import off.szymon.vmessage.message.MessagesHandler
import off.szymon.vmessage.message.ServerAliases

class ChatHandler : MessagesHandler("chat") {

    private val serverAliases = ServerAliases.get()
    private val order: PostOrder = try {
        PostOrder.valueOf(Config.get().tree.messages.chat.order.uppercase())
    } catch (e: IllegalArgumentException) {
        VMessage.get().logger.warn("Invalid order value in config for chat messages: ${Config.get().tree.messages.chat.order}. Defaulting to LAST.")
        PostOrder.LAST
    }

    fun onChat(event: PlayerChatEvent): EventTask? {
        // If other plugins cancelled (like LibertyBans or LiteBans if the player is muted)
        if (!event.result.isAllowed) return null

        return EventTask.async {
            val placeholders = getPlaceholders(event)
            val msg = DefaultParser(placeholders, event.player).parse(Config.get().tree.messages.chat.format)
            try {
                event.result = PlayerChatEvent.ChatResult.message(msg) // Possible because of Signed Velocity Dependency
            } catch (e: Exception) {
                VMessage.get().logger.error("Failed to inject signed chat modifications. Is SignedVelocity missing?", e)
            }
        }
    }

    fun getPlaceholders(event: PlayerChatEvent): Map<String, String> {
        val placeholders = mutableMapOf<String, String>()
        placeholders[$$"$player$"] = event.player.username
        placeholders[$$"$message$"] = if (Config.get().tree.messages.chat.allowMiniMessage) event.message else MiniMessage.miniMessage().escapeTags(event.message)
        placeholders[$$"$server$"] = serverAliases.getServerName(event.player.currentServer)
        return placeholders
    }

    // I had to do it this way because annotation args must be compile-time static

    @Subscribe(order = PostOrder.FIRST)
    fun onChatFirst(event: PlayerChatEvent): EventTask? = if (order == PostOrder.FIRST) onChat(event) else null

    @Subscribe(order = PostOrder.EARLY)
    fun onChatEarly(event: PlayerChatEvent): EventTask? = if (order == PostOrder.EARLY) onChat(event) else null

    @Subscribe(order = PostOrder.NORMAL)
    fun onChatNormal(event: PlayerChatEvent): EventTask? = if (order == PostOrder.NORMAL) onChat(event) else null

    @Subscribe(order = PostOrder.LATE)
    fun onChatLate(event: PlayerChatEvent): EventTask? = if (order == PostOrder.LATE) onChat(event) else null

    @Subscribe(order = PostOrder.LAST)
    fun onChatLast(event: PlayerChatEvent): EventTask? = if (order == PostOrder.LAST) onChat(event) else null

}