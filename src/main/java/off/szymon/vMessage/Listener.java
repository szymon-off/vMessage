package off.szymon.vMessage;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import com.velocitypowered.api.event.player.ServerPostConnectEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.kyori.adventure.text.minimessage.MiniMessage;
import off.szymon.vMessage.compatibility.LuckPermsCompatibilityProvider;
import off.szymon.vMessage.compatibility.mute.MutePluginCompatibilityProvider;

import java.util.Map;
import java.util.Optional;

public class Listener {

    @Subscribe
    private void onMessageSend(PlayerChatEvent e) {
        //noinspection deprecation - SignedVelocity is installed
        e.setResult(PlayerChatEvent.ChatResult.denied());

        Player player = e.getPlayer();

        MutePluginCompatibilityProvider mpcp = VMessagePlugin.getInstance().getMutePluginCompatibilityProvider();

        mpcp.isMuted(player).thenAcceptAsync(isMuted -> {
            if (isMuted) {
                mpcp.getMute(player).thenAcceptAsync(mute -> {
                    Broadcaster broadcaster = VMessagePlugin.getInstance().getBroadcaster();

                    String msg = Config.getString("messages.chat.muted-message");
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

                    LuckPermsCompatibilityProvider lp = VMessagePlugin.getInstance().getLuckPermsCompatibilityProvider();

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
                VMessagePlugin.getInstance().getBroadcaster().message(e.getPlayer(), e.getMessage());
            }
        });
    }

    @Subscribe
    private void onPlayerLeave(DisconnectEvent e) {
        try {
            VMessagePlugin.getInstance().getBroadcaster().leave(e.getPlayer());
        } catch (Exception ex) {
            VMessagePlugin.getInstance().getLogger().error("Error while broadcasting player leave event: {}", ex.getMessage());
        }
    }

    @Subscribe
    private void onPlayerConnect(ServerPostConnectEvent e) {
        RegisteredServer pre = e.getPreviousServer();
        if (pre == null) {
            VMessagePlugin.getInstance().getBroadcaster().join(e.getPlayer());
        } else {
            VMessagePlugin.getInstance().getBroadcaster().change(e.getPlayer(),pre.getServerInfo().getName());
        }
    }

}
