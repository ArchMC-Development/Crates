package gg.scala.crates.crate.prize

import gg.scala.crates.itemStackFromBase64
import me.lucko.helper.random.Weighted
import net.evilblock.cubed.serializers.impl.AbstractTypeSerializable
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.function.UnaryOperator

/**
 * @author GrowlyX
 * @since 8/13/2022
 */
abstract class CratePrize(
    var name: String,
    val material: String,
    var weightInternal: Double,
    var description: MutableList<String>,
    var rarity: CratePrizeRarity = CratePrizeRarity.Common
) : UnaryOperator<Player>, Weighted, AbstractTypeSerializable
{
    override fun getWeight() = this.weightInternal
    abstract fun applicableTo(player: Player): Boolean

    fun getItemStack(): ItemStack = itemStackFromBase64(material)
}
