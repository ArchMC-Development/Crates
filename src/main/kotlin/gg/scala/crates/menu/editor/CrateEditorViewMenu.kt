package gg.scala.crates.menu.editor

import gg.scala.crates.CratesSpigotPlugin
import gg.scala.crates.configuration
import gg.scala.crates.crate.Crate
import gg.scala.crates.crate.CrateService
import gg.scala.crates.sendToPlayer
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.buttons.AddButton
import net.evilblock.cubed.menu.menus.ConfirmMenu
import net.evilblock.cubed.menu.pagination.PaginatedMenu
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.ItemBuilder
import net.evilblock.cubed.util.bukkit.Tasks
import net.evilblock.cubed.util.bukkit.prompt.InputPrompt
import org.bukkit.Material
import org.bukkit.entity.Player

/**
 * @author GrowlyX
 * @since 8/14/2022
 */
class CrateEditorViewMenu(
    private val plugin: CratesSpigotPlugin
) : PaginatedMenu()
{
    init
    {
        placeholdBorders = true
        updateAfterClick = true
    }

    override fun size(buttons: Map<Int, Button>) = 36

    override fun getAllPagesButtonSlots() = mutableListOf<Int>()
        .apply {
            addAll(10..16)
            addAll(19..25)
            addAll(28..34)
        }

    override fun getGlobalButtons(player: Player): Map<Int, Button>
    {
        val buttons = mutableMapOf<Int, Button>()

        buttons[4] = ItemBuilder
            .copyOf(
                object : AddButton()
                {}.getButtonItem(player)
            )
            .name("${CC.B_GREEN}Create a Crate")
            .addToLore(
                "${CC.GRAY}Create a new crate.",
                "",
                "${CC.GREEN}Click to create!",
            )
            .toButton { _, _ ->
                player.closeInventory()

                InputPrompt()
                    .withText("${CC.GREEN}Enter a unique id...")
                    .acceptInput { _, uniqueId ->
                        if (uniqueId.contains(" "))
                        {
                            player.sendMessage("${CC.RED}Unique IDs must not contain spaces!")
                            return@acceptInput
                        }

                        val crate = Crate(
                            uniqueId = uniqueId,
                            displayName = "${CC.B_AQUA}Example crate",
                            prizes = mutableListOf()
                        )

                        kotlin
                            .runCatching {
                                CrateService.save(crate)
                            }
                            .onSuccess {
                                configuration.crateCreated.sendToPlayer(player)
                                openMenu(player)
                            }
                            .onFailure {
                                it.printStackTrace()
                                configuration.internalError.sendToPlayer(player)
                            }
                    }
                    .start(player)
            }

        return buttons
    }

    override fun getAllPagesButtons(player: Player): Map<Int, Button>
    {
        val buttons = mutableMapOf<Int, Button>()

        CrateService.allCrates()
            .forEach {
                buttons[buttons.size] = ItemBuilder
                    .of(Material.CHEST)
                    .name("${CC.B_AQUA}${it.uniqueId}")
                    .addToLore(
                        "${CC.GRAY}Display name:",
                        "${CC.WHITE}${it.displayName}",
                        "",
                        "${CC.GRAY}Crate prizes: ${CC.WHITE}${it.prizes.size} prizes",
                        "",
                        "${CC.GREEN}Click to edit crate!",
                        "${CC.RED}Right-Click to delete crate!",
                    )
                    .toButton { _, type ->
                        if (type!!.isRightClick)
                        {
                            Tasks.sync {
                                ConfirmMenu(confirm = true) { option ->
                                    if (!option)
                                    {
                                        configuration.crateDeletionQuit.sendToPlayer(player)

                                        Tasks.delayed(1L)
                                        {
                                            openMenu(player)
                                        }
                                        return@ConfirmMenu
                                    }

                                    CrateService.delete(it.uniqueId)

                                    configuration.crateDeletionSuccess.sendToPlayer(player)
                                }.openMenu(player)
                            }
                            return@toButton
                        }

                        Tasks.sync {
                            CrateEditorMenu(it, plugin).openMenu(player)
                        }
                    }
            }

        return buttons
    }

    override fun getPrePaginatedTitle(player: Player) = "Viewing crates"
}
