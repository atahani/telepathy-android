package com.atahani.telepathy.adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import mobi.atahani.telepathy.R;
import com.atahani.telepathy.ui.component.DetectLTextView;
import com.atahani.telepathy.ui.utility.CropCircleTransformation;

/**
 * Dashboard search view cursor adapter
 */
public class DashboardSearchViewCursorAdapter extends CursorAdapter {

    private LayoutInflater mLayoutInflater;
    private SearchSuggestEventListener mSearchSuggestEventListener;


    public static String[] COLUMNS = new String[]{
            "_id",
            "withUserId",
            "withUserDisplayName",
            "withUserImageUrl"
    };
    public static final int ID_COLUMN_INDEX = 0;
    public static final int USER_ID_COLUMN_INDEX = 1;
    public static final int DISPLAY_NAME_COLUMN_INDEX = 2;
    public static final int IMAGE_ULR_COLUMN_INDEX = 3;

    public DashboardSearchViewCursorAdapter(Context context, Cursor c, boolean autoRequery) {
        super(context, c, autoRequery);
        this.mLayoutInflater = LayoutInflater.from(context);
    }

    /**
     * set the event lister to handle event on search suggest item
     *
     * @param mSearchSuggestEventListener SearchSuggestEventListener
     */
    public void setSearchSuggestEventListener(SearchSuggestEventListener mSearchSuggestEventListener) {
        this.mSearchSuggestEventListener = mSearchSuggestEventListener;
    }


    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return mLayoutInflater.inflate(R.layout.user_search_suggest_row, parent, false);
    }

    @Override
    public void bindView(View view, Context context, final Cursor cursor) {
        ImageView mImageProfile = (ImageView) view.findViewById(R.id.im_image_profile);
        DetectLTextView mTxDisplayName = (DetectLTextView) view.findViewById(R.id.tx_display_name);
        Picasso.with(context).load(cursor.getString(IMAGE_ULR_COLUMN_INDEX))
                .placeholder(R.drawable.image_place_holder)
                .transform(new CropCircleTransformation())
                .into(mImageProfile);
        mTxDisplayName.setText(cursor.getString(DISPLAY_NAME_COLUMN_INDEX));
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSearchSuggestEventListener != null) {
                    mSearchSuggestEventListener.clickOnUserSuggest(cursor.getString(USER_ID_COLUMN_INDEX), cursor.getString(DISPLAY_NAME_COLUMN_INDEX));
                }
            }
        });
    }

    public interface SearchSuggestEventListener {
        void clickOnUserSuggest(String userId, String userDisplayName);
    }
}
