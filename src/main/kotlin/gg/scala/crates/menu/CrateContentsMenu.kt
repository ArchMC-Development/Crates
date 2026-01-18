package gg.scala.crates.menu

import com.cryptomorin.xseries.XMaterial
import gg.scala.crates.configuration
import gg.scala.crates.crate.Crate
import gg.scala.crates.keyProvider
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.pagination.PaginatedMenu
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.ItemBuilder
import net.evilblock.cubed.util.bukkit.Tasks
import org.bukkit.ChatColor
import org.bukkit.entity.Player

/**
 * @author GrowlyX
 * @since 8/14/2022
 */
class CrateContentsMenu(
    private val crate: Crate
) : PaginatedMenu()
{
    init
    {
        placeholdBorders = true
        keepBottomMechanics = false
    }

    override fun size(buttons: Map<Int, Button>) = 45

    override fun getAllPagesButtonSlots() = mutableListOf<Int>()
        .apply {
            addAll(10..16)
            addAll(19..25)
            addAll(28..34)
        }

    override fun onClose(player: Player, manualClose: Boolean)
    {
        if (manualClose)
        {
            Tasks.delayed(1L)
            {
                CrateViewMenu.open(player)
            }
        }
    }

    override fun getGlobalButtons(player: Player): Map<Int, Button> = mutableMapOf<Int, Button>().also { buttons ->
        buttons[40] = ItemBuilder.of(XMaterial.TRIPWIRE_HOOK)
            .name("${CC.YELLOW}${crate.displayName}")
            .addToLore(
                "${CC.WHITE}You have ${CC.GREEN}${
                    keyProvider()
                        .getKeysFor(
                            player.uniqueId, crate
                        )
                } ${CC.WHITE}keys",
                "${CC.WHITE}that you can use for this",
                "${CC.WHITE}crate. Want more? Check",
                "${CC.WHITE}out our store.",
                "",
                "${CC.GREEN}store.arch.mc"
            ).toButton()
    }

    override fun getAllPagesButtons(player: Player): Map<Int, Button>
    {
        val buttons = mutableMapOf<Int, Button>()

        for (prize in crate.prizes.sortedByDescending { it.weight })
        {
            buttons[buttons.size] = ItemBuilder
                .copyOf(prize.getItemStack())
                .name("${CC.B_AQUA}${prize.name}")
                .apply {
                    if (prize.description.isNotEmpty())
                    {
                        addToLore(*prize.description.toTypedArray())
                    }
                }
                .toButton()
        }

        return buttons
    }

    override fun getPrePaginatedTitle(player: Player) = "Viewing Crate: ${crate.displayName}"
}
