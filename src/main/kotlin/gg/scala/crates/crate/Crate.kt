package gg.scala.crates.crate

import gg.scala.crates.crate.prize.CratePrize
import org.bukkit.Location

/**
 * @author GrowlyX
 * @since 8/13/2022
 */
data class Crate(
    val uniqueId: String,
    var displayName: String,
    val prizes: MutableList<CratePrize>,
    var applicable: Boolean = false,
    var isSelectItem: Boolean = false,
    var physicalLocation: Location? = null
)