package universe.constellation.orion.viewer.prefs

import android.content.SharedPreferences
import universe.constellation.orion.viewer.PageOptions
import universe.constellation.orion.viewer.PageWalker
import universe.constellation.orion.viewer.R
import universe.constellation.orion.viewer.errorInDebug
import java.io.Serializable

class GlobalOptions(
    context: OrionApplication,
    prefs: SharedPreferences
) : PreferenceWrapper(prefs), Serializable, PageOptions {

    private val registeredPreferences = mutableMapOf<String, Preference<*>>()

    val isEnableTouchMove: Boolean
        get() = getBooleanProperty(ENABLE_TOUCH_MOVE, true)

    val isEnableMoveOnPinchZoom: Boolean
        get() = getBooleanProperty(ENABLE_MOVE_ON_PINCH_ZOOM, false)

    val defaultZoom: Int
        get() = getIntFromStringProperty(DEFAULT_ZOOM, 0)

    val defaultContrast: Int
        get() = getIntFromStringProperty(DEFAULT_CONTRAST, 100)


    val dictionary: String
        get() = getStringProperty(DICTIONARY, "FORA")


    override val verticalOverlapping: Int
        get() = getIntFromStringProperty(SCREEN_OVERLAPPING_VERTICAL, 3)

    override val horizontalOverlapping: Int
        get() = getIntFromStringProperty(SCREEN_OVERLAPPING_HORIZONTAL, 3)

    val brightness: Int
        get() = getIntFromStringProperty(BRIGHTNESS, 100)

    val isCustomBrightness: Boolean
        get() = getBooleanProperty(CUSTOM_BRIGHTNESS, false)

    val walkOrder: String
        get() = getStringProperty(WALK_ORDER, PageWalker.WALK_ORDER.ABCD.name)

    val pageLayout: Int
        get() = getInt(PAGE_LAYOUT, 0)

    val colorMode: String
        get() = getStringProperty(COLOR_MODE, "CM_NORMAL")

    val LONG_TAP_ACTION = pref(Companion.LONG_TAP_ACTION, context.resources.getString(R.string.action_key_select_text_new))

    val DOUBLE_TAP_ACTION = pref(Companion.DOUBLE_TAP_ACTION, context.resources.getString(R.string.action_key_select_word_and_translate_new))

    val SHOW_BATTERY_STATUS = pref(Companion.SHOW_BATTERY_STATUS, true)

    val STATUS_BAR_POSITION = pref(Companion.STATUS_BAR_POSITION, "TOP")

    val STATUS_BAR_SIZE = pref(Companion.STATUS_BAR_SIZE, "MEDIUM")

    val SHOW_OFFSET_ON_STATUS_BAR = pref(Companion.SHOW_OFFSET_ON_STATUS_BAR, true)

    val SHOW_TIME_ON_STATUS_BAR = pref(Companion.SHOW_TIME_ON_STATUS_BAR, true)

    fun <T> subscribe(pref: Preference<T>) {
        registeredPreferences.put(pref.key, pref)?.also {
            errorInDebug("Pref with key ${pref.key} already registered: $pref ")
        }
    }

    companion object {

        const val DEFAULT_ZOOM: String = "DEFAULT_ZOOM"

        const val DEFAULT_CONTRAST: String = "DEFAULT_CONTRAST_3"

        const val DOUBLE_TAP_ACTION: String = "DOUBLE_TAP_ACTION"

        const val SHOW_BATTERY_STATUS: String = "SHOW_BATTERY_STATUS"

        const val STATUS_BAR_POSITION: String = "STATUS_BAR_POSITION"

        const val STATUS_BAR_SIZE: String = "STATUS_BAR_SIZE"

        const val LONG_TAP_ACTION: String = "LONG_TAP_ACTION"

        const val OLD_UI: String = "OLD_UI"

        const val SHOW_OFFSET_ON_STATUS_BAR: String = "SHOW_OFFSET_ON_STATUS_BAR"

        const val SHOW_TIME_ON_STATUS_BAR: String = "SHOW_TIME_ON_STATUS_BAR"

        const val DICTIONARY: String = "DICTIONARY"

        const val SCREEN_OVERLAPPING_HORIZONTAL: String = "SCREEN_OVERLAPPING_HORIZONTAL"

        const val SCREEN_OVERLAPPING_VERTICAL: String = "SCREEN_OVERLAPPING_VERTICAL"

        const val BRIGHTNESS: String = "BRIGHTNESS"

        const val CUSTOM_BRIGHTNESS: String = "CUSTOM_BRIGHTNESS"

        const val APPLICATION_THEME: String = "APPLICATION_THEME"

        const val WALK_ORDER: String = "WALK_ORDER"

        const val PAGE_LAYOUT: String = "PAGE_LAYOUT"

        const val COLOR_MODE: String = "COLOR_MODE"

        const val SHOW_TAP_HELP: String = "SHOW_TAP_HELP"

        const val TEST_SCREEN_WIDTH: String = "TEST_SCREEN_WIDTH"

        const val TEST_SCREEN_HEIGHT: String = "TEST_SCREEN_HEIGHT"

        const val OPEN_AS_TEMP_BOOK: String = "OPEN_AS_TEMP_BOOK"

        const val TEST_FLAG: String = "ORION_VIEWER_TEST_FLAG"

        const val ENABLE_TOUCH_MOVE: String = "ENABLE_TOUCH_MOVE"

        const val ENABLE_MOVE_ON_PINCH_ZOOM: String = "ENABLE_MOVE_ON_PINCH_ZOOM"
    }
}
