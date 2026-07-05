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

interface IntegrationManager<T : Integration> {

    fun loadIntegrations()

    fun unloadIntegrations()

    fun reloadIntegrations() {
        unloadIntegrations()
        loadIntegrations()
    }

    fun loadIntegrationIfEnabled(clazz: Class<out T>)

    fun unloadIntegration(clazz: Class<out T>)

    fun <I : T> getIntegration(clazz: Class<I>): I?

}