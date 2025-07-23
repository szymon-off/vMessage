package off.szymon.vMessage;

import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.cacheddata.CachedMetaData;
import org.simpleyaml.configuration.ConfigurationSection;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Broadcaster {

    private final HashMap<String,String> serverAliases; // Server name, Server alias
    private final LuckPerms lp;
    private final HashMap<String,String> metaPlaceholders; // Placeholder, Meta key

    public Broadcaster() {
        serverAliases = new HashMap<>();
        ConfigurationSection aliases = Config.getYaml().getConfigurationSection("ServerAliases");
        if (aliases != null) {
            for (String key : aliases.getKeys(false)) {
                serverAliases.put(key, aliases.getString(key));
            }
        }

        /* LuckPerms */
        lp = VMessagePlugin.getInstance().getLuckPerms();
        metaPlaceholders = new HashMap<>();
        if (lp != null) {
            ConfigurationSection metas = Config.getYaml().getConfigurationSection("LuckPermsMeta");
            if (metas != null) {
                for (String key : metas.getKeys(false)) {
                    metaPlaceholders.put("&"+key+"&",metas.getString(key));
                }
            }
        }
    }

    public void message(Player player, String message) {
        if (!Config.getYaml().getBoolean("messages.chat.enabled")) return;

        String msg = Config.getString("messages.chat.format");
        msg = msg
                .replace("%player%", player.getUsername())
                .replace("%message%", message)
                .replace("%server%", parseAlias(player.getCurrentServer().get().getServerInfo().getName()));
        if (lp != null) {
            CachedMetaData data = lp.getPlayerAdapter(Player.class).getMetaData(player);
            msg = msg
                    .replace("%suffix%", Optional.ofNullable(data.getSuffix()).orElse(""))
                    .replace("%prefix%", Optional.ofNullable(data.getPrefix()).orElse(""));

            for (Map.Entry<String,String> entry : metaPlaceholders.entrySet()) {
                msg = msg.replace(
                        entry.getKey(),
                        Optional.ofNullable(data.getMetaValue(entry.getValue())).orElse("")
                );
            }
        }
        VMessagePlugin.getInstance().getServer().sendMessage(MiniMessage.miniMessage().deserialize(msg));
    }

    public void join(Player player) {
        if (!Config.getYaml().getBoolean("messages.join.enabled")) return;

        String msg = Config.getString("messages.join.format");
        msg = msg
                .replace("%player%", player.getUsername())
                .replace("%server%", parseAlias(player.getCurrentServer().get().getServerInfo().getName()));

        if (lp != null) {
            CachedMetaData data = lp.getPlayerAdapter(Player.class).getMetaData(player);
            msg = msg
                    .replace("%suffix%", Optional.ofNullable(data.getSuffix()).orElse(""))
                    .replace("%prefix%", Optional.ofNullable(data.getPrefix()).orElse(""));

            for (Map.Entry<String,String> entry : metaPlaceholders.entrySet()) {
                msg = msg.replace(
                        entry.getKey(),
                        Optional.ofNullable(data.getMetaValue(entry.getValue())).orElse("")
                );
            }
        }
        VMessagePlugin.getInstance().getServer().sendMessage(MiniMessage.miniMessage().deserialize(msg));
    }

    public void leave(Player player) {
        if (!Config.getYaml().getBoolean("messages.leave.enabled")) return;

        String msg = Config.getString("messages.leave.format");
        String serverName = player.getCurrentServer()
                .map(server -> server.getServerInfo().getName())
                .map(this::parseAlias)
                .orElse(null);

        if (serverName == null) {
            return; // invalid server connection, do not send leave message
        }


        msg = msg
                .replace("%player%", player.getUsername())
                .replace("%server%", serverName);


        if (lp != null) {
            CachedMetaData data = lp.getPlayerAdapter(Player.class).getMetaData(player);
            msg = msg
                    .replace("%suffix%", Optional.ofNullable(data.getSuffix()).orElse(""))
                    .replace("%prefix%", Optional.ofNullable(data.getPrefix()).orElse(""));

            for (Map.Entry<String,String> entry : metaPlaceholders.entrySet()) {
                msg = msg.replace(
                        entry.getKey(),
                        Optional.ofNullable(data.getMetaValue(entry.getValue())).orElse("")
                );
            }
        }
        VMessagePlugin.getInstance().getServer().sendMessage(MiniMessage.miniMessage().deserialize(msg));
    }

    public void change(Player player, String oldServer) {
        if (!Config.getYaml().getBoolean("messages.change.enabled")) return;

        String msg = Config.getString("messages.change.format");
        msg = msg
                .replace("%player%", player.getUsername())
                .replace("%new_server%", parseAlias(player.getCurrentServer().get().getServerInfo().getName()))
                .replace("%old_server%", parseAlias(oldServer));

        if (lp != null) {
            CachedMetaData data = lp.getPlayerAdapter(Player.class).getMetaData(player);
            msg = msg
                    .replace("%suffix%", Optional.ofNullable(data.getSuffix()).orElse(""))
                    .replace("%prefix%", Optional.ofNullable(data.getPrefix()).orElse(""));

            for (Map.Entry<String,String> entry : metaPlaceholders.entrySet()) {
                msg = msg.replace(
                        entry.getKey(),
                        Optional.ofNullable(data.getMetaValue(entry.getValue())).orElse("")
                );
            }
        }
        VMessagePlugin.getInstance().getServer().sendMessage(MiniMessage.miniMessage().deserialize(msg));
    }

    public void reload() {
        serverAliases.clear();
        ConfigurationSection aliases = Config.getYaml().getConfigurationSection("ServerAliases");
        if (aliases != null) {
            for (String key : aliases.getKeys(false)) {
                serverAliases.put(key, aliases.getString(key));
            }
        }

        metaPlaceholders.clear();
        if (lp != null) {
            ConfigurationSection metas = Config.getYaml().getConfigurationSection("LuckPermsMeta");
            if (metas != null) {
                for (String key : metas.getKeys(false)) {
                    metaPlaceholders.put("&"+key+"&",metas.getString(key));
                }
            }
        }
    }

    public void broadcast(String message) {
        if (!Config.getYaml().getBoolean("commands.broadcast.enabled")) return;

        String msg = Config.getString("commands.broadcast.format");
        msg = msg.replace("%message%", message);

        VMessagePlugin.getInstance().getServer().sendMessage(MiniMessage.miniMessage().deserialize(msg));
    }

    public String parseAlias(String serverName) {
        String output;
        for (Map.Entry<String,String> entry : serverAliases.entrySet()) {
            if (serverName.equalsIgnoreCase(entry.getKey())) {
                output = entry.getValue();
                return output;
            }
        }
        return serverName;
    }

    public HashMap<String, String> getMetaPlaceholders() {
        return metaPlaceholders;
    }
}
