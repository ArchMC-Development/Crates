package gg.scala.crates

import gg.scala.commons.ExtendedScalaPlugin
import gg.scala.commons.annotations.container.ContainerEnable
import gg.scala.commons.core.plugin.Plugin
import gg.scala.commons.core.plugin.PluginApiVersion
import gg.scala.commons.core.plugin.PluginAuthor
import gg.scala.commons.core.plugin.PluginDependency
import gg.scala.commons.core.plugin.PluginDependencyComposite
import gg.scala.commons.core.plugin.PluginWebsite
import gg.scala.crates.keys.DefaultKeyProvider
import gg.scala.crates.keys.KeyProvider
import gg.scala.crates.placeholder.CratePlaceholder
import org.bukkit.Bukkit

/**
 * @author GrowlyX
 * @since 8/13/2022
 */
@Plugin(
    name = "Crates",
    version = "%remote%/%branch%/%id%"
)
@PluginAuthor("Scala")
@PluginWebsite("https://scala.gg")
@PluginApiVersion("1.20")
@PluginDependencyComposite(
    PluginDependency("scala-commons"),
    PluginDependency("Survival"),
    PluginDependency("cloudsync", soft = true),
    PluginDependency("PlaceholderAPI", soft = true)
)
class CratesSpigotPlugin : ExtendedScalaPlugin()
{
    var keyProvider: KeyProvider = DefaultKeyProvider

    @ContainerEnable
    fun containerEnable()
    {
        configuration = this.config()
        plugin = this

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI"))
        {
            CratePlaceholder().register()
        }
    }
}
