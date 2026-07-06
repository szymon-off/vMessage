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

import off.szymon.vmessage.VMessage
import off.szymon.vmessage.config.Config
import off.szymon.vmessage.message.handler.ChangeHandler
import off.szymon.vmessage.message.handler.ChatHandler
import off.szymon.vmessage.message.handler.JoinHandler
import off.szymon.vmessage.message.handler.LeaveHandler

class HandlerManager {

    val handlers = mutableMapOf<Class<out MessagesHandler>, MessagesHandler>()
    val vMessage = VMessage.get()

    companion object {
        @JvmStatic
        private lateinit var instance: HandlerManager

        @JvmStatic
        fun get(): HandlerManager = instance
    }

    init {
        instance = this
        loadHandlers()
    }

    fun loadHandlers() {
        loadHandlerIfEnabled("chat", ChatHandler::class.java)
        loadHandlerIfEnabled("join", JoinHandler::class.java)
        loadHandlerIfEnabled("leave", LeaveHandler::class.java)
        loadHandlerIfEnabled("change", ChangeHandler::class.java)
    }

    fun unloadHandlers() {
        for (handlerClass in handlers.keys) {
            unloadHandler(handlerClass)
        }
    }

    fun reloadHandlers() {
        unloadHandlers()
        loadHandlers()
    }

    fun loadHandlerIfEnabled(name: String, handlerClass: Class<out MessagesHandler>) {
        if (Config.get().root.node("messages",name,"enabled").getBoolean(false)) {
            vMessage.logger.info("Loading '$name' handler...")
            val handler = handlerClass.getDeclaredConstructor().newInstance()
            vMessage.server.eventManager.register(vMessage, handler)
            handlers[handlerClass] = handler
        } else {
            vMessage.logger.info("Skipping '$name' handler...")
        }
    }

    fun unloadHandler(handlerClass: Class<out MessagesHandler>) {
        val handler = handlers[handlerClass] ?: return
        vMessage.logger.info("Unloading '${handler.id}' handler...")
        vMessage.server.eventManager.unregisterListener(vMessage, handler)
        handlers.remove(handlerClass)
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : MessagesHandler> getHandler(handlerClass: Class<T>): T? {
        val handler: MessagesHandler = handlers[handlerClass] ?: return null
        return handler as? T
    }

}