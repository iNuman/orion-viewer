package universe.constellation.orion.viewer.selection;

import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;
import android.view.MotionEvent;

import androidx.annotation.NonNull;

import java.util.List;

import universe.constellation.orion.viewer.Action;
import universe.constellation.orion.viewer.Controller;
import universe.constellation.orion.viewer.OrionViewerActivity;
import universe.constellation.orion.viewer.R;
import universe.constellation.orion.viewer.dialog.DialogOverView;
import universe.constellation.orion.viewer.document.TextAndSelection;
import universe.constellation.orion.viewer.view.PageLayoutManager;

public class SelectionAutomata extends DialogOverView {

    public enum STATE {START, MOVING, END, CANCELED}

    private final static int SINGLE_WORD_AREA = 2;

    private STATE state = STATE.CANCELED;

    private int startX, startY, width, height;

    private Rect rectF;
    private final SelectionView selectionView;

    private boolean isSingleWord = false;
    private boolean translate = false;
    private String textWhole;
    private SelectedTextActions selectedTextActions;

    public SelectionAutomata(final OrionViewerActivity activity) {
        super(activity, universe.constellation.orion.viewer.R.layout.text_selector, android.R.style.Theme_Translucent_NoTitleBar);

        selectionView = dialog.findViewById(R.id.text_selector);
        selectionView.setOnSelectionChangedListener(new SelectionView.OnSelectionChangedListener() {


            @Override
            public void onSelectionChanged(int startXX, int startYY, int widthh, int heightt, Rect newRectF) {
                startX = startXX;
                startY = startYY;
                width = widthh;
                height = heightt;
                rectF = newRectF;
                selectText(isSingleWord, translate, getSelectionRectangle(), getScreenSelectionRect());

            }

            @Override
            public void onStateChanged(STATE newState) {
                state = newState;
                if (state == STATE.END) {
                    selectedTextActions.updatePosition(textWhole, rectF);
                }
            }
        });

        selectedTextActions = new SelectedTextActions(activity, dialog);

    }

    public void selectText(
            boolean isSingleWord, boolean translate, List<PageAndSelection> data, Rect originSelection
    ) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        Controller controller = activity.getController();
        if (controller == null) return;

        for (PageAndSelection selection: data) {
            Rect rect = selection.getAbsoluteRectWithoutCrop();
            TextAndSelection text = controller.selectRawText(selection.getPage(), rect.left, rect.top, rect.width(), rect.height(), isSingleWord);
            if (text != null) {
                if (!first) {
                    sb.append(" ");
                }
                sb.append(text.getValue());
                first = false;
            }
            if (isSingleWord && text != null) {
                RectF originRect = text.getRect();
                RectF sceneRect = selection.getPageView().getSceneRect(originRect);
                originSelection = new Rect((int) sceneRect.left, (int) sceneRect.top, (int) sceneRect.right, (int) sceneRect.bottom);

                selectionView.updateView((int) sceneRect.left, (int) sceneRect.top, (int) sceneRect.right, (int) sceneRect.bottom);
            }
        }
        String text = sb.toString();
        if (!text.isEmpty()) {
            if (isSingleWord && translate) {
                dialog.dismiss();
                Action.DICTIONARY.doAction(controller, activity, text);
            } else {
                if (isSingleWord && !dialog.isShowing()) {
                    //TODO: refactor
                    final Rect origin = originSelection;
                    rectF = origin;
                    dialog.setOnShowListener(dialog2 -> {
                        if (selectedTextActions == null){
                            selectedTextActions = new SelectedTextActions(activity, dialog);
                        }else {
                            selectedTextActions.show(text, origin);
                        }
                        dialog.setOnShowListener(null);
                    });
                    startSelection(true, false, true);
                    state = STATE.END;
                } else {
                    textWhole = text;
                    rectF = originSelection;
                    if (selectedTextActions == null) {
                        selectedTextActions = new SelectedTextActions(activity, dialog);
                    } else {
                        selectedTextActions.show(text, originSelection);
                    }
                }
            }
        } else {
            dialog.dismiss();
            resetSelection();
            activity.showFastMessage(R.string.warn_no_text_in_selection);
        }
    }

    public void startSelection(boolean isSingleWord, boolean translate) {
        startSelection(isSingleWord, translate, false);
    }

    public void startSelection(boolean isSingleWord, boolean translate, boolean quite) {
        selectionView.setColorFilter(activity.getFullScene().getColorStuff().getBackgroundPaint().getColorFilter());
        if (!quite) {
//            selectionView.reset();
            resetSelection();
        }
        initDialogSize();
        dialog.show();
        if (!quite) {
            String msg = activity.getResources().getString(isSingleWord ? R.string.msg_select_word : R.string.msg_select_text);
//            resetSelection();
            activity.showFastMessage(msg);
        }
        state = STATE.START;
        this.isSingleWord = isSingleWord;
        this.translate = translate;
    }

    private List<PageAndSelection> getSelectionRectangle() {
        Rect screenRect = rectF;//getScreenSelectionRect();
        return getSelectionRectangle(screenRect.left, screenRect.top, screenRect.width(), screenRect.height(), isSingleWord, activity.getController().getPageLayoutManager());
    }

    private Rect getScreenSelectionRect() {
        int startX = this.startX;
        int startY = this.startY;
        int width = this.width;
        int height = this.height;

        if (width < 0) {
            startX += width;
            width = -width;
        }
        if (height < 0) {
            startY += height;
            height = -height;
        }

        return new Rect(startX, startY, startX + width, startY + height);
    }

    public void resetSelection() {
        state = STATE.CANCELED;
        startX = 0;
        startY = 0;
        width = 0;
        height = 0;
        textWhole = null;
        rectF = null; // Reset rectF
        selectionView.reset(); // Reset the SelectionView
        dialog.dismiss();
        dialog.setOnShowListener(null);
        selectionView.invalidate();
        selectedTextActions = null;
    }
    public static List<PageAndSelection> getSelectionRectangle(int startX, int startY, int width, int height, boolean isSingleWord, PageLayoutManager pageLayoutManager) {
        Rect rect = getSelectionRect(startX, startY, width, height, isSingleWord);
        return pageLayoutManager.findPageAndPageRect(rect);
    }

    @NonNull
    public static Rect getSelectionRect(int startX, int startY, int width, int height, boolean isSingleWord) {
        int singleWordDelta = isSingleWord ? SINGLE_WORD_AREA : 0;
        int x = startX - singleWordDelta;
        int y = startY - singleWordDelta;
        return new Rect(x, y, x + width + singleWordDelta, y + height + singleWordDelta);
    }
}
