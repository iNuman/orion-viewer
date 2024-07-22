package universe.constellation.orion.viewer

import android.graphics.Rect
import universe.constellation.orion.viewer.selection.ClickInfo
import universe.constellation.orion.viewer.selection.ClickType
import universe.constellation.orion.viewer.selection.SelectionAutomata

enum class ContextAction(customName: String? = null) {

    //now just do word selection
    SELECT_TEXT {
        override fun doAction(activity: PdfFragment, clickInfo: ClickInfo) {
            val pos = clickInfo as? ClickInfo ?: return
            activity.selectionAutomata.selectText(
                true, false,
                SelectionAutomata.getSelectionRectangle(
                    pos.x,
                    pos.y,
                    0,
                    0,
                    true,
                    activity.controller?.pageLayoutManager ?: return
                ),
                Rect()
            )
        }
    };


    val key = customName ?: name

    open fun doAction(activity: PdfFragment, clickInfo: ClickInfo) {

    }

    companion object {
        fun findAction(action: String): ContextAction? {
            return entries.firstOrNull { it.key == action }
        }
    }
}