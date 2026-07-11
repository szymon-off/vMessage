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
import net.kyori.adventure.text.Component
import off.szymon.fishy.api.messenger.FishyMessenger

abstract class MessagesHandler(val id: String) : FishyMessenger(Component.empty()) {

    open fun broadcast(player: Player) {
        throw UnsupportedOperationException("This message handler ($id) doesn't support traditional broadcast")
    }

}