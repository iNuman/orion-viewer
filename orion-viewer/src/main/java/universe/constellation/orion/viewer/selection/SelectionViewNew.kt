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

private val sin15 = sin(15.0/180.0*Math.PI).toFloat()

private val cos15 = cos(15.0/180.0*Math.PI).toFloat()

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
    private var handleSize: Int = 0
    private var touchAreaSize: Int = 0
    private var canvass: Canvas?= null

    private val startDrawable =
        ContextCompat.getDrawable(context!!, R.drawable.pspdf__text_select_handle_left)?.let {
            DrawableCompat.wrap(it).apply { DrawableCompat.setTint(this, -0xda8d54) }
        }

    private val endDrawable =
        ContextCompat.getDrawable(context!!, R.drawable.pspdf__text_select_handle_right)?.let {
            DrawableCompat.wrap(it).apply { DrawableCompat.setTint(this, -0xda8d54) }
        }

    init {
        handleSize = startDrawable?.intrinsicWidth?.minus(24) ?: 0
        touchAreaSize = handleSize * 2
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

        canvass = canvas
        paint.style = Paint.Style.FILL
        paint.alpha = 96
        rects.forEach {
            canvas.drawRect(it, paint)
        }

//        startHandler?.let { handler ->
//            drawHandlerDrawable(canvas, handler, startDrawable)
//        }
//
//        endHandler?.let { handler ->
//            drawHandlerDrawable(canvas, handler, endDrawable)
//        }


        startHandler?.let { handler ->
            canvas.drawPath(handlerPath(handler), paint)
        }

        endHandler?.let { handler ->
            canvas.drawPath(handlerPath(handler), paint)
        }


//        updateHandlerPositions()

        startHandler?.let { startHandler ->
            endHandler?.let { endHandler ->
                val left = minOf(startHandler.x, endHandler.x)
                val top = minOf(startHandler.y, endHandler.y)
                val right = maxOf(startHandler.x, endHandler.x)
                val bottom = maxOf(startHandler.y, endHandler.y)

                paint.style = Paint.Style.STROKE
                paint.alpha = 64
                canvas.drawRect(left, top, right, bottom, paint)
            }
        }
    }

    private fun drawHandlerDrawable(canvas: Canvas, handler: Handler, drawable: Drawable?) {
        drawable?.let {
            val x = handler.x
            val y = handler.y
            val halfWidth = drawable.intrinsicWidth / 3.5
            val halfHeight = drawable.intrinsicHeight / 3.5
            drawable.setBounds(
                (x - halfWidth).toInt(),
                (y - halfHeight).toInt(),
                (x + halfWidth).toInt(),
                (y + halfHeight).toInt()
            )
            drawable.draw(canvas)
        }
    }

    private fun handlerPath(handler: Handler): Path {
        val path = if (handler.isStart) start else end
        path.reset()
        val x = handler.x
        val y = handler.y
        val r = if (handler.isStart) handler.triangleSize else -handler.triangleSize
        path.moveTo(x, y)
        path.lineTo(x - r * sin15, y - r * cos15)
        path.lineTo(x - r * cos15, y - r * sin15)
        path.lineTo(x, y)
        path.close()
        return path
    }

    fun setHandlers(startHandler: Handler, endHandler: Handler) {
        this.startHandler = startHandler
        this.endHandler = endHandler
        println("ffnet" + startHandler + endHandler)
    }

    fun updateView(lineRects: List<RectF>) {
        this.rects = mergeRects(lineRects.distinct())
//        this.rects = lineRects.distinct()
        invalidate()
    }

    private fun mergeRects(rects: List<RectF>): List<RectF> {
        if (rects.isEmpty()) return emptyList()

        val sortedRects = rects.sortedBy { it.left } // Sort by left edge for horizontal merging
        val mergedRects = mutableListOf<RectF>()
        var currentRect = RectF(sortedRects[0])

        for (rect in sortedRects.drop(1)) {
            if (currentRect.intersectsHorizontally(rect) || isAdjacentHorizontally(currentRect, rect)) {
                currentRect.union(rect)
            } else {mergedRects.add(currentRect)
                currentRect = RectF(rect)
            }
        }

        mergedRects.add(currentRect)
        return mergedRects
    }

    // Helper functions for horizontal checks
    private fun RectF.intersectsHorizontally(other: RectF): Boolean {
        return (left <= other.right && other.left <= right)
    }

    private fun isAdjacentHorizontally(rect1: RectF, rect2: RectF): Boolean {
        return rect1.right >= rect2.left || rect1.left <= rect2.right
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
        paint.strokeWidth = 2f
    }

    private fun updateHandlerPositions() {
        if (rects.isNotEmpty()) {
            startHandler?.let { handler ->
                val firstRect = rects.first()
                handler.x = firstRect.left-18
                handler.y = firstRect.top+20
//                canvass?.let { drawHandlerDrawable(it, handler, startDrawable) }

            }
            endHandler?.let { handler ->
                val lastRect = rects.last()
                handler.x = lastRect.right+19
                handler.y = lastRect.bottom
//                canvass?.let { drawHandlerDrawable(it, handler, endDrawable) }

            }
        }
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
    var delta = - triangleSize * sqrt6 / 4 / 2
    if (!isStart) delta = -delta
    val x1 = this.x + delta
    val y1 = this.y + delta

    val dx = x1 - x
    val dy = y1 - y
    return sqrt((dx * dx + dy * dy).toDouble()).toFloat()
}
