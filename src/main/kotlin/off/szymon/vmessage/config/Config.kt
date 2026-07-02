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

package off.szymon.vmessage.config

import off.szymon.fishy.api.file.FishySerializedConfigurateFile
import off.szymon.vmessage.VMessage
import off.szymon.vmessage.config.tree.MainConfig
import org.spongepowered.configurate.CommentedConfigurationNode
import org.spongepowered.configurate.ConfigurationOptions
import org.spongepowered.configurate.yaml.YamlConfigurationLoader
import kotlin.jvm.java

class Config : FishySerializedConfigurateFile<YamlConfigurationLoader, CommentedConfigurationNode, YamlConfigurationLoader.Builder, MainConfig> (
    "config.yml", VMessage.get().dataDir, YamlConfigurationLoader.Builder::class.java, MainConfig::class.java,
    """
        vMessage Config
        Thanks for downloading my plugin! I hope you like it!
    """.trimIndent()
) {

    companion object {

        @JvmStatic
        private lateinit var instance: Config

        @JvmStatic
        fun get(): Config = instance

    }

    init {
        instance = this
    }

    override fun applyMoreAdditionalOptions(options: ConfigurationOptions): ConfigurationOptions {
        return options
            .implicitInitialization(true)
    }

}