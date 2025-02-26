package gg.scala.crates.player.sync

import com.google.gson.JsonObject
import lol.arch.survival.rootkit.action.PlayerAction
import org.bukkit.ChatColor
import org.bukkit.entity.Player

class SendCrateMessageAction: PlayerAction
{
    private lateinit var message: String

    override fun accept(metadata: JsonObject)
    {
        message = metadata.get("message").asString
    }

    override fun run(player: Player)
    {
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', message))
    }
}