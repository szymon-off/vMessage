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

package off.szymon.vmessage;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import com.velocitypowered.api.event.player.ServerPostConnectEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.kyori.adventure.text.minimessage.MiniMessage;
import off.szymon.vmessage.compatibility.LuckPermsCompatibilityProvider;
import off.szymon.vmessage.compatibility.mute.MutePluginCompatibilityProvider;
import off.szymon.vmessage.config.ConfigManager;

import java.util.Map;
import java.util.Optional;

public class Listener {

    @Subscribe
    private void onMessageSend(PlayerChatEvent e) {
        //noinspection deprecation - SignedVelocity is installed
        e.setResult(PlayerChatEvent.ChatResult.denied());

        Player player = e.getPlayer();

        MutePluginCompatibilityProvider mpcp = VMessagePlugin.get().getMutePluginCompatibilityProvider();

        mpcp.isMuted(player).thenAcceptAsync(isMuted -> {
            if (isMuted) {
                mpcp.getMute(player).thenAcceptAsync(mute -> {
                    Broadcaster broadcaster = VMessagePlugin.get().getBroadcaster();

                    String msg = ConfigManager.get().getConfig().getMessages().getChat().getMutedMessage();
                    String serverName = player.getCurrentServer()
                            .map(server -> broadcaster.parseAlias(server.getServerInfo().getName()))
                            .orElse("Unknown");

                    String reason = mute.reason();
                    String endDate = mute.endDateString();
                    String moderator = mute.moderator();

                    msg = msg
                            .replace("%player%", player.getUsername())
                            .replace("%message%", e.getMessage())
                            .replace("%server%", serverName)
                            .replace("%reason%", reason)
                            .replace("%end-date%", endDate)
                            .replace("%moderator%", moderator);

                    LuckPermsCompatibilityProvider lp = VMessagePlugin.get().getLuckPermsCompatibilityProvider();

                    if (lp != null) {
                        LuckPermsCompatibilityProvider.PlayerData data = lp.getMetaData(player);
                        msg = msg
                                .replace("%suffix%", Optional.ofNullable(data.metaData().getSuffix()).orElse(""))
                                .replace("%prefix%", Optional.ofNullable(data.metaData().getPrefix()).orElse(""));

                        for (Map.Entry<String, String> entry : broadcaster.getMetaPlaceholders().entrySet()) {
                            msg = msg.replace(
                                    entry.getKey(),
                                    Optional.ofNullable(data.metaData().getMetaValue(entry.getValue())).orElse("")
                            );
                        }
                    }
                    player.sendMessage(MiniMessage.miniMessage().deserialize(msg));
                });
            } else {
                VMessagePlugin.get().getBroadcaster().message(e.getPlayer(), e.getMessage());
            }
        });
    }

    @Subscribe
    private void onPlayerLeave(DisconnectEvent e) {
        try {
            VMessagePlugin.get().getBroadcaster().leave(e.getPlayer());
        } catch (Exception ex) {
            VMessagePlugin.get().getLogger().error("Error while broadcasting player leave event: {}", ex.getMessage());
        }
    }

    @Subscribe
    private void onPlayerConnect(ServerPostConnectEvent e) {
        RegisteredServer pre = e.getPreviousServer();
        if (pre == null) {
            VMessagePlugin.get().getBroadcaster().join(e.getPlayer());
        } else {
            VMessagePlugin.get().getBroadcaster().change(e.getPlayer(),pre.getServerInfo().getName());
        }
    }

}
