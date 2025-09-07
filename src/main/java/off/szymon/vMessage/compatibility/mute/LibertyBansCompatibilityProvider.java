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

package off.szymon.vMessage.compatibility.mute;

import com.velocitypowered.api.proxy.Player;
import off.szymon.vMessage.VMessagePlugin;
import space.arim.libertybans.api.*;
import space.arim.libertybans.api.punish.Punishment;
import space.arim.omnibus.Omnibus;
import space.arim.omnibus.OmnibusProvider;

import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class LibertyBansCompatibilityProvider implements MutePluginCompatibilityProvider {

    private final LibertyBans lb;

    public LibertyBansCompatibilityProvider() {
        Omnibus omnibus = OmnibusProvider.getOmnibus();
        lb = omnibus.getRegistry()
                .getProvider(LibertyBans.class)
                .orElseThrow();
    }

    @Override
    public CompletableFuture<Boolean> isMuted(Player player) {
        return getPunishment(player).thenApply(punishmentOpt -> {
            Punishment punishment = punishmentOpt.orElse(null);
            return punishment != null && !punishment.isExpired();
        });
    }

    @Override
    public CompletableFuture<Mute> getMute(Player player) {
        return getPunishment(player).thenApply(punishmentOpt -> {
            Punishment punishment = punishmentOpt.orElse(null);
            if (punishment == null) return null;

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
        });
    }

    private CompletableFuture<Optional<Punishment>> getPunishment(Player player) {
        return lb.getSelector()
                .selectionByApplicabilityBuilder(player.getUniqueId(), player.getRemoteAddress().getAddress())
                .type(PunishmentType.MUTE)
                .build()
                .getFirstSpecificPunishment()
                .toCompletableFuture();
    }

}
