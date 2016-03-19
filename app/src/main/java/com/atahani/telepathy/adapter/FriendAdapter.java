package com.atahani.telepathy.adapter;

import android.content.Context;
import android.graphics.PorterDuff;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.atahani.telepathy.realm.UserModelRealm;
import com.atahani.telepathy.realm.utility.RealmRecyclerViewAdapter;
import com.atahani.telepathy.ui.component.DetectLTextView;
import com.atahani.telepathy.ui.utility.RoundedImageTransformation;

import mobi.atahani.telepathy.R;

/**
 * FriendAdapter for display friends in recycler view
 */
public class FriendAdapter extends RealmRecyclerViewAdapter<UserModelRealm> {

    private Context mContext;
    private LayoutInflater mLayoutInflater;
    private FriendEventListener mFriendEventListener;

    /**
     * initial method
     *
     * @param mContext            Context context
     * @param friendEventListener interface of FriendEventListener
     */
    public FriendAdapter(Context mContext, FriendEventListener friendEventListener) {
        this.mContext = mContext;
        this.mLayoutInflater = LayoutInflater.from(this.mContext);
        this.mFriendEventListener = friendEventListener;
    }

    /**
     * bind view holder
     *
     * @param parent   ViewGroup parent
     * @param viewType int viewType
     * @return
     */
    @Override
    public FriendViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mLayoutInflater.inflate(R.layout.user_column, parent, false);
        return new FriendViewHolder(view);
    }

    /**
     * bind current friend to viewHolder
     *
     * @param holder   holder
     * @param position int current position
     */
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        FriendViewHolder custom_holder = (FriendViewHolder) holder;
        UserModelRealm currentItem = getItem(position);
        //load friend image profile via glide
        //since the lower LOLLIPOP version doesn't round images in CardView, first check version and then manually rounded images by transform method
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Picasso.with(mContext).load(currentItem.getImageUrl())
                    .placeholder(R.drawable.image_place_holder)
                    .fit().into(custom_holder.mImPhoto);
        } else {
            custom_holder.mUserCardView.setPreventCornerOverlap(false);
            Picasso.with(mContext).load(currentItem.getImageUrl())
                    .placeholder(R.drawable.image_place_holder)
                    .transform(new RoundedImageTransformation(4, 0, true))
                    .fit().into(custom_holder.mImPhoto);
        }
        custom_holder.mTxDisplayName.setText(currentItem.getDisplayName());
        custom_holder.mBtnFriendAction.setImageDrawable(ContextCompat.getDrawable(this.mContext, R.mipmap.ic_person_remove_white_24dp));
        custom_holder.mBtnFriendAction.setContentDescription(this.mContext.getString(R.string.action_remove_from_friends));
        custom_holder.mBtnTelepathy.getDrawable().mutate().setColorFilter(null);
        custom_holder.mBtnTelepathy.getDrawable().setColorFilter(ContextCompat.getColor(this.mContext, R.color.theme_primary_tint_icon_color), PorterDuff.Mode.MULTIPLY);
        if (currentItem.getUsername() != null) {
            custom_holder.mTxUsername.setText("@" + currentItem.getUsername());
        }


    }


    /**
     * get size of recycler view items
     *
     * @return
     */
    @Override
    public int getItemCount() {
        if (getRealmAdapter() != null) {
            return getRealmAdapter().getCount();
        }
        return 0;
    }

    /**
     * we have one view holder for friend_column.xml layout
     */
    class FriendViewHolder extends RecyclerView.ViewHolder {

        private CardView mUserCardView;
        private ImageView mImPhoto;
        private DetectLTextView mTxDisplayName;
        private AppCompatTextView mTxUsername;
        private AppCompatImageButton mBtnTelepathy;
        private AppCompatImageButton mBtnFriendAction;

        public FriendViewHolder(View itemView) {
            super(itemView);
            mUserCardView = (CardView) itemView.findViewById(R.id.user_card_view);
            mImPhoto = (ImageView) itemView.findViewById(R.id.im_photo);
            mTxDisplayName = (DetectLTextView) itemView.findViewById(R.id.tx_display_name);
            mTxUsername = (AppCompatTextView) itemView.findViewById(R.id.tx_username);
            mBtnTelepathy = (AppCompatImageButton) itemView.findViewById(R.id.btn_telepathy);
            mBtnFriendAction = (AppCompatImageButton) itemView.findViewById(R.id.btn_friend_action);
            mBtnTelepathy.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mFriendEventListener != null) {
                        UserModelRealm friendModelRealm = getItem(getAdapterPosition());
                        mFriendEventListener.onStartTelepathy(friendModelRealm.getUserId(), friendModelRealm.getUsername(), friendModelRealm.getDisplayName(), friendModelRealm.getImageUrl(), friendModelRealm.getTheme());
                    }
                }
            });
            mBtnFriendAction.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    UserModelRealm friendModelRealm = getItem(getAdapterPosition());
                    if (mFriendEventListener != null) {
                        if (friendModelRealm != null) {
                            //means have this friend and now should remove as friend
                            mFriendEventListener.onRemoveAsFriend(friendModelRealm.getUserId(), getAdapterPosition());
                        }

                    }
                }
            });
        }
    }

    /**
     * interface to handle friend recycler view events
     * NOTE : do not have addFriend since this is friendAdapter
     */
    public interface FriendEventListener {
        void onStartTelepathy(String userId, String username, String displayName, String imageUrl, String themeName);

        void onRemoveAsFriend(String userId, int itemPosition);
    }
}
