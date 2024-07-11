package universe.constellation.orion.viewer.dialog;

import static universe.constellation.orion.viewer.LoggerKt.log;

import android.app.Dialog;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;

import universe.constellation.orion.viewer.OrionViewerActivity;
import universe.constellation.orion.viewer.view.OrionDrawScene;

public class TextDialogOverView {

    protected final OrionViewerActivity activity;

    public final Dialog dialog;

    public TextDialogOverView(OrionViewerActivity activity, int layoutId, int style) {
        this.activity = activity;

        dialog = new Dialog(activity, style);
        dialog.setCanceledOnTouchOutside(true);
        dialog.setContentView(layoutId);
    }

    protected void initDialogSize() {
        OrionDrawScene view = activity.getView();
        int width = view.getSceneWidth();
        int height = view.getSceneHeight();
        log("Dialog dim: " + width + "x" + height);
        Window window = dialog.getWindow();
        if (window == null) return;
        WindowManager.LayoutParams params = window.getAttributes();
        params.gravity = Gravity.TOP;
        params.width = width;
        params.height = height;
        params.y = view.getSceneYLocationOnScreen();
        window.setAttributes(params);
    }
}
