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

import net.kyori.adventure.text.minimessage.MiniMessage

// Player supplied text is the only part of a message vMessage doesn't control, so it is cleaned
// here, once, right before it is handed to a parser as a placeholder value. Every caller passes
// its own allow-mini-message setting so the feature that owns the message decides the formatting.
//
// this the only actually vibe coded class because I could not make something like this due to lack cyber-sec knowledge
object MessageSanitizer {

    // escapeTags() only neutralizes MiniMessage tags, so section signs would survive it and still
    // be rendered as legacy formatting by the client. Legacy color codes are not supported
    // (see settings.legacy-color-codes), and control characters have no business in a message.
    private val ILLEGAL = Regex("[\\u00A7\\u0000-\\u001F\\u007F]")

    fun sanitize(message: String, allowMiniMessage: Boolean): String {
        val cleaned = ILLEGAL.replace(message, "")
        if (allowMiniMessage) return cleaned
        return MiniMessage.miniMessage().escapeTags(cleaned)
    }

}
