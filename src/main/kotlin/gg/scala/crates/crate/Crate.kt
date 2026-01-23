package gg.scala.crates.crate

import gg.scala.crates.crate.prize.CratePrize
import net.evilblock.cubed.entity.EntityHandler
import net.evilblock.cubed.entity.hologram.HologramEntity
import net.evilblock.cubed.util.CC
import org.bukkit.Location

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
    var hologramLines: MutableList<String> = mutableListOf(
        displayName,
        "",
        "${CC.WHITE}Left-Click to preview crate",
        "${CC.WHITE}Right-Click to open crate",
    ),
    var physicalLocation: Location? = null
)
{
    @Transient
    var hologram: HologramEntity? = null

    fun initializeBukkit()
    {
        decommissionBukkit()

        if (physicalLocation != null && isHologramEnabled)
        {
            hologram = HologramEntity(displayName, physicalLocation!!.clone().add(0.0, 0.5, 0.0))
                .also { entity ->
                    entity.updateLines(hologramLines)

                    entity.initializeData()
                    EntityHandler.trackEntity(entity)
                }
        }
    }

    fun decommissionBukkit()
    {
        if (hologram != null)
        {
            hologram!!.destroyForCurrentWatchers()
            EntityHandler.forgetEntity(hologram!!)
        }
    }
}