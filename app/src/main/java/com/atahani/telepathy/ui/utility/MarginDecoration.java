package com.atahani.telepathy.ui.utility;

import android.content.Context;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * custom Decoration for set margin in recycler view items
 */
public class MarginDecoration extends RecyclerView.ItemDecoration {

    private int leftMargin = 0;
    private int topMargin = 0;
    private int rightMargin = 0;
    private int bottomMargin = 0;

    /**
     * @param context                      the context of activity
     * @param leftMarginDimensResourceId   left margin dimens resourceId
     * @param topMarginDimensResourceId    top margin dimens resourceId
     * @param rightMarginDimensResourceId  right margin dimens resourceId
     * @param bottomMarginDimensResourceId bottom margin dimens resourceId
     */
    public MarginDecoration(Context context, int leftMarginDimensResourceId, int topMarginDimensResourceId, int rightMarginDimensResourceId, int bottomMarginDimensResourceId) {
        this.leftMargin = context.getResources().getDimensionPixelOffset(leftMarginDimensResourceId);
        this.topMargin = context.getResources().getDimensionPixelOffset(topMarginDimensResourceId);
        this.rightMargin = context.getResources().getDimensionPixelOffset(rightMarginDimensResourceId);
        this.bottomMargin = context.getResources().getDimensionPixelOffset(bottomMarginDimensResourceId);
    }

    /**
     * @param context                      the context of activity
     * @param leftMarginDimensResourceId   left margin dimens resourceId
     * @param rightMarginDimensResourceId  right margin dimens resourceId
     * @param bottomMarginDimensResourceId bottom margin dimens resourceId
     */
    public MarginDecoration(Context context, int leftMarginDimensResourceId, int rightMarginDimensResourceId, int bottomMarginDimensResourceId) {
        this.leftMargin = context.getResources().getDimensionPixelOffset(leftMarginDimensResourceId);
        this.rightMargin = context.getResources().getDimensionPixelOffset(rightMarginDimensResourceId);
        this.bottomMargin = context.getResources().getDimensionPixelOffset(bottomMarginDimensResourceId);
    }

    public MarginDecoration(Context context, int bottomMarginDimensResourceId) {
        this.bottomMargin = context.getResources().getDimensionPixelOffset(bottomMarginDimensResourceId);
    }

    /**
     * set this margin to side of the recycler view items
     *
     * @param outRect
     * @param view
     * @param parent
     * @param state
     */
    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        outRect.set(leftMargin, topMargin, rightMargin, bottomMargin);
    }

}
