package universe.constellation.orion.viewer.dialog;

import android.graphics.Rect;
import android.view.View;

import universe.constellation.orion.viewer.OrionViewerActivity;
import universe.constellation.orion.viewer.R;
import universe.constellation.orion.viewer.selection.HighlightSelectionView;
import universe.constellation.orion.viewer.selection.SelectionAutomata;
import universe.constellation.orion.viewer.selection.SelectionView;

public class HighlightTextDialog extends TextDialogOverView {

    private final HighlightSelectionView selectionView;
    private Rect rectF;
    private String textWhole;
    private SelectionAutomata selectionAutomata;

    public HighlightTextDialog(final OrionViewerActivity activity, String txt, Rect rect) {
        super(activity, R.layout.highlight_text, android.R.style.Theme_Translucent_NoTitleBar);

        selectionAutomata = new SelectionAutomata(activity);
        rectF = rect;
        textWhole = txt;
        selectionView = dialog.findViewById(R.id.text_selector);
        selectionView.setHighlight(rectF);
        initDialogSize();
       new SelectionView(activity.getBaseContext()).reset();
        dialog.show();
        selectionView.setOnClickListener(v -> {
            selectionAutomata.resetSelection();
            selectionView.reset();
            dialog.dismiss();

        });
    }

}
