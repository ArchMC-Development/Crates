package gg.scala.crates.menu.opening

import gg.scala.crates.configuration
import gg.scala.crates.crate.Crate
import gg.scala.crates.keyProvider
import gg.scala.crates.sendToPlayer
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.Menu
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.ItemBuilder
import org.bukkit.Sound
import org.bukkit.entity.Player

class CrateSelectRewardOpenMenu(
    private val player: Player,
    private val crate: Crate
) : Menu(
    configuration.crateOpenTitle
)
{
    init
    {
        autoUpdateInterval = 100L
        keepBottomMechanics = false
    }

    private val applicable = this.crate.prizes
        .shuffled()
        .filter {
            (this.crate.applicable || it.applicableTo(this.player))
        }
        .toMutableList()


    override fun getButtons(player: Player): Map<Int, Button>
    {
        val buttons = mutableMapOf<Int, Button>()

        applicable.forEach {
            buttons[buttons.size] = ItemBuilder
                .copyOf(it.getItemStack())
                .name(
                    "${
                        CC.YELLOW
                    }${it.name}"
                )
                .addToLore(
                    "${CC.GRAY}Rarity: ${it.rarity.chatColor}${it.rarity.name}",
                    "",
                    "${CC.YELLOW}Click to select!"
                )
                .apply {
                    this.glow()
                }
                .toButton { _, _ ->
                    it.apply(player)

                    configuration.crateWin.sendToPlayer(
                        player, "<cratePrizeName>" to it.name
                    )

                    player.playSound(player.location, Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 1.0F, 1.0F)
                    player.closeInventory()
                }
        }

        return buttons
    }

    override fun onClose(player: Player, manualClose: Boolean)
    {
        if (manualClose)
        {
            if (this.autoUpdateInterval >= 400)
            {
                configuration.crateWinRefundFailure.sendToPlayer(player)
                return
            }

            configuration.crateWinRefundSuccess.sendToPlayer(player)
            this.refundCrateKey(player)
        }
    }

    private fun refundCrateKey(player: Player)
    {
        keyProvider().addKeysFor(player.uniqueId, crate.uniqueId, 1)
    }
}
