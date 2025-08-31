package off.szymon.vMessage.cmd;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.minimessage.MiniMessage;
import off.szymon.vMessage.Config;
import off.szymon.vMessage.VMessagePlugin;

public class MessageCommand {

    public BrigadierCommand createCommand() {
        return new BrigadierCommand(
                LiteralArgumentBuilder.<CommandSource>literal("message")
                        .requires(src -> src.hasPermission("vmessage.command.message"))
                        .then(RequiredArgumentBuilder.<CommandSource, String>argument("player", StringArgumentType.word())
                                .then(RequiredArgumentBuilder.<CommandSource, String>argument("message", StringArgumentType.greedyString())
                                        .executes(ctx -> {
                                            String senderFormat = Config.getString("commands.message.format.sender");
                                            String receiverFormat = Config.getString("commands.message.format.receiver");
                                            CommandSource sender = ctx.getSource();
                                            Player senderPlayer = ctx.getSource() instanceof Player ? (Player) ctx.getSource() : null;
                                            Player receiver;
                                            try {
                                                receiver = VMessagePlugin.getInstance().getServer().getPlayer(StringArgumentType.getString(ctx, "player")).orElseThrow();
                                            } catch (Exception e) {
                                                sender.sendRichMessage("<red>Player not found!");
                                                return Command.SINGLE_SUCCESS;
                                            }
                                            if (senderPlayer != null && senderPlayer.getUniqueId().equals(receiver.getUniqueId())) {
                                                sender.sendRichMessage("<red>You cannot message yourself!");
                                                return Command.SINGLE_SUCCESS;
                                            }
                                            String message = StringArgumentType.getString(ctx, "message");
                                            if (!Config.getYaml().getBoolean("commands.message.allow-minimessage")) {
                                                message = MiniMessage.miniMessage().escapeTags(message);
                                            }
                                            if (senderPlayer != null) {
                                                String serverName = senderPlayer.getCurrentServer()
                                                        .map(server -> VMessagePlugin.getInstance().getBroadcaster().parseAlias(server.getServerInfo().getName()))
                                                        .orElse("Unknown");
                                                senderFormat = senderFormat
                                                        .replace("%sender%", senderPlayer.getUsername())
                                                        .replace("%sender-server%", serverName);
                                                receiverFormat = receiverFormat
                                                        .replace("%sender%", senderPlayer.getUsername())
                                                        .replace("%sender-server%", serverName);
                                            } else {
                                                senderFormat = senderFormat
                                                        .replace("%sender%", "Console")
                                                        .replace("%sender-server%", "Console");
                                                receiverFormat = receiverFormat
                                                        .replace("%sender%", "Console")
                                                        .replace("%sender-server%", "Console");
                                            }
                                            senderFormat = senderFormat
                                                    .replace("%receiver%",receiver.getUsername())
                                                    .replace("%message%",message);
                                            receiverFormat = receiverFormat
                                                    .replace("%receiver%",receiver.getUsername())
                                                    .replace("%message%",message);
                                            sender.sendRichMessage(senderFormat);
                                            receiver.sendRichMessage(receiverFormat);
                                            return Command.SINGLE_SUCCESS;
                                        })
                                )
                        )
        );
    }

}
