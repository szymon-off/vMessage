package off.szymon.vMessage.cmd;

import com.velocitypowered.api.command.CommandManager;
import off.szymon.vMessage.Config;
import off.szymon.vMessage.VMessagePlugin;

public class CommandRegisterer {

    public static void registerCommands() {
        VMessagePlugin vMessage = VMessagePlugin.getInstance();

        CommandManager cmdManager = vMessage.getServer().getCommandManager();
        cmdManager.register(cmdManager.metaBuilder("vmessage")
                .plugin(vMessage)
                .aliases("vm","vmsg")
                .build(),
                new VMessageCommand().createCommand()
        );
        if (Config.getYaml().getBoolean("commands.broadcast.enabled")) {
            cmdManager.register(cmdManager.metaBuilder("broadcast")
                            .plugin(vMessage)
                            .aliases("bc","bcast")
                            .build(),
                    new BroadcastCommand().createCommand()
            );
        }
    }

}
