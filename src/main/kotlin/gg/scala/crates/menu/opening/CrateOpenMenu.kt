package gg.scala.crates.menu.opening

import gg.scala.commons.acf.ConditionFailedException
import gg.scala.crates.configuration
import gg.scala.crates.crate.Crate
import gg.scala.crates.crate.prize.CratePrize
import gg.scala.crates.keyProvider
import gg.scala.crates.sendDebug
import gg.scala.crates.sendToPlayer
import me.lucko.helper.Schedulers
import me.lucko.helper.random.RandomSelector
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.Menu
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.ItemBuilder
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import java.util.*

/**
 * @author GrowlyX
 * @since 8/14/2022
 */
class CrateOpenMenu(
    private val player: Player,
    private val crate: Crate
) : Menu(
    configuration.crateOpenTitle
)
{
    companion object
    {
        const val ITERATION_SPEED = 700L
        const val ITERATION_BUMP = 20L
    }

    private var crateRollStopped = false
    private var manuallyClosed = false

    private val applicable = this.crate.prizes
        .shuffled()
        .filter {
            /**
             * We have an option that allows players to receive the same
             * item twice from crates.
             *
             * This functionality is turned off by default.
             */
            (this.crate.applicable || it.applicableTo(this.player))
        }
        .toMutableList()

    private val itemsRequired = LinkedList<CratePrize>()
    private val selectedRandom: CratePrize

    private var expectedIterationAmount = 0
    private var iterationAmount = 0

    init
    {
        /**
         * Ensure there are items we can use to calculate our fancy logic here.
         *
         * If a crate is marked as do-not-check-for-applicable, this
         * won't ever be empty unless there are no items in the crate.
         *
         * We're throwing an exception as the menu is eagerly opened on
         * initialization, and there's no way to stop it from opening if
         * we don't throw an exception or callback something on
         * initialization, which would be pretty stupid.
         */
        if (applicable.isEmpty())
        {
            throw ConditionFailedException("You cannot win any more items from this crate.")
        }

        /**
         * Start our auto update interval at 10 ticks.
         *
         * This value is bumped up +ITERATION_BUMP over the
         * course of the multiple menu iterations.
         */
        autoUpdateInterval = 10L

        placeholdBorders = true
        autoUpdate = true

        sendDebug("=== Developer Debug ===")

        /**
         * Calculates the number of iterations required to meet
         * the final iteration speed of (currently) 700ms.
         *
         * Since one prize element is removed during each iteration,
         * we have to calculate this while compensating for the
         * growth of delay with the iteration speed.
         */
        while (autoUpdateInterval <= ITERATION_SPEED)
        {
            expectedIterationAmount += 1
            autoUpdateInterval += ITERATION_BUMP
        }

        sendDebug("Required by logic: $expectedIterationAmount")

        // randomly select an element based on its internal rarity/weight.
        this.selectedRandom = RandomSelector
            .weighted(applicable).pick()

        sendDebug("Selected: ${selectedRandom.name}")

        // ensure the iteration amount matches the amounts we have.
        // we remove the first entries of these required items every menu iteration.
        while (expectedIterationAmount > itemsRequired.size)
        {
            itemsRequired += applicable.shuffled()
            sendDebug("  - Added applicable: ${applicable.size}, now ${itemsRequired.size}")
        }

        // remove any extra elements at the end, we'll fill
        // this stuff in with our prize and some filler items later
        while (itemsRequired.size != expectedIterationAmount)
        {
            itemsRequired.removeLast()
            sendDebug("  - Removed element: now ${itemsRequired.size}")
        }

        val shuffled = applicable.shuffled()

        // compensate for empty space at the end
        // (we want it to be in the middle of the menu)
        itemsRequired.removeLast()
        itemsRequired += shuffled.take(5)

        itemsRequired.add(selectedRandom)
        itemsRequired += shuffled

        sendDebug("  - Added selected random: now ${itemsRequired.size}")
        sendDebug("=======================")

        // reset our auto-update interval after we decide the end choice
        autoUpdateInterval = 10L
    }

    /**
     * We'll use this for the fun rainbow-obfuscated slot logic.
     */
    private val availableColors = listOf(3, 2, 10, 4, 1, 13, 11, 9, 6, 14, 5)
    private var completedCompletionLogic = false

    override fun getButtons(player: Player): Map<Int, Button>
    {
        val buttons = mutableMapOf<Int, Button>()

        if (!completedCompletionLogic)
        {
            iterationAmount += 1

            // Ensure the refresh delay is less than the targeted iteration speed.
            if (manuallyClosed || autoUpdateInterval >= ITERATION_SPEED)
            {
                sendDebug(
                    "Went through $iterationAmount iterations, expected $expectedIterationAmount"
                )
                sendDebug("=======================")

                crateRollStopped = true
            } else
            {
                autoUpdateInterval += 20L
            }

            if (crateRollStopped)
            {
                // stopping auto update while we handle completion logic
                this.autoUpdate = false

                this.completedCompletionLogic = true
                this.selectedRandom.apply(player)

                configuration.crateWin.sendToPlayer(
                    player, "<cratePrizeName>" to this.selectedRandom.name
                )

                player.playSound(player.location, Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 1.0F, 1.0F)
                player.closeInventory()

                Schedulers.sync()
                    .callLater({
                        if (!player.isOnline)
                        {
                            return@callLater
                        }

                        player.closeInventory()
                    }, 100L)

                this.autoUpdateInterval = 50L
                this.autoUpdate = true
            } else
            {
                // shift last to first, pushes everything else forward
                this.itemsRequired.removeFirst()
                player.playSound(player.location, Sound.UI_BUTTON_CLICK, 1.0F, 1.0F)
            }
        }

        // add items in the current index to the button map
        for (index in 1..7)
        {
            // Use the selected random to prevent weird issues if there aren't any filler items.
            val prizeInIndex = if (this.crateRollStopped)
            {
                this.selectedRandom
            } else
            {
                this.itemsRequired
                    .getOrNull(index)
                    ?: continue
            }

            if (this.crateRollStopped && index != 4)
            {
                buttons[index] = Button.placeholder(
                    Material.LEGACY_STAINED_GLASS_PANE,
                    availableColors.random().toByte(),
                    "${CC.B_GOLD}You won!"
                )
                continue
            }

            buttons[index] = ItemBuilder
                .copyOf(prizeInIndex.getItemStack())
                .name("${
                    if (crateRollStopped && index == 4) CC.B_GOLD else CC.AQUA
                }${prizeInIndex.name}")
                .addToLore(
                    "${CC.GRAY}Rarity: ${prizeInIndex.rarity.chatColor}${prizeInIndex.rarity.name}"
                )
                .apply {
                    if (crateRollStopped && index == 4)
                    {
                        this.glow()
                    }
                }
                .toButton()
        }

        return buttons
    }

    override fun onClose(player: Player, manualClose: Boolean)
    {
        if (manualClose && !this.crateRollStopped)
        {
            this.manuallyClosed = true

            /**
             * Ensure players don't exploit crate functionality
             * by leaving after estimating their prize.
             *
             * We'll make sure that they aren't refunded if they
             * exit too late into the rolling animation.
             */
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
