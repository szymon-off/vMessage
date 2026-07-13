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

package off.szymon.vmessage.command.message

import com.velocitypowered.api.proxy.Player
import net.kyori.adventure.text.minimessage.MiniMessage
import off.szymon.fishy.api.messenger.parser.MessageParser
import off.szymon.fishy.api.messenger.parser.MultiParser
import off.szymon.fishy.api.messenger.parser.PlaceholderParser
import off.szymon.fishy.api.messenger.parser.PlaceholderParserBuilder
import off.szymon.vmessage.config.Config
import off.szymon.vmessage.integration.IntegrationManager
import off.szymon.vmessage.message.ServerAliases

class MessageCommandParser(val sender: Player, val receiver: Player, val parsePlayer: Player, val message: String): MessageParser {

    override fun parse(string: String): String {
        val builder = PlaceholderParserBuilder()

        builder.addPlaceholder($$"$sender$", sender.username)
        builder.addPlaceholder($$"$receiver$", receiver.username)
        builder.addPlaceholder($$"$sender_server$", ServerAliases.get().getServerName(sender.currentServer))
        builder.addPlaceholder($$"$receiver_sender$", ServerAliases.get().getServerName(receiver.currentServer))

        val message = if (Config.get().tree.messages.chat.allowMiniMessage)
            message
        else
            MiniMessage.miniMessage().escapeTags(message)

        return MultiParser(
            builder.build(),
            IntegrationManager.get().asMessageParser(parsePlayer),
            PlaceholderParser($$"$message$" to message), // parse message at the end
        ).parse(string)
    }

}