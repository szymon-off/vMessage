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
import net.luckperms.api.LuckPermsProvider
import off.szymon.fishy.api.messenger.parser.PlaceholderParserBuilder
import off.szymon.vmessage.config.Config

class LuckPermsIntegration : Integration("luck-perms", "luckperms") {

    val api = LuckPermsProvider.get()
    val playerAdapter = api.getPlayerAdapter(Player::class.java)

    private val customMeta: Map<String, String> =
        Config.get().root.node("placeholders", id, "custom-meta").childrenMap()
            .mapNotNull { (k, v) -> v.string?.let { "&$k&" to it } }
            .toMap()

    override fun parse(string: String, player: Player): String {
        val metaData = playerAdapter.getMetaData(player)

        val builder = PlaceholderParserBuilder()
        builder.addPlaceholder($$"$prefix$", metaData.prefix ?: "")
        builder.addPlaceholder($$"$suffix$", metaData.suffix ?: "")

        customMeta.forEach { (placeholder, metaKey) ->
            builder.addPlaceholder(placeholder, metaData.getMetaValue(metaKey) ?: "")
        }

        return builder.build().parse(string)
    }

}