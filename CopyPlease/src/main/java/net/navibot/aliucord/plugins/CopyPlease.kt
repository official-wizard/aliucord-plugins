package net.navibot.aliucord.plugins

import android.content.Context
import com.aliucord.Utils
import com.aliucord.Utils.log
import com.aliucord.annotations.AliucordPlugin
import com.aliucord.entities.Plugin
import com.discord.databinding.UserProfileHeaderViewBinding
import com.discord.databinding.WidgetUserSheetBinding
import com.discord.widgets.chat.MessageContent
import com.discord.widgets.user.profile.UserProfileHeaderView
import com.discord.widgets.user.profile.UserProfileHeaderViewModel
import com.discord.widgets.user.usersheet.WidgetUserSheet
import com.discord.widgets.user.usersheet.WidgetUserSheetViewModel
import de.robv.android.xposed.XC_MethodHook
import java.lang.StringBuilder
import java.util.concurrent.atomic.AtomicReference


@AliucordPlugin(requiresRestart = true)
class CopyPlease : Plugin() {
    private val inputClass = "com.discord.widgets.user.profile.UserProfileHeaderView"
    private val listenerMethod = "configureSecondaryName"

    private val sheetClass = "com.discord.widgets.user.usersheet.WidgetUserSheet"
    private val sheetMethod = "configureAboutMe"

    override fun start(context: Context) {
        try {
            patcher.patch(inputClass, listenerMethod, arrayOf(UserProfileHeaderViewModel.ViewState.Loaded::class.java), object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {

                    try {

                        val view = UserProfileHeaderView::class.java.getDeclaredField("binding").apply {
                            isAccessible = true
                        }.get(param.thisObject) as UserProfileHeaderViewBinding

                        // copy username/nickname
                        view.j.setOnClickListener {
                            val loaded = (param.args.first() as UserProfileHeaderViewModel.ViewState.Loaded)
                            val full = loaded.hasNickname.let {
                                loaded.guildMember?.nick
                            } ?: run {
                                "${loaded.user.username}#${loaded.user.discriminator}"
                            }

                            Utils.setClipboard("cord-name", full)
                            Utils.showToast("copied '$full'")
                        }

                        // copy username
                        view.k.setOnClickListener {
                            val loaded = (param.args.first() as UserProfileHeaderViewModel.ViewState.Loaded)
                            val full =  "${loaded.user.username}#${loaded.user.discriminator}"

                            Utils.setClipboard("cord-username", full)
                            Utils.showToast("copied '$full'")
                        }

                        // copy custom status
                        view.i.setOnClickListener {
                            val data = view.i.text.toString()

                            Utils.setClipboard("cord-status", data)
                            Utils.showToast("copied custom status!")
                        }
                    } catch (e : Exception) {
                        logger.error("'CopyPlease' error onHooked[1]", e)
                    }
                }
            })

            patcher.patch(sheetClass, sheetMethod, arrayOf(WidgetUserSheetViewModel.ViewState.Loaded::class.java), object: XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    try {
                        val binding = WidgetUserSheet::class.java.getDeclaredMethod("getBinding").apply {
                            isAccessible = true
                        }.invoke(param.thisObject) as WidgetUserSheetBinding

                        // copy about me
                        binding.g.setOnClickListener {
                            Utils.setClipboard("cord-about", binding.g.text.toString())
                            Utils.showToast("copied description!")
                        }
                    } catch (e : Exception) {
                        logger.error("'CopyPlease' error onHooked[2]", e)
                    }
                }
            })
        } catch (e: Exception) {
            logger.error("'CopyPlease' error tryHook", e)
        }
    }

    override fun stop(context: Context) {}
}
