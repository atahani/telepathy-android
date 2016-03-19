package com.atahani.telepathy.ui.utility;

import com.atahani.telepathy.ui.component.SwipeBackLayout;

/**
 * the interface to used in swipeback activity
 */
public interface SwipeBackInterface {
    /**
     * @return the SwipeBackLayout associated with this activity.
     */
    public abstract SwipeBackLayout getSwipeBackLayout();

    public abstract void setSwipeBackEnable(boolean enable);

    /**
     * Scroll out contentView and finish the activity
     */
    public abstract void scrollToFinishActivity();

}
