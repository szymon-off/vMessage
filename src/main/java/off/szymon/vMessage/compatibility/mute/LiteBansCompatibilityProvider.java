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
import litebans.api.Database;
import litebans.api.Entry;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;

public class LiteBansCompatibilityProvider implements MutePluginCompatibilityProvider {

    private final Database database;

    public LiteBansCompatibilityProvider() {
        database = Database.get();
    }

    @Override
    public CompletableFuture<Boolean> isMuted(Player player) {
        return CompletableFuture.supplyAsync(() ->
                database.isPlayerMuted(
                        player.getUniqueId(),
                        player.getRemoteAddress().getAddress().toString(),
                        player.getCurrentServer().get().getServerInfo().getName()
                )
        );
    }

    @Override
    public CompletableFuture<Mute> getMute(Player player) {
        return CompletableFuture.supplyAsync(() -> {
            Entry entry = Database.get().getMute(
                    player.getUniqueId(),
                    player.getRemoteAddress().getAddress().toString(),
                    player.getCurrentServer().get().getServerInfo().getName()
            );
            String playerName = player.getUsername();
            String reason = entry.getReason() != null ? entry.getReason() : "No reason specified";
            String moderator = entry.getExecutorName() != null ? entry.getExecutorName() : "Unknown";
            Instant endDate = Instant.ofEpochSecond(entry.getDateEnd());
            return new Mute(playerName, reason, moderator, endDate);
        });
    }

}