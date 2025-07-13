package off.szymon.vMessage.mute;

import com.velocitypowered.api.proxy.Player;
import off.szymon.vMessage.VMessagePlugin;
import org.jetbrains.annotations.Nullable;
import space.arim.libertybans.api.*;
import space.arim.libertybans.api.punish.Punishment;
import space.arim.omnibus.Omnibus;
import space.arim.omnibus.OmnibusProvider;

import java.time.Instant;

public class LibertyBansCompatibilityProvider implements MutePluginCompatibilityProvider {

    private final LibertyBans lb;

    public LibertyBansCompatibilityProvider() {
        Omnibus omnibus = OmnibusProvider.getOmnibus();
        lb = omnibus.getRegistry()
                .getProvider(LibertyBans.class)
                .orElseThrow();
    }

    @Override
    public boolean isMuted(Player player) {
        return getPunishment(player) != null && !getPunishment(player).isExpired();
    }


    @Override
    public Mute getMute(Player player) {
        Punishment punishment = getPunishment(player);
        String playerName = player.getUsername();
        String reason = punishment.getReason() != null ? punishment.getReason() : "No reason specified";
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
        Instant endDate = punishment.getEndDate();

        return new Mute(playerName, reason, moderator, endDate);
    }

    @Nullable
    private Punishment getPunishment(Player player) {
        return lb.getSelector()
                .selectionByApplicabilityBuilder(player.getUniqueId(), player.getRemoteAddress().getAddress())
                .type(PunishmentType.MUTE)
                .build()
                .getFirstSpecificPunishment()
                .toCompletableFuture() // Ensure it's a CompletableFuture
                .join() // Block and wait for the result
                .orElse(null); // Return null if no punishment found
    }

}
