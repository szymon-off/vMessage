/*
 * vMessage
 * Copyright (c) 2025.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * See the LICENSE file in the project root for details.
 */

@file:Suppress("unused")

package off.szymon.vMessage.config.tree

import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.Comment

@ConfigSerializable
class MainConfig {
    @Comment("This is a comment test.\nLet's hope this works properly.")
    var messages = MessagesConfig()
    @Comment("This is an another comment test.\nLet's hope this works properly as well.")
    var commands = CommandsConfig()
    var luckPermsMeta = LuckPermsMetaConfig()
    var serverAliases = ServerAliasesConfig()
}

/* Messages Config */
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
    var format = "%prefix% <b>%player%:</b> %message%"
    var allowMiniMessage = false
    var mutedMessage = "<red>You are muted and cannot send messages.</red>"
}

@ConfigSerializable
class JoinConfig {
    var enabled = true
    var format = "<dark_gray>(<green>+<dark_gray>) <gray>%player%"
}

@ConfigSerializable
class LeaveConfig {
    var enabled = true
    var format = "<dark_gray>(<red>-<dark_gray>) <gray>%player%"
}

@ConfigSerializable
class ChangeConfig {
    var enabled = true
    var format = "<dark_gray>(<gold>•<dark_gray>) <gray>%player% <dark_gray>(<gold>%old_server%→%new_server%<dark_gray>)"
}

/* Commands Config */
@ConfigSerializable
class CommandsConfig {
    var broadcast = BroadcastConfig()
    var message = MessageConfig()
}

@ConfigSerializable
class BroadcastConfig {
    var enabled = true
    var format = "<bold><#00ffff>Server:<reset> %message%"
}

@ConfigSerializable
class MessageConfig {
    var enabled = true
    var format = MessageFormatConfig()
    var allowMiniMessage = false
    var enableReplyCommand = true
}

@ConfigSerializable
class MessageFormatConfig {
    var sender = "<b>(<#00ffff>You </#00ffff>→ <#00ffff>%receiver%</#00ffff>): </b>%message%"
    var receiver = "<b>(<#00ffff>%sender% </#00ffff>→ <#00ffff>You</#00ffff>): </b>%message%"
}

@ConfigSerializable
class LuckPermsMetaConfig {
    // Not to be used in-code, these only serve as example keys for the config file
    var customName1 = "meta_key1"
    var customName2 = "meta_key2"
}

@ConfigSerializable
class ServerAliasesConfig {
    // Not to be used in-code, these only serve as example aliases for the config file
    var lobby1 = "Lobby"
    var lobby2 = "Lobby"
}