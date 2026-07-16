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

package off.szymon.vmessage.command

import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.StringArgumentType
import com.velocitypowered.api.command.BrigadierCommand
import com.velocitypowered.api.proxy.Player
import off.szymon.fishy.api.messenger.parser.PlaceholderParserBuilder
import off.szymon.vmessage.VMessage
import off.szymon.vmessage.config.Config
import off.szymon.vmessage.message.parser.ChatParser

class BroadcastCommand : PluginCommand("broadcast", "bcast", "bc", "shout") {

    override fun createCommand(): BrigadierCommand {
        return BrigadierCommand(
            BrigadierCommand.literalArgumentBuilder("broadcast")
                .requires { it.hasPermission("vmessage.command.broadcast") }
                .then(BrigadierCommand.requiredArgumentBuilder("message", StringArgumentType.greedyString())
                    .executes { ctx ->
                        val message = ctx.getArgument("message", String::class.java)

                        val player = ctx.source as? Player

                        if (player == null) {
                            val format = Config.get().tree.commands.broadcast.format.console
                            sendMessage(VMessage.get().proxy, format, PlaceholderParserBuilder()
                                .addPlaceholder($$"$message$", message)
                                .build())
                        } else {
                            val format = Config.get().tree.commands.broadcast.format.player
                            sendMessage(VMessage.get().proxy, format, ChatParser(player, message))
                        }

                        return@executes Command.SINGLE_SUCCESS
                    }
                )
                .build()
        )
    }

}