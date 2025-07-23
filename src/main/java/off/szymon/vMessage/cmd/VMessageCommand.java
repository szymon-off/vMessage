package off.szymon.vMessage.cmd;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.minimessage.MiniMessage;
import off.szymon.vMessage.Config;
import off.szymon.vMessage.VMessagePlugin;

import java.util.Optional;

public class VMessageCommand {

    public BrigadierCommand createCommand() {
        return new BrigadierCommand(
                LiteralArgumentBuilder.<CommandSource>literal("vmessage")
                        .requires(src -> src.hasPermission("vmessage.command"))
                        .executes(ctx -> {
                            ctx.getSource().sendMessage(MiniMessage.miniMessage().deserialize("""
                        <#00ffff>vMessage</#00ffff> by <#00ffff>SzymON_OFF</#00ffff>
                        Version: <#00ffff>%s</#00ffff>
                        """.formatted(VMessagePlugin.getInstance().getPlugin().getDescription().getVersion().get())));
                            return 1;
                        })

                        // /vmessage help
                        .then(LiteralArgumentBuilder.<CommandSource>literal("help")
                                .requires(src -> src.hasPermission("vmessage.command.help"))
                                .executes(ctx -> {
                                    ctx.getSource().sendMessage(MiniMessage.miniMessage().deserialize("""
                            <#00ffff>vMessage</#00ffff> Help:
                            <#00ffff>/vmessage say <player> <message></#00ffff> - Sends a message as a player
                            <#00ffff>/vmessage reload</#00ffff> - Reload the config
                            <#00ffff>/vmessage help</#00ffff> - Show this help message
                            """));
                                    return 1;
                                })
                        )

                        // /vmessage reload
                        .then(LiteralArgumentBuilder.<CommandSource>literal("reload")
                                .requires(src -> src.hasPermission("vmessage.command.reload"))
                                .executes(ctx -> {
                                    Config.reload();
                                    VMessagePlugin.getInstance().getBroadcaster().reload();
                                    ctx.getSource().sendMessage(MiniMessage.miniMessage().deserialize("<#00ffff>vMessage</#00ffff> config reloaded!"));
                                    return 1;
                                })
                        )

                        // /vmessage say <player> <message...>
                        .then(LiteralArgumentBuilder.<CommandSource>literal("say")
                                .requires(src -> src.hasPermission("vmessage.command.say"))
                                .then(RequiredArgumentBuilder.<CommandSource, String>argument("player", StringArgumentType.word())
                                        .suggests((ctx, builder) -> {
                                            VMessagePlugin.getInstance().getServer().getAllPlayers().stream()
                                                    .map(Player::getUsername)
                                                    .filter(name -> name.toLowerCase().startsWith(builder.getRemainingLowerCase()))
                                                    .forEach(builder::suggest);
                                            return builder.buildFuture();
                                        })
                                        .then(RequiredArgumentBuilder.<CommandSource, String>argument("message", StringArgumentType.greedyString())
                                                .executes(ctx -> {
                                                    String playerName = StringArgumentType.getString(ctx, "player");
                                                    String message = StringArgumentType.getString(ctx, "message");
                                                    Optional<Player> target = VMessagePlugin.getInstance().getServer().getPlayer(playerName);
                                                    if (target.isEmpty()) {
                                                        ctx.getSource().sendMessage(MiniMessage.miniMessage().deserialize("<red>Player not found."));
                                                        return 1;
                                                    }

                                                    VMessagePlugin.getInstance().getBroadcaster().message(target.get(), message);
                                                    return 1;
                                                })
                                        )
                                )
                        )
        );
    }
}

