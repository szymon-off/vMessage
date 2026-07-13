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
import net.william278.papiproxybridge.api.PlaceholderAPI
import off.szymon.vmessage.VMessage
import off.szymon.vmessage.config.Config
import java.util.concurrent.TimeUnit

class PlaceholderApiIntegration : Integration("placeholder-api", "papiproxybridge") {

    val api = PlaceholderAPI.createInstance()
    val timeout = Config.get().tree.placeholders.placeholderApi.bridgeTimeout

    override fun parse(string: String, player: Player): String {
        return try {
            api
                .formatPlaceholders(string, player.uniqueId)
                .orTimeout(timeout, TimeUnit.MILLISECONDS)
                .join() // safe because the entire event is running async
        } catch (e: Exception) {
            VMessage.get().logger.warn("Failed to parse placeholders for player ${player.username} (${player.uniqueId}) using PlaceholderAPI: ${e.message}")
            string
        }

    }

}