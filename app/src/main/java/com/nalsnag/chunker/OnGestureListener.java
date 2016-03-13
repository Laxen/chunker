package com.nalsnag.chunker;

import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

public class OnGestureListener implements GestureDetector.OnGestureListener {
    private View view;
    private int position;
    private RecyclerAdapter adapter;

    private static final int SWIPE_THRESHOLD = 100;
    private static final int SWIPE_VELOCITY_THRESHOLD = 0;

    public OnGestureListener(RecyclerAdapter adapter) {
        super();

        this.adapter = adapter;
    }

    /**
     * Notified when a tap occurs with the down MotionEvent that triggered it
     */
    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    /**
     * The user has performed a down MotionEvent and not performed a move or up yet
     */
    @Override
    public void onShowPress(MotionEvent e) {

    }

    /**
     * Notified when a tap occurs with the up MotionEvent that triggered it
     */
    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        if(position != -1) { // -1 means that we clicked where there is no view
            adapter.onClick(position);
        }
        return false;
    }

    /**
     * Notified when a scroll occurs with the initial on down MotionEvent and the current move MotionEvent
     */
    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    /**
     * Notified when a long press occurs with the initial on down MotionEvent that triggered it
     */
    @Override
    public void onLongPress(MotionEvent e) {

    }

    /**
     * Notified of a fling event when it occurs with the initial on down MotionEvent and the matching up MotionEvent
     */
    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        boolean result = false;
        try {
            float diffY = e2.getY() - e1.getY();
            float diffX = e2.getX() - e1.getX();
            if (Math.abs(diffX) > Math.abs(diffY)) {
                if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffX > 0) {
                        onSwipeRight();
                    } else {
                        onSwipeLeft();
                    }
                }
                result = true;
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return result;
    }

    private void onSwipeRight() {
        Log.d("HEJ", "SWIPE RIGHT");
    }

    private void onSwipeLeft() {
        Log.d("HEJ", "SWIPE LEFT");
    }

    public void setView(View view) {
        this.view = view;
    }

    /**
     * Sets the position of the view that the touch occurred on (called from MainActivity onTouch)
     */
    public void setPosition(int position) {
        this.position = position;
    }
}
