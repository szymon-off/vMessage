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

package off.szymon.vMessage;

import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.minimessage.MiniMessage;
import off.szymon.vMessage.compatibility.LuckPermsCompatibilityProvider;
import off.szymon.vMessage.config.ConfigManager;
import org.spongepowered.configurate.CommentedConfigurationNode;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class Broadcaster {

    private final HashMap<String,String> serverAliases; // Server name, Server alias
    private final LuckPermsCompatibilityProvider lp;
    private final HashMap<String,String> metaPlaceholders; // Placeholder, Meta key

    public Broadcaster() {
        serverAliases = new HashMap<>();
        reloadAliases();

        /* LuckPerms */
        lp = VMessagePlugin.get().getLuckPermsCompatibilityProvider();

        metaPlaceholders = new HashMap<>();
        reloadMetaPlaceholders();
    }

    public void message(Player player, String message) {
        if (!ConfigManager.get().getConfig().getMessages().getChat().getEnabled()) return;

        String msg = ConfigManager.get().getConfig().getMessages().getChat().getFormat();

        //noinspection OptionalGetWithoutIsPresent
        msg = msg
                .replace("%player%", player.getUsername())
                .replace("%message%", escapeMiniMessage(message))
                .replace("%server%", parseAlias(player.getCurrentServer().get().getServerInfo().getName()));
        if (lp != null) {
            LuckPermsCompatibilityProvider.PlayerData data = lp.getMetaData(player);
            msg = msg
                    .replace("%suffix%", Optional.ofNullable(data.metaData().getSuffix()).orElse(""))
                    .replace("%prefix%", Optional.ofNullable(data.metaData().getPrefix()).orElse(""));

            for (Map.Entry<String,String> entry : metaPlaceholders.entrySet()) {
                msg = msg.replace(
                        entry.getKey(),
                        Optional.ofNullable(data.metaData().getMetaValue(entry.getValue())).orElse("")
                );
            }
        }
        VMessagePlugin.get().getServer().sendMessage(MiniMessage.miniMessage().deserialize(msg));
    }

    public void join(Player player) {
        if (!ConfigManager.get().getConfig().getMessages().getJoin().getEnabled()) return;
        if (player.hasPermission("vmessage.silent.join")) {
            VMessagePlugin.get().getLogger().info("{} has silent join permission, not broadcasting join message", player.getUsername());
            return;
        }

        String msg = ConfigManager.get().getConfig().getMessages().getJoin().getFormat();
        //noinspection OptionalGetWithoutIsPresent
        msg = msg
                .replace("%player%", player.getUsername())
                .replace("%server%", parseAlias(player.getCurrentServer().get().getServerInfo().getName()));

        if (lp != null) {
            LuckPermsCompatibilityProvider.PlayerData data = lp.getMetaData(player);
            msg = msg
                    .replace("%suffix%", Optional.ofNullable(data.metaData().getSuffix()).orElse(""))
                    .replace("%prefix%", Optional.ofNullable(data.metaData().getPrefix()).orElse(""));

            for (Map.Entry<String,String> entry : metaPlaceholders.entrySet()) {
                msg = msg.replace(
                        entry.getKey(),
                        Optional.ofNullable(data.metaData().getMetaValue(entry.getValue())).orElse("")
                );
            }
        }
        VMessagePlugin.get().getServer().sendMessage(MiniMessage.miniMessage().deserialize(msg));
    }

    public void leave(Player player) {
        if (!ConfigManager.get().getConfig().getMessages().getLeave().getEnabled()) return;
        if (player.hasPermission("vmessage.silent.leave")) {
            VMessagePlugin.get().getLogger().info("{} has silent leave permission, not broadcasting leave message", player.getUsername());
            return;
        }

        String msg = ConfigManager.get().getConfig().getMessages().getLeave().getFormat();
        String serverName = player.getCurrentServer()
                .map(server -> server.getServerInfo().getName())
                .map(this::parseAlias)
                .orElse(null);

        if (serverName == null) {
            return; // invalid server connection, do not send leave message
        }

        msg = msg
                .replace("%player%", player.getUsername())
                .replace("%server%", serverName);

        if (lp != null) {
            LuckPermsCompatibilityProvider.PlayerData data = lp.getMetaData(player);
            msg = msg
                    .replace("%suffix%", Optional.ofNullable(data.metaData().getSuffix()).orElse(""))
                    .replace("%prefix%", Optional.ofNullable(data.metaData().getPrefix()).orElse(""));

            for (Map.Entry<String,String> entry : metaPlaceholders.entrySet()) {
                msg = msg.replace(
                        entry.getKey(),
                        Optional.ofNullable(data.metaData().getMetaValue(entry.getValue())).orElse("")
                );
            }
        }
        VMessagePlugin.get().getServer().sendMessage(MiniMessage.miniMessage().deserialize(msg));
    }

    public void change(Player player, String oldServer) {
        if (!ConfigManager.get().getConfig().getMessages().getChange().getEnabled()) return;
        if (player.hasPermission("vmessage.silent.change")) {
            VMessagePlugin.get().getLogger().info("{} has silent change permission, not broadcasting change message", player.getUsername());
            return;
        }

        String msg = ConfigManager.get().getConfig().getMessages().getChange().getFormat();
        //noinspection OptionalGetWithoutIsPresent
        msg = msg
                .replace("%player%", player.getUsername())
                .replace("%new_server%", parseAlias(player.getCurrentServer().get().getServerInfo().getName()))
                .replace("%old_server%", parseAlias(oldServer));

        if (lp != null) {
            LuckPermsCompatibilityProvider.PlayerData data = lp.getMetaData(player);

            msg = msg
                    .replace("%suffix%", Optional.ofNullable(data.metaData().getSuffix()).orElse(""))
                    .replace("%prefix%", Optional.ofNullable(data.metaData().getPrefix()).orElse(""));

            for (Map.Entry<String,String> entry : metaPlaceholders.entrySet()) {
                msg = msg.replace(
                        entry.getKey(),
                        Optional.ofNullable(data.metaData().getMetaValue(entry.getValue())).orElse("")
                );
            }
        }
        VMessagePlugin.get().getServer().sendMessage(MiniMessage.miniMessage().deserialize(msg));
    }

    public void reload() {
        reloadAliases();
        reloadMetaPlaceholders();
    }

    public void reloadAliases() {
        serverAliases.clear();
        Set<Map.Entry<Object, CommentedConfigurationNode>> aliases = ConfigManager.get().getNode("server-aliases").childrenMap().entrySet();
        for (Map.Entry<Object, CommentedConfigurationNode> entry : aliases) {
            serverAliases.put(entry.getKey().toString(),entry.getValue().toString());
        }
    }

    public void reloadMetaPlaceholders() {
        metaPlaceholders.clear();
        if (lp != null) {
            Set<Map.Entry<Object, CommentedConfigurationNode>> metas = ConfigManager.get().getNode("luckPermsMeta").childrenMap().entrySet();
            for (Map.Entry<Object, CommentedConfigurationNode> entry : metas) {
                metaPlaceholders.put("&"+entry.getKey().toString()+"&",entry.getValue().toString());
            }
        }
    }

    public void broadcast(String message) {
        String msg = ConfigManager.get().getConfig().getCommands().getBroadcast().getFormat();
        msg = msg.replace("%message%", message);

        VMessagePlugin.get().getServer().sendMessage(MiniMessage.miniMessage().deserialize(msg));
    }

    public String parseAlias(String serverName) {
        String output;
        for (Map.Entry<String,String> entry : serverAliases.entrySet()) {
            if (serverName.equalsIgnoreCase(entry.getKey())) {
                output = entry.getValue();
                return output;
            }
        }
        return serverName;
    }

    public HashMap<String, String> getMetaPlaceholders() {
        return metaPlaceholders;
    }

    private String escapeMiniMessage(String input) {
        return ConfigManager.get().getConfig().getMessages().getChat().getAllowMiniMessage() ? input : MiniMessage.miniMessage().escapeTags(input);
    }
}
