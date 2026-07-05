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

import net.kyori.adventure.audience.Audience
import off.szymon.fishy.api.messenger.parser.MessageParser

class DefaultParser(val handlerPlaceholders: Map<String, String>) : MessageParser {

    override fun parse(string: String, audience: Audience): String {
        var output = string
        handlerPlaceholders.forEach {
            output = output.replace(it.key, it.value)
        }
        return output
    }

}