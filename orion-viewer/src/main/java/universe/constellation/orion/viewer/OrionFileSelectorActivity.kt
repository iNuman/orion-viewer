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

package universe.constellation.orion.viewer

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.net.toUri
import universe.constellation.orion.viewer.FallbackDialogs.Companion.saveFileByUri
import universe.constellation.orion.viewer.Permissions.checkWritePermission
import universe.constellation.orion.viewer.filemanager.OrionFileManagerActivityBase
import java.io.File
import java.io.FilenameFilter
import java.util.Locale

class OrionSaveFileActivity : OrionFileManagerActivityBase() {

    companion object {
        const val SUGGESTED_FILE_NAME = "SUGGESTED_FILE_NAME"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        findViewById<View>(R.id.saveFileIdView).visibility = View.VISIBLE
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.let {
            it.getStringExtra(SUGGESTED_FILE_NAME)?.let { name ->
                findViewById<TextView>(R.id.fileName).text = name
            }
        }
        checkWritePermission(this)
    }

    private fun saveFile(targetFile: File) {
        val fileUri = intent.extras!!.get(FallbackDialogs.URI) as Uri
        saveFile(intent, targetFile, fileUri)
    }

    private fun saveFile(intent: Intent, target: File, fileUri: Uri) {
        saveFileByUri(intent, fileUri, target.toUri()) {
            openFile(target)
        }
    }
}

class OrionFileSelectorActivity : OrionFileManagerActivityBase() {

    override fun openFile(file: File) {
        val result = Intent()
        result.putExtra(RESULT_FILE_NAME, file.absolutePath)
        setResult(Activity.RESULT_OK, result)
        finish()
    }

    companion object {

        const val RESULT_FILE_NAME = "RESULT_FILE_NAME"
    }
}
