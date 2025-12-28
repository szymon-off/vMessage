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

package off.szymon.vmessage.compatibility;

import com.velocitypowered.api.proxy.Player;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.cacheddata.CachedMetaData;

public class LuckPermsCompatibilityProvider {

    private final LuckPerms lp;

    public LuckPermsCompatibilityProvider() {
        lp = LuckPermsProvider.get();
    }

    public PlayerData getMetaData(Player player) {
        return new PlayerData(lp.getPlayerAdapter(Player.class).getMetaData(player));
    }

    public record PlayerData(CachedMetaData metaData) {}
}
