package universe.constellation.orion.viewer

import android.app.AlertDialog
import android.app.Dialog
import android.app.ProgressDialog
import android.content.ContentResolver
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import universe.constellation.orion.viewer.filemanager.OrionFileManagerActivity
import java.io.File
import java.util.Locale

class ResourceIdAndString(val id: Int, val value: String) {
    override fun toString(): String {
        return value
    }
}

open class FallbackDialogs {

    fun createBadIntentFallbackDialog(activity: OrionViewerActivity, fileInfo: FileInfo?, intent: Intent): Dialog {
//        val isContentScheme = intent.isContentScheme()
        return createFallbackDialog(
            activity,
            fileInfo,
            intent,
            R.string.fileopen_error_during_intent_processing,
            R.string.fileopen_error_during_intent_processing_info,
            null,
//            if (isContentScheme) R.string.fileopen_open_in_temporary_file else null,
            listOfNotNull(
//                R.string.fileopen_open_in_temporary_file.takeIf { isContentScheme },
                R.string.fileopen_open_recent_files,
                R.string.fileopen_report_error_by_github_and_return,
                R.string.fileopen_report_error_by_email_and_return
            )
        )
    }

     //content intent
     private fun createFallbackDialog(activity: OrionViewerActivity, fileInfo: FileInfo?, intent: Intent, title: Int, info: Int, defaultAction: Int?, list: List<Int>): Dialog {
         val dialogTitle = activity.getString(title)
//         activity.showErrorOrFallbackPanel(dialogTitle, intent, cause = dialogTitle)

         val uri = intent.data!!
         val view = activity.layoutInflater.inflate(R.layout.intent_problem_dialog, null)
         val infoText = view.findViewById<TextView>(R.id.intent_problem_info)
         infoText.setText(info)

         val builder = AlertDialog.Builder(activity)

         builder.setTitle(dialogTitle).setView(view)
             .setNegativeButton(R.string.string_cancel) { dialog, _ ->
                 dialog.cancel()
             }

         if (defaultAction != null) {
             builder.setPositiveButton(defaultAction) { dialog, _ ->
                 processAction(defaultAction, activity, fileInfo, dialog, uri, intent)
             }
         }

         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
             builder.setOnDismissListener { _ ->
             }
         }

         val alertDialog = builder.create()
         val fallbacks = view.findViewById<ListView>(R.id.intent_fallback_list)
         fallbacks.adapter = ArrayAdapter(activity, android.R.layout.simple_list_item_1, list.map { ResourceIdAndString(it, activity.getString(it)) })

         fallbacks.setOnItemClickListener { _, _, position, _ ->
             val id = (fallbacks.adapter.getItem(position) as ResourceIdAndString).id
             processAction(id, activity, fileInfo, alertDialog, uri, intent)
         }
         return alertDialog
    }

    private fun processAction(
        id: Int,
        activity: OrionViewerActivity,
        fileInfo: FileInfo?,
        alertDialog: DialogInterface,
        uri: Uri,
        intent: Intent
    ) {
        when (id) {

            R.string.fileopen_save_to_file -> {
//                if (isAtLeastKitkat()) {
//                    sendCreateFileRequest(activity, fileInfo, intent)
//                } else {
                    activity.startActivity(
                        Intent(activity, OrionSaveFileActivity::class.java).apply {
                            putExtra(URI, uri)
                            fileInfo?.name?.let {
                                putExtra(OrionSaveFileActivity.SUGGESTED_FILE_NAME, it)
                            }
                        }
                    )
//                }
                alertDialog.dismiss()
            }

            R.string.fileopen_open_in_temporary_file -> {
            }

            R.string.fileopen_open_recent_files -> {
                alertDialog.dismiss()
                activity.startActivity(
                    Intent(activity, OrionFileManagerActivity::class.java).apply {
                        putExtra(OrionFileManagerActivity.OPEN_RECENTS_TAB, true)

                    })
            }

            R.string.fileopen_report_error_by_github_and_return -> {
                val title =
                    activity.applicationContext.getString(R.string.crash_on_intent_opening_title)
//                activity.reportErrorVia(false, title, intent.toString())

            }

            R.string.fileopen_report_error_by_email_and_return -> {
                val title =
                    activity.applicationContext.getString(R.string.crash_on_intent_opening_title)
//                activity.reportErrorVia(true, title, intent.toString())
            }

            else -> error("Unknown option id: $id")
        }
    }

    companion object {

        const val URI = "URI"

        fun OrionBaseActivity.saveFileByUri(
            intent: Intent,
            originalContentUri: Uri,
            targetFileUri: Uri,
            handler: CoroutineExceptionHandler = CoroutineExceptionHandler { _, exception ->
                exception.printStackTrace()

            },
            callbackAction: () -> Unit
        ) {
            val res = this.orionApplication.idlingRes
            res.busy()

            GlobalScope.launch(Dispatchers.Main + handler) {
                val progressBar = ProgressDialog(this@saveFileByUri)
                progressBar.isIndeterminate = true
                progressBar.show()
                try {
                    withContext(Dispatchers.IO) {
                        (contentResolver.openInputStream(originalContentUri)?.use { input ->
                            contentResolver.openOutputStream(targetFileUri)?.use { output ->
                                input.copyTo(output)
                            } ?: error("Can't open output stream for $targetFileUri")
                        } ?: error("Can't read file data: $originalContentUri"))
                    }
                    callbackAction()
                } finally {
                    progressBar.dismiss()
                    res.free()
                }
            }
        }
    }
}

private fun Context.tmpContentFolderForFile(fileInfo: FileInfo?): File {
    val contentFolder = cacheContentFolder()
    return if (fileInfo == null) contentFolder
    else File(contentFolder, fileInfo.uri.host + "/" + (fileInfo.id ?: ("_" + fileInfo.size)) + "/")
}

fun Context.cacheContentFolder(): File {
    return File(cacheDir, ContentResolver.SCHEME_CONTENT)
}



internal fun Context.createTmpFile(fileInfo: FileInfo?, extension: String): File {
    val fileFolder = tmpContentFolderForFile(fileInfo)
    fileFolder.mkdirs()
    if (fileInfo?.canHasTmpFileWithStablePath() == true) {
        return File(fileFolder, fileInfo.name!!)
    } else {
        val fullName = (fileInfo?.name ?: fileInfo?.file?.name ?: "test_book")
        val noExtName = if (fullName.lowercase(Locale.getDefault()).endsWith(".$extension")) {
            fullName.substringBeforeLast(".$extension")
        } else {
            fullName
        }

        return File.createTempFile(
            if (noExtName.length < 3) "tmp$noExtName" else noExtName,
            ".$extension",
            fileFolder
        )
    }
}


fun FileInfo.canHasTmpFileWithStablePath(): Boolean {
    return !id.isNullOrBlank() && size != 0L && !name.isNullOrBlank()
}

fun Context.getStableTmpFileIfExists(fileInfo: FileInfo): File? {
    if (!fileInfo.canHasTmpFileWithStablePath()) return null
    val file = File(tmpContentFolderForFile(fileInfo), fileInfo.name ?: return null)
    return file.takeIf { it.exists() }
}
