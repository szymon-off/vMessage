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

package off.szymon.vmessage.cmd;

import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.permission.Tristate;
import com.velocitypowered.api.proxy.Player;
import off.szymon.vmessage.VMessagePlugin;
import off.szymon.vmessage.config.ConfigManager;

public class CommandHandler {

    public static void registerCommands() {
        VMessagePlugin vMessage = VMessagePlugin.get();

        CommandManager cmdManager = vMessage.getServer().getCommandManager();
        cmdManager.register(cmdManager.metaBuilder("vmessage")
                        .plugin(vMessage)
                        .aliases("vm","vmsg")
                        .build(),
                new VMessageCommand().createCommand()
        );
        if (ConfigManager.get().getConfig().getCommands().getBroadcast().getEnabled()) {
            cmdManager.register(cmdManager.metaBuilder("broadcast")
                            .plugin(vMessage)
                            .aliases("bc", "bcast", "shout")
                            .build(),
                    new BroadcastCommand().createCommand()
            );
        }
        if (ConfigManager.get().getConfig().getCommands().getMessage().getEnabled()) {
            cmdManager.register(cmdManager.metaBuilder("message")
                            .plugin(vMessage)
                            .aliases("msg","tell","whisper","w")
                            .build(),
                    new MessageCommand().createCommand()
            );
        }
        if (ConfigManager.get().getConfig().getCommands().getMessage().getEnableReplyCommand()) {
            cmdManager.register(cmdManager.metaBuilder("reply")
                            .plugin(vMessage)
                            .aliases("r")
                            .build(),
                    new ReplyCommand().createCommand()
            );
        }
    }

    public static boolean requiresPermission(CommandSource src, String perm, boolean defaultValue) {
        if (!(src instanceof Player player)) {
            return true;
        }

        var value = player.getPermissionValue(perm);
        if (!Tristate.UNDEFINED.equals(value)) {
            return value.asBoolean();
        }
        return defaultValue;
    }

}
