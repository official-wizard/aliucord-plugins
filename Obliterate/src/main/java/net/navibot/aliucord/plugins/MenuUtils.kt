package net.navibot.aliucord.plugins

import android.content.Context
import android.graphics.Bitmap
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.aliucord.utils.DimenUtils.dp
import com.lytefast.flexinput.R
import java.util.*

class MenuUtils {
    companion object {
        fun createButton(context: Context, text: String, leftDrawable: Drawable?, tag: String?): TextView {
            return TextView(context).apply {
                val red = ContextCompat.getColor(context, R.c.status_red_500)

                this.setCompoundDrawablesWithIntrinsicBounds(leftDrawable, null, null, null)
                this.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
                this.setBackgroundResource(getBackgroundId(context))
                this.setPadding(17.dp, 17.dp, 17.dp, 17.dp)
                this.setTextColor(red)

                this.gravity = Gravity.CENTER_VERTICAL
                this.compoundDrawablePadding = 32.dp
                this.isClickable = true
                this.text = text

                setTag(tag ?: text.lowercase(Locale.getDefault()).replace(" ", "_"))
            }
        }

        fun getTintedDrawable(context: Context, id: Int): Drawable? {
            val drawable = ContextCompat.getDrawable(context, id) ?: return null

            return BitmapDrawable(context.resources, Bitmap.createScaledBitmap((drawable as BitmapDrawable).bitmap, 24.dp, 24.dp, true)).apply {
                this.colorFilter = PorterDuffColorFilter(ContextCompat.getColor(context, R.c.status_red_500), PorterDuff.Mode.SRC_IN)
            }
        }

        private fun getBackgroundId(context: Context): Int {
            val outValue = TypedValue()
            context.theme.resolveAttribute(android.R.attr.selectableItemBackground, outValue, true)

            return outValue.resourceId
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