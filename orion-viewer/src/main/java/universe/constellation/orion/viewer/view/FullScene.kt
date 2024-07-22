package universe.constellation.orion.viewer.view

import android.annotation.SuppressLint
import android.graphics.drawable.ColorDrawable
import android.view.ViewGroup
import androidx.core.graphics.drawable.DrawableCompat
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat
import universe.constellation.orion.viewer.OrionBookListener
import universe.constellation.orion.viewer.OrionViewerActivity
import universe.constellation.orion.viewer.PdfFragment
import universe.constellation.orion.viewer.R

interface Scene  {
    fun setColorMatrix(colorMatrix: FloatArray?) {
    }

    fun setDrawOffPage(drawOffPage: Boolean) {
    }
}

@SuppressLint("UseCompatLoadingForDrawables")
class FullScene(private val scene: ViewGroup, val drawView: OrionDrawScene, statusBar: ViewGroup, val context: PdfFragment) : Scene, OrionBookListener {

    val statusBarHelper = StatusBar(scene, statusBar, context)

    val colorStuff = ColorStuff()

    init {
        val drawable = VectorDrawableCompat.create(context.resources, R.drawable.loading, null)
            ?: ColorDrawable(context.resources.getColor(R.color.orion_orange))
        DrawableCompat.setTint(drawable, context.resources.getColor(R.color.orion_orange))
        drawView.init(colorStuff, statusBarHelper, drawable)
    }

    override fun onNewBook(title: String?, pageCount: Int) {
        statusBarHelper.onNewBook(title, pageCount)
    }

    override fun setColorMatrix(colorMatrix: FloatArray?) {
        colorStuff.setColorMatrix(scene, colorMatrix)
//        statusBarHelper.setColorMatrix(colorMatrix)
    }

    override fun setDrawOffPage(drawOffPage: Boolean) {
        colorStuff.renderOffPage(scene, drawOffPage)
    }
}