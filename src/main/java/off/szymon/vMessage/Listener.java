package off.szymon.vMessage;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import com.velocitypowered.api.event.player.ServerPostConnectEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.cacheddata.CachedMetaData;
import org.jetbrains.annotations.Nullable;
import space.arim.libertybans.api.*;
import space.arim.libertybans.api.punish.Punishment;

import java.net.InetAddress;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class Listener {

    @Subscribe
    private void onMessageSend(PlayerChatEvent e) {
        //noinspection deprecation - SignedVelocity is installed
        e.setResult(PlayerChatEvent.ChatResult.denied());

        Player player = e.getPlayer();

        Punishment punishment = getMute(player.getUniqueId(), player.getRemoteAddress().getAddress());
        if (punishment != null && !punishment.isExpired()) {
            Broadcaster broadcaster = VMessagePlugin.getInstance().getBroadcaster();

            String msg = Config.getString("messages.chat.muted-message");
            String serverName = player.getCurrentServer()
                    .map(server -> broadcaster.parseAlias(server.getServerInfo().getName()))
                    .orElse("Unknown");

            String reason = punishment.getReason() != null ? punishment.getReason() : "No reason specified";
            String endDate = punishment.isExpired()
                    ? "Expired"
                    : (punishment.getEndDate() != null ? punishment.getEndDate().toString() : "Permanent");
            String moderator;
            Operator operator = punishment.getOperator();
            if (operator instanceof PlayerOperator playerOp) {
                Player p = VMessagePlugin.getInstance().getServer().getPlayer(playerOp.getUUID()).orElse(null);
                moderator = p != null ? p.getUsername() : "Unknown Player";
            } else if (operator instanceof ConsoleOperator) {
                moderator = "Console";
            } else {
                moderator = "Unknown";
            }

            msg = msg
                    .replace("%player%", player.getUsername())
                    .replace("%message%", e.getMessage())
                    .replace("%server%", serverName)
                    .replace("%reason%", reason)
                    .replace("%end-date%", endDate)
                    .replace("%moderator%", moderator);

            LuckPerms lp = VMessagePlugin.getInstance().getLuckPerms();
            if (lp != null) {
                CachedMetaData data = lp.getPlayerAdapter(Player.class).getMetaData(player);
                msg = msg
                        .replace("%suffix%", Optional.ofNullable(data.getSuffix()).orElse(""))
                        .replace("%prefix%", Optional.ofNullable(data.getPrefix()).orElse(""));

                for (Map.Entry<String, String> entry : broadcaster.getMetaPlaceholders().entrySet()) {
                    msg = msg.replace(
                            entry.getKey(),
                            Optional.ofNullable(data.getMetaValue(entry.getValue())).orElse("")
                    );
                }
            }
            player.sendMessage(MiniMessage.miniMessage().deserialize(msg));
        } else {
            VMessagePlugin.getInstance().getBroadcaster().message(e.getPlayer(), e.getMessage());
        }


    }

    @Nullable
    private Punishment getMute(UUID playerUuid, InetAddress playerAddress) {
        LibertyBans lb = VMessagePlugin.getInstance().getLibertyBans();
        if (lb == null) {
            return null; // LibertyBans is not available
        }

        Optional<Punishment> punishment = lb.getSelector()
                .selectionByApplicabilityBuilder(playerUuid, playerAddress)
                .type(PunishmentType.MUTE)
                .build()
                .getFirstSpecificPunishment()
                .toCompletableFuture() // Ensure it's a CompletableFuture
                .join(); // Block and wait for the result

        return punishment.orElse(null); // Return true if the player is muted
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
