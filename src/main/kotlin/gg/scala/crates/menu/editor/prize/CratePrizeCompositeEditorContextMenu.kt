package gg.scala.crates.menu.editor.prize

import gg.scala.crates.CratesSpigotPlugin
import gg.scala.crates.crate.Crate
import gg.scala.crates.crate.prize.composable.CompositeCratePrizeService
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.Menu
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.ItemBuilder
import net.evilblock.cubed.util.bukkit.Tasks
import org.bukkit.Material
import org.bukkit.entity.Player

/**
 * @author GrowlyX
 * @since 8/13/2022
 */
class CratePrizeCompositeEditorContextMenu(
    private val crate: Crate,
    private val plugin: CratesSpigotPlugin,
    private val fallback: Menu? = null
) : Menu("Choose a prize type")
{
    init
    {
        placeholdBorders = true
    }

    override fun size(buttons: Map<Int, Button>) = 27

    override fun onClose(player: Player, manualClose: Boolean)
    {
        if (manualClose && fallback != null)
        {
            Tasks.delayed(1L)
            {
                fallback.openMenu(player)
            }
        }
    }

    override fun getButtons(player: Player): Map<Int, Button>
    {
        val buttons = mutableMapOf<Int, Button>()

        for (composite in CompositeCratePrizeService.composites.values)
        {
            buttons[10 + buttons.size] = ItemBuilder
                .of(Material.PAPER)
                .name("${CC.B_AQUA}${composite.getName()}")
                .addToLore(
                    "${CC.GRAY}Click to use this as a template."
                )
                .toButton { _, _ ->
                    Tasks.sync {
                        CratePrizeCompositeEditorConfigureMenu(
                            crate, plugin, composite, composite.createSession()
                        ).openMenu(player)
                    }
                }
        }

        return buttons
    }
}
