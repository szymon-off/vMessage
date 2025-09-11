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

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.minimessage.MiniMessage;
import off.szymon.vMessage.VMessagePlugin;
import off.szymon.vMessage.compatibility.LuckPermsCompatibilityProvider;
import off.szymon.vMessage.config.ConfigManager;

public class MessageCommand {

    private final LuckPermsCompatibilityProvider lp;

    public MessageCommand() {
        lp = VMessagePlugin.get().getLuckPermsCompatibilityProvider();
    }

    public BrigadierCommand createCommand() {
        return new BrigadierCommand(
                LiteralArgumentBuilder.<CommandSource>literal("message")
                        .requires(src -> src.hasPermission("vmessage.command.message"))
                        .then(RequiredArgumentBuilder.<CommandSource, String>argument("player", StringArgumentType.word())
                                .suggests(
                                        (ctx, builder) -> {
                                            for (Player player : VMessagePlugin.get().getServer().getAllPlayers()) {
                                                builder.suggest(player.getUsername());
                                            }
                                            return builder.buildFuture();
                                        }
                                )
                                .then(RequiredArgumentBuilder.<CommandSource, String>argument("message", StringArgumentType.greedyString())
                                        .executes(ctx -> {
                                            String senderFormat = ConfigManager.get().getConfig().getCommands().getMessage().getFormat().getSender();
                                            String receiverFormat = ConfigManager.get().getConfig().getCommands().getMessage().getFormat().getReceiver();
                                            CommandSource sender = ctx.getSource();
                                            Player senderPlayer = ctx.getSource() instanceof Player ? (Player) ctx.getSource() : null;
                                            Player receiver;
                                            try {
                                                receiver = VMessagePlugin.get().getServer().getPlayer(StringArgumentType.getString(ctx, "player")).orElseThrow();
                                            } catch (Exception e) {
                                                sender.sendRichMessage("<red>Player not found!");
                                                return Command.SINGLE_SUCCESS;
                                            }
                                            if (senderPlayer != null && senderPlayer.getUniqueId().equals(receiver.getUniqueId())) {
                                                sender.sendRichMessage("<red>You cannot message yourself!");
                                                return Command.SINGLE_SUCCESS;
                                            }
                                            String message = StringArgumentType.getString(ctx, "message");
                                            if (!ConfigManager.get().getConfig().getCommands().getMessage().getAllowMiniMessage()) {
                                                message = MiniMessage.miniMessage().escapeTags(message);
                                            }

                                            /* Placeholders */

                                            // Player names (sender and receiver), servers (sender and receiver), message
                                            if (senderPlayer != null) {
                                                String senderServer = senderPlayer.getCurrentServer()
                                                        .map(server -> VMessagePlugin.get().getBroadcaster().parseAlias(server.getServerInfo().getName()))
                                                        .orElse("Unknown");
                                                String receiverServer = receiver.getCurrentServer()
                                                        .map(server -> VMessagePlugin.get().getBroadcaster().parseAlias(server.getServerInfo().getName()))
                                                        .orElse("Unknown");
                                                senderFormat = senderFormat
                                                        .replace("%sender%", senderPlayer.getUsername())
                                                        .replace("%sender-server%", senderServer)
                                                        .replace("%receiver-server%", receiverServer);
                                                receiverFormat = receiverFormat
                                                        .replace("%sender%", senderPlayer.getUsername())
                                                        .replace("%sender-server%", senderServer)
                                                        .replace("%receiver-server%", receiverServer);
                                            } else {
                                                senderFormat = senderFormat
                                                        .replace("%sender%", "Console")
                                                        .replace("%sender-server%", "Console");
                                                receiverFormat = receiverFormat
                                                        .replace("%sender%", "Console")
                                                        .replace("%sender-server%", "Console");
                                            }
                                            senderFormat = senderFormat
                                                    .replace("%receiver%",receiver.getUsername())
                                                    .replace("%message%",message);
                                            receiverFormat = receiverFormat
                                                    .replace("%receiver%",receiver.getUsername())
                                                    .replace("%message%",message);
                                            // Sender and receiver prefixes and suffixes (if LuckPerms is installed)
                                            if (lp != null) {
                                                if (senderPlayer != null) {
                                                    LuckPermsCompatibilityProvider.PlayerData senderData = lp.getMetaData(senderPlayer);
                                                    senderFormat = senderFormat
                                                            .replace("%sender-prefix%", senderData.metaData().getPrefix() != null ? senderData.metaData().getPrefix() : "")
                                                            .replace("%sender-suffix%", senderData.metaData().getSuffix() != null ? senderData.metaData().getSuffix() : "");
                                                    for (var entry : VMessagePlugin.get().getBroadcaster().getMetaPlaceholders().entrySet()) {
                                                        senderFormat = senderFormat.replace(
                                                                entry.getKey(),
                                                                senderData.metaData().getMetaValue(entry.getValue()) != null ? senderData.metaData().getMetaValue(entry.getValue()) : ""
                                                        );
                                                    }
                                                } else {
                                                    senderFormat = senderFormat
                                                            .replace("%sender-prefix%", "")
                                                            .replace("%sender-suffix%", "");
                                                }
                                                LuckPermsCompatibilityProvider.PlayerData receiverData = lp.getMetaData(receiver);
                                                senderFormat = senderFormat
                                                        .replace("%receiver-prefix%", receiverData.metaData().getPrefix() != null ? receiverData.metaData().getPrefix() : "")
                                                        .replace("%receiver-suffix%", receiverData.metaData().getSuffix() != null ? receiverData.metaData().getSuffix() : "");
                                                receiverFormat = receiverFormat
                                                        .replace("%receiver-prefix%", receiverData.metaData().getPrefix() != null ? receiverData.metaData().getPrefix() : "")
                                                        .replace("%receiver-suffix%", receiverData.metaData().getSuffix() != null ? receiverData.metaData().getSuffix() : "");
                                                for (var entry : VMessagePlugin.get().getBroadcaster().getMetaPlaceholders().entrySet()) {
                                                    senderFormat = senderFormat.replace(
                                                            entry.getKey(),
                                                            receiverData.metaData().getMetaValue(entry.getValue()) != null ? receiverData.metaData().getMetaValue(entry.getValue()) : ""
                                                    );
                                                    receiverFormat = receiverFormat.replace(
                                                            entry.getKey(),
                                                            receiverData.metaData().getMetaValue(entry.getValue()) != null ? receiverData.metaData().getMetaValue(entry.getValue()) : ""
                                                    );
                                                }
                                            } else {
                                                senderFormat = senderFormat
                                                        .replace("%sender-prefix%", "")
                                                        .replace("%sender-suffix%", "")
                                                        .replace("%receiver-prefix%", "")
                                                        .replace("%receiver-suffix%", "");
                                            }

                                            sender.sendRichMessage(senderFormat);
                                            receiver.sendRichMessage(receiverFormat);

                                            if (senderPlayer != null) {
                                                ReplyCommand.messageSent(senderPlayer.getUniqueId(), receiver.getUniqueId());
                                            }
                                            return Command.SINGLE_SUCCESS;
                                        })
                                )
                        )
        );
    }

}
