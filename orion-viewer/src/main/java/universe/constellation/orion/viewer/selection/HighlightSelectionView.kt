package universe.constellation.orion.viewer.selection

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import universe.constellation.orion.viewer.R


class HighlightSelectionView : View {

    private var oldRect: Rect? = null
    private var paint = Paint()

    private var canvass: Canvas? = null

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    )

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvass = canvas
        oldRect?.let {
            canvas.drawRect(it, paint)
        }
    }


    fun setHighlight(rect: Rect) {
        reset()
        oldRect = rect
        paint.style = Paint.Style.FILL
        paint.color = Color.YELLOW
        paint.alpha = 70
        canvass?.drawRect(rect, paint)
        invalidate(rect)
    }


    fun reset() {
        oldRect = null
        invalidate()
    }


}

