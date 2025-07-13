package off.szymon.vMessage.mute;

import com.velocitypowered.api.proxy.Player;

public class EmptyMuteCompatibilityProvider implements MutePluginCompatibilityProvider {

    @Override
    public boolean isMuted(Player player) {
        return false;
    }

    @Override
    public Mute getMute(Player player) {
        return null;
    }

}
