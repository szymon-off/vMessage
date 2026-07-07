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
import java.util.concurrent.TimeUnit


class PlaceholderApiIntegration(player: Player) : Integration("placeholder-api", "papiproxybridge", player) {

    val api = PlaceholderAPI.createInstance()

    override fun parse(string: String): String {
        return api
            .formatPlaceholders(string, player.uniqueId)
            .orTimeout(500, TimeUnit.MILLISECONDS) // TODO configurable timeout
            .join()
    }

}