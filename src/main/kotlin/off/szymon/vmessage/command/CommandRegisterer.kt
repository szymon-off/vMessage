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

class CommandRegisterer {

    private val vMessage = VMessage.get()

    init {
        registerCommand(VMessageCommand())
        registerCommand(MessageCommand())
        registerCommand(ReplyCommand())
    }

    fun registerCommand(command: PluginCommand) {
        // TODO check if enabled in config
        val cmdManager = vMessage.proxy.commandManager
        cmdManager.register(
            cmdManager.metaBuilder(command.name)
                .plugin(vMessage)
                .aliases(*command.aliases)
                .build(),
            command.createCommand()
        )
    }

}