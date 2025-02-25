package gg.scala.crates

import me.lucko.helper.utils.Players
import net.evilblock.cubed.util.Color
import org.bukkit.inventory.ItemStack
import org.bukkit.util.io.BukkitObjectInputStream
import org.bukkit.util.io.BukkitObjectOutputStream
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

import org.bukkit.entity.Player

/**
 * @author GrowlyX
 * @since 8/14/2022
 */
lateinit var configuration: CratesSpigotConfig
lateinit var plugin: CratesSpigotPlugin

fun keyProvider() = plugin.keyProvider

fun itemStackToBase64(item: ItemStack): String
{
    try
    {
        val outputStream: ByteArrayOutputStream = ByteArrayOutputStream()
        val dataOutput = BukkitObjectOutputStream(outputStream)
        dataOutput.writeObject(item)
        dataOutput.close()
        return String(Base64Coder.encode(outputStream.toByteArray()))
    } catch (e: Exception)
    {
        throw IllegalStateException("err", e)
    }
}

fun itemStackFromBase64(data: String): ItemStack
{
    try
    {
        val inputStream = ByteArrayInputStream(Base64Coder.decode(data))
        val dataInput = BukkitObjectInputStream(inputStream)
        val item = dataInput.readObject() as ItemStack
        dataInput.close()
        return item
    } catch (e: java.lang.Exception)
    {
        throw java.lang.IllegalStateException("err", e)
    }
}

fun List<String>.sendToPlayer(
    player: Player,
    vararg replacements: Pair<String, String>
)
{
    for (line in this)
    {
        line.sendToPlayer(player, *replacements)
    }
}

fun String.sendToPlayer(
    player: Player,
    vararg replacements: Pair<String, String>
)
{
    var cached = this

    for (replacement in replacements)
    {
        cached = cached.replace(
            replacement.first, replacement.second
        )
    }

    player.sendMessage(
        Color.translate(cached)
    )
}

fun sendDebug(message: String)
{
    if (configuration.debugMode)
    {
        Players.all()
            .filter { it.isOp }
            .forEach {
                it.sendMessage(message)
            }
    }
}
