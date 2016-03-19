package com.atahani.telepathy.adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Build;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.atahani.telepathy.model.AppSettingItemModel;
import com.atahani.telepathy.utility.AppPreferenceTools;

import mobi.atahani.telepathy.R;

import com.atahani.telepathy.ui.component.DetectLTextView;

import java.util.Collections;
import java.util.List;

/**
 * Application Settings item adapter with custom views
 */
public class AppSettingsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<AppSettingItemModel> mData = Collections.emptyList();
    private LayoutInflater mLayoutInflater;
    private AppPreferenceTools mAppPreference;
    private SettingsItemClickListener mSettingsItemClickListener;

    /**
     * initial method
     *
     * @param context Context base context
     * @param data    List of AppSettingItemModel
     */
    public AppSettingsAdapter(Context context, List<AppSettingItemModel> data) {
        this.mLayoutInflater = LayoutInflater.from(context);
        this.mData = data;
        this.mAppPreference = new AppPreferenceTools(context);
    }

    /**
     * set settingItemListener
     *
     * @param settingsItemClickListener SettingsItemClickListener
     */
    public void setSettingsItemClickListener(SettingsItemClickListener settingsItemClickListener) {
        this.mSettingsItemClickListener = settingsItemClickListener;
    }

    /**
     * update list when profile updated
     *
     * @param data
     */
    public void updateWhenProfileUpdate(List<AppSettingItemModel> data) {
        this.mData = data;
        notifyDataSetChanged();
    }

    /**
     * create View holder assign different view holder for application settings
     *
     * @param parent   ViewGroup parent
     * @param viewType int type of view
     * @return Recycler.ViewHolder
     */
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //return view AppSetting item type
        if (viewType == AppSettingItemModel.ITEM_DIVIDER_TYPE) {
            View view = mLayoutInflater.inflate(R.layout.app_setting_divider, parent, false);
            return new AppSettingItemDividerViewHolder(view);
        } else if (viewType == AppSettingItemModel.HEADER_ITEM_TYPE) {
            View view = mLayoutInflater.inflate(R.layout.app_setting_header_row, parent, false);
            return new AppSettingHeaderViewHolder(view);
        } else if (viewType == AppSettingItemModel.ITEM_WITH_CHECKBOX) {
            View view = mLayoutInflater.inflate(R.layout.app_setting_checkbox_row, parent, false);
            return new AppSettingCheckBoxViewHolder(view);
        } else {
            View view = mLayoutInflater.inflate(R.layout.app_setting_main_row, parent, false);
            return new AppSettingViewHolder(view);
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
        //bind only if holder main or header
        int item_type = holder.getItemViewType();
        AppSettingItemModel current_model = mData.get(position);
        if (item_type == AppSettingItemModel.HEADER_ITEM_TYPE) {
            ((AppSettingHeaderViewHolder) holder).mTxAppSettingsHeader.setText(current_model.getTitle());
            ((AppSettingHeaderViewHolder) holder).mTxAppSettingsHeader.setTextColor(mAppPreference.getAccentColor());
        } else if (item_type == AppSettingItemModel.ITEM_WITH_CHECKBOX) {
            ((AppSettingCheckBoxViewHolder) holder).mTxItemTitle.setText(current_model.getTitle());
            ((AppSettingCheckBoxViewHolder) holder).mChItem.setVisibility(View.VISIBLE);
            ((AppSettingCheckBoxViewHolder) holder).mChItem.setChecked(current_model.isItemChecked());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                ((AppSettingCheckBoxViewHolder) holder).mChItem.setButtonTintList(ColorStateList.valueOf(mAppPreference.getAccentColor()));
            }
        } else if (item_type == AppSettingItemModel.ITEM_TO_OPEN_DIALOG || item_type == AppSettingItemModel.ITEM_TO_LOG_OUT || item_type == AppSettingItemModel.ITEM_OPEN_ACTIVITY) {
            ((AppSettingViewHolder) holder).mTxItemTitle.setText(current_model.getTitle());
            if (current_model.getStatus() != null) {
                ((AppSettingViewHolder) holder).mTxItemStatus.setVisibility(View.VISIBLE);
                ((AppSettingViewHolder) holder).mTxItemStatus.setText(current_model.getStatus());
            } else {
                ((AppSettingViewHolder) holder).mTxItemStatus.setVisibility(View.GONE);
            }
        }
    }

    /**
     * change status of open activity setting item
     *
     * @param targetActivityClass class targetActivityClass
     * @param status              String status
     */
    public void changeOpenActivitySettingStatus(Class targetActivityClass, String status) {
        for (int i = 0; i < mData.size(); i++) {
            if (mData.get(i).getTargetActivityClass() == targetActivityClass) {
                mData.get(i).setStatus(status);
                notifyItemChanged(i);
            }
        }
    }

    /**
     * change status of dialog setting item
     *
     * @param dialogSettingType int dialog setting type
     * @param status            String status
     */
    public void changeOpenDialogSettingStatus(int dialogSettingType, String status) {
        for (int i = 0; i < mData.size(); i++) {
            if (mData.get(i).getDialogSettingType() == dialogSettingType && mData.get(i).getType() == AppSettingItemModel.ITEM_TO_OPEN_DIALOG) {
                mData.get(i).setStatus(status);
                notifyItemChanged(i);
                break;
            }
        }
    }

    /**
     * get size of List of item in recycler view
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
     * @param position the current position
     * @return
     */
    @Override
    public int getItemViewType(int position) {
        return mData.get(position).getType();
    }

    /**
     * AppSettings Item view holder
     */
    class AppSettingViewHolder extends RecyclerView.ViewHolder {
        private LinearLayout mLyItemContent;
        private AppCompatTextView mTxItemTitle;
        private DetectLTextView mTxItemStatus;

        public AppSettingViewHolder(View itemView) {
            super(itemView);
            mLyItemContent = (LinearLayout) itemView.findViewById(R.id.ly_item_content);
            mTxItemTitle = (AppCompatTextView) itemView.findViewById(R.id.tx_item_title);
            mTxItemStatus = (DetectLTextView) itemView.findViewById(R.id.tx_item_status);
            //set on click listener for application setting item
            mLyItemContent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mSettingsItemClickListener != null) {
                        AppSettingItemModel selectedModel = mData.get(getAdapterPosition());
                        if (selectedModel.getType() == AppSettingItemModel.ITEM_TO_LOG_OUT) {
                            mSettingsItemClickListener.onLogOut();
                        } else if (selectedModel.getType() == AppSettingItemModel.ITEM_TO_OPEN_DIALOG) {
                            mSettingsItemClickListener.onOpenDialog(selectedModel);
                        } else if (selectedModel.getType() == AppSettingItemModel.ITEM_OPEN_ACTIVITY) {
                            mSettingsItemClickListener.openActivity(selectedModel.getTargetActivityClass());
                        }
                    }
                }
            });
        }
    }

    /**
     * view for item with checkbox
     */
    class AppSettingCheckBoxViewHolder extends RecyclerView.ViewHolder {

        private RelativeLayout mLyItemContent;
        private AppCompatTextView mTxItemTitle;
        private CheckBox mChItem;

        public AppSettingCheckBoxViewHolder(View itemView) {
            super(itemView);
            mLyItemContent = (RelativeLayout) itemView.findViewById(R.id.ly_item_content);
            mTxItemTitle = (AppCompatTextView) itemView.findViewById(R.id.tx_item_title);
            mChItem = (CheckBox) itemView.findViewById(R.id.ch_item);
            //set on click listener for click on main layout
            mLyItemContent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mSettingsItemClickListener != null) {
                        //check the checkbox and fire the event handler
                        mChItem.setChecked(!mChItem.isChecked());
                        mSettingsItemClickListener.onCheckItem(mData.get(getAdapterPosition()).getCheckBoxType(), mChItem.isChecked());
                    }
                }
            });
            //set on checkbox check
            mChItem.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (mSettingsItemClickListener != null) {
                        AppSettingItemModel selectedModel = mData.get(getAdapterPosition());
                        mSettingsItemClickListener.onCheckItem(selectedModel.getCheckBoxType(), isChecked);
                    }
                }
            });
        }
    }

    /**
     * App setting item header
     */
    class AppSettingHeaderViewHolder extends RecyclerView.ViewHolder {
        private AppCompatTextView mTxAppSettingsHeader;

        public AppSettingHeaderViewHolder(View itemView) {
            super(itemView);
            mTxAppSettingsHeader = (AppCompatTextView) itemView.findViewById(R.id.tx_app_settings_header);
        }
    }

    /**
     * App setting item divider view holder
     */
    class AppSettingItemDividerViewHolder extends RecyclerView.ViewHolder {
        public AppSettingItemDividerViewHolder(View itemView) {
            super(itemView);
        }
    }

    /**
     * interface to handle on click action on application setting item
     */
    public interface SettingsItemClickListener {

        void onLogOut();

        void openActivity(Class targetActivityClass);

        void onOpenDialog(AppSettingItemModel selectedItem);

        void onCheckItem(int checkBoxType, boolean isChecked);
    }
}
