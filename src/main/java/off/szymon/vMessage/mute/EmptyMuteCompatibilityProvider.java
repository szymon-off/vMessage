package off.szymon.vMessage.mute;

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
