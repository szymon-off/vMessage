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

package off.szymon.vmessage.integration

import com.velocitypowered.api.proxy.Player
import net.kyori.adventure.audience.Audience
import off.szymon.fishy.api.messenger.parser.PlaceholderParserBuilder
import off.szymon.vmessage.config.Config

class ServerAliasesIntegration : Integration("server-aliases", "vmessage") {

    override fun parse(string: String, player: Player): String {
        val builder = PlaceholderParserBuilder()
        Config.get().root.node("integrations","placeholder",id,"aliases").childrenMap().forEach { (key, value) ->
            builder.addPlaceholder(key.toString(), value.toString())
        }
        val parser = builder.build()
        return parser.parse(string, Audience.empty())
    }

}