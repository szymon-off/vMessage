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

package off.szymon.vmessage.compatibility.mute;

import com.velocitypowered.api.proxy.Player;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;

public interface MutePluginCompatibilityProvider {

    CompletableFuture<Boolean> isMuted(Player player);

    CompletableFuture<Mute> getMute(Player player);

    record Mute(String playerName, String reason, String moderator, Instant endDate) {

        public boolean isExpired() {
            return endDate != null && endDate.isBefore(Instant.now());
        }

        public String endDateString() {
            return isExpired()
                    ? "Expired"
                    : (endDate != null ? endDate.toString() : "Permanent");
        }

    }

}
