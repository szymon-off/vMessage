package off.szymon.vMessage;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class VMessageCommand implements SimpleCommand {

    @Override
    public void execute(Invocation invocation) {
        String[] args = invocation.arguments();

        if (args.length == 0) {
            invocation.source().sendRichMessage("""
                    <#00ffff>vMessage</#00ffff> by <#00ffff>SzymON_OFF</#00ffff>
                    Version: <#00ffff>%s</#00ffff>
                    """.formatted(VMessagePlugin.getInstance().getPlugin().getDescription().getVersion().get()));
            return;
        }

        switch (args[0].toLowerCase()) {
            case "help" -> invocation.source().sendRichMessage("""
                    <#00ffff>vMessage</#00ffff> Help:
                    <#00ffff>/vmessage say <player> <message></#00ffff> - Sends a message as a player
                    <#00ffff>/vmessage reload</#00ffff> - Reload the config
                    <#00ffff>/vmessage help</#00ffff> - Show this help message
                    """);
            case "reload" -> {
                if (invocation.source().hasPermission("vmessage.command.reload")) {
                    Config.reload();
                    VMessagePlugin.getInstance().getBroadcaster().reload();
                    invocation.source().sendRichMessage("<#00ffff>vMessage</#00ffff> config reloaded!");
                } else {
                    invocation.source().sendRichMessage("<red>You don't have permission to use this command.");
                }
            }
            case "say" -> {
                if (args.length < 3) {
                    invocation.source().sendRichMessage("<red>Invalid options:</red> Use <#00ffff>/vmessage help</#00ffff> for more information.");
                    return;
                }
                Player player = VMessagePlugin.getInstance().getServer().getPlayer(args[1]).orElse(null);
                if (player == null) {
                    invocation.source().sendRichMessage("<red>Player not found.");
                    return;
                }
                String message = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
                VMessagePlugin.getInstance().getBroadcaster().message(player, message);
            }
            default -> invocation.source().sendRichMessage("<red>Invalid options:</red> Use <#00ffff>/vmessage help</#00ffff> for more information.");
        }
    }

    @Override
    public boolean hasPermission(final Invocation invocation) {
        return invocation.source().hasPermission("vmessage.command");
    }

    @Override
    public CompletableFuture<List<String>> suggestAsync(final Invocation invocation) {
        String[] args = invocation.arguments();
        List<String> options = List.of("help", "reload", "say");

        if (args.length == 1) {
            return CompletableFuture.completedFuture(options.stream()
                    .filter(option -> option.toLowerCase().startsWith(args[0].toLowerCase()))
                    .sorted()
                    .collect(Collectors.toList()));
        } else if (args.length == 0) {
            return CompletableFuture.completedFuture(options);
        } else if (args.length == 2 && args[0].equalsIgnoreCase("say")) {
            return CompletableFuture.completedFuture(VMessagePlugin.getInstance().getServer().getAllPlayers().stream()
                    .map(Player::getUsername)
                    .filter(player -> player.toLowerCase().startsWith(args[1].toLowerCase()))
                    .sorted()
                    .collect(Collectors.toList()));
        }

        return CompletableFuture.completedFuture(List.of());
    }
}
