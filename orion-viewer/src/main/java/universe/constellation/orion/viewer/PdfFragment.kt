package universe.constellation.orion.viewer

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import universe.constellation.orion.viewer.databinding.MainViewBinding
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import android.os.Debug
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.doOnLayout
import kotlinx.coroutines.*
//import universe.constellation.orion.viewer.dialog.SearchDialog
import universe.constellation.orion.viewer.layout.SimpleLayoutStrategy
import universe.constellation.orion.viewer.prefs.OrionApplication
import universe.constellation.orion.viewer.prefs.initalizer
import universe.constellation.orion.viewer.selection.NewTouchProcessor
import universe.constellation.orion.viewer.selection.NewTouchProcessorWithScale
import universe.constellation.orion.viewer.selection.SelectionAutomata
import universe.constellation.orion.viewer.view.FullScene
import universe.constellation.orion.viewer.view.OrionDrawScene
import universe.constellation.orion.viewer.view.StatusBar
import java.util.concurrent.Executors


class PdfFragment : Fragment() {

    private val binding by lazy(LazyThreadSafetyMode.NONE) {
        MainViewBinding.inflate(layoutInflater)
    }

    internal val subscriptionManager = SubscriptionManager()

    private var lastPageInfo: LastPageInfo? = null

    var controller: Controller? = null
        private set

    var _isResumed: Boolean = false

    val selectionAutomata: SelectionAutomata by lazy {
        SelectionAutomata(this)
    }

    private var newTouchProcessor: NewTouchProcessor? = null

    lateinit var fullScene: FullScene
        private set

    val view: OrionDrawScene
        get() = binding.view

    private val statusBarHelper: StatusBar
        get() = fullScene.statusBarHelper

    val orionApplication: OrionApplication
        get() = requireActivity().applicationContext as OrionApplication

    val globalOptions
        get() = orionApplication.options

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = binding.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        log("Creating PdfFragment...")

        val book = File("${requireContext().filesDir.path}/Android.pdf")
        if (book.exists()) {
            log("Opening recent book $book")
            openFile(book)
        }

        fullScene = FullScene(
            binding.orionFullScene,
            binding.view,
            binding.statusBarContainer.orionStatusBar,
            this@PdfFragment
        )
        fullScene.setDrawOffPage(true)
        // fullScene.setDrawOffPage(globalOptions.isDrawOffPage)

        newTouchProcessor = NewTouchProcessorWithScale(this.view, this@PdfFragment)
        view.setOnTouchListener { _, event ->
            newTouchProcessor!!.onTouch(event)
        }
    }

    @Throws(Exception::class)
    private fun openFile(file: File) {
        log("Runtime.getRuntime().totalMemory(): ${Runtime.getRuntime().totalMemory()}")
        log("Debug.getNativeHeapSize(): ${Debug.getNativeHeapSize()}")
        log("openFileAndDestroyOldController")

        GlobalScope.launch(Dispatchers.Main) {
            log("Trying to open file: $file")
            val rootJob = Job()
            val executor = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
            val newDocument = try {
                withContext(executor + rootJob) {
                    FileUtil.openFile(file)
                }
            } catch (e: Exception) {
                executor.close()
                return@launch
            }

            try {
                if (newDocument.pageCount == 0) {
                    newDocument.destroy()
                    return@launch
                }

                val layoutStrategy = SimpleLayoutStrategy.create()
                val controller1 = Controller(
                    this@PdfFragment,
                    newDocument,
                    layoutStrategy,
                    rootJob,
                    context = executor
                )

                val lastPageInfo1 = loadBookParameters(rootJob, file)
                log("Read LastPageInfo for page ${lastPageInfo1.pageNumber}")
                lastPageInfo = lastPageInfo1
                orionApplication.currentBookParameters = lastPageInfo1

                controller = controller1
                bind(binding.view, controller1)

                updateViewOnNewBook((newDocument.title?.takeIf { it.isNotBlank() }
                    ?: file.name.substringBeforeLast(".")))

                val drawView = fullScene.drawView
                controller1.init(lastPageInfo1, drawView.sceneWidth, drawView.sceneHeight)

                subscriptionManager.sendDocOpenedNotification(controller1)

                lastPageInfo1.totalPages = newDocument.pageCount
                orionApplication.onNewBook(file.name)
                doOnLayout(lastPageInfo1)
            } catch (e: Exception) {
                if (controller != null) {
                    log("Exception for page ${e.printStackTrace()}")

                    destroyController()
                } else {
                    newDocument.destroy()
                }
            } finally {
                // orionApplication.idlingRes.free()
            }
        }
    }

    private suspend fun loadBookParameters(
        rootJob: CompletableJob,
        bookFile: File,
    ): LastPageInfo {
        return withContext(Dispatchers.Default + rootJob) {
            loadBookParameters(
                requireActivity() as OrionViewerActivity,
                bookFile.absolutePath,
                initalizer(globalOptions)
            )
        }
    }

    private fun bind(view: OrionDrawScene, controller: Controller) {
        this.controller = controller
        view.setDimensionAware(controller)
    }

    private fun updateViewOnNewBook(title: String?) {
        fullScene.onNewBook(title, controller!!.pageCount)
        (activity as? AppCompatActivity)?.supportActionBar?.title = title
    }

    override fun onPause() {
        log("PdfFragment: onPause")
        _isResumed = false
        super.onPause()
        statusBarHelper.onPause(requireContext())
    }

    override fun onResume() {
        _isResumed = true
        super.onResume()
        updateBrightness()
        log("onResume")

        if (controller != null) {
            controller!!.processPendingEvents()
        }
        statusBarHelper.onResume(requireContext())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        log("onDestroyView")
        destroyController()
        orionApplication.destroyMainActivity()
    }

    fun doAction(actionCode: Int) {
        val action = Action.getAction(actionCode)
        doAction(action)
        log("Code action $actionCode")
    }

    internal fun doAction(action: Action) {
        action.doAction(controller, this, null)
    }

    private fun updateBrightness() {
        val params = requireActivity().window.attributes
        val oldBrightness = params.screenBrightness
        if (globalOptions.isCustomBrightness) {
            params.screenBrightness = globalOptions.brightness.toFloat() / 100
            requireActivity().window.attributes = params
        } else {
            if (oldBrightness >= 0) {
                params.screenBrightness = -1f
                requireActivity().window.attributes = params
            }
        }
    }

    fun textSelectionMode(isSingleSelection: Boolean, translate: Boolean) {
        selectionAutomata.startSelection(isSingleSelection, translate)
    }

    private fun doOnLayout(lastPageInfo1: LastPageInfo) {
        (binding.view as View).doOnLayout {
            controller?.drawPage(
                lastPageInfo1.pageNumber,
                lastPageInfo1.newOffsetX,
                lastPageInfo1.newOffsetY,
                lastPageInfo1.isSinglePageMode
            )
            controller?.pageLayoutManager?.updateCacheAndRender()
        }
    }

    fun startSearch() {
//        SearchDialog.newInstance().show(parentFragmentManager, "search")
    }

    private fun destroyController() {
        log("Controller: destroy")
        controller?.destroy()
        controller = null
    }

    fun showWarning(warning: String) {
        Toast.makeText(requireContext(), warning, Toast.LENGTH_SHORT).show()
    }

    fun showFastMessage(stringId: Int) {
        showWarning(resources.getString(stringId))
    }

    fun showFastMessage(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        @JvmStatic
        fun copyFileFromAssetsToInternal(context: Context, assetFileName: String): String? {
            val inputStream: InputStream
            val outputStream: OutputStream
            try {
                inputStream = context.assets.open(assetFileName)
                val outputFile = File(context.filesDir, assetFileName)
                outputStream = FileOutputStream(outputFile)
                val buffer = ByteArray(1024)
                var length: Int
                while (inputStream.read(buffer).also { length = it } > 0) {
                    outputStream.write(buffer, 0, length)
                }
                outputStream.flush()
                outputStream.close()
                inputStream.close()
                return outputFile.absolutePath
            } catch (e: IOException) {
                e.printStackTrace()
            }
            return null
        }

        private const val ARG_IMAGE_URI = "image_uri"
        private var imageUri: Uri? = null

        @JvmStatic
        fun newInstance(imageUri: Uri): PdfFragment {
            val fragment = PdfFragment()
            val args = Bundle()
            args.putParcelable(ARG_IMAGE_URI, imageUri)
            fragment.arguments = args
            return fragment
        }
    }
}
