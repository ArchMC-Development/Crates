package gg.scala.crates.menu.editor

import gg.scala.crates.CratesSpigotPlugin
import gg.scala.crates.crate.Crate
import gg.scala.crates.crate.CrateService
import gg.scala.crates.menu.editor.prize.CratePrizeCompositeEditorContextMenu
import gg.scala.lemon.util.CallbackInputPrompt
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.Menu
import net.evilblock.cubed.menu.buttons.AddButton
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.Color
import net.evilblock.cubed.util.bukkit.ItemBuilder
import net.evilblock.cubed.util.bukkit.Tasks
import net.evilblock.cubed.util.bukkit.prompt.InputPrompt
import org.bukkit.Material
import org.bukkit.entity.Player

/**
 * @author GrowlyX
 * @since 8/14/2022
 */
class CrateEditorMenu(
    private val crate: Crate,
    private val plugin: CratesSpigotPlugin
) : Menu("Editing crate ${crate.displayName}${CC.GRAY}...")
{
    init
    {
        updateAfterClick = true
        placeholder = true
    }

    override fun size(buttons: Map<Int, Button>) = 36

    override fun onClose(player: Player, manualClose: Boolean)
    {
        if (manualClose)
        {
            Tasks.delayed(1L) {
                CrateEditorViewMenu(plugin).openMenu(player)
            }
        }
    }

    override fun getButtons(player: Player): Map<Int, Button>
    {
        val buttons = mutableMapOf<Int, Button>()

        buttons[10] = ItemBuilder
            .of(Material.OAK_SIGN)
            .name("${CC.GREEN}Display Name")
            .addToLore(
                "${CC.GRAY}Current:",
                "${CC.WHITE}${this.crate.displayName}",
                "",
                "${CC.YELLOW}Click to edit!"
            )
            .toButton { _, _ ->
                player.closeInventory()

                InputPrompt()
                    .withText("${CC.GREEN}Enter a display name...")
                    .acceptInput { _, displayName ->
                        this.crate.displayName =
                            Color.translate(displayName)

                        CrateService.save(crate)
                        openMenu(player)

                        player.sendMessage(
                            "${CC.SEC}Set new display name: ${CC.PRI}${this.crate.displayName}"
                        )
                    }
                    .start(player)
            }

        buttons[11] = ItemBuilder
            .of(Material.COOKED_PORKCHOP)
            .name("${CC.GREEN}Manage Items")
            .addToLore(
                "${CC.GRAY}View all crate prizes.",
                "",
                "${CC.YELLOW}Click to manage!"
            )
            .toButton { _, _ ->
                Tasks.sync {
                    CrateEditorContentsMenu(crate, plugin).openMenu(player)
                }
            }

        buttons[12] = ItemBuilder
            .of(Material.GOLDEN_CARROT)
            .name("${CC.GREEN}Applicable status")
            .addToLore(
                "${CC.GRAY}Allow players to receive same items multiple times.",
                "",
                "${CC.WHITE}Current: ${CC.GREEN}${crate.applicable}",
                "",
                "${CC.YELLOW}Click to toggle"
            )
            .toButton { _, _ ->
                crate.applicable = !crate.applicable
                CrateService.save(crate)

                player.sendMessage("${CC.GREEN}ok done ${crate.applicable}")
            }

        buttons[13] = ItemBuilder
            .copyOf(
                object : AddButton()
                {}.getButtonItem(player)
            )
            .name("${CC.GREEN}Add Items")
            .addToLore(
                "${CC.GRAY}Add a new crate prize.",
                "",
                "${CC.YELLOW}Click to add items!"
            )
            .toButton { _, _ ->
                Tasks.sync {
                    CratePrizeCompositeEditorContextMenu(crate, plugin, this).openMenu(player)
                }
            }

        buttons[14] = ItemBuilder
            .of(Material.BLAZE_POWDER)
            .name("${CC.GREEN}Select Items")
            .addToLore(
                "${CC.GRAY}Allow players to select the items they want.",
                "",
                "${CC.WHITE}Current: ${CC.GREEN}${crate.isSelectItem}",
                "",
                "${CC.YELLOW}Click to toggle"
            )
            .toButton { _, _ ->
                crate.isSelectItem = !crate.isSelectItem
                CrateService.save(crate)

                player.sendMessage("${CC.GREEN}ok done ${crate.isSelectItem}")
            }

        buttons[15] = ItemBuilder
            .of(Material.DIRT)
            .name("${CC.GREEN}Physical Location")
            .addToLore(
                "${CC.GRAY}Allow the crate to be at a physical location.",
                "",
                "${CC.WHITE}Current: ${CC.GREEN}${if (crate.physicalLocation != null) "${crate.physicalLocation!!.blockX},${crate.physicalLocation!!.blockY},${crate.physicalLocation!!.blockZ}" else "None"}",
                "",
                "${CC.YELLOW}Left-Click to set",
                "${CC.RED}Right-Click to clear"
            )
            .toButton { _, click ->
                if (click?.isLeftClick == true)
                {
                    crate.physicalLocation = player.location.toBlockLocation()
                } else
                {
                    crate.physicalLocation = null
                }

                CrateService.save(crate)
                CrateService.reinitializeCrates()
                player.sendMessage("${CC.GREEN}ok done")
            }

        buttons[16] = ItemBuilder
            .of(Material.OAK_SIGN)
            .name("${CC.GREEN}Enable Hologram")
            .addToLore(
                "${CC.GRAY}Enables the use of a crate hologram.",
                "",
                "${CC.WHITE}Current: ${CC.WHITE}${if (crate.isHologramEnabled) "${CC.GREEN}Enabled" else "${CC.RED}Disabled"}",
                "",
                "${CC.YELLOW}Left-Click to set",
            )
            .toButton { _, _ ->
                crate.isHologramEnabled = !crate.isHologramEnabled

                CrateService.save(crate)
                CrateService.reinitializeCrates()
                player.sendMessage("${CC.GREEN}ok done")
            }

        buttons[19] = ItemBuilder
            .of(Material.REDSTONE)
            .name("${CC.GREEN}Compressed Menu")
            .addToLore(
                "${CC.GRAY}Enables the use of a compressed menu.",
                "",
                "${CC.WHITE}Current: ${CC.WHITE}${if (crate.shouldCompressMenu) "${CC.GREEN}Enabled" else "${CC.RED}Disabled"}",
                "",
                "${CC.YELLOW}Left-Click to set",
            )
            .toButton { _, _ ->
                crate.shouldCompressMenu = !crate.shouldCompressMenu

                CrateService.save(crate)
                player.sendMessage("${CC.GREEN}ok done")
            }

        buttons[20] = ItemBuilder
            .of(Material.ENDER_CHEST)
            .name("${CC.GREEN}Crate Group")
            .addToLore(
                "${CC.GRAY}Enables the use of grouped crates.",
                "",
                "${CC.WHITE}Current: ${CC.WHITE}${if (crate.group != null) crate.group else "${CC.RED}No group"}",
                "",
                "${CC.YELLOW}Left-Click to set",
            )
            .toButton { _, _ ->
                CallbackInputPrompt("${CC.GREEN}Enter new crate group") { input ->
                    crate.group = input

                    CrateService.save(crate)
                    player.sendMessage("${CC.GREEN}ok done")
                }.start(player)
            }


        return buttons
    }
}
