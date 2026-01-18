package gg.scala.crates.menu.editor

import com.cryptomorin.xseries.XMaterial
import gg.scala.crates.CratesSpigotPlugin
import gg.scala.crates.crate.Crate
import gg.scala.crates.crate.CrateService
import gg.scala.crates.crate.prize.CratePrizeRarity
import gg.scala.crates.crate.prize.composable.CompositeCratePrizeService
import gg.scala.crates.crate.prize.composable.test.CommandCratePrize
import gg.scala.crates.crate.prize.composable.test.ItemCratePrize
import gg.scala.crates.menu.editor.prize.CratePrizeCompositeEditorConfigureMenu
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.Menu
import net.evilblock.cubed.menu.pagination.PaginatedMenu
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.ItemBuilder
import net.evilblock.cubed.util.bukkit.Tasks
import okhttp3.internal.immutableListOf
import org.bukkit.Material
import org.bukkit.entity.Player

/**
 * @author GrowlyX
 * @since 8/14/2022
 */
class CrateEditorContentsMenu(
    private val crate: Crate,
    private val plugin: CratesSpigotPlugin
) : PaginatedMenu()
{
    init
    {
        placeholdBorders = true
        updateAfterClick = true
    }

    override fun size(buttons: Map<Int, Button>) = 45

    override fun getAllPagesButtonSlots() = mutableListOf<Int>()
        .apply {
            addAll(10..16)
            addAll(19..25)
            addAll(28..34)
        }

    override fun getGlobalButtons(player: Player): Map<Int, Button> = mutableMapOf<Int, Button>().also { buttons ->
        buttons[4] = ItemBuilder.of(XMaterial.CHEST)
            .name("${CC.GREEN}Use Inventory as Contents")
            .addToLore(
                "${CC.WHITE}Click to set the contents of this",
                "${CC.WHITE}crate to what is in your",
                "${CC.WHITE}inventory.",
                "",
                "${CC.RED}Note: For command-based prizes, insert",
                "${CC.RED}manually.",
                "",
                "${CC.YELLOW}Click to set crate contents"
            ).toButton { _, _ ->
                crate.prizes.clear()
                crate.prizes = player.inventory.storageContents.filter {
                    it != null && it.type != Material.AIR
                }.map { stack ->
                    val item = stack!!
                    val displayName =
                        if (item.hasItemMeta() && item.itemMeta.hasDisplayName()) item.itemMeta.displayName else item.type.name.split(
                            "_"
                        ).joinToString(" ") { string ->
                            string.lowercase().replaceFirstChar { it.uppercaseChar() }
                        }

                    ItemCratePrize(
                        displayName, item.clone(),
                        100.0, CratePrizeRarity.Common,
                        mutableListOf()
                    )
                }.toMutableList()

                CrateService.save(crate)
                player.sendMessage("${CC.GREEN}ok done.")
            }
    }

    override fun onClose(player: Player, manualClose: Boolean)
    {
        if (manualClose)
        {
            Tasks.delayed(1L)
            {
                CrateEditorMenu(this.crate, this.plugin).openMenu(player)
            }
        }
    }

    override fun getAllPagesButtons(player: Player): Map<Int, Button>
    {
        val buttons = mutableMapOf<Int, Button>()

        for (prize in crate.prizes.sortedByDescending { it.weight })
        {
            buttons[buttons.size] = ItemBuilder
                .copyOf(prize.getItemStack())
                .name("${CC.B_GOLD}${prize.name}")
                .addToLore(
                    "${CC.WHITE}Rarity: ${prize.rarity.chatColor}${prize.rarity.name}",
                    ""
                )
                .apply {
                    if (prize.description.isEmpty())
                    {
                        addToLore("${CC.RED}No description.")
                    } else
                    {
                        addToLore(*prize.description.toTypedArray())
                    }

                    addToLore("")
                    addToLore("${CC.WHITE}Rarity: ${CC.GOLD}${prize.rarity.name}")
                    addToLore("${CC.WHITE}Material: ${CC.GOLD}${prize.getItemStack().type.name}")
                    addToLore("${CC.WHITE}Weight: ${CC.GOLD}${prize.weightInternal}")
                    if (prize is CommandCratePrize)
                    {
                        addToLore("${CC.WHITE}Command: ${CC.GOLD}${prize.internalCommand}")
                    }
                    addToLore("")
                    addToLore("${CC.GREEN}Left-Click to edit!")
                    addToLore("${CC.RED}Right-Click to delete!")
                }
                .toButton { _, click ->
                    if (click?.isLeftClick == true)
                    {
                        val mapping = CompositeCratePrizeService
                            .composites[prize::class]!!

                        Tasks.sync {
                            CratePrizeCompositeEditorConfigureMenu(
                                this.crate, this.plugin, mapping,
                                mapping.createSessionFromExisting(prize),
                                prize, this
                            ).openMenu(player)
                        }
                    } else
                    {
                        this.crate.prizes.remove(prize)
                        CrateService.save(crate)

                        player.sendMessage("${CC.RED}Deleted item!")
                    }
                }
        }

        return buttons
    }

    override fun getPrePaginatedTitle(player: Player) = "Viewing crate ${crate.displayName}..."
}
