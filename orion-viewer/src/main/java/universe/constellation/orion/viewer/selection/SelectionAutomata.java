package universe.constellation.orion.viewer.selection;

import android.graphics.Rect;
import android.graphics.RectF;
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

    private enum STATE {START, MOVING, END, CANCELED}

    private final static int SINGLE_WORD_AREA = 2;

    private STATE state = STATE.MOVING;

    private int startX, startY, endX, endY;
    private int initialStartX, initialStartY, initialEndX, initialEndY;

    //    private boolean isDraggingStart = false;
//    private boolean isDraggingEnd = false;
    private static final int HANDLE_TOUCH_THRESHOLD = 50; // Adjust as needed

    private final SelectionView selectionView;

    private boolean isSingleWord = false;
    private boolean translate = false;

    public SelectionAutomata(final OrionViewerActivity activity) {
        super(activity, universe.constellation.orion.viewer.R.layout.text_selector, android.R.style.Theme_Translucent_NoTitleBar);

        selectionView = dialog.findViewById(R.id.text_selector);
//        selectionView.setOnTouchListener((v, event) -> SelectionAutomata.this.onTouch(event));
    }


//    public boolean onTouch(MotionEvent event) {
//        int action = event.getAction();
//        STATE oldState = state;
//        boolean result = true;
//
//        switch (state) {
//            case START:
//                if (action == MotionEvent.ACTION_DOWN) {
//                    Log.d("ffnet", "onTouch: ");
//                    // Determine if touch is near start or end handle
//                    if (isNearStartHandle(event)) {
//                        // Start dragging start handle
//                        state = STATE.MOVING;
//                        isDraggingStart = true;
//                        startX = (int) event.getX();
//                        startY = (int) event.getY();
//                        initialStartX = startX;
//                        initialStartY = startY;
//                        state = STATE.MOVING;
////                        selectionView.reset();
//
//                    } else if (isNearEndHandle(event)) {
//                        // Start dragging end handle
//                        state = STATE.MOVING;
//                        isDraggingEnd = true;
//                    } else {
//                        state = STATE.CANCELED;
//                    }
//                } else {
//                    state = STATE.CANCELED;
//                }
//                break;
//
//            case MOVING:
//                if (action == MotionEvent.ACTION_MOVE) {
//                    if (isDraggingStart && selectionView.startPoint != null) {
//                        startX = (int) event.getX();
//                        startY = (int) event.getY();
//                        initialStartX = startX;
//                        initialStartY = startY;
//                        selectionView.updateView(Math.min(initialStartX, endX), Math.min(initialStartY, endY), Math.max(initialStartX, endX), Math.max(initialStartY, endY));
//
////                        updateSelectionView();
//                        selectionView.invalidate();
//                    } else if (isDraggingEnd && selectionView.endPoint != null) {
//                        // Update endPoint in SelectionView
//                        endX = (int) event.getX();
//                        endY = (int) event.getY();
//                        selectionView.updateView(Math.min(initialStartX, endX), Math.min(initialStartY, endY), Math.max(initialStartX, endX), Math.max(initialStartY, endY));
//                        selectionView.invalidate();
//                    }
//
//
//                } else if (action == MotionEvent.ACTION_UP) {
//                    state = STATE.END;
//                    isDraggingStart = false;
//                    isDraggingEnd = false;
//                }
//                break;
//
//            default:
//                result = false;
//        }
//
//        if (oldState != state) {
//            switch (state) {
//                case CANCELED:
//                    dialog.dismiss();
//                    break;
//
//                case END:
//                    // Use startPoint and endPoint from SelectionView for selection
//                    selectText(isSingleWord, translate, getSelectionRectangle(), getScreenSelectionRect());
//                    break;
//            }
//        }
//        return result;
//    }

//    private boolean isNearStartHandle(MotionEvent event) {
//        // Calculate distance from touch point to startPoint and check if within threshold
//        if (selectionView.startPoint != null) {
//            double distance = Math.sqrt(
//                    Math.pow((event.getX() - selectionView.startPoint.x), 2.0) +
//                            Math.pow((event.getY() - selectionView.startPoint.y), 2.0)
//            );
//            return distance <= HANDLE_TOUCH_THRESHOLD;
//        }
//        return false;
//    }

//    private boolean isNearEndHandle(MotionEvent event) {
//        // Calculate distance from touch point to endPoint and check if within threshold
//        if (selectionView.endPoint != null) {
//            double distance = Math.sqrt(
//                    Math.pow((event.getX() - selectionView.endPoint.x), 2.0) +
//                            Math.pow((event.getY() - selectionView.endPoint.y), 2.0)
//            );
//            return distance <= HANDLE_TOUCH_THRESHOLD;
//        }
//        return false;
//    }


//
//    public boolean onTouch(MotionEvent event) {
//        int action = event.getAction();
//        STATE oldState = state;
//        boolean result = true;
//        switch (state) {
//            case START:
//                if (action == MotionEvent.ACTION_DOWN) {
//                    startX = (int) event.getX();
//                    startY = (int) event.getY();
//                    initialStartX = startX;
//                    initialStartY = startY;
//                    state = STATE.MOVING;
//                    selectionView.reset();
//                } else {
//                    state = STATE.CANCELED;
//                }
//                break;
//
//            case MOVING:
//                endX = (int) event.getX();
//                endY = (int) event.getY();
//                if (action == MotionEvent.ACTION_UP) {
//                    state = STATE.END;
//                } else {
//                    selectionView.updateView(Math.min(initialStartX, endX), Math.min(initialStartY, endY), Math.max(initialStartX, endX), Math.max(initialStartY, endY));
//                }
//                break;
//
//            default:
//                result = false;
//        }
//
//        if (oldState != state) {
//            switch (state) {
//                case CANCELED:
//                    dialog.dismiss();
//                    break;
//
//                case END:
//                    selectText(isSingleWord, translate, getSelectionRectangle(), getScreenSelectionRect());
//                    break;
//            }
//        }
//        return result;
//    }


//    public void updateSelection(int left, int top, int right, int bottom) {
//        selectionView.updateView(left, top, right, bottom);
//    }

    public void selectText(
            boolean isSingleWord, boolean translate, List<PageAndSelection> data, Rect originSelection
    ) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        Controller controller = activity.getController();
        if (controller == null) return;

        for (PageAndSelection selection : data) {
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
                    final Rect origin = originSelection;
                    dialog.setOnShowListener(dialog2 -> {
                        new SelectedTextActions(activity, dialog).show(text, origin);
                        dialog.setOnShowListener(null);
                    });
                    startSelection(true, false, true);
                    state = STATE.END;
                } else {
                    new SelectedTextActions(activity, dialog).show(text, originSelection);
                }
            }
        } else {
            dialog.dismiss();
            activity.showFastMessage(R.string.warn_no_text_in_selection);
        }
    }

    public void startSelection(boolean isSingleWord, boolean translate) {
        startSelection(isSingleWord, translate, false);
    }

    public void startSelection(boolean isSingleWord, boolean translate, boolean quite) {
        selectionView.setColorFilter(activity.getFullScene().getColorStuff().getBackgroundPaint().getColorFilter());
        if (!quite) {
            selectionView.reset();
        }
        initDialogSize();
        dialog.show();
        if (!quite) {
            String msg = activity.getResources().getString(isSingleWord ? R.string.msg_select_word : R.string.msg_select_text);
            activity.showFastMessage(msg);
        }
        state = STATE.START;
        this.isSingleWord = isSingleWord;
        this.translate = translate;
    }

    private List<PageAndSelection> getSelectionRectangle() {
        Rect screenRect = getScreenSelectionRect();
        return getSelectionRectangle(screenRect.left, screenRect.top, screenRect.width(), screenRect.height(), isSingleWord, activity.getController().getPageLayoutManager());
    }

    private Rect getScreenSelectionRect() {
        return new Rect(
                Math.min(initialStartX, endX),
                Math.min(initialStartY, endY),
                Math.max(initialStartX, endX),
                Math.max(initialStartY, endY)
        );
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
