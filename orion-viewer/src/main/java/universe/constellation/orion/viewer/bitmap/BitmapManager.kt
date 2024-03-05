package universe.constellation.orion.viewer.bitmap

import android.graphics.Rect
import universe.constellation.orion.viewer.PageView
import universe.constellation.orion.viewer.view.PageLayoutManager

class BitmapManager(val pageLayoutManager: PageLayoutManager) {

    private val viewInfo
        get() = pageLayoutManager.sceneRect


    private val Int.upperHalf
        get(): Int {
            return this /2 + this % 2
        }

    fun createDefaultBitmap(width: Int, height: Int): FlexibleBitmap {
        return FlexibleBitmap(width, height, viewInfo.width().upperHalf, viewInfo.height().upperHalf)
    }

    private val rect = Rect()

    fun actualizeActive(pageView: PageView) {
        rect.set(pageLayoutManager.sceneRect)
        val bitmap = pageView.bitmap ?: return
        rect.inset(-viewInfo.width().upperHalf / 2, -viewInfo.height().upperHalf / 2)

        actualizeActive(
            bitmap,
            pageView.layoutData.visibleOnScreenPart(rect) ?: run {
                bitmap.disableAll(pageLayoutManager.controller.bitmapCache)
                return
            })
    }

    private fun actualizeActive(
        bitmap: FlexibleBitmap,
        visiblePart: Rect
    ) {
        bitmap.updateDrawAreaAndUpdateNonRenderingPart(
            visiblePart,
            pageLayoutManager.controller.bitmapCache
        )
    }
}