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

import mobi.atahani.telepathy.R;
import com.atahani.telepathy.model.UserModel;
import com.atahani.telepathy.ui.component.DetectLTextView;
import com.atahani.telepathy.ui.utility.RoundedImageTransformation;

import java.util.ArrayList;
import java.util.List;

/**
 * User adapter to bind data into recycler view it's like FriendView but the data different it's comes from net
 */
public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewColumnHolder> {

    private Context mContext;
    private LayoutInflater mLayoutInflater;
    private UserItemEventListener mUserItemEventListener;
    private List<UserModel> mData = new ArrayList<>();
    private ArrayList<String> mFriendIdList = new ArrayList<>();


    /**
     * initial method
     *
     * @param context               Context
     * @param userItemEventListener implement interface of event listener
     */
    public UserAdapter(Context context, UserItemEventListener userItemEventListener) {
        this.mContext = context;
        this.mLayoutInflater = LayoutInflater.from(context);
        this.mUserItemEventListener = userItemEventListener;
    }

    /**
     * set friends list
     *
     * @param friendIdList ArrayList of String
     */
    public void setFriendIdList(ArrayList<String> friendIdList) {
        this.mFriendIdList = friendIdList;
    }


    @Override
    public UserViewColumnHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mLayoutInflater.inflate(R.layout.user_column, parent, false);
        return new UserViewColumnHolder(view);
    }


    /**
     * bind data to view holder
     *
     * @param holder   UserViewHolder
     * @param position position of current item
     */
    @Override
    public void onBindViewHolder(UserViewColumnHolder holder, int position) {
        UserModel currentModel = mData.get(position);//since the lower LOLLIPOP version doesn't round images in CardView, first check version and then manually rounded images by transform method
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Picasso.with(mContext).load(currentModel.image_url)
                    .placeholder(R.drawable.image_place_holder)
                    .fit().into(holder.mImPhoto);
        } else {
            holder.mUserCardView.setPreventCornerOverlap(false);
            Picasso.with(mContext).load(currentModel.image_url)
                    .placeholder(R.drawable.image_place_holder)
                    .transform(new RoundedImageTransformation(4, 0, true))
                    .fit().into(holder.mImPhoto);

        }
        holder.mTxDisplayName.setText(currentModel.display_name);
        if (currentModel.username != null) {
            holder.mTxUsername.setText("@" + currentModel.username);
        }

        //check is user friend already
        if (mFriendIdList.contains(currentModel.user_id)) {
            holder.mBtnFriendAction.setImageDrawable(ContextCompat.getDrawable(this.mContext, R.mipmap.ic_person_remove_white_24dp));
            holder.mBtnFriendAction.setContentDescription(this.mContext.getString(R.string.action_remove_from_friends));
        } else {
            holder.mBtnFriendAction.setImageDrawable(ContextCompat.getDrawable(this.mContext, R.mipmap.ic_person_add_white_24dp));
            holder.mBtnFriendAction.setContentDescription(this.mContext.getString(R.string.action_add_as_friend));
        }
        holder.mBtnTelepathy.getDrawable().mutate().setColorFilter(null);
        holder.mBtnTelepathy.getDrawable().setColorFilter(ContextCompat.getColor(this.mContext, R.color.theme_primary_tint_icon_color), PorterDuff.Mode.MULTIPLY);
    }

    /**
     * get number of User item in mData
     *
     * @return
     */
    @Override
    public int getItemCount() {
        return mData.size();
    }

    /**
     * update user  data
     *
     * @param userModelList List of UserModel
     */
    public void updateUserData(List<UserModel> userModelList) {
        mData = userModelList;
        notifyDataSetChanged();
    }

    /**
     * add one user model into Data List
     *
     * @param userModel UserModel Object
     */
    public UserModel addUserModel(UserModel userModel) {
        //first check is add already

        for (UserModel model : mData) {
            if (model.user_id.equals(userModel.user_id)) {
                return model;
            }
        }
        mData.add(userModel);
        notifyDataSetChanged();
        return userModel;
    }

    /**
     * remove user item by userId from Data List
     *
     * @param userId String userId
     */
    public void removeUserItemByUserId(String userId) {
        for (int i = 0; i < mData.size(); i++) {
            if (mData.get(i).user_id.equals(userId)) {
                mData.remove(i);
            }
        }
        notifyDataSetChanged();
    }

    /**
     * when add new friend call this function to change data in this
     *
     * @param friendId     String friendId that should be added
     * @param itemPosition the itemPosition of user
     */
    public void notifyToAddedFriend(String friendId, int itemPosition) {
        //add friendId to mFriendList
        mFriendIdList.add(friendId);
        //notify to change item at that position
        notifyItemChanged(itemPosition);
    }

    /**
     * when user removed as friend call this function to change data
     *
     * @param friendId     String friendId that should be added
     * @param itemPosition the itemPosition of user
     */
    public void notifyToRemovedFriend(String friendId, int itemPosition) {
        //remove friendId from mFriendList
        int itemIndex = mFriendIdList.indexOf(friendId);
        if (itemIndex != -1) {
            mFriendIdList.remove(itemIndex);
            notifyItemChanged(itemPosition);
        } else {
            notifyItemChanged(itemPosition);
        }
    }

    /**
     * clear all of the  result from recycler view
     */
    public void clearItems() {
        mData.clear();
        notifyDataSetChanged();
    }

    /**
     * custom view holder for User column for user_column.xml layout
     */
    class UserViewColumnHolder extends RecyclerView.ViewHolder {

        private CardView mUserCardView;
        private ImageView mImPhoto;
        private AppCompatTextView mTxUsername;
        private DetectLTextView mTxDisplayName;
        private AppCompatImageButton mBtnTelepathy;
        private AppCompatImageButton mBtnFriendAction;

        public UserViewColumnHolder(View itemView) {
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
                    if (mUserItemEventListener != null) {
                        UserModel userModel = mData.get(getAdapterPosition());
                        mUserItemEventListener.onStartTelepathy(userModel.user_id, userModel.username, userModel.display_name, userModel.image_url, userModel.theme);
                    }
                }
            });
            mBtnFriendAction.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    UserModel currentModel = mData.get(getAdapterPosition());
                    if (mUserItemEventListener != null) {
                        //check is this user friend or not
                        if (mFriendIdList.contains(currentModel.user_id)) {
                            //remove as friend
                            mUserItemEventListener.onRemoveAsFriend(currentModel.user_id, getAdapterPosition());
                        } else {
                            mUserItemEventListener.onAddAsFriend(currentModel.user_id, getAdapterPosition());
                        }
                    }
                }
            });
        }
    }

    /**
     * interface to handle on events for user row layout
     */
    public interface UserItemEventListener {
        void onStartTelepathy(String userId, String username, String displayName, String imageUrl, String themeName);

        void onAddAsFriend(String userId, int itemPosition);

        void onRemoveAsFriend(String userId, int itemPosition);
    }
}
