package universe.constellation.orion.viewer

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Rect
import android.widget.Toast
import androidx.annotation.IntegerRes
import androidx.annotation.StringRes
import universe.constellation.orion.viewer.filemanager.OrionFileManagerActivity
import universe.constellation.orion.viewer.dialog.HighlightTextDialog
import universe.constellation.orion.viewer.util.ColorUtil.getColorMode

enum class Action(@StringRes val nameRes: Int, @IntegerRes idRes: Int, val isVisible: Boolean = true) {
    NONE(R.string.action_none, R.integer.action_none) {
        override fun doAction(
            controller: Controller?,
            activity: PdfFragment,
            parameter: Any?
        ) {
            //none action
        }
    },

    FIRST_PAGE(R.string.action_first_page, R.integer.action_first_page, isVisible = false) {
        override fun doAction(
            controller: Controller?,
            activity: PdfFragment,
            parameter: Any?
        ) {
            controller?.drawPage(0)
        }
    },

    LAST_PAGE(R.string.action_last_page, R.integer.action_last_page, isVisible = false) {
        override fun doAction(
            controller: Controller?,
            activity: PdfFragment,
            parameter: Any?
        ) {
            controller?.drawPage(controller.pageCount - 1)
        }
    },


    SEARCH(R.string.action_crop_page, R.integer.action_crop_page) {
        override fun doAction(
            controller: Controller?,
            activity: PdfFragment,
            parameter: Any?
        ) {
            activity.startSearch()
        }
    },

    HIGHLIGHT(R.string.action_highlight_text, R.integer.action_highlight_text) {
        override fun doAction(
            controller: Controller?,
            activity: PdfFragment,
            parameter: Any?,
            rect: Rect
        ) {

            HighlightTextDialog(
                activity,
                parameter as String,
                rect
            )
        }
    },

    SELECT_TEXT(R.string.action_select_text, R.integer.action_select_text) {
        override fun doAction(
            controller: Controller?,
            activity: PdfFragment,
            parameter: Any?
        ) {
            activity.textSelectionMode(false, false)
        }
    },

    SELECT_WORD(R.string.action_select_word, R.integer.action_select_word) {
        override fun doAction(
            controller: Controller?,
            activity: PdfFragment,
            parameter: Any?
        ) {
            activity.textSelectionMode(true, false)
        }
    },

    SELECT_WORD_AND_TRANSLATE(
        R.string.action_select_word_and_translate,
        R.integer.action_select_word_and_translate
    ) {
        override fun doAction(
            controller: Controller?,
            activity: PdfFragment,
            parameter: Any?
        ) {
            activity.textSelectionMode(true, true)
        }
    },
    GOTO(R.string.action_goto_page, R.integer.action_goto_page) {
        override fun doAction(
            controller: Controller?,
            activity: PdfFragment,
            parameter: Any?
        ) {
//            activity.showOrionDialog(PdfFragment.PAGE_SCREEN, this, null)
        }
    };

//    @JvmField
//    val code: Int = instance.resources.getInteger(idRes)

    open fun doAction(controller: Controller?, activity: PdfFragment, parameter: Any?) {
        doAction(activity)
    }

    open fun doAction(controller: Controller?, activity: PdfFragment, parameter: Any?, rect: Rect) {
        doAction(controller, activity, parameter, rect)
    }

    open fun doAction(activity: PdfFragment) {
    }


    companion object {
        private val actions = HashMap<Int, Action>()

        init {
            val values = entries.toTypedArray()
            for (value in values) {
//                actions[value.code] = value
            }
        }

        @JvmStatic
        fun getAction(code: Int): Action {
            val result = actions[code]
            return result ?: NONE
        }
    }
}
