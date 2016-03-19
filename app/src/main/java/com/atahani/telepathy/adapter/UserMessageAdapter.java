package com.atahani.telepathy.adapter;

import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import io.realm.RealmResults;
import mobi.atahani.telepathy.R;
import com.atahani.telepathy.TApplication;
import com.atahani.telepathy.realm.MessageModelRealm;
import com.atahani.telepathy.ui.component.DetectLTextView;
import com.atahani.telepathy.ui.utility.TimeUtils;
import com.atahani.telepathy.utility.AppPreferenceTools;
import com.atahani.telepathy.utility.ThemeUtil;

/**
 * User message adapter for
 */
public class UserMessageAdapter extends RecyclerView.Adapter<UserMessageAdapter.UserMessageViewHolder> {

    private UserMessageEventListener mUserMessageListener;
    private LayoutInflater mLayoutInflater;
    private RealmResults<MessageModelRealm> mData;
    private AppPreferenceTools mAppPreferenceTools;
    private TimeUtils mTimeUtils;
    private ThemeUtil mThemeUtil;
    private int mMessageStatusSize;
    private int mHalfPadding;
    private int mMinMessageWidth;
    private String mUserTheme;

    public UserMessageAdapter(LayoutInflater layoutInflater, String userTheme, AppPreferenceTools appPreferenceTools, RealmResults<MessageModelRealm> data, UserMessageEventListener userMessageEventListener) {
        this.mLayoutInflater = layoutInflater;
        this.mUserTheme = userTheme;
        this.mAppPreferenceTools = appPreferenceTools;
        this.mData = data;
        this.mUserMessageListener = userMessageEventListener;
        mTimeUtils = new TimeUtils(TApplication.applicationContext);
        mThemeUtil = new ThemeUtil(TApplication.applicationContext);
        mMessageStatusSize = TApplication.applicationContext.getResources().getDimensionPixelSize(R.dimen.message_icon_status);
        mHalfPadding = TApplication.applicationContext.getResources().getDimensionPixelOffset(R.dimen.half_vertical_margin);
        mMinMessageWidth = TApplication.applicationContext.getResources().getDimensionPixelOffset(R.dimen.user_message_min_width);
    }

    /**
     * update the user them
     *
     * @param userTheme
     */
    public void updateTheUserTheme(String userTheme) {
        this.mUserTheme = userTheme;
        notifyDataSetChanged();
    }

    /**
     * update realmResult of adapter
     *
     * @param mData RealmResult<MessageModelRealm> mData
     */
    public void updateRealmResult(RealmResults<MessageModelRealm> mData) {
        this.mData = mData;
        notifyDataSetChanged();
    }

    /**
     * bind view holder
     *
     * @param parent
     * @param viewType
     * @return
     */
    @Override
    public UserMessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mLayoutInflater.inflate(R.layout.user_message_row, parent, false);
        return new UserMessageViewHolder(view);
    }

    /**
     * bind current message to viewHolder
     *
     * @param holder
     * @param position
     */
    @Override
    public void onBindViewHolder(UserMessageViewHolder holder, int position) {
        MessageModelRealm currentItem = mData.get(position);
        holder.mTxMatchedStatus.setText(String.format(TApplication.applicationContext.getString(R.string.label_telepathies_matched),
                DateUtils.getRelativeTimeSpanString(currentItem.getMatchedAt().getTime(), System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS, DateUtils.FORMAT_ABBREV_RELATIVE)));
        holder.mTxCrossIn.setText(String.format(TApplication.applicationContext.getString(R.string.label_telepathies_cross), mTimeUtils.getMatchedInTime(currentItem.getMatchedInSec())));
        holder.mTxCrossIn.setTextColor(mThemeUtil.getAccentColorByThemeName(mUserTheme));
        holder.mVSeparator.setBackgroundColor(mThemeUtil.getPrimaryLightColorByThemeName(mUserTheme));
        holder.mBtnDeleteMessage.setColorFilter(mThemeUtil.getAccentColorByThemeName(mUserTheme), PorterDuff.Mode.MULTIPLY);
        //check is user send first telepathy or not
        if (currentItem.isYouAreFirst()) {
            //bind first message
            holder.mTxFirstMessageBody.setText(currentItem.getBodySend());
            holder.mTxFirstMessageBody.setBackground(ContextCompat.getDrawable(TApplication.applicationContext, R.drawable.bubble_right));
            holder.mTxFirstMessageBody.getBackground().setColorFilter(mAppPreferenceTools.getPrimaryLightColor(), PorterDuff.Mode.MULTIPLY);
            //pull right the first message body
            RelativeLayout.LayoutParams firstMessageLayoutParam = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            firstMessageLayoutParam.addRule(RelativeLayout.ALIGN_PARENT_END, RelativeLayout.TRUE);
            firstMessageLayoutParam.addRule(RelativeLayout.BELOW, R.id.ly_message_info);
            firstMessageLayoutParam.setMargins(0, mHalfPadding, 0, mHalfPadding);
            holder.mTxFirstMessageBody.setLayoutParams(firstMessageLayoutParam);
            holder.mTxFirstMessageBody.setPadding(
                    mHalfPadding,
                    mHalfPadding,
                    mHalfPadding,
                    mHalfPadding
            );
            //bind second message
            holder.mTxSecondMessageBody.setText(currentItem.getBodyReceive());
            holder.mTxSecondMessageBody.setBackground(ContextCompat.getDrawable(TApplication.applicationContext, R.drawable.bubble_left));
            holder.mTxSecondMessageBody.getBackground().setColorFilter(mThemeUtil.getPrimaryLightColorByThemeName(mUserTheme), PorterDuff.Mode.MULTIPLY);
            //pull left the second message body
            RelativeLayout.LayoutParams secondLayoutParam = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            secondLayoutParam.addRule(RelativeLayout.ALIGN_PARENT_START, RelativeLayout.TRUE);
            secondLayoutParam.addRule(RelativeLayout.BELOW, R.id.tx_first_message_body);
            secondLayoutParam.setMargins(0, mHalfPadding, 0, mHalfPadding);
            holder.mTxSecondMessageBody.setLayoutParams(secondLayoutParam);
            holder.mTxSecondMessageBody.setPadding(
                    mHalfPadding,
                    mHalfPadding,
                    mHalfPadding,
                    mHalfPadding
            );
            //config btn_delete and pull it right
            holder.mTxMessageStatus.setVisibility(View.GONE);
        } else {
            //set the message status when the user not first in message
            if (currentItem.isRead() || currentItem.isReceive()) {
                RelativeLayout.LayoutParams messageStatusLayoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                messageStatusLayoutParams.addRule(RelativeLayout.ALIGN_END, R.id.tx_first_message_body);
                messageStatusLayoutParams.addRule(RelativeLayout.ALIGN_BOTTOM, R.id.tx_first_message_body);
                holder.mTxMessageStatus.setLayoutParams(messageStatusLayoutParams);
            }
            if (currentItem.isRead()) {
                Drawable readDrawable = ContextCompat.getDrawable(TApplication.applicationContext, R.mipmap.ic_read_status_24dp);
                readDrawable.mutate();
                readDrawable.setColorFilter(mThemeUtil.getPrimaryDarkColorByThemeName(mUserTheme), PorterDuff.Mode.MULTIPLY);
                readDrawable.setBounds(0, 0, mMessageStatusSize, mMessageStatusSize);
                holder.mTxMessageStatus.setText(DateUtils.getRelativeTimeSpanString(currentItem.getUpdatedAt().getTime(), System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS, DateUtils.FORMAT_ABBREV_RELATIVE));
                holder.mTxMessageStatus.setCompoundDrawables(null, null, readDrawable, null);
                holder.mTxMessageStatus.setCompoundDrawablePadding(mHalfPadding / 2);
                holder.mTxMessageStatus.setTextColor(mThemeUtil.getPrimaryDarkColorByThemeName(mUserTheme));
                holder.mTxMessageStatus.setVisibility(View.VISIBLE);
            } else if (currentItem.isReceive()) {
                Drawable receiveDrawable = ContextCompat.getDrawable(TApplication.applicationContext, R.mipmap.ic_done_white_24dp);
                receiveDrawable.mutate();
                receiveDrawable.setColorFilter(mThemeUtil.getPrimaryDarkColorByThemeName(mUserTheme), PorterDuff.Mode.MULTIPLY);
                receiveDrawable.setBounds(0, 0, mMessageStatusSize, mMessageStatusSize);
                holder.mTxMessageStatus.setText(DateUtils.getRelativeTimeSpanString(currentItem.getUpdatedAt().getTime(), System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS, DateUtils.FORMAT_ABBREV_RELATIVE));
                holder.mTxMessageStatus.setCompoundDrawables(null, null, receiveDrawable, null);
                holder.mTxMessageStatus.setTextColor(mThemeUtil.getPrimaryDarkColorByThemeName(mUserTheme));
                holder.mTxMessageStatus.setCompoundDrawablePadding(mHalfPadding / 2);
                holder.mTxMessageStatus.setVisibility(View.VISIBLE);
            } else {
                holder.mTxMessageStatus.setVisibility(View.GONE);
            }
            //bind first message
            holder.mTxFirstMessageBody.setText(currentItem.getBodyReceive());
            holder.mTxFirstMessageBody.setBackground(ContextCompat.getDrawable(TApplication.applicationContext, R.drawable.bubble_left));
            holder.mTxFirstMessageBody.getBackground().setColorFilter(mThemeUtil.getPrimaryLightColorByThemeName(mUserTheme), PorterDuff.Mode.MULTIPLY);
            //pull left the first message body
            RelativeLayout.LayoutParams firstMessageLayoutParam = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            firstMessageLayoutParam.addRule(RelativeLayout.ALIGN_PARENT_START, RelativeLayout.TRUE);
            firstMessageLayoutParam.addRule(RelativeLayout.BELOW, R.id.ly_message_info);
            firstMessageLayoutParam.setMargins(0, mHalfPadding, 0, mHalfPadding);
            holder.mTxFirstMessageBody.setMinimumWidth(mMinMessageWidth);
            holder.mTxFirstMessageBody.setLayoutParams(firstMessageLayoutParam);

            if (currentItem.isRead() || currentItem.isReceive()) {
                holder.mTxFirstMessageBody.setPadding(
                        mHalfPadding,
                        mHalfPadding,
                        mHalfPadding,
                        3 * mHalfPadding //bottom of firstMessage
                );
            } else {
                holder.mTxFirstMessageBody.setPadding(
                        mHalfPadding,
                        mHalfPadding,
                        mHalfPadding,
                        mHalfPadding
                );
            }
            //bind second message
            holder.mTxSecondMessageBody.setText(currentItem.getBodySend());
            holder.mTxSecondMessageBody.setBackground(ContextCompat.getDrawable(TApplication.applicationContext, R.drawable.bubble_right));
            holder.mTxSecondMessageBody.getBackground().setColorFilter(mAppPreferenceTools.getPrimaryLightColor(), PorterDuff.Mode.MULTIPLY);
            //pull right the second message body
            RelativeLayout.LayoutParams secondMessageLayoutParam = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            secondMessageLayoutParam.addRule(RelativeLayout.BELOW, R.id.tx_first_message_body);
            secondMessageLayoutParam.addRule(RelativeLayout.ALIGN_PARENT_END, RelativeLayout.TRUE);
            secondMessageLayoutParam.setMargins(0, mHalfPadding, 0, mHalfPadding);
            holder.mTxSecondMessageBody.setLayoutParams(secondMessageLayoutParam);
            holder.mTxSecondMessageBody.setPadding(
                    mHalfPadding,
                    mHalfPadding,
                    mHalfPadding,
                    mHalfPadding
            );
        }
    }

    @Override
    public int getItemCount() {
        if (mData != null) {
            return mData.size();
        } else {
            return 0;
        }
    }


    /**
     * one view holder for user_message_row.xml
     */
    class UserMessageViewHolder extends RecyclerView.ViewHolder {

        private DetectLTextView mTxFirstMessageBody;
        private DetectLTextView mTxSecondMessageBody;
        private AppCompatTextView mTxMatchedStatus;
        private AppCompatImageButton mBtnDeleteMessage;
        private AppCompatTextView mTxMessageStatus;
        private AppCompatTextView mTxCrossIn;
        private View mVSeparator;

        public UserMessageViewHolder(View itemView) {
            super(itemView);
            mTxFirstMessageBody = (DetectLTextView) itemView.findViewById(R.id.tx_first_message_body);
            mTxSecondMessageBody = (DetectLTextView) itemView.findViewById(R.id.tx_second_message_body);
            mTxMatchedStatus = (AppCompatTextView) itemView.findViewById(R.id.tx_matched_status);
            mBtnDeleteMessage = (AppCompatImageButton) itemView.findViewById(R.id.btn_delete);
            mTxMessageStatus = (AppCompatTextView) itemView.findViewById(R.id.tx_message_status);
            mTxCrossIn = (AppCompatTextView) itemView.findViewById(R.id.tx_cross_in);
            mVSeparator = itemView.findViewById(R.id.v_separator);
            //handle delete action
            mBtnDeleteMessage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mUserMessageListener != null) {
                        mUserMessageListener.onDeleteMessage(mData.get(getAdapterPosition()).getMessageId(), getAdapterPosition());
                    }
                }
            });
        }
    }

    /**
     * interface to handle message recycler item event
     */
    public interface UserMessageEventListener {
        void onDeleteMessage(String messageId, int itemPosition);
    }
}
