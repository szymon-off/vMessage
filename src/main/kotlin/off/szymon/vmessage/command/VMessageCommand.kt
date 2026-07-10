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
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.velocitypowered.api.command.BrigadierCommand
import com.velocitypowered.api.command.CommandSource
import off.szymon.fishy.api.FishyAPI
import off.szymon.vmessage.VMessage

class VMessageCommand {

    fun createCommand(): BrigadierCommand {
        return BrigadierCommand(
            LiteralArgumentBuilder.literal<CommandSource>("vmessage")
//                .requires { src -> src.hasPermission("vmessage.command.vmessage") }
                .executes { ctx ->
                    val description = VMessage.get().plugin.description
                    val authors = description.authors.joinToString("</#00ffff><gray>,</gray> <#00ffff>")
                    ctx.source.sendRichMessage("""
                        <dark_gray>▎</dark_gray><bold><#00ffff>vMessage</#00ffff></bold> <white>»</white> <gray>by</gray> <#00ffff>$authors</#00ffff>
                        <dark_gray>▎</dark_gray><gray>Version</gray> <white>»</white> <#00ffff>${description.version}</#00ffff>
                        <dark_gray>▎</dark_gray><gray>Powered by</gray> <white>»</white> <#00ffff>FishyAPI</#00ffff> <gray>v${FishyAPI.VERSION}</gray>
                    """.trimIndent())
                    return@executes Command.SINGLE_SUCCESS
                }
                .build()
        )
    }

}