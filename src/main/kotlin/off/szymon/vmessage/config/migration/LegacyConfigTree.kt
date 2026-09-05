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
    var textComponentSettings: LegacyTextComponentConfig = null!!
    var messages: LegacyMessagesConfig = null!!
    var commands: LegacyCommandsConfig = null!!
    var luckPermsMeta: LegacyLuckPermsMetaConfig = null!!
    var serverAliases: ServerAliasesConfig = null!!
    var backupConfig: Boolean = null!!
}

@ConfigSerializable
class LegacyTextComponentConfig {
    var textDeserializer: String = null!!
    var legacyTextCharacter: String = null!!
}

/* Messages Config */
@ConfigSerializable
class LegacyMessagesConfig {
    var chat: LegacyChatConfig = null!!
    var join: LegacyJoinConfig = null!!
    var leave: LegacyLeaveConfig = null!!
    var change: LegacyChangeConfig = null!!
}

@ConfigSerializable
class LegacyChatConfig {
    var enabled: Boolean = null!!
    var format: String? = null
    var allowMiniMessage: Boolean? = null
    var mutedMessage: String = null!!
}

@ConfigSerializable
class LegacyJoinConfig {
    var enabled: Boolean? = null
    var format: String = null!!
}

@ConfigSerializable
class LegacyLeaveConfig {
    var enabled: Boolean = null!!
    var format: String = null!!
}

@ConfigSerializable
class LegacyChangeConfig {
    var enabled: Boolean = null!!
    var format: String = null!!
}

@ConfigSerializable
class LegacyCommandsConfig {
    var broadcast: Boolean? = null
    var message: LegacyMessageConfig = null!!
}

@ConfigSerializable
class LegacyBroadcastConfig {
    var enabled: Boolean = null!!
    var format: String = null!!
    var allowByDefault: Boolean = null!!
    var allowMiniMessage: Boolean = null!!
}

@ConfigSerializable
class LegacyMessageConfig {
    var enabled: Boolean = null!!
    var format: LegacyMessageFormatConfig = null!!
    var allowMiniMessage: Boolean = null!!
    var allowByDefault: Boolean = null!!
    var enableReplyCommand: Boolean = null!!
}

@ConfigSerializable
class LegacyMessageFormatConfig {
    var sender: String = null!!
    var receiver: String = null!!
}

@ConfigSerializable
class LegacyLuckPermsMetaConfig {
    var customName1: String = null!!
    var customName2: String = null!!
}

@ConfigSerializable
class LegacyServerAliasesConfig {
    var lobby: String = null!!
    var lobby1: String = null!!
    var lobby2: String = null!!
}