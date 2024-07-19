/*
 * Orion Viewer - pdf, djvu, xps and cbz file viewer for android devices
 *
 * Copyright (C) 2011-2013  Michael Bogdanov & Co
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package universe.constellation.orion.viewer.prefs

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.res.Resources
import android.os.Build
import android.os.Build.VERSION.CODENAME
import android.os.Build.VERSION.RELEASE
import android.system.Os
import androidx.core.os.ConfigurationCompat
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.multidex.MultiDex
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import universe.constellation.orion.viewer.BuildConfig.DEBUG
import universe.constellation.orion.viewer.BuildConfig.VERSION_NAME
import universe.constellation.orion.viewer.LastPageInfo
import universe.constellation.orion.viewer.OrionViewerActivity
import universe.constellation.orion.viewer.R
import universe.constellation.orion.viewer.log
import universe.constellation.orion.viewer.prefs.GlobalOptions.Companion.DEFAULT_LANGUAGE
import universe.constellation.orion.viewer.test.IdlingResource
import java.io.File
import java.util.Locale
import kotlin.properties.Delegates

class OrionApplication : Application(), DefaultLifecycleObserver {

    internal var idlingRes = IdlingResource()

    val options: GlobalOptions by lazy {
        GlobalOptions(this, androidx.preference.PreferenceManager.getDefaultSharedPreferences(this))
    }

    val keyBindingPrefs: KeyBindingPreferences by lazy {
        KeyBindingPreferences(getSharedPreferences("key_binding", Context.MODE_PRIVATE))
    }


    var tempOptions: TemporaryOptions? = null
        private set


    var viewActivity: OrionViewerActivity? = null

    var currentBookParameters: LastPageInfo? = null

    private var currentLanguage: String = DEFAULT_LANGUAGE

    private val appTheme: String
        get() {
            val theme = options.applicationTheme
            return theme
        }

    private val themeId: Int
        get() = when(appTheme) {
            "DARK" -> R.style.Theme_Orion_Dark_NoActionBar
            "LIGHT" -> R.style.Theme_Orion_Light_NoActionBar
            "ANDROID_LIGHT" ->  R.style.Theme_Orion_Android_Light_NoActionBar
            "ANDROID_DARK" ->  R.style.Theme_Orion_Android_Dark_NoActionBar
            else -> R.style.Theme_Orion_Dark_NoActionBar
        }

    val sdkVersion: Int
        get() = Build.VERSION.SDK_INT

    override fun onCreate() {
        instance = this
        super<Application>.onCreate()
        setLanguage(options.appLanguage)
        logOrionAndDeviceInfo()
        initDjvuResources(this)
    }

    fun setLanguage(langCode: String) {
        currentLanguage = langCode
    }

    fun updateLanguage(res: Resources) {
        try {
            val currentLocales = ConfigurationCompat.getLocales(res.configuration)
            val newLocale =
                if (DEFAULT_LANGUAGE == currentLanguage) Locale.getDefault() else Locale(
                    currentLanguage
                )
            if (!currentLocales.isEmpty) {
                if (newLocale.language == currentLocales[0]?.language) return
            }
            log("Updating locale to $currentLanguage from ${currentLocales[0]?.language}")

            if (Build.VERSION.SDK_INT >= 17) {
                val prevLocales = Array(currentLocales.size()) { currentLocales.get(it) }
                ConfigurationCompat.setLocales(
                    res.configuration,
                    LocaleListCompat.create(newLocale, *prevLocales)
                )
            } else {
                res.configuration.locale = newLocale
            }
            res.updateConfiguration(res.configuration, res.displayMetrics)
        } catch (e: Exception) {
            log("Error setting locale: $currentLanguage", e)
        }

    }

    fun onNewBook(fileName: String) {
        tempOptions = TemporaryOptions().also { it.openedFile = fileName }
    }


    fun applyTheme(activity: Activity) {
        if (this.themeId != -1) {
            activity.setTheme(this.themeId)
        }
    }


    fun destroyMainActivity() {
        viewActivity = null
        currentBookParameters = null
    }



    //temporary hack
    fun processBookOptionChange(key: String, value: Any) {
        viewActivity?.controller?.run {
            when (key) {
                "walkOrder" -> changetWalkOrder(value as String)
                "pageLayout" -> changetPageLayout(value as Int)
                "contrast" -> changeContrast(value as Int)
                "threshold" -> changeThreshhold(value as Int)
                "screenOrientation" -> changeOrinatation(value as String)
                "colorMode" -> changeColorMode(value as String, true)
                "zoom" -> changeZoom(value as Int)
            }
        }
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        if (DEBUG) {
            MultiDex.install(this)
        }
    }

    fun debugLogFolder(): File? {
        val download = getExternalFilesDir(null) ?: return null
        return File(download, "debug/logs")
    }



    companion object {
        var instance: OrionApplication by Delegates.notNull()
            private set

        fun logOrionAndDeviceInfo() {
            log("Orion Viewer $VERSION_NAME")
            log("Device: $DEVICE")
            log("Model: $MODEL")
            log("Manufacturer:  $MANUFACTURER")
            log("Android version :  $CODENAME $RELEASE")
        }



        @JvmField
        val MANUFACTURER = getField("MANUFACTURER")

        @JvmField
        val MODEL = getField("MODEL")

        @JvmField
        val DEVICE = getField("DEVICE")

        @JvmField
        val HARDWARE = getField("HARDWARE")


        @JvmField
        val RK30SDK = "rk30sdk".equals(MODEL, ignoreCase = true) && ("T62D".equals(
            DEVICE,
            ignoreCase = true
        ) || DEVICE.lowercase(Locale.getDefault()).contains("onyx"))

        private fun getField(name: String): String =
            try {
                Build::class.java.getField(name).get(null) as String
            } catch (e: Exception) {
                log("Exception on extracting Build property: $name")
                "!ERROR!"
            }


        @JvmField
        val version: String = Build.VERSION.INCREMENTAL

        fun initDjvuResources(orionApplication: Context): Job? {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val fileToCopy = File(orionApplication.filesDir, "djvuConf")
                val envPath = File(fileToCopy, "osi").absolutePath
                return CoroutineScope(SupervisorJob() + Dispatchers.Default).launch {
                    copyResIfNotExists(orionApplication.assets, "osi", fileToCopy)
                }.also {
                    Os.setenv("DJVU_CONFIG_DIR", envPath, true)
                }
            }
            return null
        }
    }
}
