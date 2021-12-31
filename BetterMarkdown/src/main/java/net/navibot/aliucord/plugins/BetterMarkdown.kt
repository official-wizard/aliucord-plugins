package net.navibot.aliucord.plugins

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.text.Editable
import android.text.Spanned
import android.text.style.*
import android.util.Patterns
import com.aliucord.Utils.log
import com.aliucord.annotations.AliucordPlugin
import com.aliucord.entities.Plugin
import com.discord.widgets.chat.input.WidgetChatInputEditText
import com.lytefast.flexinput.widget.FlexEditText
import de.robv.android.xposed.XC_MethodHook

@AliucordPlugin(requiresRestart = true)
class BetterMarkdown : Plugin() {
    private val inputClass = "com.discord.widgets.chat.input.WidgetChatInputEditText"
    private val listenerMethod = "setOnTextChangedListener"

    // list of markdowns
    private val styles = arrayOf(
        Pair<Regex, CharacterStyle>(Regex("(^|[^*])(\\`[^*]+\\`)"), BackgroundColorSpan(Color.argb(200, 47, 49, 54))),
        Pair<Regex, CharacterStyle>(Regex("(^|[^*])(\\*[^*]+\\*)"), StyleSpan(Typeface.ITALIC)),
        Pair<Regex, CharacterStyle>(Regex("(^|[^*])(\\_[^_]+\\_)"), StyleSpan(Typeface.ITALIC)),
        Pair<Regex, CharacterStyle>(Regex("(^|[^*])(\\*{2}[^*]+\\*{2})"), StyleSpan(Typeface.BOLD)),
        Pair<Regex, CharacterStyle>(Regex("(^|[^|])(\\|{2}[^|]+\\|{2})"), BackgroundColorSpan(Color.argb(200, 47, 49, 54))),
        Pair<Regex, CharacterStyle>(Regex("(^|[^*])(\\*{3}[^*]+\\*{3})"), StyleSpan(Typeface.BOLD_ITALIC)),
        Pair<Regex, CharacterStyle>(Regex("(^|[^~])(~{2}[^~]+~{2})"), StrikethroughSpan()),
        Pair<Regex, CharacterStyle>(Regex("(^|[^_])(_{2}[^_]+_{2})"), UnderlineSpan()),
        Pair<Regex, CharacterStyle>(Regex("((https?:\\/\\/(www\\.)?[-a-zA-Z0-9@:%._\\+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6}\\b([-a-zA-Z0-9()@:%_\\+.~#?&//=]*)))"), ForegroundColorSpan(Color.parseColor("#139FD9")))
    )

    override fun start(context: Context) {

        try {
            patcher.patch(inputClass, listenerMethod, arrayOfNulls(0), object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    try {
                        // Obtain the editor
                        val widget = param.thisObject as WidgetChatInputEditText
                        val flex = WidgetChatInputEditText::class.java.getDeclaredField("editText").apply {
                            this.isAccessible = true
                        }[widget] as FlexEditText

                        // Listen for changes made to input
                        flex.addTextChangedListener(object :
                            net.navibot.aliucord.plugins.simple.TextWatcher() {
                            override fun afterTextChanged(s: Editable) {
                                val data = s.toString()

                                // Apply any found patterns
                                styles.forEach { pattern ->
                                    pattern.first.findAll(data, 0).forEach { match ->
                                        setSpan(s, pattern.second, data, match.groupValues[2].trim())
                                    }
                                }
                            }
                        })

                    } catch (e: Exception) {
                        logger.error("'BetterMarkdown' error onHooked", e)
                    }
                }
            })
        } catch (e: Exception) {
            logger.error("'BetterMarkdown' error tryHook", e)
        }

    }

    override fun stop(context: Context) {
        patcher.unpatchAll()
    }

    private fun setSpan(editable: Editable, span: CharacterStyle, data: String, markdown: String) {
        val range = getRange(data, markdown)
        editable.setSpan(CharacterStyle.wrap(span), range.start, range.last, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        if (span is UnderlineSpan) {
            styles.forEach { pair ->
                if (pair.second !is UnderlineSpan) {
                    pair.first.findAll(data, 0).forEach { match ->
                        setSpan(editable, pair.second, data, match.groupValues.first().trim())
                    }
                }
            }
        }
    }

    private fun getRange(data: String, markdown: String) : IntRange {
        val start = data.indexOf(markdown)
        val end = start + markdown.length

        return IntRange(start, end)
    }
}