package net.navibot.aliucord.plugins

import android.content.Context
import android.graphics.Typeface
import android.text.Editable
import android.text.Spanned
import android.text.style.CharacterStyle
import android.text.style.StrikethroughSpan
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import com.aliucord.Utils
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
    private val patterns = arrayOf(
        Pair<Regex, CharacterStyle>(Regex("\\*\\*\\*(.*?)\\*\\*\\*"), StyleSpan(Typeface.BOLD_ITALIC)),
        Pair<Regex, CharacterStyle>(Regex("\\*\\*(.*?)\\*\\*"), StyleSpan(Typeface.BOLD)),
        Pair<Regex, CharacterStyle>(Regex("\\*[^*]+\\*"), StyleSpan(Typeface.ITALIC)),
        Pair<Regex, CharacterStyle>(Regex("~~(.*?)~~"), StrikethroughSpan()),
        Pair<Regex, CharacterStyle>(Regex("__(.*?)__"), UnderlineSpan())
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
                                patterns.forEach { pattern ->
                                    pattern.first.findAll(data, 0).forEach { match ->
                                        setSpan(s, pattern.second, data, match.groupValues.first())
                                    }
                                }
                            }
                        })

                    } catch (e: Exception) {
                        log(e.stackTraceToString())
                    }
                }
            })
        } catch (e: Exception) {
            log(e.stackTraceToString())
        }

    }

    override fun stop(context: Context) {

    }

    private fun setSpan(editable: Editable, span: CharacterStyle, data: String, markdown: String) {
        val range = getRange(data, markdown)

        editable.setSpan(span, range.start, range.last, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    }

    private fun getRange(data: String, markdown: String) : IntRange {
        val start = data.indexOf(markdown)
        val end = start + markdown.length

        return IntRange(start, end)
    }
}