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
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.connection.DisconnectEvent
import com.velocitypowered.api.proxy.Player
import off.szymon.vmessage.VMessage
import off.szymon.vmessage.command.PluginCommand
import off.szymon.vmessage.config.Config
import off.szymon.vmessage.message.MessageSanitizer
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.jvm.optionals.getOrElse

class MessageCommand : PluginCommand("message", "msg", "tell", "whisper", "w") {

    companion object {
        private lateinit var instance: MessageCommand

        @JvmStatic
        fun get() = instance
    }

    init {
        instance = this
        VMessage.get().proxy.eventManager.register(VMessage.get(), this)
    }

    // written from every command thread that sends a message and from the disconnect listener
    private val replyMap = ConcurrentHashMap<UUID, UUID>()

    @Subscribe
    fun onLeave(event: DisconnectEvent) {
        val uuid = event.player.uniqueId
        replyMap.remove(uuid) // where they would be the one replying
        replyMap.values.removeIf { it == uuid } // where they would be the one replied to
    }

    fun getReplyReceiver(replySender: UUID): UUID? /* replyReceiver */ = replyMap[replySender]

    // both sides of a conversation can reply to each other, so /reply follows the conversation
    // instead of pointing at whoever happened to message you last
    fun setReplyTargets(sender: UUID, receiver: UUID) {
        replyMap[sender] = receiver
        replyMap[receiver] = sender
    }

    override fun createCommand(): BrigadierCommand {
        return BrigadierCommand(
            BrigadierCommand.literalArgumentBuilder("message")
                .requires { checkPermission(it, "vmessage.command.message") }
                .then(BrigadierCommand.requiredArgumentBuilder("player", StringArgumentType.word())
                    .then(BrigadierCommand.requiredArgumentBuilder("message", StringArgumentType.greedyString())
                        .executes { ctx ->
                            val sender = ctx.source as? Player ?: run {
                                ctx.source.sendRichMessage("<dark_gray>▎</dark_gray><gray>This command can only be invoked by a <#00ffff>player</#00ffff>")
                                return@executes Command.SINGLE_SUCCESS // handled properly
                            }

                            val receiverString = ctx.getArgument("player", String::class.java).lowercase()
                            val receiver = VMessage.get().proxy.getPlayer(receiverString).getOrElse {
                                sendMessage(ctx.source, "<dark_gray>▎</dark_gray><gray>Invalid argument provided for '<#00ffff>player</#00ffff>'</gray>")
                                return@executes Command.SINGLE_SUCCESS // handled properly
                            }

                            val messageConfig = Config.get().tree.commands.message

                            val message = MessageSanitizer.sanitize(
                                ctx.getArgument("message", String::class.java),
                                messageConfig.allowMiniMessage
                            )

                            val senderFormat = messageConfig.format.sender
                            val receiverFormat = messageConfig.format.receiver

                            sendMessage(receiver, receiverFormat, MessageCommandParser(sender, receiver, sender, message))
                            sendMessage(sender, senderFormat, MessageCommandParser(sender, receiver, receiver, message))

                            setReplyTargets(sender.uniqueId, receiver.uniqueId)

                            return@executes Command.SINGLE_SUCCESS
                        }
                    )
                    .suggests { _, builder ->
                        VMessage.get().proxy.allPlayers.forEach { builder.suggest(it.username) }
                        return@suggests builder.buildFuture()
                    }
                )
                .build()
        )
    }

}