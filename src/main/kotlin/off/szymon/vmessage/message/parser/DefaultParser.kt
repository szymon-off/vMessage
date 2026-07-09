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

package off.szymon.vmessage.message.parser

import com.velocitypowered.api.proxy.Player
import off.szymon.fishy.api.messenger.parser.MessageParser
import off.szymon.fishy.api.messenger.parser.PlaceholderParser
import off.szymon.vmessage.integration.IntegrationManager

class DefaultParser(val handlerPlaceholders: Map<String, String>, val player: Player) : MessageParser {

    override fun parse(string: String): String {
        val parser = PlaceholderParser(handlerPlaceholders)

        return IntegrationManager.get().getMultiParser(player).parse(parser.parse(string))
    }

}
