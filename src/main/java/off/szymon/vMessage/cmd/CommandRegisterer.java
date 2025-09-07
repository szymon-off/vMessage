/*
 * vMessage
 * Copyright (c) 2025.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * See the LICENSE file in the project root for details.
 */

package off.szymon.vMessage.cmd;

import com.velocitypowered.api.command.CommandManager;
import off.szymon.vMessage.Config;
import off.szymon.vMessage.VMessagePlugin;

public class CommandRegisterer {

    public static void registerCommands() {
        VMessagePlugin vMessage = VMessagePlugin.getInstance();

        CommandManager cmdManager = vMessage.getServer().getCommandManager();
        cmdManager.register(cmdManager.metaBuilder("vmessage")
                        .plugin(vMessage)
                        .aliases("vm","vmsg")
                        .build(),
                new VMessageCommand().createCommand()
        );
        if (Config.getYaml().getBoolean("commands.broadcast.enabled")) {
            cmdManager.register(cmdManager.metaBuilder("broadcast")
                            .plugin(vMessage)
                            .aliases("bc","bcast")
                            .build(),
                    new BroadcastCommand().createCommand()
            );
        }
        if (Config.getYaml().getBoolean("commands.message.enabled")) {
            cmdManager.register(cmdManager.metaBuilder("message")
                            .plugin(vMessage)
                            .aliases("msg","tell","whisper","w")
                            .build(),
                    new MessageCommand().createCommand()
            );
        }
        if (Config.getYaml().getBoolean("commands.message.enable-reply-command")) {
            cmdManager.register(cmdManager.metaBuilder("reply")
                            .plugin(vMessage)
                            .aliases("r")
                            .build(),
                    new ReplyCommand().createCommand()
            );
        }
    }

}
