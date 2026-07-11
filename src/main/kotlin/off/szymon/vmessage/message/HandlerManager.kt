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

    private val vMessage = VMessage.get()

    private val handlers = mutableMapOf<Class<out MessagesHandler>, MessagesHandler>()
    val defaultHandlers = mapOf(
        "chat" to ChatHandler::class.java,
        "join" to JoinHandler::class.java,
        "leave" to LeaveHandler::class.java,
        "change" to ChangeHandler::class.java,
    )

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
        for (e in defaultHandlers.entries) {
            loadHandlerIfEnabled(e.key, e.value)
        }
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

    fun loadHandlerIfEnabled(id: String, handlerClass: Class<out MessagesHandler>) {
        if (Config.get().root.node("messages",id,"enabled").getBoolean(false)) {
            vMessage.logger.info("Loading '$id' handler...")
            val handler = handlerClass.getDeclaredConstructor().newInstance()
            vMessage.proxy.eventManager.register(vMessage, handler)
            handlers[handlerClass] = handler
        } else {
            vMessage.logger.info("Skipping '$id' handler...")
        }
    }

    fun unloadHandler(handlerClass: Class<out MessagesHandler>) {
        val handler = handlers[handlerClass] ?: return
        vMessage.logger.info("Unloading '${handler.id}' handler...")
        vMessage.proxy.eventManager.unregisterListener(vMessage, handler)
        handlers.remove(handlerClass)
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : MessagesHandler> getHandler(handlerClass: Class<T>): T? {
        val handler: MessagesHandler = handlers[handlerClass] ?: return null
        return handler as? T
    }

    fun getHandler(id: String): MessagesHandler? {
        return handlers.values.find { it.id == id }
    }

}