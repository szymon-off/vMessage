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

import java.util.concurrent.CompletableFuture;

public class EmptyMuteCompatibilityProvider implements MutePluginCompatibilityProvider {

    @Override
    public CompletableFuture<Boolean> isMuted(Player player) {
        return CompletableFuture.completedFuture(false);
    }

    @Override
    public CompletableFuture<Mute> getMute(Player player) {
        return CompletableFuture.completedFuture(null);
    }

}
