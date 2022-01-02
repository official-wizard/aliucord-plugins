package net.navibot.aliucord.plugins

import com.discord.models.message.Message
import com.discord.stores.StoreChat
import com.discord.widgets.chat.MessageContent
import com.discord.widgets.chat.list.entries.MessageEntry
import net.navibot.aliucord.plugins.error.ParseException
import java.lang.reflect.Modifier


fun MessageContent.set(text: String) {
    val field = MessageContent::class.java.getDeclaredField("textContent").apply {
        isAccessible = true
    }


    field.set(this, text)
}

fun Message.set(text: String) {
    val field = MessageEntry::class.java.getDeclaredField("content").apply {
        isAccessible = true
        setInt(this, modifiers and Modifier.FINAL.inv())
    }

    field.set(this, text)
}

class ColorUtils {
    companion object {
        private val map = (('A'..'F').mapIndexed { i, c -> Pair(c, "\u200B".repeat(i + 1)) }.toMap() +
                ('0'..'9').mapIndexed { i, c -> Pair(c, "\u200E".repeat(i + 1)) }.toMap())

        fun encode(hex: String): String {
            val builder = StringBuilder()

            hex.forEach { c ->
                builder.append(map[c.uppercaseChar()] ?: throw ParseException("Invalid HEX Provided!")).append("\u200C")
            }

            return "\u200D$builder\u200D"
        }

        fun isDiscordEmote(data: String) :  Boolean {
            return data.matches(Regex("(((:[a-zA-Z0-9_]+:)|<(|[a-zA-Z0-9]+):[a-zA-Z0-9_]+:[0-9]+>)(| ))+"))
        }

        fun decode(data: String): String {
            if (!data.matches(Regex("((.|\\n)*)\u200D[\u200C\u200B\u200E]+\u200D$"))) {
                throw ParseException("No valid encoded HEX found!")
            }

            val chunk = data.substring(data.indexOf("\u200D") + 1, data.lastIndexOf("\u200D"))
            return try {
                String(chunk.split("\u200C").filter { e1 -> e1.isNotEmpty() }.map { e1 ->
                    map.entries.first { e2 -> e2.value == e1 }.key
                }.toCharArray())
            } catch (n : NoSuchElementException) {
                throw ParseException("No valid encoded HEX found!")
            }
        }

        fun isCommand(textContent: String): Boolean {
            return textContent.startsWith("/")
        }
    }
}