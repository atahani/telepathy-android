package com.atahani.telepathy.ui.component.spinnerwheel;

/**
 * Wheel scrolled listener interface.
 */
public interface OnWheelScrollListener {
    /**
     * Callback method to be invoked when scrolling started.
     * @param wheel the spinnerwheel view whose state has changed.
     */
    void onScrollingStarted(AbstractWheel wheel);

    /**
     * Callback method to be invoked when scrolling ended.
     * @param wheel the spinnerwheel view whose state has changed.
     */
    void onScrollingFinished(AbstractWheel wheel);
}
