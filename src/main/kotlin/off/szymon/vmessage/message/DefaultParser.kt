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

package off.szymon.vmessage.message

import com.velocitypowered.api.proxy.Player
import off.szymon.vmessage.integration.IntegrationManager

class DefaultParser(val handlerPlaceholders: Map<String, String>, val player: Player) {

    fun parse(string: String): String {
        var output = string
        handlerPlaceholders.forEach {
            output = output.replace(it.key, it.value)
        }
        IntegrationManager.get().getIntegrations().forEach { output = it.parse(output, player) }
        return output
    }

}