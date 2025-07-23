package off.szymon.vMessage.cmd;

import com.velocitypowered.api.command.CommandManager;
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
        cmdManager.register(cmdManager.metaBuilder("broadcast")
                .plugin(vMessage)
                .aliases("bc","bcast","alert","announce")
                .build(),
                new BroadcastCommand().createCommand()
        );
    }

}
