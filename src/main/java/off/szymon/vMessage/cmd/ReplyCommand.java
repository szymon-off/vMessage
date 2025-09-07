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
import off.szymon.vMessage.Config;
import off.szymon.vMessage.VMessagePlugin;
import off.szymon.vMessage.compatibility.LuckPermsCompatibilityProvider;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class ReplyCommand {

    private static final Map<UUID, UUID> repliers = new HashMap<>();

    private final LuckPermsCompatibilityProvider lp;

    public ReplyCommand() {
        this.lp = VMessagePlugin.getInstance().getLuckPermsCompatibilityProvider();
    }

    public BrigadierCommand createCommand() {
        return new BrigadierCommand(
                LiteralArgumentBuilder.<CommandSource>literal("reply")
                        .requires(src -> src.hasPermission("vmessage.command.reply"))
                        .then(RequiredArgumentBuilder.<CommandSource, String>argument("message", StringArgumentType.greedyString())
                                .executes( ctx -> {
                                    String senderFormat = Config.getString("commands.message.format.sender");
                                    String receiverFormat = Config.getString("commands.message.format.receiver");
                                    CommandSource sender = ctx.getSource();
                                    Player senderPlayer;
                                    if (ctx.getSource() instanceof Player) {
                                        senderPlayer = (Player) sender;
                                    } else {
                                        sender.sendRichMessage("<red>This command can only be executed by a player.");
                                        return Command.SINGLE_SUCCESS;
                                    }
                                    Player receiver;
                                    try {
                                        receiver = VMessagePlugin.getInstance().getServer().getPlayer(Optional.ofNullable(repliers.get(senderPlayer.getUniqueId())).orElseThrow()).orElseThrow();
                                    } catch (Exception e) {
                                        ctx.getSource().sendRichMessage("<red>You have no one to reply to.");
                                        return Command.SINGLE_SUCCESS;
                                    }
                                    String message = StringArgumentType.getString(ctx, "message");
                                    if (!Config.getYaml().getBoolean("commands.message.allow-minimessage")) {
                                        message = MiniMessage.miniMessage().escapeTags(message);
                                    }

                                    /* Placeholders */

                                    // Player names (sender and receiver), servers (sender and receiver), message
                                    String senderServer = senderPlayer.getCurrentServer()
                                            .map(server -> VMessagePlugin.getInstance().getBroadcaster().parseAlias(server.getServerInfo().getName()))
                                            .orElse("Unknown");
                                    String receiverServer = receiver.getCurrentServer()
                                            .map(server -> VMessagePlugin.getInstance().getBroadcaster().parseAlias(server.getServerInfo().getName()))
                                            .orElse("Unknown");
                                    senderFormat = senderFormat
                                            .replace("%sender%", senderPlayer.getUsername())
                                            .replace("%sender-server%", senderServer)
                                            .replace("%receiver-server%", receiverServer);
                                    receiverFormat = receiverFormat
                                            .replace("%sender%", senderPlayer.getUsername())
                                            .replace("%sender-server%", senderServer)
                                            .replace("%receiver-server%", receiverServer);
                                    senderFormat = senderFormat
                                            .replace("%receiver%",receiver.getUsername())
                                            .replace("%message%",message);
                                    receiverFormat = receiverFormat
                                            .replace("%receiver%",receiver.getUsername())
                                            .replace("%message%",message);
                                    // Sender and receiver prefixes and suffixes (if LuckPerms is installed)
                                    if (lp != null) {
                                        LuckPermsCompatibilityProvider.PlayerData senderData = lp.getMetaData(senderPlayer);
                                        senderFormat = senderFormat
                                                .replace("%sender-prefix%", senderData.metaData().getPrefix() != null ? senderData.metaData().getPrefix() : "")
                                                .replace("%sender-suffix%", senderData.metaData().getSuffix() != null ? senderData.metaData().getSuffix() : "");
                                        for (var entry : VMessagePlugin.getInstance().getBroadcaster().getMetaPlaceholders().entrySet()) {
                                            senderFormat = senderFormat.replace(
                                                    entry.getKey(),
                                                    senderData.metaData().getMetaValue(entry.getValue()) != null ? senderData.metaData().getMetaValue(entry.getValue()) : ""
                                            );
                                        }
                                        LuckPermsCompatibilityProvider.PlayerData receiverData = lp.getMetaData(receiver);
                                        senderFormat = senderFormat
                                                .replace("%receiver-prefix%", receiverData.metaData().getPrefix() != null ? receiverData.metaData().getPrefix() : "")
                                                .replace("%receiver-suffix%", receiverData.metaData().getSuffix() != null ? receiverData.metaData().getSuffix() : "");
                                        receiverFormat = receiverFormat
                                                .replace("%receiver-prefix%", receiverData.metaData().getPrefix() != null ? receiverData.metaData().getPrefix() : "")
                                                .replace("%receiver-suffix%", receiverData.metaData().getSuffix() != null ? receiverData.metaData().getSuffix() : "");
                                        for (var entry : VMessagePlugin.getInstance().getBroadcaster().getMetaPlaceholders().entrySet()) {
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

                                    messageSent(senderPlayer.getUniqueId(), receiver.getUniqueId());

                                    return Command.SINGLE_SUCCESS;
                                })
                        )
                        .build()
        );
    }

    public static void messageSent(UUID sender, UUID receiver) {
        repliers.put(receiver, sender);
    }

}
