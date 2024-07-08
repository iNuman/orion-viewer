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


class SelectionView : View {

    private var onSelectionChangedListener: OnSelectionChangedListener? = null

    fun setOnSelectionChangedListener(listener: OnSelectionChangedListener) {
        this.onSelectionChangedListener = listener
    }

    private var oldRect: Rect? = null
    private val paint = Paint()
    private var isDraggingStart = false
    private var isDraggingEnd = false
    private var startHandleDrawable: Drawable? = null
    private var endHandleDrawable: Drawable? = null
    private var handleSize: Int = 0
    private var touchAreaSize: Int = 0

    private var startPoint: PointF = PointF(0f, 0f)
    private var endPoint: PointF = PointF(0f, 0f)

    private var startX: Int = 0
    private var startY: Int = 0
    private var width: Int = 0
    private var height: Int = 0

    private var state: SelectionAutomata.STATE = SelectionAutomata.STATE.CANCELED

    init {
        startHandleDrawable = ContextCompat.getDrawable(context!!, R.drawable.pspdf__text_select_handle_left)?.let {
            DrawableCompat.wrap(it).apply { DrawableCompat.setTint(this, -0xda8d54) }
        }
        endHandleDrawable = ContextCompat.getDrawable(context!!, R.drawable.pspdf__text_select_handle_right)?.let {
            DrawableCompat.wrap(it).apply { DrawableCompat.setTint(this, -0xda8d54) }
        }
        handleSize = startHandleDrawable?.intrinsicWidth?.minus(24) ?: 0
        touchAreaSize = handleSize * 2
    }

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle)

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        oldRect?.let {
            canvas.drawRect(it, paint)
            drawHandle(canvas, startHandleDrawable!!, startPoint, true)
            drawHandle(canvas, endHandleDrawable!!, endPoint, false)
        }
    }

    private fun updateView() {
        val newRect = Rect(startPoint.x.toInt(), startPoint.y.toInt(), endPoint.x.toInt(), endPoint.y.toInt())
        val invalidate = Rect(newRect)
        oldRect?.let {
            invalidate.union(it)
        }
        oldRect = newRect
        invalidate(invalidate)
    }

    fun updateView(left: Int, top: Int, right: Int, bottom: Int) {
        val newRect = Rect(left, top, right, bottom)
        val invalidate = Rect(newRect)
        oldRect?.let {
            invalidate.union(it)
        }
        oldRect = newRect
        startPoint = PointF(left.toFloat(), top.toFloat())
        endPoint = PointF(right.toFloat(), bottom.toFloat())
        invalidate(invalidate)
    }

    private fun drawHandle(canvas: Canvas, handle: Drawable, point: PointF, isStart: Boolean) {
        val left = if (isStart) (point.x - handleSize).toInt() else point.x.toInt()
        val top = if (isStart) point.y.toInt() else (point.y - handleSize / 2).toInt()
        handle.setBounds(left, top, left + handleSize, top + handleSize)
        handle.draw(canvas)
    }



    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                state = SelectionAutomata.STATE.START
                if (isInsideHandle(event, startPoint)) {
                    startX = event.x.toInt()
                    startY = event.y.toInt()
                    isDraggingStart = true
                } else if (isInsideHandle(event, endPoint)) {
                    startX = event.x.toInt()
                    startY = event.y.toInt()
                    isDraggingEnd = true
                }
                onSelectionChangedListener?.onStateChanged(state)
            }
            MotionEvent.ACTION_MOVE -> {
                state = SelectionAutomata.STATE.MOVING
                if (isDraggingStart) {
                    startPoint.set(event.x, event.y)
                    val endX = event.x.toInt()
                    val endY = event.y.toInt()
                    width = kotlin.math.abs(endX - startX)
                    height = kotlin.math.abs(endY - startY)
                    updateView()
                } else if (isDraggingEnd) {
                    endPoint.set(event.x, event.y)
                    val endX = event.x.toInt()
                    val endY = event.y.toInt()
                    width = kotlin.math.abs(endX - startX)
                    height = kotlin.math.abs(endY - startY)
                    updateView()
                }
                invalidate()
                onSelectionChangedListener?.onStateChanged(state)
            }
            MotionEvent.ACTION_UP -> {
                isDraggingStart = false
                isDraggingEnd = false
                state = SelectionAutomata.STATE.END
                notifySelectionChanged()
                onSelectionChangedListener?.onStateChanged(state)
            }
        }
        return true
    }

    private fun isInsideHandle(event: MotionEvent, handlePoint: PointF): Boolean {
        val touchRect = RectF(
            handlePoint.x - touchAreaSize / 2,
            handlePoint.y - touchAreaSize / 2,
            handlePoint.x + touchAreaSize / 2,
            handlePoint.y + touchAreaSize / 2
        )
        return touchRect.contains(event.x, event.y)
    }

    private fun notifySelectionChanged() {
        onSelectionChangedListener?.onSelectionChanged(startX, startY, width, height, oldRect)
    }

    fun reset() {
        oldRect = null
        invalidate()
        state = SelectionAutomata.STATE.CANCELED
        onSelectionChangedListener?.onStateChanged(state)
    }

    fun setColorFilter(colorFilter: ColorFilter?) {
        paint.color = Color.BLACK
        paint.colorFilter = colorFilter
        paint.alpha = 64
    }

    interface OnSelectionChangedListener {
        fun onSelectionChanged(startX: Int, startY: Int, width: Int, height: Int, newRectF: Rect?)
        fun onStateChanged(state: SelectionAutomata.STATE)
    }
}

