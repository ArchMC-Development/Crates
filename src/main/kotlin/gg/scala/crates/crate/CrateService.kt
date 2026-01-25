package gg.scala.crates.crate

import gg.scala.commons.acf.ConditionFailedException
import gg.scala.commons.agnostic.sync.ServerSync
import gg.scala.commons.annotations.commands.customizer.CommandManagerCustomizer
import gg.scala.commons.command.ScalaCommandManager
import gg.scala.crates.CratesSpigotPlugin
import gg.scala.crates.configuration
import gg.scala.crates.crate.prize.CratePrize
import gg.scala.crates.datasync.CrateDataSyncService
import gg.scala.crates.keyProvider
import gg.scala.crates.menu.CrateContentsMenu
import gg.scala.crates.menu.opening.CrateOpenMenu
import gg.scala.crates.menu.opening.CrateSelectRewardOpenMenu
import gg.scala.crates.sendToPlayer
import gg.scala.flavor.inject.Inject
import gg.scala.flavor.service.Configure
import gg.scala.flavor.service.Service
import me.lucko.helper.Events
import net.evilblock.cubed.serializers.Serializers
import net.evilblock.cubed.serializers.impl.AbstractTypeSerializer
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.Tasks
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import java.util.*

/**
 * @author GrowlyX
 * @since 8/13/2022
 */
@Service(priority = 100)
object CrateService
{
    @Inject
    lateinit var plugin: CratesSpigotPlugin

    fun config() = CrateDataSyncService.cached()
    fun allCrates() = config().crates.values
    fun allCratesScoped() = config().crates.values.filter { it.group == null || ServerSync.local.groups.contains(it.group) }

    private val lastInteract = mutableMapOf<UUID, Long>()

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

        reinitializeCrates()

        Events.subscribe(PlayerInteractEvent::class.java)
            .filter {
                it.hand == EquipmentSlot.HAND &&
                        (it.action == Action.LEFT_CLICK_BLOCK || it.action == Action.RIGHT_CLICK_BLOCK)
            }
            .handler { event ->
                val player = event.player
                val click = event.clickedBlock?.location
                    ?: return@handler
                val crateFromLocation = crateFromLocation(click)
                    ?: return@handler

                val now = System.currentTimeMillis()
                val last = lastInteract[player.uniqueId] ?: 0L
                if (now - last < 5L)
                {
                    return@handler
                }
                lastInteract[player.uniqueId] = now

                event.isCancelled = true

                if (event.action == Action.LEFT_CLICK_BLOCK)
                {
                    CrateContentsMenu(crateFromLocation).openMenu(player)
                } else if (event.action == Action.RIGHT_CLICK_BLOCK)
                {
                    try
                    {
                        openCrate(player, crateFromLocation)
                    } catch (exception: ConditionFailedException)
                    {
                        player.sendMessage("${CC.RED}${exception.message}")
                    }
                }
            }
    }

    fun save(crate: Crate)
    {
        with(CrateDataSyncService.cached())
        {
            this.crates[crate.uniqueId] = crate
            CrateDataSyncService.sync(this)
        }
    }

    fun delete(uniqueId: String)
    {
        with(CrateDataSyncService.cached())
        {
            this.crates.remove(uniqueId)
            CrateDataSyncService.sync(this)
        }
    }

    fun reinitializeCrates()
    {
        allCratesScoped().forEach { crate ->
            crate.initializeBukkit()
        }
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

    fun crateFromLocation(location: Location) =
        CrateDataSyncService.cached().crates.values.firstOrNull {
            val crateLocation = it.physicalLocation?.toBlockLocation()
            crateLocation != null &&
                    crateLocation.world?.name == location.world?.name &&
                    crateLocation.blockX == location.blockX &&
                    crateLocation.blockY == location.blockY &&
                    crateLocation.blockZ == location.blockZ
        }
}

