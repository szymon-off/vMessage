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
import net.kyori.adventure.audience.Audience.audience
import net.luckperms.api.LuckPermsProvider
import off.szymon.fishy.api.messenger.parser.PlaceholderParserBuilder
import off.szymon.vmessage.config.Config

class LuckPermsIntegration(player: Player) : Integration("luck-perms", "luckperms", player) {

    val api = LuckPermsProvider.get()
    val playerAdapter = api.getPlayerAdapter(Player::class.java)

    override fun parse(string: String): String {
        val metaData = playerAdapter.getMetaData(player)

        val builder = PlaceholderParserBuilder()
        builder.addPlaceholder($$"$prefix$", metaData.prefix ?: "")
        builder.addPlaceholder($$"$suffix$", metaData.suffix ?: "")

        Config.get().root.node("integrations","placeholder",id,"custom-meta").childrenMap().forEach { (key, value) ->
            val metaKey = key.toString()
            val metaValue = metaData.getMetaValue(value.string ?: "") ?: ""
            builder.addPlaceholder("&$metaKey&", metaValue)
        }

        return builder.build().parse(string)
    }

}