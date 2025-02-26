package gg.scala.crates.command

import com.google.gson.JsonObject
import gg.scala.commons.acf.CommandHelp
import gg.scala.commons.acf.annotation.*
import gg.scala.commons.annotations.commands.AutoRegister
import gg.scala.commons.command.ScalaCommand
import gg.scala.crates.CratesSpigotPlugin
import gg.scala.crates.configuration
import gg.scala.crates.crate.Crate
import gg.scala.crates.crate.CrateService
import gg.scala.crates.keyProvider
import gg.scala.crates.menu.editor.CrateEditorViewMenu
import gg.scala.crates.player.sync.SendCrateMessageAction
import gg.scala.flavor.inject.Inject
import gg.scala.lemon.player.wrapper.AsyncLemonPlayer
import gg.scala.lemon.util.QuickAccess
import lol.arch.survival.rootkit.action.PlayerActionDispatcher
import net.evilblock.cubed.util.CC
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.concurrent.CompletableFuture

/**
 * @author GrowlyX
 * @since 8/13/2022
 */
@AutoRegister
@CommandAlias("crates-manage|cm|cratesadmin")
@CommandPermission("crates.command.manage")
object CratesManageCommand : ScalaCommand()
{
    @Inject
    lateinit var plugin: CratesSpigotPlugin

    @Default
    @HelpCommand
    fun onHelp(help: CommandHelp)
    {
        help.showHelp()
    }

    @Subcommand("give-key")
    @CommandCompletion("@players @crates")
    fun onGiveKey(
        player: CommandSender, target: AsyncLemonPlayer,
        crate: Crate, amount: Int
    ): CompletableFuture<Void>
    {
        return target.validatePlayers(player, false) { lemonPlayer ->
            keyProvider().addKeysFor(lemonPlayer.uniqueId, crate.uniqueId, amount)

            player.sendMessage(
                "${CC.SEC}Gave ${
                    QuickAccess.computeColoredName(
                        lemonPlayer.uniqueId,
                        lemonPlayer.name
                    )
                }${CC.SEC} ${CC.PRI}$amount${CC.SEC} crate keys."
            )

            PlayerActionDispatcher.send(lemonPlayer.uniqueId, SendCrateMessageAction(), JsonObject().also {
                it.addProperty(
                    "message",
                    configuration.crateBalanceChange.replace("<crateDisplayName>", crate.displayName)
                        .replace("<amount>", amount.toString())
                )
            })
        }

    }

    @Subcommand("control-panel")
    fun onControlPanel(player: Player)
    {
        CrateEditorViewMenu(this.plugin).openMenu(player)
    }

    @Subcommand("reload-config")
    fun onConfigReload(player: CommandSender)
    {
        CrateService.loadConfig()

        player.sendMessage("${CC.GREEN}Reloaded config.")
    }
}
