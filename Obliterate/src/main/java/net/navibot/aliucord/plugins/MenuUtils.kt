package net.navibot.aliucord.plugins

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.ContextThemeWrapper
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.aliucord.Constants
import com.aliucord.utils.DimenUtils.dp
import com.lytefast.flexinput.R
import java.util.*

class MenuUtils {
    companion object {
        fun createButton(context: Context, text: String, leftDrawable: Drawable?, tag: String?): TextView {
            return TextView(ContextThemeWrapper(context, R.i.UiKit_Settings_Item)).apply {
                this.setCompoundDrawablesWithIntrinsicBounds(leftDrawable, null, null, null)
                this.setTextColor(ContextCompat.getColor(context, R.c.status_red_500))

                this.tag = tag ?: text.lowercase(Locale.getDefault()).replace(" ", "_")
                this.typeface = ResourcesCompat.getFont(context, Constants.Fonts.ginto_regular)
                this.compoundDrawablePadding = 32.dp
                this.text = text
            }
        }
    }
}

fun ViewGroup.addViewIfNotTagged(view: View) : Boolean {
    if (findViewWithTag<View>(view.tag) != null) {
        return false
    }

    addView(view)
    return true
}