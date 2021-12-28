package net.navibot.aliucord.plugins

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.Spannable
import android.text.Spanned
import android.text.style.CharacterStyle
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.TextView
import com.aliucord.Utils
import com.aliucord.Utils.log
import com.aliucord.annotations.AliucordPlugin
import com.aliucord.api.SettingsAPI
import com.aliucord.entities.Plugin
import com.aliucord.views.TextInput
import com.aliucord.widgets.BottomSheet
import com.discord.utilities.view.text.SimpleDraweeSpanTextView
import com.discord.utilities.view.text.TextWatcher
import com.discord.widgets.chat.list.entries.MessageEntry
import de.robv.android.xposed.XC_MethodHook


@AliucordPlugin(requiresRestart = true)
class GreenText : Plugin() {
    init {
        settingsTab = SettingsTab(
            GreenTextSettings::class.java,
            SettingsTab.Type.BOTTOM_SHEET
        ).withArgs(settings)
    }

    private val inputClass = "com.discord.widgets.chat.list.adapter.WidgetChatListAdapterItemMessage"
    private val listenerMethod = "processMessageText"

    private val highlight = Color.parseColor(settings.getString("highlightColor","#789922"))
    private val customMarkdownPattern = Regex("((^|\n)>[^ ][^\n]+)")

    override fun start(context: Context) {

        try {
            patcher.patch(inputClass, listenerMethod, arrayOf(SimpleDraweeSpanTextView::class.java, MessageEntry::class.java), object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    try {
                        val entry = (param.args[1] as MessageEntry)
                        val view = (param.args[0] as TextView)
                        val message = entry.message

                        if (!message.isLoading) {
                            val data = message.content

                            customMarkdownPattern.findAll(data, 0).forEach { green ->
                                setSpan(view, ForegroundColorSpan(highlight), data, green.groupValues.first())
                            }
                        }
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
        patcher.unpatchAll()
    }

    private fun setSpan(view: TextView, span: CharacterStyle, data: String, markdown: String) {
        val range = getRange(data, markdown)
        val spanned = view.text as Spannable

        spanned.setSpan(span, range.start, range.last, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    }

    private fun getRange(data: String, markdown: String) : IntRange {
        val start = data.indexOf(markdown)
        val end = start + markdown.length

        return IntRange(start, end)
    }

    class GreenTextSettings(private val settings: SettingsAPI) : BottomSheet() {
        override fun onViewCreated(view: View, bundle: Bundle?) {
            super.onViewCreated(view, bundle)
            val ctx = view.context

            TextInput(ctx, "Greentext color in hex").run {
                editText.run {
                    maxLines = 1
                    setText(settings.getString("highlightColor", "#789922"))

                    addTextChangedListener(object : TextWatcher() {
                        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
                        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
                        override fun afterTextChanged(s: Editable) {
                            settings.setString("highlightColor", s.toString())
                            Utils.promptRestart()
                        }
                    })
                }

                linearLayout.addView(this)
            }
        }
    }
}
