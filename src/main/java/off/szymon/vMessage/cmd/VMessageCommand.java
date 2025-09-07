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

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.minimessage.MiniMessage;
import off.szymon.vMessage.Config;
import off.szymon.vMessage.VMessagePlugin;

import java.util.Optional;

public class VMessageCommand {

    public BrigadierCommand createCommand() {
        return new BrigadierCommand(
                LiteralArgumentBuilder.<CommandSource>literal("vmessage")
                        .requires(src -> src.hasPermission("vmessage.command"))
                        .executes(ctx -> {
                            ctx.getSource().sendMessage(MiniMessage.miniMessage().deserialize("""
                        <#00ffff>vMessage</#00ffff> by <#00ffff>%s</#00ffff>
                        Version: <#00ffff>%s</#00ffff>"""
                                    .formatted(String.join(",",VMessagePlugin.getInstance().getPlugin().getDescription().getAuthors()),VMessagePlugin.getInstance().getPlugin().getDescription().getVersion().get())));
                            return 1;
                        })

                        // /vmessage help
                        .then(LiteralArgumentBuilder.<CommandSource>literal("help")
                                .requires(src -> src.hasPermission("vmessage.command.help"))
                                .executes(ctx -> {
                                    ctx.getSource().sendMessage(MiniMessage.miniMessage().deserialize("""
                            <#00ffff>vMessage</#00ffff> Help:
                            <#00ffff>/vmessage say <player> <message></#00ffff> - Sends a message as a player
                            <#00ffff>/vmessage fake <join/leave/change> [player] [old-server]</#00ffff> - Sends a fake join/leave/change message
                            <#00ffff>/vmessage reload</#00ffff> - Reload the config
                            <#00ffff>/vmessage help</#00ffff> - Show this help message
                            <#00ffff>/vmessage</#00ffff> - Show the plugin version and author"""));
                                    return 1;
                                })
                        )

                        // /vmessage reload
                        .then(LiteralArgumentBuilder.<CommandSource>literal("reload")
                                .requires(src -> src.hasPermission("vmessage.command.reload"))
                                .executes(ctx -> {
                                    Config.reload();
                                    VMessagePlugin.getInstance().getBroadcaster().reload();
                                    ctx.getSource().sendMessage(MiniMessage.miniMessage().deserialize("<#00ffff>vMessage</#00ffff> config reloaded!"));
                                    return 1;
                                })
                        )

                        // /vmessage say <player> <message...>
                        .then(LiteralArgumentBuilder.<CommandSource>literal("say")
                                .requires(src -> src.hasPermission("vmessage.command.say"))
                                .then(RequiredArgumentBuilder.<CommandSource, String>argument("player", StringArgumentType.word())
                                        .suggests((ctx, builder) -> {
                                            VMessagePlugin.getInstance().getServer().getAllPlayers().stream()
                                                    .map(Player::getUsername)
                                                    .filter(name -> name.toLowerCase().startsWith(builder.getRemainingLowerCase()))
                                                    .forEach(builder::suggest);
                                            return builder.buildFuture();
                                        })
                                        .then(RequiredArgumentBuilder.<CommandSource, String>argument("message", StringArgumentType.greedyString())
                                                .executes(ctx -> {
                                                    String playerName = StringArgumentType.getString(ctx, "player");
                                                    String message = StringArgumentType.getString(ctx, "message");
                                                    Optional<Player> target = VMessagePlugin.getInstance().getServer().getPlayer(playerName);
                                                    if (target.isEmpty()) {
                                                        ctx.getSource().sendMessage(MiniMessage.miniMessage().deserialize("<red>Player not found."));
                                                        return 1;
                                                    }

                                                    VMessagePlugin.getInstance().getBroadcaster().message(target.get(), message);
                                                    return 1;
                                                })
                                        )
                                )
                        )

                        // /vmessage fake <join/leave/change> [player] [old-server]
                        .then(LiteralArgumentBuilder.<CommandSource>literal("fake")
                                .requires(src -> src.hasPermission("vmessage.command.fake"))
                                .then(LiteralArgumentBuilder.<CommandSource>literal("join")
                                        .requires(src -> src.hasPermission("vmessage.command.fake.join"))
                                        .executes(ctx -> {
                                            Player player = ctx.getSource() instanceof Player ? (Player) ctx.getSource() : null;
                                            if (player == null) {
                                                ctx.getSource().sendMessage(MiniMessage.miniMessage().deserialize("<red>You must be a player to use this command."));
                                                return 1;
                                            }
                                            VMessagePlugin.getInstance().getBroadcaster().join(player);
                                            ctx.getSource().sendMessage(MiniMessage.miniMessage().deserialize("<green>Fake join message sent for " + player.getUsername() + "."));
                                            return 1;
                                        })
                                        .then(RequiredArgumentBuilder.<CommandSource, String>argument("player", StringArgumentType.word())
                                                .suggests((ctx, builder) -> {
                                                    VMessagePlugin.getInstance().getServer().getAllPlayers().stream()
                                                            .map(Player::getUsername)
                                                            .filter(name -> name.toLowerCase().startsWith(builder.getRemainingLowerCase()))
                                                            .forEach(builder::suggest);
                                                    return builder.buildFuture();
                                                })
                                                .executes(ctx -> {
                                                    String playerName = StringArgumentType.getString(ctx, "player");
                                                    Optional<Player> target = VMessagePlugin.getInstance().getServer().getPlayer(playerName);
                                                    if (target.isEmpty()) {
                                                        ctx.getSource().sendMessage(MiniMessage.miniMessage().deserialize("<red>Player "+playerName+" not found."));
                                                        return 1;
                                                    }
                                                    VMessagePlugin.getInstance().getBroadcaster().join(target.get());
                                                    ctx.getSource().sendMessage(MiniMessage.miniMessage().deserialize("<green>Fake join message sent for " + target.get().getUsername() + "."));
                                                    return 1;
                                                })
                                                )
                                        )
                                .then(LiteralArgumentBuilder.<CommandSource>literal("leave")
                                        .requires(src -> src.hasPermission("vmessage.command.fake.leave"))
                                        .executes(ctx -> {
                                            Player player = ctx.getSource() instanceof Player ? (Player) ctx.getSource() : null;
                                            if (player == null) {
                                                ctx.getSource().sendMessage(MiniMessage.miniMessage().deserialize("<red>You must be a player to use this command."));
                                                return 1;
                                            }
                                            VMessagePlugin.getInstance().getBroadcaster().leave(player);
                                            ctx.getSource().sendMessage(MiniMessage.miniMessage().deserialize("<green>Fake leave message sent for " + player.getUsername() + "."));
                                            return 1;
                                        })
                                        .then(RequiredArgumentBuilder.<CommandSource, String>argument("player", StringArgumentType.word())
                                                .suggests((ctx, builder) -> {
                                                    VMessagePlugin.getInstance().getServer().getAllPlayers().stream()
                                                            .map(Player::getUsername)
                                                            .filter(name -> name.toLowerCase().startsWith(builder.getRemainingLowerCase()))
                                                            .forEach(builder::suggest);
                                                    return builder.buildFuture();
                                                })
                                                .executes(ctx -> {
                                                    String playerName = StringArgumentType.getString(ctx, "player");
                                                    Optional<Player> target = VMessagePlugin.getInstance().getServer().getPlayer(playerName);
                                                    if (target.isEmpty()) {
                                                        ctx.getSource().sendMessage(MiniMessage.miniMessage().deserialize("<red>Player "+playerName+" not found."));
                                                        return 1;
                                                    }
                                                    VMessagePlugin.getInstance().getBroadcaster().leave(target.get());
                                                    ctx.getSource().sendMessage(MiniMessage.miniMessage().deserialize("<green>Fake leave message sent for " + target.get().getUsername() + "."));
                                                    return 1;
                                                })
                                        )
                                )
                                .then(LiteralArgumentBuilder.<CommandSource>literal("change")
                                        .requires(src -> src.hasPermission("vmessage.command.fake.change"))
                                        .executes(ctx -> {
                                            Player player = ctx.getSource() instanceof Player ? (Player) ctx.getSource() : null;
                                            if (player == null) {
                                                ctx.getSource().sendMessage(MiniMessage.miniMessage().deserialize("<red>You must be a player to use this command."));
                                                return 1;
                                            }
                                            VMessagePlugin.getInstance().getBroadcaster().change(player, player.getCurrentServer().get().getServerInfo().getName());
                                            ctx.getSource().sendMessage(MiniMessage.miniMessage().deserialize("<green>Fake change message sent for " + player.getUsername() + "."));
                                            return 1;
                                        })
                                        .then(RequiredArgumentBuilder.<CommandSource, String>argument("player", StringArgumentType.word())
                                                .suggests((ctx, builder) -> {
                                                    VMessagePlugin.getInstance().getServer().getAllPlayers().stream()
                                                            .map(Player::getUsername)
                                                            .filter(name -> name.toLowerCase().startsWith(builder.getRemainingLowerCase()))
                                                            .forEach(builder::suggest);
                                                    return builder.buildFuture();
                                                })
                                                .then(RequiredArgumentBuilder.<CommandSource, String>argument("old-server", StringArgumentType.word())
                                                        .executes(ctx -> {
                                                            String playerName = StringArgumentType.getString(ctx, "player");
                                                            String oldServer = StringArgumentType.getString(ctx, "old-server");
                                                            Optional<Player> target = VMessagePlugin.getInstance().getServer().getPlayer(playerName);
                                                            if (target.isEmpty()) {
                                                                ctx.getSource().sendMessage(MiniMessage.miniMessage().deserialize("<red>Player "+playerName+" not found."));
                                                                return 1;
                                                            }
                                                            VMessagePlugin.getInstance().getBroadcaster().change(target.get(), oldServer);
                                                            ctx.getSource().sendMessage(MiniMessage.miniMessage().deserialize("<green>Fake change message sent for " + target.get().getUsername() + "."));
                                                            return 1;
                                                        })
                                                )
                                        )
                                )
                        )

        );
    }
}

