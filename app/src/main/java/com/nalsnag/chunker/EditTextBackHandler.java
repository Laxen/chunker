package com.nalsnag.chunker;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

public class EditTextBackHandler extends EditText {
    private RecyclerAdapter.ViewHolder vh;

    public EditTextBackHandler(Context context) {
        super(context);
    }

    public EditTextBackHandler(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public EditTextBackHandler(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /**
     * Handles the click on the back key when dismissing the keyboard
     */
    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        Log.d("HEJ", "PreIME");

        if(event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            this.setFocusableInTouchMode(false); // Make titleView unfocusable by touch
            this.clearFocus(); // And clear its focus

            vh.updateTask(); // Saves the changes to the task object

            // Hide soft keyboard
            final InputMethodManager inputMethodManager = (InputMethodManager) this.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(this.getWindowToken(), 0);
        }

        return false;
    }

    /**
     * Sets the ViewHolder that this EditText is in
     */
    public void setViewHolder(RecyclerAdapter.ViewHolder vh) {
        this.vh = vh;
    }
}
