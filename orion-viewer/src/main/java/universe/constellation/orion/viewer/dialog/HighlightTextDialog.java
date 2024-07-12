package universe.constellation.orion.viewer.dialog;

import android.graphics.Rect;
import android.view.View;

import java.util.List;

import universe.constellation.orion.viewer.OrionViewerActivity;
import universe.constellation.orion.viewer.R;
import universe.constellation.orion.viewer.selection.HighlightSelectionView;
import universe.constellation.orion.viewer.selection.SelectionViewNew;

public class HighlightTextDialog extends DialogOverView {

    private final HighlightSelectionView selectionView;
    private List<Rect> rectF;
    private String textWhole;

    public HighlightTextDialog(final OrionViewerActivity activity, String txt, List<Rect> rect) {
        super(activity, R.layout.highlight_text, android.R.style.Theme_Translucent_NoTitleBar);

        rectF = rect;
        textWhole = txt;
        selectionView = dialog.findViewById(R.id.text_selector);
        for (int i = 0; i < rect.size(); i++) {
            selectionView.setHighlight(rectF.get(i));
        }
        initDialogSize();
       new SelectionViewNew(activity.getBaseContext()).reset();
        dialog.show();
        selectionView.setOnClickListener(v -> {
            selectionView.reset();
            dialog.dismiss();

        });
    }

}
