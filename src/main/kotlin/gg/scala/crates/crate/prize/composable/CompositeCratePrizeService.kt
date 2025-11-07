package gg.scala.crates.crate.prize.composable

import gg.scala.crates.crate.prize.composable.test.CommandCompositeCratePrizeEditSession
import gg.scala.crates.crate.prize.composable.test.CommandCratePrize
import gg.scala.crates.crate.prize.composable.test.ItemCompositeCratePrizeEditSession
import gg.scala.crates.crate.prize.composable.test.ItemCratePrize
import gg.scala.flavor.service.Configure
import gg.scala.flavor.service.Service
import gg.scala.lemon.util.CallbackInputPrompt
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.ItemBuilder
import net.evilblock.cubed.util.bukkit.Tasks
import net.evilblock.cubed.util.bukkit.prompt.InputPrompt
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import kotlin.reflect.KClass

/**
 * @author GrowlyX
 * @since 8/13/2022
 */
@Service
object CompositeCratePrizeService
{
    val composites = mutableMapOf<KClass<*>, CompositeCratePrize>()

    @Configure
    fun configure()
    {
        composite<ItemCratePrize> {
            name = "Item"

            createSession {
                if (this != null)
                {
                    this as ItemCratePrize

                    return@createSession ItemCompositeCratePrizeEditSession(
                        name, internalStack, weight, rarity, description
                    )
                }

                ItemCompositeCratePrizeEditSession()
            }

            updatePrize { session, prize ->
                prize as ItemCratePrize
                session as ItemCompositeCratePrizeEditSession

                prize.name = session.name
                prize.internalStack = ItemStack(session.material)
                prize.weightInternal = session.weight
                prize.rarity = session.rarity
                prize.description = session.description
            }

            createPrize {
                this as ItemCompositeCratePrizeEditSession

                ItemCratePrize(
                    name, material,
                    weight, rarity,
                    description
                )
            }

            button { session, menu ->
                session as ItemCompositeCratePrizeEditSession

                ItemBuilder
                    .of(Material.PAPER)
                    .name("${CC.GREEN}Item Material")
                    .addToLore("${CC.GRAY}Current: ${CC.WHITE}${session.material.type.name}")
                    .toButton { player, _ ->
                        player!!.sendMessage("${CC.GREEN}Enter a material...")
                        player.closeInventory()

                        InputPrompt()
                            .acceptInput { _, s ->
                                session.material = ItemStack(Material.valueOf(s))
                                player.sendMessage("${CC.SEC}Set material to: ${CC.PRI}${session.material.type.name}")

                                menu.openMenu(player)
                            }
                            .start(player)
                    }
            }

            button { session, menu ->
                session as ItemCompositeCratePrizeEditSession

                ItemBuilder
                    .of(Material.BOOK)
                    .name("${CC.GREEN}Use Held Item")
                    .addToLore("${CC.GRAY}Current: ${CC.WHITE}${session.material.type.name}")
                    .toButton { player, _ ->
                        if (player!!.inventory.itemInMainHand.type == Material.AIR)
                        {
                            player.sendMessage("${CC.RED}You must be holding an item in order to add it!")
                            return@toButton
                        }

                        session.material = player.inventory.itemInMainHand
                        session.name = LegacyComponentSerializer.legacySection()
                            .serializeOrNull(player.inventory.itemInMainHand.displayName())
                            ?.replace("[", "")
                            ?.replace("]", "")
                            ?: player.inventory.itemInMainHand.type.name.split("_")
                                .joinToString { string ->
                                    string.lowercase().replaceFirstChar { it.uppercase() }
                                }
                        player.sendMessage("${CC.SEC}Set material to: ${CC.PRI}${session.material.type.name}")

                        Tasks.sync {
                            menu.openMenu(player)
                        }
                    }
            }
        }

        composite<CommandCratePrize> {
            name = "Command"

            createSession {
                if (this != null)
                {
                    this as CommandCratePrize

                    return@createSession CommandCompositeCratePrizeEditSession(
                        name, internalStack, internalCommand, weight, rarity, description
                    )
                }

                CommandCompositeCratePrizeEditSession()
            }

            updatePrize { session, prize ->
                prize as CommandCratePrize
                session as CommandCompositeCratePrizeEditSession

                prize.name = session.name
                prize.internalStack = ItemStack(session.material)
                prize.weightInternal = session.weight
                prize.rarity = session.rarity
                prize.description = session.description
                prize.internalCommand = session.command
            }

            createPrize {
                this as CommandCompositeCratePrizeEditSession

                CommandCratePrize(
                    name, material, command,
                    weight, rarity,
                    description
                )
            }

            button { session, menu ->
                session as CommandCompositeCratePrizeEditSession

                ItemBuilder
                    .of(Material.PAPER)
                    .name("${CC.GREEN}Item Material")
                    .addToLore("${CC.GRAY}Current: ${CC.WHITE}${session.material.type.name}")
                    .toButton { player, _ ->
                        player!!.sendMessage("${CC.GREEN}Enter a material...")
                        player.closeInventory()

                        InputPrompt()
                            .acceptInput { _, s ->
                                session.material = ItemStack(Material.valueOf(s))
                                player.sendMessage("${CC.SEC}Set material to: ${CC.PRI}${session.material.type.name}")

                                menu.openMenu(player)
                            }
                            .start(player)
                    }
            }

            button { session, menu ->
                session as CommandCompositeCratePrizeEditSession

                ItemBuilder
                    .of(Material.BOOK)
                    .name("${CC.GREEN}Use Held Item")
                    .addToLore("${CC.GRAY}Current: ${CC.WHITE}${session.material.type.name}")
                    .toButton { player, _ ->
                        if (player!!.inventory.itemInMainHand.type == Material.AIR)
                        {
                            player.sendMessage("${CC.RED}You must be holding an item in order to add it!")
                            return@toButton
                        }

                        session.material = player.inventory.itemInMainHand
                        session.name = LegacyComponentSerializer.legacySection()
                            .serializeOrNull(player.inventory.itemInMainHand.displayName())
                            ?: player.inventory.itemInMainHand.type.name.split("_")
                                .joinToString { string ->
                                    string.lowercase().replaceFirstChar { it.uppercase() }
                                }
                        player.sendMessage("${CC.SEC}Set material to: ${CC.PRI}${session.material.type.name}")

                        Tasks.sync {
                            menu.openMenu(player)
                        }
                    }
            }

            button { session, menu ->
                session as CommandCompositeCratePrizeEditSession

                ItemBuilder
                    .of(Material.COMMAND_BLOCK)
                    .name("${CC.GREEN}Set Command")
                    .addToLore("${CC.GRAY}Current: ${CC.WHITE}${session.command}")
                    .toButton { player, _ ->
                        CallbackInputPrompt("${CC.GREEN}Enter the command that you want to be executed when a user wins this crate (Use <player> as the player placeholder):") {
                            session.command = it
                            player!!.sendMessage("${CC.SEC}Set command to: ${CC.PRI}${session.command}")

                            menu.openMenu(player)
                        }
                    }
            }
        }
    }
}
