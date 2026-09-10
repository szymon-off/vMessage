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

package off.szymon.vmessage.config.migration

import off.szymon.vmessage.config.tree.ServerAliasesConfig
import org.spongepowered.configurate.objectmapping.ConfigSerializable

@ConfigSerializable
class LegacyMainConfig {
    lateinit var textComponentSettings: LegacyTextComponentConfig
    lateinit var messages: LegacyMessagesConfig
    lateinit var commands: LegacyCommandsConfig
    lateinit var luckPermsMeta: LegacyLuckPermsMetaConfig
    lateinit var serverAliases: ServerAliasesConfig
    var backupConfig: Boolean = false
}

@ConfigSerializable
class LegacyTextComponentConfig {
    lateinit var textDeserializer: String
    lateinit var legacyTextCharacter: String
}

/* Messages Config */
@ConfigSerializable
class LegacyMessagesConfig {
    lateinit var chat: LegacyChatConfig
    lateinit var join: LegacyJoinConfig
    lateinit var leave: LegacyLeaveConfig
    lateinit var change: LegacyChangeConfig
}

@ConfigSerializable
class LegacyChatConfig {
    var enabled: Boolean = false
    lateinit var format: String
    var allowMiniMessage: Boolean = false
    lateinit var mutedMessage: String
}

@ConfigSerializable
class LegacyJoinConfig {
    var enabled: Boolean = false
    lateinit var format: String
}

@ConfigSerializable
class LegacyLeaveConfig {
    var enabled: Boolean = false
    lateinit var format: String
}

@ConfigSerializable
class LegacyChangeConfig {
    var enabled: Boolean = false
    lateinit var format: String
}

@ConfigSerializable
class LegacyCommandsConfig {
    lateinit var broadcast: LegacyBroadcastConfig
    lateinit var message: LegacyMessageConfig
}

@ConfigSerializable
class LegacyBroadcastConfig {
    var enabled: Boolean = false
    lateinit var format: String
    var allowByDefault: Boolean = false
    var allowMiniMessage: Boolean = false
}

@ConfigSerializable
class LegacyMessageConfig {
    var enabled: Boolean = false
    lateinit var format: LegacyMessageFormatConfig
    var allowMiniMessage: Boolean = false
    var allowByDefault: Boolean = false
    var enableReplyCommand: Boolean = false
}

@ConfigSerializable
class LegacyMessageFormatConfig {
    lateinit var sender: String
    lateinit var receiver: String
}

@ConfigSerializable
class LegacyLuckPermsMetaConfig {
    lateinit var customName1: String
    lateinit var customName2: String
}

@ConfigSerializable
class LegacyServerAliasesConfig {
    lateinit var lobby: String
    lateinit var lobby1: String
    lateinit var lobby2: String
}