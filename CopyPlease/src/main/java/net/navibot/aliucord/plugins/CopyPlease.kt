package net.navibot.aliucord.plugins

import android.content.Context
import com.aliucord.Utils
import com.aliucord.Utils.log
import com.aliucord.annotations.AliucordPlugin
import com.aliucord.entities.Plugin
import com.discord.databinding.UserProfileHeaderViewBinding
import com.discord.widgets.user.profile.UserProfileHeaderView
import com.discord.widgets.user.profile.UserProfileHeaderViewModel
import de.robv.android.xposed.XC_MethodHook


@AliucordPlugin(requiresRestart = true)
class CopyPlease : Plugin() {
    private val inputClass = "com.discord.widgets.user.profile.UserProfileHeaderView"
    private val listenerMethod = "configureSecondaryName"

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
        } catch (e: Exception) {
            log(e.stackTraceToString())
        }
    }

    override fun stop(context: Context) {}
}
