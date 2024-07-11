package universe.constellation.orion.viewer.selection

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import universe.constellation.orion.viewer.R
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

private val sin15 = sin(15.0 / 180.0 * Math.PI).toFloat()

private val cos15 = cos(15.0 / 180.0 * Math.PI).toFloat()

private val sqrt6 = sqrt(6.0).toFloat()

class SelectionViewNew : View {

    private val paint = Paint()

    private var rects: List<RectF> = emptyList()

    var startHandler: Handler? = null
        private set

    var endHandler: Handler? = null
        private set

    private val start = Path()

    private val end = Path()

    private val startDrawable =
        ContextCompat.getDrawable(context!!, R.drawable.pspdf__text_select_handle_left)?.let {
            DrawableCompat.wrap(it).apply { DrawableCompat.setTint(this, -0xda8d54) }
        }

    private val endDrawable =
        ContextCompat.getDrawable(context!!, R.drawable.pspdf__text_select_handle_right)?.let {
            DrawableCompat.wrap(it).apply { DrawableCompat.setTint(this, -0xda8d54) }
        }

    constructor(context: Context?) : super(context)

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    )

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        paint.style = Paint.Style.FILL
        paint.alpha = 96
        rects.forEach {
            canvas.drawRect(it, paint)
        }

        paint.alpha = 128

        startHandler?.let { handler ->
            drawHandlerDrawable(canvas, handler, startDrawable)
        }

        endHandler?.let { handler ->
            drawHandlerDrawable(canvas, handler, endDrawable)
        }


    }

    private fun drawHandlerDrawable(canvas: Canvas, handler: Handler, drawable: Drawable?) {
        drawable?.let {
            val x = handler.x
            val y = handler.y
            val halfWidth = drawable.intrinsicWidth / 3
            val halfHeight = drawable.intrinsicHeight / 3
            drawable.setBounds(
                (x - halfWidth).toInt(),
                (y - halfHeight).toInt(),
                (x + halfWidth).toInt(),
                (y + halfHeight).toInt()
            )
            drawable.draw(canvas)
        }
    }


    fun setHandlers(startHandler: Handler, endHandler: Handler) {
        this.startHandler = startHandler
        this.endHandler = endHandler
        println("" + startHandler + endHandler)
    }

    fun updateView(rects: List<RectF>) {
        this.rects = rects
        invalidate()
    }

    fun reset() {
        rects = emptyList()
        startHandler = null
        endHandler = null
    }

    fun setColorFilter(colorFilter: ColorFilter?) {
        paint.color = 0x147494
        paint.colorFilter = colorFilter
        paint.alpha = 64
        paint.strokeWidth = 0f
    }
}

data class Handler(var x: Float, var y: Float, var triangleSize: Float, val isStart: Boolean)

fun SelectionViewNew.findClosestHandler(x: Float, y: Float, trashHold: Float): Handler? {
    val min = listOfNotNull(
        startHandler to startHandler?.distance(x, y),
        endHandler to endHandler?.distance(x, y)
    ).minByOrNull { it.second ?: Float.MAX_VALUE } ?: return null
    return min.takeIf { trashHold >= (it.second ?: Float.MAX_VALUE) }?.first
}

fun Handler.distance(x: Float, y: Float): Float {
    var delta = -triangleSize * sqrt6 / 4 / 2
    if (!isStart) delta = -delta
    val x1 = this.x + delta
    val y1 = this.y + delta

    val dx = x1 - x
    val dy = y1 - y
    return sqrt((dx * dx + dy * dy).toDouble()).toFloat()
}
