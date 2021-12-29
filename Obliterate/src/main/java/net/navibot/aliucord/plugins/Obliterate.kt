package net.navibot.aliucord.plugins

import android.content.Context
import androidx.core.content.ContextCompat
import com.aliucord.Utils
import com.aliucord.Utils.log
import com.aliucord.annotations.AliucordPlugin
import com.aliucord.entities.Plugin
import com.discord.databinding.WidgetServerSettingsEditMemberBinding
import com.discord.restapi.RestAPIParams
import com.discord.utilities.rest.RestAPI.getApi
import com.discord.utilities.rx.ObservableExtensionsKt
import com.discord.widgets.servers.WidgetServerSettingsEditMember
import de.robv.android.xposed.XC_MethodHook
import com.lytefast.flexinput.R

// Favoritism
@AliucordPlugin(requiresRestart = true)
class Obliterate : Plugin() {
    private val path = "com.discord.widgets.servers.WidgetServerSettingsEditMember"
    private val function = "configureUI"

    override fun start(context: Context) {
        try {
            patcher.patch(path, function, arrayOf(WidgetServerSettingsEditMember.Model::class.java), object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    try {
                        val self = param.thisObject as WidgetServerSettingsEditMember
                        val model = param.args.first() ?: return
                        model as WidgetServerSettingsEditMember.Model

                        // check if we have enough permissions to manage this user
                        if (!model.canManage) {
                            return
                        }

                        // get all the necessary objects to proceed with management
                        val binding = WidgetServerSettingsEditMember::class.java.getDeclaredMethod("getBinding").apply {
                            isAccessible = true
                        }.invoke(self) as WidgetServerSettingsEditMemberBinding

                        // obtain unmanageable roles
                        val unmanageable = model.roleItems.filter { item -> !item.isManageable }
                            .map { item -> item.key.toLong() }

                        // bind view if not already
                        binding.b.addViewIfNotTagged(MenuUtils.createButton(binding.b.context, "Clear Roles", ContextCompat.getDrawable(binding.b.context, R.e.ic_x_red_24dp), "remove_roles").apply {
                            setOnClickListener { // remove all roles
                                try {
                                    ObservableExtensionsKt.appSubscribe(
                                        ObservableExtensionsKt.`ui$default`(
                                            ObservableExtensionsKt.`restSubscribeOn$default`(
                                                getApi().changeGuildMember(
                                                    model.guild.id,
                                                    model.user.id,
                                                    RestAPIParams.GuildMember.createWithRoles(unmanageable)
                                                ), false, 1, null
                                            ), self, null, 2, null
                                        ),
                                        self.javaClass,
                                        self.context,
                                        {}, {}, {}, {}, {}
                                    )
                                } catch (e: Exception) {
                                    Utils.showToast("There was an issue removing roles, please try again later!")
                                }
                            }
                        })
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            })
        } catch (e: Exception) {
            log("'Obliterate' error -> " + e.stackTraceToString())
        }
    }

    override fun stop(context: Context) {
        patcher.unpatchAll()
    }
}
