package net.navibot.aliucord.plugins

import android.content.Context
import com.aliucord.Utils
import com.aliucord.Utils.log
import com.aliucord.annotations.AliucordPlugin
import com.aliucord.entities.Plugin
import com.discord.databinding.UserProfileHeaderViewBinding
import com.discord.databinding.WidgetUserSheetBinding
import com.discord.widgets.user.profile.UserProfileHeaderView
import com.discord.widgets.user.profile.UserProfileHeaderViewModel
import com.discord.widgets.user.usersheet.WidgetUserSheet
import com.discord.widgets.user.usersheet.WidgetUserSheetViewModel
import de.robv.android.xposed.XC_MethodHook


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

                        view.j.setOnClickListener {
                            val loaded = (param.args.first() as UserProfileHeaderViewModel.ViewState.Loaded)
                            val full =  "${loaded.user.username}#${loaded.user.discriminator}"

                            Utils.setClipboard("cord-username", full)
                            Utils.showToast("copied '$full'")
                        }
                    } catch (e : java.lang.Exception) {
                        log(e.stackTraceToString())
                    }

                }
            })

            patcher.patch(sheetClass, sheetMethod, arrayOf(WidgetUserSheetViewModel.ViewState.Loaded::class.java), object: XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    try {
                        val binding = WidgetUserSheet::class.java.getDeclaredMethod("getBinding").apply {
                            isAccessible = true
                        }.invoke(param.thisObject) as WidgetUserSheetBinding

                        val view = binding.g
                        view.setOnClickListener {
                            Utils.setClipboard("cord-about", view.text.toString())
                            Utils.showToast("copied description!")
                        }
                    } catch (e : java.lang.Exception) {
                        e.printStackTrace()
                    }
                }
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    override fun stop(context: Context) {}
}
