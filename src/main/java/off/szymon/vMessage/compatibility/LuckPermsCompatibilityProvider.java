package off.szymon.vMessage.compatibility;

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
