package com.atahani.telepathy.adapter;

import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.format.DateUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.atahani.telepathy.realm.MessageModelRealm;
import com.atahani.telepathy.ui.utility.CropCircleTransformation;

import io.realm.Realm;
import mobi.atahani.telepathy.R;
import com.atahani.telepathy.TApplication;
import com.atahani.telepathy.model.ClassifyMessageModel;
import com.atahani.telepathy.model.UserModel;
import com.atahani.telepathy.ui.component.DetectLTextView;
import com.atahani.telepathy.ui.utility.Constants;
import com.atahani.telepathy.utility.AndroidUtilities;
import com.atahani.telepathy.utility.ThemeUtil;

import java.util.Collections;
import java.util.List;

/**
 * Adapter to show last message of person
 */
public class ClassifyMessageAdapter extends RecyclerView.Adapter<ClassifyMessageAdapter.ClassifyMessageViewHolder> {

    private LayoutInflater mLayoutInflater;
    private List<ClassifyMessageModel> mData = Collections.emptyList();
    private ClassifyEventListener mClassifyEventListener;
    private ThemeUtil mThemeUtil;
    private Realm mRealmDB;
    private String mCurrentSelectedUserId;
    private int mMessageStatusSize;
    private int mHalfPadding;


    public ClassifyMessageAdapter(LayoutInflater mLayoutInflater, List<ClassifyMessageModel> mData, ClassifyEventListener mClassifyEventListener) {
        this.mLayoutInflater = mLayoutInflater;
        this.mData = mData;
        this.mClassifyEventListener = mClassifyEventListener;
        this.mThemeUtil = new ThemeUtil(TApplication.applicationContext);
        mMessageStatusSize = TApplication.applicationContext.getResources().getDimensionPixelSize(R.dimen.message_icon_status);
        mHalfPadding = TApplication.applicationContext.getResources().getDimensionPixelOffset(R.dimen.half_vertical_margin);
    }

    public void setRealmDB(Realm realmDB) {
        this.mRealmDB = realmDB;
    }

    public void updateDate(List<ClassifyMessageModel> data) {
        this.mData = data;
        notifyDataSetChanged();
    }

    @Override
    public ClassifyMessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mLayoutInflater.inflate(R.layout.classify_message_row, parent, false);
        return new ClassifyMessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ClassifyMessageViewHolder holder, int position) {
        ClassifyMessageModel currentItem = mData.get(position);
        int userAccentColor = mThemeUtil.getAccentColorByThemeName(currentItem.with_user.theme);
        int userPrimaryColor = mThemeUtil.getPrimaryColorByThemeName(currentItem.with_user.theme);
        int userPrimaryDarkColor = mThemeUtil.getPrimaryDarkColorByThemeName(currentItem.with_user.theme);
        holder.mView.setBackgroundColor(userPrimaryColor);
        if (currentItem.with_user.username != null && currentItem.with_user.username.equals(Constants.DELETED_ACCOUNT_VALUE)) {
            Picasso.with(TApplication.applicationContext)
                    .load(R.drawable.user_deleted)
                    .transform(new CropCircleTransformation())
                    .into(holder.mImUserPhoto);
            holder.mTxDisplayName.setText(TApplication.applicationContext.getString(R.string.label_account_deleted));
        } else {
            Picasso.with(TApplication.applicationContext).load(currentItem.with_user.image_url)
                    .placeholder(R.drawable.image_place_holder)
                    .transform(new CropCircleTransformation())
                    .into(holder.mImUserPhoto);
            holder.mTxDisplayName.setText(currentItem.with_user.display_name);
        }
        holder.mTxDisplayName.setTextColor(userPrimaryDarkColor);
        if (TApplication.applicationContext.getString(R.string.direction_type).equals("RTL")) {
            if (AndroidUtilities.getTypeOfTextDirection(holder.mTxDisplayName.getText().toString()) == View.TEXT_DIRECTION_LTR) {
                holder.mTxDisplayName.setGravity(Gravity.END);
            } else {
                holder.mTxDisplayName.setGravity(Gravity.START);
            }
        } else {
            if (AndroidUtilities.getTypeOfTextDirection(holder.mTxDisplayName.getText().toString()) == View.TEXT_DIRECTION_LTR) {
                holder.mTxDisplayName.setGravity(Gravity.START);
            } else {
                holder.mTxDisplayName.setGravity(Gravity.END);
            }
        }
        //bind the second message
        if (currentItem.last_message.you_are_first) {
            holder.mTxSecondMessageBody.setText(currentItem.last_message.body_receive);
        } else {
            Spannable spanYouSay = new SpannableString(String.format(TApplication.applicationContext.getString(R.string.label_you_say), currentItem.last_message.body_send));
            spanYouSay.setSpan(new ForegroundColorSpan(userAccentColor), 0, 4, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            spanYouSay.setSpan(new StyleSpan(Typeface.BOLD), 0, 4, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            holder.mTxSecondMessageBody.setText(spanYouSay);
        }
        if (!currentItem.last_message.you_are_first) {
            if (currentItem.last_message.is_read) {
                Drawable readDrawable = ContextCompat.getDrawable(TApplication.applicationContext, R.mipmap.ic_read_status_24dp);
                readDrawable.mutate();
                readDrawable.setColorFilter(userAccentColor, PorterDuff.Mode.MULTIPLY);
                readDrawable.setBounds(0, 0, mMessageStatusSize, mMessageStatusSize);
                holder.mTxMessageStatus.setText(DateUtils.getRelativeTimeSpanString(currentItem.last_message.updated_at.getTime(), System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS, DateUtils.FORMAT_ABBREV_RELATIVE));
                holder.mTxMessageStatus.setCompoundDrawables(readDrawable, null, null, null);
                holder.mTxMessageStatus.setCompoundDrawablePadding(mHalfPadding / 5);
                holder.mTxMessageStatus.setTextColor(userAccentColor);
            } else if (currentItem.last_message.is_receive) {
                Drawable receiveDrawable = ContextCompat.getDrawable(TApplication.applicationContext, R.mipmap.ic_done_white_24dp);
                receiveDrawable.mutate();
                receiveDrawable.setColorFilter(userAccentColor, PorterDuff.Mode.MULTIPLY);
                receiveDrawable.setBounds(0, 0, mMessageStatusSize, mMessageStatusSize);
                holder.mTxMessageStatus.setText(DateUtils.getRelativeTimeSpanString(currentItem.last_message.updated_at.getTime(), System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS, DateUtils.FORMAT_ABBREV_RELATIVE));
                holder.mTxMessageStatus.setCompoundDrawables(receiveDrawable, null, null, null);
                holder.mTxMessageStatus.setCompoundDrawablePadding(mHalfPadding / 5);
                holder.mTxMessageStatus.setTextColor(userAccentColor);
            } else {
                holder.mTxMessageStatus.setText(DateUtils.getRelativeTimeSpanString(currentItem.last_message.updated_at.getTime(), System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS, DateUtils.FORMAT_ABBREV_RELATIVE));
                holder.mTxMessageStatus.setCompoundDrawables(null, null, null, null);
                holder.mTxMessageStatus.setTextColor(userAccentColor);
            }
        } else {
            holder.mTxMessageStatus.setText(DateUtils.getRelativeTimeSpanString(currentItem.last_message.updated_at.getTime(), System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS, DateUtils.FORMAT_ABBREV_RELATIVE));
            holder.mTxMessageStatus.setCompoundDrawables(null, null, null, null);
            holder.mTxMessageStatus.setTextColor(userAccentColor);
        }
        configUnreadMessageCounterIndicator(holder.mTxUnreadMessageCounter, currentItem.with_user.user_id, userAccentColor);
        if (mCurrentSelectedUserId != null && currentItem.with_user.user_id.equals(mCurrentSelectedUserId)) {
            holder.mLyMainContent.setBackgroundColor(ContextCompat.getColor(TApplication.applicationContext, R.color.theme_selected_classify_message_background));
        } else {
            holder.mLyMainContent.setBackground(null);
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

    public void changeSelectedItemInTablet(String currentSelectedUserId) {
        this.mCurrentSelectedUserId = currentSelectedUserId;
        notifyDataSetChanged();
    }

    public void deSelectItemInTablet() {
        this.mCurrentSelectedUserId = null;
        notifyDataSetChanged();
    }

    /**
     * config un read message counter indicator
     *
     * @param txUnreadMessageCounterIndicator AppCompatTextView txUnreadMessageCounterIndicator
     * @param userId                          String userId
     * @param userThemeAccentColor            the accent color of user theme
     */
    private void configUnreadMessageCounterIndicator(TextView txUnreadMessageCounterIndicator, String userId, int userThemeAccentColor) {
        //get the number of unread message from this user
        if (!this.mRealmDB.isClosed()) {
            long numberOfUnreadMessage = mRealmDB.where(MessageModelRealm.class).equalTo("withUserId", userId).equalTo("youAreFirst", true).equalTo("isSendReadSignal", false).count();
            if (numberOfUnreadMessage > 0) {
                ((GradientDrawable) txUnreadMessageCounterIndicator.getBackground()).setColor(userThemeAccentColor);
                txUnreadMessageCounterIndicator.setText(String.format("%d", numberOfUnreadMessage));
                txUnreadMessageCounterIndicator.setVisibility(View.VISIBLE);
            } else {
                txUnreadMessageCounterIndicator.setVisibility(View.GONE);
            }
        } else {
            txUnreadMessageCounterIndicator.setVisibility(View.GONE);
        }
    }

    class ClassifyMessageViewHolder extends RecyclerView.ViewHolder {

        private RelativeLayout mLyMainContent;
        private ImageView mImUserPhoto;
        private DetectLTextView mTxUnreadMessageCounter;
        private DetectLTextView mTxDisplayName;
        private DetectLTextView mTxSecondMessageBody;
        private AppCompatTextView mTxMessageStatus;
        private View mView;

        public ClassifyMessageViewHolder(View itemView) {
            super(itemView);
            mLyMainContent = (RelativeLayout) itemView.findViewById(R.id.ly_classify_message_content);
            mImUserPhoto = (ImageView) itemView.findViewById(R.id.im_user_photo);
            mTxUnreadMessageCounter = (DetectLTextView) itemView.findViewById(R.id.tx_unread_message_counter);
            mTxDisplayName = (DetectLTextView) itemView.findViewById(R.id.tx_display_name);
            mTxSecondMessageBody = (DetectLTextView) itemView.findViewById(R.id.tx_second_message_body);
            mTxMessageStatus = (AppCompatTextView) itemView.findViewById(R.id.tx_message_status);
            mLyMainContent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mClassifyEventListener != null) {
                        mClassifyEventListener.onClickClassifyMessage(mData.get(getAdapterPosition()).with_user, mData.get(getAdapterPosition()).message_count);
                    }
                }
            });
            mView = itemView.findViewById(R.id.v_separator);
        }
    }

    public interface ClassifyEventListener {
        void onClickClassifyMessage(UserModel userModel, long numberOfUserMessage);
    }
}
