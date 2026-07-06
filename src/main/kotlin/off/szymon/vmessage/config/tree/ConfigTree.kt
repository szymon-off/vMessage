/*
 * vMessage
 * Copyright (c) 2026.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * See the LICENSE file in the project root for details.
 */

package off.szymon.vmessage.config.tree

import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.Comment

@ConfigSerializable
class MainConfig {

    var messages = MessagesConfig()
    var commands = CommandsConfig()
    var placeholders = PlaceholdersConfig()

}

@ConfigSerializable
class MessagesConfig {

    var chat = ChatConfig()
    var join = JoinConfig()
    var leave = LeaveConfig()
    var change = ChangeConfig()

}

@ConfigSerializable
class ChatConfig {

    var enabled = true
    @Comment("This is to give way to other plugins that may want to handle chat messages before vMessage does.\n" +
            "For example punishment plugins like LiteBans or LibertyBans may want to prevent chat messages from being sent if sender is muted.\n" +
            "Available options (in order): FIRST, EARLY, NORMAL, LATE, LAST")
    var order = "LAST"
    @Comment($$"Available placeholders: $player$, $message$, $server$, $prefix$, $suffix$, (custom luckperms metas), (PlaceholderAPI placeholders)")
    var format = $$"$prefix$ <b>$player$:</b> $message$"
    @Comment("Whether to allow players to use MiniMessage in their messages\nThis may break your formatting, so it is recommended to keep this disabled.")
    var allowMiniMessage = false

}

@ConfigSerializable
class JoinConfig {
    var enabled = true
    @Comment($$"Available placeholders: $player$, $server$, $prefix$, $suffix$, (custom luckperms metas), (PlaceholderAPI placeholders)")
    var format = $$"<dark_gray>(<green>+<dark_gray>) <gray>$player$"
}

@ConfigSerializable
class LeaveConfig {

    var enabled = true
    @Comment($$"Available placeholders: $player$, $server$, $prefix$, $suffix$, (custom luckperms metas), (PlaceholderAPI placeholders)")
    var format = $$"<dark_gray>(<red>-<dark_gray>) <gray>$player$"

}

@ConfigSerializable
class ChangeConfig {
    var enabled = true
    @Comment($$"Available placeholders: $player$, $old_server$, $new_server$, $prefix$, $suffix$, (custom luckperms metas), (PlaceholderAPI placeholders)")
    var format = $$"<dark_gray>(<gold>•<dark_gray>) <gray>$player$ <dark_gray>(<gold>$old_server$→$new_server$<dark_gray>)"
}

@ConfigSerializable
class CommandsConfig {
    // TODO
}

@ConfigSerializable
class PlaceholdersConfig {

    @Comment("For technical reasons this is here even though it's not really an integration with another plugin.")
    var serverAliases = ServerAliasesConfig()
    var luckPerms = LuckPermsConfig()
    @Comment("via PAPIProxyBridge")
    var placeholderApi = PlaceholderApiConfig()

}

@ConfigSerializable
class ServerAliasesConfig {

    var lobby = "Lobby"
    var lobby1 = "Lobby"
    var lobby2 = "Lobby"

}

@ConfigSerializable
class LuckPermsConfig {

    var enabled = true
    @Comment("You must predefine your custom metas here to use them in your formats.")
    var customMeta = CustomMetaConfig()
}

@ConfigSerializable
class CustomMetaConfig {

    @Comment("Used as &custom1&")
    var custom1 = "meta_key1"
    @Comment("Used as &custom2& etc.")
    var custom2 = "meta_key2"

}

@ConfigSerializable
class PlaceholderApiConfig {

    var enabled = true

}