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

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import off.szymon.vmessage.VMessagePlugin;
import off.szymon.vmessage.config.ConfigManager;

public class BroadcastCommand {

    public BrigadierCommand createCommand() {
        return new BrigadierCommand(
                LiteralArgumentBuilder.<CommandSource>literal("broadcast")
                        .requires(src -> CommandHandler.requiresPermission(src, "vmessage.command.broadcast",
                                ConfigManager.get().getConfig().getCommands().getBroadcast().getAllowByDefault()))
                        .then(RequiredArgumentBuilder.<CommandSource, String>argument("message", StringArgumentType.greedyString())
                                .executes(ctx -> {
                                    String message = StringArgumentType.getString(ctx, "message");
                                    VMessagePlugin.get().getBroadcaster().broadcast(message, ctx.getSource() instanceof Player player ? player : null);
                                    return 1; // Command executed successfully
                                }
        )));
    }

}
