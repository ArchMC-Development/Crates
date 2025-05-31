package gg.scala.crates.crate

import gg.scala.commons.acf.ConditionFailedException
import gg.scala.commons.annotations.commands.customizer.CommandManagerCustomizer
import gg.scala.commons.command.ScalaCommandManager
import gg.scala.commons.util.Files
import gg.scala.crates.CratesSpigotPlugin
import gg.scala.crates.configuration
import gg.scala.crates.crate.prize.CratePrize
import gg.scala.crates.datasync.CrateContainer
import gg.scala.crates.datasync.CrateDataSyncService
import gg.scala.crates.keyProvider
import gg.scala.crates.menu.opening.CrateOpenMenu
import gg.scala.crates.menu.opening.CrateSelectRewardOpenMenu
import gg.scala.crates.sendToPlayer
import gg.scala.flavor.inject.Inject
import gg.scala.flavor.service.Configure
import gg.scala.flavor.service.Service
import net.evilblock.cubed.serializers.Serializers
import net.evilblock.cubed.serializers.impl.AbstractTypeSerializer
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.Tasks
import org.bukkit.entity.Player
import java.io.File

/**
 * @author GrowlyX
 * @since 8/13/2022
 */
@Service(priority = 100)
object CrateService
{
    @Inject
    lateinit var plugin: CratesSpigotPlugin

    private lateinit var config: CrateContainer

    fun config() = this.config
    fun allCrates() = config().crates.values

    fun findCrate(name: String) =
        this.config().crates.values
            .firstOrNull {
                it.uniqueId == name
            }

    fun openCrate(player: Player, crate: Crate)
    {
        val balance = keyProvider()
            .getKeysFor(
                player.uniqueId, crate
            )

        if (balance <= 0)
        {
            configuration.noKeyOpenAttempt.sendToPlayer(player)
            return
        }

        keyProvider().useKeyFor(player.uniqueId, crate.uniqueId)
            .thenRun {
                if (!crate.isSelectItem)
                {
                    Tasks.sync {
                        CrateOpenMenu(player, crate).openMenu(player)
                    }
                } else
                {
                    Tasks.sync {
                        CrateSelectRewardOpenMenu(player, crate).openMenu(player)
                    }
                }
            }
    }

    @Configure
    fun configure()
    {
        Serializers.create {
            this.setPrettyPrinting()
            this.registerTypeAdapter(
                CratePrize::class.java,
                AbstractTypeSerializer<CratePrize>()
            )
        }
    }

    fun loadConfig()
    {
        this.config = CrateDataSyncService.cached()
    }

    fun saveConfig()
    {
        CrateDataSyncService.sync(this.config)
    }

    @CommandManagerCustomizer
    fun customize(manager: ScalaCommandManager)
    {
        manager.commandContexts
            .registerContext(Crate::class.java) { context ->
                val firstArg = context.popFirstArg()

                this.config().crates.values
                    .firstOrNull {
                        it.uniqueId == firstArg
                    }
                    ?: throw ConditionFailedException(
                        "No crate by the name ${CC.YELLOW}$firstArg${CC.RED} exists."
                    )
            }

        manager.commandCompletions
            .registerCompletion("crates") {
                this.config().crates.values.map { it.uniqueId }
            }
    }
}

