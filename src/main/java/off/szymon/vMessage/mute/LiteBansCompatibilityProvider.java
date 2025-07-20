package off.szymon.vMessage.mute;

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