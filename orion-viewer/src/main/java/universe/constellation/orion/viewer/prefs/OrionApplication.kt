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

    var tempOptions: TemporaryOptions? = null
        private set


    var viewActivity: OrionViewerActivity? = null

    var currentBookParameters: LastPageInfo? = null


    val sdkVersion: Int
        get() = Build.VERSION.SDK_INT

    override fun onCreate() {
        instance = this
        super<Application>.onCreate()
    }

    fun onNewBook(fileName: String) {
        tempOptions = TemporaryOptions().also { it.openedFile = fileName }
    }

    fun destroyMainActivity() {
        viewActivity = null
        currentBookParameters = null
    }


    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        if (DEBUG) {
            MultiDex.install(this)
        }
    }



    companion object {
        var instance: OrionApplication by Delegates.notNull()
            private set

    }
}
