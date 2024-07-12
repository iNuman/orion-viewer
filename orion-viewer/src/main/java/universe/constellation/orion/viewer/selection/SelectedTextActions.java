package universe.constellation.orion.viewer.selection;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.text.ClipboardManager;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.PopupWindow;

import universe.constellation.orion.viewer.Action;
import universe.constellation.orion.viewer.OrionBaseActivityKt;
import universe.constellation.orion.viewer.OrionViewerActivity;
import universe.constellation.orion.viewer.R;

public class SelectedTextActions {

    private final PopupWindow popup;

    private final int height;

    private String text;
    private Rect rectSelection;

    private final Dialog originalDialog;


    public SelectedTextActions(final OrionViewerActivity activity, final Dialog originalDialog) {
        height = activity.getView().getSceneHeight();
        this.originalDialog = originalDialog;
        popup = new PopupWindow(activity);
//        popup.setFocusable(true);
//        popup.setTouchable(true);
//        popup.setOutsideTouchable(true);
        popup.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
        popup.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);

        View view = activity.getLayoutInflater().inflate(R.layout.text_actions_new, null);

        popup.setContentView(view);

        ImageView copy_to_Clipboard = view.findViewById(R.id.stext_copy_to_clipboard);
        copy_to_Clipboard.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                popup.dismiss();
                ClipboardManager clipboard = (ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
                clipboard.setText(text);
                Action.HIGHLIGHT.doAction(activity.getController(), activity, text, rectSelection);

                activity.showFastMessage("Copied to clipboard");

            }
        });

        ImageView add_bookmark = view.findViewById(R.id.stext_add_bookmark);
        add_bookmark.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                popup.dismiss();
                Action.ADD_BOOKMARK.doAction(activity.getController(), activity, text);
            }
        });

        ImageView open_dictionary = view.findViewById(R.id.stext_open_dictionary);
        open_dictionary.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                popup.dismiss();
                Action.DICTIONARY.doAction(activity.getController(), activity, text);
            }
        });

        ImageView external_actions = view.findViewById(R.id.stext_send_text);
        external_actions.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                popup.dismiss();
                Intent intent = new Intent(android.content.Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(android.content.Intent.EXTRA_TEXT, text);
                activity.startActivity(Intent.createChooser(intent, null));
            }
        });

        popup.setTouchInterceptor(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
                    popup.dismiss();
                    return true;
                }
                return false;
            }
        });

        popup.setOnDismissListener(originalDialog::dismiss);
    }

    public void updatePosition(String text, Rect selectionRect) {
        rectSelection = selectionRect;
        int[] locationOnScreen = new int[2];
        originalDialog.getWindow().getDecorView().getLocationOnScreen(locationOnScreen);
        int absoluteTop = selectionRect.top + locationOnScreen[1];
        int absoluteBottom = selectionRect.bottom + locationOnScreen[1];

        int x = selectionRect.left;
        int y = 0;

        int popupHeight = popup.getHeight();
        int dialogHeight = originalDialog.getWindow().getDecorView().getHeight();
        if (absoluteTop >= popupHeight + OrionBaseActivityKt.dpToPixels(originalDialog.getContext(), 5)) {
            y = (int) (absoluteTop - popupHeight - OrionBaseActivityKt.dpToPixels(originalDialog.getContext(), 60));
        } else if (absoluteBottom <= locationOnScreen[1] + dialogHeight * 4 / 5) {
            y = (int) (absoluteBottom + OrionBaseActivityKt.dpToPixels(originalDialog.getContext(), 5));
        } else {
            y = absoluteTop + (selectionRect.height() - popupHeight) / 2;
        }

        y -= locationOnScreen[1];
        this.text = text;
        popup.update(x, y, -1, -1);
    }

    public void show(String text, Rect selectionRect) {
        rectSelection = selectionRect;
        int x = selectionRect.left, y = 0;
        System.out.println(selectionRect);
        // Calculate the height of the popup
        int popupHeight = popup.getHeight();

        // Check if there's enough space above the selectionRect to show the popup
        if (selectionRect.top >= popupHeight + OrionBaseActivityKt.dpToPixels(originalDialog.getContext(), 5)) {
            y = (int) (selectionRect.top - popupHeight - OrionBaseActivityKt.dpToPixels(originalDialog.getContext(), 60));
        } else if (selectionRect.bottom <= height * 4 / 5) {
            y = (int) (selectionRect.bottom + OrionBaseActivityKt.dpToPixels(originalDialog.getContext(), 5));
        } else {
            y = selectionRect.centerY();
        }

        this.text = text;
        View decorView = originalDialog.getWindow().getDecorView();
        popup.showAsDropDown(decorView, x, y - decorView.getHeight());
    }


    public void dismissOnlyDialog() {
        popup.setOnDismissListener(null);
        popup.dismiss();

    }
}
