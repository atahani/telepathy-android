package com.atahani.telepathy.adapter;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import mobi.atahani.telepathy.R;
import com.atahani.telepathy.model.ChooseItemModel;
import com.atahani.telepathy.ui.component.DetectLTextView;

import java.util.Collections;
import java.util.List;

/**
 * Choose Item Adapter
 */
public class ChooseItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private LayoutInflater mLayoutInflater;
    private List<ChooseItemModel> mData = Collections.emptyList();
    private ChooseItemClickListener mChooseItemClickListener;

    /**
     * initial method with data
     *
     * @param context Context base context
     */
    public ChooseItemAdapter(Context context) {
        this.mContext = context;
        this.mLayoutInflater = LayoutInflater.from(this.mContext);

    }

    /**
     * set the data list
     *
     * @param data List of ChooseItemModel
     */
    public void setData(List<ChooseItemModel> data) {
        this.mData = data;
    }

    /**
     * assign different view holder for bottom sheet
     *
     * @param parent   ViewGroup parent
     * @param viewType int type of view
     * @return Recycler.ViewHolder
     */
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == ChooseItemModel.ITEM_ITEM_WITH_OUT_ICON || viewType == ChooseItemModel.ITEM_WITH_ICON) {
            View view = mLayoutInflater.inflate(R.layout.choose_item_row, parent, false);
            return new ChooseItemViewHolder(view);
        } else if (viewType == ChooseItemModel.ITEM_THEME_COLOR) {
            View view = mLayoutInflater.inflate(R.layout.theme_color_item_row, parent, false);
            return new ThemeColorItemViewHolder(view);
        } else {
            View view = mLayoutInflater.inflate(R.layout.theme_color_item_column, parent, false);
            return new ThemeColorHRItemViewHolder(view);
        }
    }


    /**
     * bind data to view holder
     *
     * @param holder   RecyclerView.ViewHolder
     * @param position int current position
     */
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        int item_type = holder.getItemViewType();
        ChooseItemModel current_model = mData.get(position);
        if (item_type == ChooseItemModel.ITEM_WITH_ICON) {
            ((ChooseItemViewHolder) holder).mTxChooseItemTitle.setText(current_model.getTitle());
            ((ChooseItemViewHolder) holder).mImChooseItemIcon.setVisibility(View.VISIBLE);
            ((ChooseItemViewHolder) holder).mImChooseItemIcon.setImageDrawable(ContextCompat.getDrawable(this.mContext, current_model.getIconMimpmapResourceId()));
            ((ChooseItemViewHolder) holder).mImChooseItemIcon.setColorFilter(ContextCompat.getColor(this.mContext, current_model.getIconColorResourceId()));
        } else if (item_type == ChooseItemModel.ITEM_ITEM_WITH_OUT_ICON) {
            ((ChooseItemViewHolder) holder).mTxChooseItemTitle.setText(current_model.getTitle());
            ((ChooseItemViewHolder) holder).mImChooseItemIcon.setVisibility(View.GONE);
        } else if (item_type == ChooseItemModel.ITEM_THEME_COLOR) {
            ((ThemeColorItemViewHolder) holder).mLyChooseItemContent.setBackgroundColor(current_model.getColor());
        } else if (item_type == ChooseItemModel.ITEM_THEME_HR_COLOR) {
            ((ThemeColorHRItemViewHolder) holder).mViewItem.setBackgroundColor(current_model.getColor());
        }
    }

    /**
     * get size of list item in recycler view
     *
     * @return
     */
    @Override
    public int getItemCount() {
        return mData.size();
    }

    /**
     * get item type by position
     *
     * @param position int position
     * @return int type of item
     */
    @Override
    public int getItemViewType(int position) {
        return mData.get(position).getType();
    }

    /**
     * set choose item click listener interface to handle events
     *
     * @param chooseItemClickListener
     */
    public void setChooseItemClickListener(ChooseItemClickListener chooseItemClickListener) {
        this.mChooseItemClickListener = chooseItemClickListener;
    }

    /**
     * Choose Item view holder
     */
    class ChooseItemViewHolder extends RecyclerView.ViewHolder {

        private LinearLayout mLyChooseItemContent;
        private ImageView mImChooseItemIcon;
        private DetectLTextView mTxChooseItemTitle;

        public ChooseItemViewHolder(View itemView) {
            super(itemView);
            mLyChooseItemContent = (LinearLayout) itemView.findViewById(R.id.ly_choose_item_content);
            mImChooseItemIcon = (ImageView) itemView.findViewById(R.id.im_choose_item_icon);
            mTxChooseItemTitle = (DetectLTextView) itemView.findViewById(R.id.tx_choose_item_title);
            mLyChooseItemContent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mChooseItemClickListener != null) {
                        mChooseItemClickListener.handleActionWhenClick(mData.get(getAdapterPosition()).getActionType(), mData.get(getAdapterPosition()).getValueCode());
                    }
                }
            });
        }
    }

    /**
     * theme color item view holder
     */
    class ThemeColorItemViewHolder extends RecyclerView.ViewHolder {
        private LinearLayout mLyChooseItemContent;

        public ThemeColorItemViewHolder(View itemView) {
            super(itemView);
            mLyChooseItemContent = (LinearLayout) itemView.findViewById(R.id.ly_choose_item_content);
            mLyChooseItemContent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mChooseItemClickListener != null) {
                        mChooseItemClickListener.handleOnChooseThemeColor(mData.get(getAdapterPosition()).getThemeName());
                    }
                }
            });
        }
    }

    /**
     * theme color item HR view holder
     */
    class ThemeColorHRItemViewHolder extends RecyclerView.ViewHolder {
        private View mViewItem;

        public ThemeColorHRItemViewHolder(View itemView) {
            super(itemView);
            mViewItem = itemView.findViewById(R.id.view_item);
            mViewItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mChooseItemClickListener != null) {
                        mChooseItemClickListener.handleOnChooseThemeColor(mData.get(getAdapterPosition()).getThemeName());
                    }
                }
            });
        }
    }

    public interface ChooseItemClickListener {
        void handleActionWhenClick(int actionType, String valueCode);

        void handleOnChooseThemeColor(String themeName);
    }
}

