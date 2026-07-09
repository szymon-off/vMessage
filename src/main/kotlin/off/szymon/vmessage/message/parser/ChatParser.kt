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

package off.szymon.vmessage.message.parser

import com.velocitypowered.api.proxy.Player
import net.kyori.adventure.text.minimessage.MiniMessage
import off.szymon.fishy.api.messenger.parser.MessageParser
import off.szymon.fishy.api.messenger.parser.MultiParser
import off.szymon.fishy.api.messenger.parser.PlaceholderParser
import off.szymon.fishy.api.messenger.parser.PlaceholderParserBuilder
import off.szymon.vmessage.config.Config
import off.szymon.vmessage.integration.IntegrationManager
import off.szymon.vmessage.message.ServerAliases

// chat has special parser because $message$ must be parsed at the end to avoid injection from players
class ChatParser(val player: Player, val message: String): MessageParser {

    override fun parse(string: String): String {
        val builder = PlaceholderParserBuilder()

        builder.addPlaceholder($$"$player$", player.username)
        builder.addPlaceholder($$"$server$", ServerAliases.get().getServerName(player.currentServer))

        val message = if (Config.get().tree.messages.chat.allowMiniMessage)
            message
        else
            MiniMessage.miniMessage().escapeTags(message)

        return MultiParser(
            builder.build(),
            IntegrationManager.get().getMultiParser(player),
            PlaceholderParser($$"$message$" to message), // parse message at the end
        ).parse(string)
    }

}