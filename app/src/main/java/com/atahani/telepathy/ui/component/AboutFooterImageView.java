package com.atahani.telepathy.ui.component;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.ImageView;

/**
 * Created by atahani on 1/3/16.
 */
public class AboutFooterImageView extends ImageView {

    private GestureDetector gestureDetector;
    private touchOnImageViewEventListener mTouchOnImageViewEventListener;

    public AboutFooterImageView(Context context) {
        super(context);
    }

    public AboutFooterImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        gestureDetector = new GestureDetector(context, new GestureListener());
    }

    public AboutFooterImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setTouchOnImageViewEventListener(touchOnImageViewEventListener touchOnImageViewEventListener) {
        this.mTouchOnImageViewEventListener = touchOnImageViewEventListener;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return gestureDetector.onTouchEvent(event);
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            if (mTouchOnImageViewEventListener != null) {
                mTouchOnImageViewEventListener.onDoubleTab();
            }
            return true;
        }
    }

    public interface touchOnImageViewEventListener {
        void onDoubleTab();
    }
}
