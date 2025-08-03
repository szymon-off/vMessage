package off.szymon.vMessage.cmd;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import off.szymon.vMessage.VMessagePlugin;

public class BroadcastCommand {

    public BrigadierCommand createCommand() {
        return new BrigadierCommand(
                LiteralArgumentBuilder.<CommandSource>literal("broadcast")
                        .requires(src -> src.hasPermission("vmessage.command.broadcast"))
                        .then(RequiredArgumentBuilder.<CommandSource, String>argument("message", StringArgumentType.greedyString())
                                .executes(ctx -> {
                                    String message = StringArgumentType.getString(ctx, "message");
                                    VMessagePlugin.getInstance().getBroadcaster().broadcast(message);
                                    return 1; // Command executed successfully
                                })
                        )
        );
    }

}
