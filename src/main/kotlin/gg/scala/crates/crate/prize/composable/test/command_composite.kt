package gg.scala.crates.crate.prize.composable.test

import gg.scala.crates.crate.prize.CratePrize
import gg.scala.crates.crate.prize.CratePrizeRarity
import gg.scala.crates.crate.prize.composable.CompositeCratePrizeEditSession
import gg.scala.crates.itemStackToBase64
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

/**
 * @author GrowlyX
 * @since 8/13/2022
 */
class CommandCratePrize(
    name: String,
    var internalStack: ItemStack,
    var internalCommand: String,
    weight: Double,
    rarity: CratePrizeRarity,
    description: MutableList<String>
) : CratePrize(
    name, itemStackToBase64(internalStack), weight, description, rarity
)
{
    override fun getAbstractType() = CommandCratePrize::class.java
    override fun applicableTo(player: Player) = true

    override fun apply(player: Player): Player
    {
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), this.internalCommand.replace("<player>", player.name))
        return player
    }
}

class CommandCompositeCratePrizeEditSession(
    override var name: String = "some item",
    var material: ItemStack = ItemStack(Material.ACACIA_DOOR),
    var command: String = "test <player>",
    override var weight: Double = 100.0,
    override var rarity: CratePrizeRarity = CratePrizeRarity.Common,
    override var description: MutableList<String> = mutableListOf()
) : CompositeCratePrizeEditSession
