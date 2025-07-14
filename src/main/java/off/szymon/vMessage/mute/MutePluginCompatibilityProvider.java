package off.szymon.vMessage.mute;

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
