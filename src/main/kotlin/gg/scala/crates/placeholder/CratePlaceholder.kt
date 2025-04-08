package gg.scala.crates.placeholder

import gg.scala.crates.player.CratesPlayerService
import me.clip.placeholderapi.expansion.PlaceholderExpansion
import org.bukkit.entity.Player

class CratePlaceholder : PlaceholderExpansion()
{
    override fun getIdentifier(): String = "crates"
    override fun getAuthor(): String = "ArchMC"
    override fun getVersion(): String = "1.0.0"

    override fun onPlaceholderRequest(player: Player, params: String): String
    {
        val profile = CratesPlayerService.find(player)
            ?: return "0"

        return when (params)
        {
            "keys" ->
            {
                return profile.balances.values.sum().toString()
            }

            "count" ->
            {
                val crate = params.split("_").getOrNull(2)
                    ?: return "0"

                return profile.balances[crate]?.toString() ?: "0"
            }

            else ->
            {
                "0"
            }
        }
    }
}