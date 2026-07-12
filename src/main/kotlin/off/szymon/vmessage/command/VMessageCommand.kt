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
import off.szymon.fishy.api.FishyAPI
import off.szymon.vmessage.VMessage
import off.szymon.vmessage.message.HandlerManager
import off.szymon.vmessage.message.handler.ChatHandler

class VMessageCommand : PluginCommand("vmessage", "vmsg", "vm") {

    override fun createCommand(): BrigadierCommand {
        return BrigadierCommand(
            BrigadierCommand.literalArgumentBuilder(name)
//                .requires { src -> src.hasPermission("vmessage.command.vmessage") }
                .executes { ctx ->
                    val description = VMessage.get().plugin.description
                    val authors = description.authors.joinToString("</#00ffff><gray>,</gray> <#00ffff>")
                    ctx.source.sendRichMessage("""
                        <dark_gray>▎</dark_gray><bold><#00ffff>vMessage</#00ffff></bold> <white>»</white> <gray>by</gray> <#00ffff>$authors</#00ffff>
                        <dark_gray>▎</dark_gray><gray>Version</gray> <white>»</white> <#00ffff>${description.version}</#00ffff>
                        <dark_gray>▎</dark_gray><gray>Powered by</gray> <white>»</white> <#00ffff>FishyAPI</#00ffff> <gray>v${FishyAPI.VERSION}</gray>
                        <dark_gray>▎</dark_gray><gray>Links</gray> <white>»</white> <gray><u><click:open_url:'https://github.com/szymon-off/vMessage/'>ⒼGitHub</click></u> <u><click:open_url:'https://modrinth.com/plugin/vmessage'>ⓂModrinth</click></u></gray>
                    """.trimIndent())
                    return@executes Command.SINGLE_SUCCESS
                }
                .then(BrigadierCommand.literalArgumentBuilder("reload")
                    .requires { it.hasPermission("vmessage.command.vmessage.reload") }
                    .executes { ctx ->
                        VMessage.get().reloadVMessage()
                        ctx.source.sendRichMessage("""
                            <dark_gray>▎</dark_gray><#00ffff>vMessage</#00ffff> <white>»</white> <gray>Reloaded</gray>
                        """.trimIndent())
                        return@executes Command.SINGLE_SUCCESS
                    }
                )
                .then(BrigadierCommand.literalArgumentBuilder("fake")
                    .requires { it.hasPermission("vmessage.command.vmessage.fake") }
                    .then(BrigadierCommand.requiredArgumentBuilder("type", StringArgumentType.word())
                        .then(BrigadierCommand.requiredArgumentBuilder("player", StringArgumentType.word())
                            .executes { ctx ->
                                @Suppress("DuplicatedCode")
                                val playerString = ctx.getArgument("player", String::class.java).lowercase()
                                val player = VMessage.get().proxy.allPlayers.find { it.username.lowercase() == playerString } ?: run {
                                    ctx.source.sendRichMessage("<dark_gray>▎</dark_gray><gray>Invalid argument provided for '<#00ffff>player</#00ffff>'</gray>")
                                    return@executes Command.SINGLE_SUCCESS // handled properly
                                }

                                val type = ctx.getArgument("type", String::class.java).lowercase()
                                val handler = HandlerManager.get().getHandler(type) ?: run {
                                    ctx.source.sendRichMessage("<dark_gray>▎</dark_gray><gray>Invalid argument provided for '<#00ffff>type</#00ffff>'</gray>")
                                    return@executes Command.SINGLE_SUCCESS // handled properly
                                }

                                handler.broadcast(player)
                                ctx.source.sendRichMessage("<dark_gray>▎</dark_gray><gray>Sent fake '<#00ffff>$type</#00ffff>' message as</gray> <#00ffff>${player.username}</#00ffff>")
                                return@executes Command.SINGLE_SUCCESS
                            }
                            .then(BrigadierCommand.requiredArgumentBuilder("message", StringArgumentType.greedyString())
                                .executes { ctx ->
                                    val type = ctx.getArgument("type", String::class.java).lowercase()
                                    if (type != "chat") {
                                        ctx.source.sendRichMessage("<dark_gray>▎</dark_gray><gray>Too many arguments provided for '<#00ffff>$type</#00ffff>'</gray>")
                                        return@executes Command.SINGLE_SUCCESS // handled properly
                                    }

                                    val playerString = ctx.getArgument("player", String::class.java).lowercase()
                                    val player = VMessage.get().proxy.allPlayers.find { it.username == playerString } ?: run {
                                        ctx.source.sendRichMessage("<dark_gray>▎</dark_gray><gray>Invalid argument provided for '<#00ffff>player</#00ffff>'</gray>")
                                        return@executes Command.SINGLE_SUCCESS // handled properly
                                    }

                                    val message = ctx.getArgument("message", String::class.java).lowercase()

                                    val handler = HandlerManager.get().getHandler(ChatHandler::class.java) ?: run {
                                        ctx.source.sendRichMessage("<dark_gray>▎</dark_gray><gray>Invalid argument provided for '<#00ffff>type</#00ffff>'</gray>")
                                        return@executes Command.SINGLE_SUCCESS // handled properly
                                    }

                                    handler.broadcast(player, message)
                                    ctx.source.sendRichMessage("<dark_gray>▎</dark_gray><gray>Sent fake '<#00ffff>chat</#00ffff>' message as</gray> <#00ffff>${player.username}</#00ffff>")

                                    return@executes Command.SINGLE_SUCCESS
                                }
                            )
                            .suggests { _, builder ->
                                VMessage.get().proxy.allPlayers.forEach { builder.suggest(it.username) }
                                return@suggests builder.buildFuture()
                            }
                        )
                        .suggests { _, builder ->
                            val types = HandlerManager.get().defaultHandlers.keys.toMutableSet()
                            types.remove("chat")
                            types.forEach { builder.suggest(it) }
                            return@suggests builder.buildFuture()
                        }
                    )
                )
                .then(BrigadierCommand.literalArgumentBuilder("help")
                    .requires { it.hasPermission("vmessage.command.vmessage.help") }
                    .executes { ctx ->
                        ctx.source.sendRichMessage("""
                            <dark_gray>▎</dark_gray><b><#00ffff>vMessage</#00ffff></b> <white>»</white> <b><gray>Help</gray></b>
                            <dark_gray>▎</dark_gray><gray>/vmessage <white>»</white> Show plugin information</gray>
                            <dark_gray>▎</dark_gray><gray>/vmessage reload <white>»</white> Reload from config</gray>
                            <dark_gray>▎</dark_gray><gray>/vmessage fake <type> <player> [message] <white>»</white> Send a fake message</gray>
                            <dark_gray>▎</dark_gray><gray>/vmessage help <white>»</white> Show this help message</gray>
                        """.trimIndent())
                        return@executes Command.SINGLE_SUCCESS
                    }
                )
                .build()
        )
    }

}