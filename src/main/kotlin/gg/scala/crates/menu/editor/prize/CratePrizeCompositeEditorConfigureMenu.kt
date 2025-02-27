package gg.scala.crates.menu.editor.prize

import com.cryptomorin.xseries.XMaterial
import gg.scala.crates.CratesSpigotPlugin
import gg.scala.crates.crate.Crate
import gg.scala.crates.crate.CrateService
import gg.scala.crates.crate.prize.CratePrize
import gg.scala.crates.crate.prize.CratePrizeRarity
import gg.scala.crates.crate.prize.composable.CompositeCratePrize
import gg.scala.crates.crate.prize.composable.CompositeCratePrizeEditSession
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.Menu
import net.evilblock.cubed.menu.buttons.AddButton
import net.evilblock.cubed.menu.menus.TextEditorMenu
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.Color
import net.evilblock.cubed.util.bukkit.ItemBuilder
import net.evilblock.cubed.util.bukkit.Tasks
import net.evilblock.cubed.util.bukkit.prompt.InputPrompt
import net.evilblock.cubed.util.bukkit.prompt.NumberPrompt
import org.bukkit.Material
import org.bukkit.entity.Player

/**
 * @author GrowlyX
 * @since 8/13/2022
 */
class CratePrizeCompositeEditorConfigureMenu(
    private val crate: Crate,
    private val plugin: CratesSpigotPlugin,
    private val composite: CompositeCratePrize,
    private val session: CompositeCratePrizeEditSession,
    private val existingItem: CratePrize? = null,
    private val fallback: Menu? = null
) : Menu("Configure your prize details")
{
    init
    {
        updateAfterClick = true
        placeholdBorders = true
    }

    override fun size(buttons: Map<Int, Button>) = 36

    override fun onClose(player: Player, manualClose: Boolean)
    {
        if (manualClose)
        {
            player.sendMessage("${CC.RED}Discarded your prize configuration.")

            Tasks.delayed(1L)
            {
                if (fallback != null)
                {
                    fallback.openMenu(player)
                    return@delayed
                }

                CratePrizeCompositeEditorContextMenu(crate, this.plugin).openMenu(player)
            }
        }
    }

    override fun getButtons(player: Player): Map<Int, Button>
    {
        val buttons = mutableMapOf<Int, Button>()

        val mappings = mutableListOf(
            "General" to listOf(
                ItemBuilder
                    .of(Material.PAPER)
                    .name("${CC.GREEN}Prize Name")
                    .addToLore("${CC.GRAY}Current: ${CC.WHITE}${this.session.name}")
                    .toButton { _, _ ->
                        player.closeInventory()

                        InputPrompt()
                            .withText("${CC.GREEN}Enter a new prize name...")
                            .acceptInput { _, text ->
                                this.session.name = Color.translate(text)
                                player.sendMessage("${CC.SEC}Set prize name to: ${CC.PRI}${this.session.name}")

                                this.openMenu(player)
                            }
                            .start(player)
                    },
                ItemBuilder
                    .of(Material.OAK_SIGN)
                    .name("${CC.GREEN}Prize Description")
                    .addToLore("${CC.GRAY}Current:")
                    .apply {
                        addToLore(*session.description.toTypedArray())
                    }
                    .toButton { _, _ ->
                        Tasks.sync {
                            object : TextEditorMenu(session.description)
                            {
                                override fun getPrePaginatedTitle(player: Player) = "Edit prize description..."

                                override fun onClose(player: Player)
                                {
                                    this@CratePrizeCompositeEditorConfigureMenu.openMenu(player)
                                }

                                override fun onSave(player: Player, list: List<String>)
                                {
                                    session.description.clear()
                                    session.description.addAll(list)

                                    player.sendMessage("${CC.GREEN}Saved changes to description!")
                                }
                            }.openMenu(player)
                        }
                    },
                ItemBuilder
                    .of(Material.CHEST)
                    .name("${CC.GREEN}Prize Weight")
                    .addToLore("${CC.GRAY}Current: ${CC.WHITE}${this.session.weight}")
                    .toButton { _, _ ->
                        player.sendMessage("${CC.GREEN}Enter a prize weight...")
                        player.closeInventory()

                        NumberPrompt()
                            .acceptInput {
                                this.session.weight = it.toDouble()
                                player.sendMessage("${CC.SEC}Set prize weight to: ${CC.PRI}${this.session.weight}")

                                this.openMenu(player)
                            }
                            .start(player)
                    },
                MultiOptionPlayerSettingsBuilder()
                    .titleOf("${CC.GREEN}Prize Rarity")
                    .materialOf(XMaterial.ENDER_EYE)
                    .descriptionOf(
                        "${CC.GRAY}What rarity do you want?"
                    )
                    .orderedValuesOf(
                        "Legendary",
                        "Rare",
                        "Common"
                    )
                    .fallbackOf("Common")
                    .providerOverrideOf { _, _ ->
                        this.session.rarity.name
                    }
                    .valueOverrideOf {
                        this.session.rarity = CratePrizeRarity.valueOf(it)
                    }
                    .asButton()
            ),
            "Custom" to this.composite.editorButtons(this.session, this)
        )

        val indexes = listOf(10, 2, 3, 1)

        for ((index, category) in mappings.withIndex())
        {
            buttons[(9 * (index + 1))] = ItemBuilder
                .of(Material.LEGACY_STAINED_GLASS_PANE)
                .data((indexes[index]).toShort())
                .name("${CC.AQUA}${category.first}")
                .toButton()

            buttons[(17 + (index * 9))] = ItemBuilder
                .of(Material.AIR)
                .toButton()

            for ((buttonIndex, button) in category.second.withIndex())
            {
                buttons[buttonIndex + (10 + (index * 9))] = button
            }
        }

        buttons[4] = ItemBuilder
            .copyOf(
                object : AddButton()
                {}.getButtonItem(player)
            )
            .name("${CC.GREEN}Commit item changes")
            .addToLore(
                "${CC.GRAY}Add item to crate!",
                "",
                "${CC.YELLOW}Click to add!",
            )
            .toButton { _, _ ->
                if (this.existingItem != null)
                {
                    this.composite.update(this.session, this.existingItem)
                } else
                {
                    val prize = this.composite
                        .create(this.session)

                    this.crate.prizes.add(prize)
                }

                kotlin
                    .runCatching {
                        CrateService.saveConfig()
                    }
                    .onFailure {
                        it.printStackTrace()
                    }

                if (fallback != null)
                {
                    fallback.openMenu(player)
                } else
                {
                    player.closeInventory()
                }

                player.sendMessage("${CC.GREEN}Added/updated item to crate ${CC.SEC}${crate.uniqueId}${CC.GREEN}!")

                Tasks.delayed(1L)
                {
                    if (fallback != null)
                    {
                        fallback.openMenu(player)
                        return@delayed
                    }

                    CratePrizeCompositeEditorContextMenu(crate, this.plugin).openMenu(player)
                }
            }

        return buttons
    }
}
