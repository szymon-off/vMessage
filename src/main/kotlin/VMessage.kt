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

import com.google.inject.Inject
import com.velocitypowered.api.plugin.Dependency
import com.velocitypowered.api.plugin.Plugin
import com.velocitypowered.api.plugin.PluginContainer
import com.velocitypowered.api.plugin.annotation.DataDirectory
import com.velocitypowered.api.proxy.ProxyServer
import off.szymon.vmessage.generated.Version
import org.bstats.velocity.Metrics
import org.slf4j.Logger
import java.nio.file.Path

@Plugin(
    id = "vmessage",
    name = "vMessage",
    description = "vMessage is the best Velocity plugin for synchronizing chat and player events across your entire proxy network",
    version = Version.VERSION, // Your IDE may show an error here, but it will compile fine. This is generated during build. Run the generateVersion task if needed.
    authors = ["SzymON_OFF"],
    url = "https://szymonoff.me/projects/vmessage.html",
    dependencies = [
        Dependency(id = "signedvelocity", optional = true),
        Dependency(id = "luckperms", optional = true),
        Dependency(id = "libertybans", optional = true),
        Dependency(id = "litebans", optional = true)
    ]
)
class VMessage @Inject constructor(
    val server: ProxyServer,
    val logger: Logger,
    @param:DataDirectory val dataDir: Path,
    val Plugin: PluginContainer,
    val metricsFactory: Metrics.Factory
) {

}