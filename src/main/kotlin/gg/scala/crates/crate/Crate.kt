package gg.scala.crates.crate

import gg.scala.crates.crate.prize.CratePrize
import net.evilblock.cubed.entity.EntityHandler
import net.evilblock.cubed.entity.hologram.HologramEntity
import net.evilblock.cubed.util.CC
import org.bukkit.Location
import kotlin.collections.set

/**
 * @author GrowlyX
 * @since 8/13/2022
 */
data class Crate(
    val uniqueId: String,
    var displayName: String,
    var prizes: MutableList<CratePrize>,
    var applicable: Boolean = false,
    var isSelectItem: Boolean = false,
    var isHologramEnabled: Boolean = false,
    var shouldCompressMenu: Boolean = false,
    var hologramLines: MutableList<String> = mutableListOf(
        displayName,
        "",
        "${CC.WHITE}Left-Click to preview crate",
        "${CC.WHITE}Right-Click to open crate",
    ),
    var physicalLocation: Location? = null,
    var group: String? = null
)
{

    fun initializeBukkit()
    {
        decommissionBukkit()

        if (physicalLocation != null && isHologramEnabled)
        {
            CrateService.crateHolograms[uniqueId] = HologramEntity(displayName, physicalLocation!!.clone().add(0.5, 1.5, 0.5))
                .also { entity ->
                    entity.updateLines(hologramLines)
                    entity.persistent = false

                    entity.initializeData()
                    EntityHandler.trackEntity(entity)
                }
        }
    }

    fun decommissionBukkit()
    {
        val hologram = CrateService.crateHolograms[uniqueId]

        if (hologram != null)
        {
            hologram.destroyForCurrentWatchers()
            EntityHandler.forgetEntity(hologram)
        }
    }
}