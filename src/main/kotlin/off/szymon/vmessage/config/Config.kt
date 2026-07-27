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

class Config : FishySerializedConfigurateFile<YamlConfigurationLoader, CommentedConfigurationNode, YamlConfigurationLoader.Builder, MainConfig> (
    "config.yml", VMessage.get().dataDir, { YamlConfigurationLoader.builder() } , MainConfig::class.java,
    """
        vMessage Config
        Thanks for downloading my plugin! I hope you like it!
        Check out the wiki for more information: https://github.com/szymon-off/vMessage/wiki/Configuration-(config.yml)
    """.trimIndent()
) {

    companion object {
        private lateinit var instance: Config

        @JvmStatic
        fun get(): Config = instance
    }

    //  TODO old config migration
    init {
        instance = this
    }

    override fun applyMoreAdditionalOptions(options: ConfigurationOptions): ConfigurationOptions {
        return options
            .implicitInitialization(true)
    }

}
