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

import off.szymon.vmessage.VMessage
import off.szymon.vmessage.command.message.MessageCommand
import off.szymon.vmessage.command.message.ReplyCommand
import off.szymon.vmessage.config.Config

class CommandManager {

    private val vMessage = VMessage.get()

    init {
        registerCommand(VMessageCommand())
        registerCommand(MessageCommand())
        if (Config.get().tree.commands.message.enabled) registerCommand(ReplyCommand())
        registerCommand(BroadcastCommand())
    }

    fun registerCommand(command: PluginCommand) {
        val isMainCommand = command.name == "vmessage" // exception for /vmessage
        if (!isMainCommand && !Config.get().root.node("commands", command.name, "enabled").getBoolean(false)) {
            vMessage.logger.info("Skipping '${command.name}' command...")
            return
        }
        val cmdManager = vMessage.proxy.commandManager
        vMessage.logger.info("Loading '${command.name}' command...")
        cmdManager.register(
            cmdManager.metaBuilder(command.name)
                .plugin(vMessage)
                .aliases(*command.aliases)
                .build(),
            command.createCommand()
        )
    }
}