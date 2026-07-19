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

package off.szymon.vmessage.command

import com.velocitypowered.api.command.BrigadierCommand
import com.velocitypowered.api.command.CommandSource
import com.velocitypowered.api.permission.Tristate
import com.velocitypowered.api.proxy.Player
import net.kyori.adventure.text.Component
import off.szymon.fishy.api.messenger.FishyMessenger
import off.szymon.vmessage.config.Config

abstract class PluginCommand(val name: String, vararg val aliases: String) : FishyMessenger(Component.empty()){

    abstract fun createCommand(): BrigadierCommand

    fun checkPermission(src: CommandSource, permission: String): Boolean {
        if (src !is Player) {
            return true
        }

        val value = src.getPermissionValue(permission)
        if (Tristate.UNDEFINED != value) {
            return value.asBoolean()
        }
        val def = Config.get().root.node("commands", name, "allow-by-default").getBoolean(false)
        return def
    }

}