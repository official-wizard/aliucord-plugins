package net.navibot.aliucord.plugins

import com.discord.widgets.chat.MessageContent
import net.navibot.aliucord.plugins.error.ParseException


fun MessageContent.set(text: String) {
    val field = MessageContent::class.java.getDeclaredField("textContent").apply {
        isAccessible = true
    }

    field.set(this, text)
}

class Utils {
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

        fun decode(data: String): String {
            if (!data.matches(Regex(".*\u200D[\u200C\u200B\u200E]+\u200D$"))) {
                throw ParseException("No valid encoded HEX found!")
            }

            val chunk = data.substring(data.indexOf("\u200D") + 1, data.lastIndexOf("\u200D"))
            if (!chunk.matches(Regex("^[\u200C\u200E\u200B]+$"))) {
                throw ParseException("No valid encoded HEX found!")
            }

            return try {
                String(chunk.split("\u200C").filter { e1 -> e1.isNotEmpty() }.map { e1 ->
                    map.entries.first { e2 -> e2.value == e1 }.key
                }.toCharArray())
            } catch (n : NoSuchElementException) {
                throw ParseException("No valid encoded HEX found!")
            }

        }
    }
}