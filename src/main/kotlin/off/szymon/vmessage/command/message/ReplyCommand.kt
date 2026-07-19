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

import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.StringArgumentType
import com.velocitypowered.api.command.BrigadierCommand
import com.velocitypowered.api.proxy.Player
import net.kyori.adventure.text.minimessage.MiniMessage
import off.szymon.vmessage.VMessage
import off.szymon.vmessage.command.PluginCommand
import off.szymon.vmessage.config.Config
import kotlin.jvm.optionals.getOrElse

class ReplyCommand : PluginCommand("reply", "r") {

    override fun createCommand(): BrigadierCommand {
        return BrigadierCommand(
            BrigadierCommand.literalArgumentBuilder("reply")
                .requires { checkPermission(it, "vmessage.command.message.reply") }
                .then(BrigadierCommand.requiredArgumentBuilder("message", StringArgumentType.greedyString())
                    .executes { ctx ->
                        val sender = ctx.source as? Player ?: run {
                            ctx.source.sendRichMessage("<dark_gray>▎</dark_gray><gray>This command can only be invoked by a <#00ffff>player</#00ffff>")
                            return@executes Command.SINGLE_SUCCESS // handled properly
                        }

                        val receiverUUID = MessageCommand.get().getReplyReceiver(sender.uniqueId)
                        val receiver = VMessage.get().proxy.getPlayer(receiverUUID).getOrElse {
                            sendMessage(ctx.source, "<dark_gray>▎</dark_gray><gray>You have <#00ffff>no one</#00ffff> to reply to</gray>")
                            return@executes Command.SINGLE_SUCCESS // handled properly
                        }

                        var message = ctx.getArgument("message", String::class.java)

                        val messageConfig = Config.get().tree.commands.message

                        if (!messageConfig.allowMiniMessage) message = MiniMessage.miniMessage().escapeTags(message)

                        val senderFormat = messageConfig.format.sender
                        val receiverFormat = messageConfig.format.receiver

                        sendMessage(receiver, receiverFormat, MessageCommandParser(sender, receiver, sender, message))
                        sendMessage(sender, senderFormat, MessageCommandParser(sender, receiver, receiver, message))

                        return@executes Command.SINGLE_SUCCESS

                    }
                )
                .build()
        )
    }

}