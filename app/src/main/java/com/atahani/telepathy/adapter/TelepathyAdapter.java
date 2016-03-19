package com.atahani.telepathy.adapter;

import android.content.res.ColorStateList;
import android.os.Build;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import io.realm.RealmResults;
import mobi.atahani.telepathy.R;
import com.atahani.telepathy.TApplication;
import com.atahani.telepathy.realm.TelepathyModelRealm;
import com.atahani.telepathy.ui.component.DetectLTextView;
import com.atahani.telepathy.ui.utility.Constants;
import com.atahani.telepathy.ui.utility.CropCircleTransformation;
import com.atahani.telepathy.utility.ThemeUtil;

/**
 * Telepathy Adapter for telepathies
 */
public class TelepathyAdapter extends RecyclerView.Adapter<TelepathyAdapter.TelepathyViewHolder> {

    private TelepathyEventListener mTelepathyEventListener;
    private ThemeUtil mThemeUtil;
    private LayoutInflater mLayoutInflater;
    private RealmResults<TelepathyModelRealm> mData;
    private StaggeredGridLayoutManager mStaggeredGridLayoutManager;


    public TelepathyAdapter(LayoutInflater inflater, RealmResults<TelepathyModelRealm> data, TelepathyEventListener telepathyEventListener) {
        this.mLayoutInflater = inflater;
        this.mData = data;
        this.mTelepathyEventListener = telepathyEventListener;
        this.mThemeUtil = new ThemeUtil(TApplication.applicationContext);
    }

    /**
     * set StaggeredGridLayoutManager for invalidate Span assignments in tablet mode
     *
     * @param staggeredGridLayoutManager StaggeredGridLayoutManager
     */
    public void setStaggeredGridLayoutManager(StaggeredGridLayoutManager staggeredGridLayoutManager) {
        this.mStaggeredGridLayoutManager = staggeredGridLayoutManager;
    }

    /**
     * update realmResult of adapter
     *
     * @param mData RealmResult<MessageModelRealm> mData
     */
    public void updateRealmResult(RealmResults<TelepathyModelRealm> mData) {
        this.mData = mData;
        notifyDataSetChanged();
        //if in tablet mode the mStaggeredGridLayoutManager is set and after update data source should invalidateSpanAssignments
        if (mStaggeredGridLayoutManager != null) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mStaggeredGridLayoutManager.invalidateSpanAssignments();
                }
            }, 100);
        }
    }

    @Override
    public TelepathyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mLayoutInflater.inflate(R.layout.telepathy_column, parent, false);
        return new TelepathyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(TelepathyViewHolder holder, int position) {
        TelepathyModelRealm currentItem = mData.get(position);
        holder.mTelepathyHeaderView.setBackgroundColor(mThemeUtil.getPrimaryColorByThemeName(currentItem.getWithUserTheme()));
        holder.mFabDeleteTelepathy.setBackgroundTintList(ColorStateList.valueOf(mThemeUtil.getAccentColorByThemeName(currentItem.getWithUserTheme())));
        if (currentItem.getWithUserUsername() != null && currentItem.getWithUserUsername().equals(Constants.DELETED_ACCOUNT_VALUE)) {
            //load deleted user image
            Picasso.with(TApplication.applicationContext)
                    .load(R.drawable.user_deleted)
                    .transform(new CropCircleTransformation())
                    .into(holder.mImImageProfile);
            holder.mTxDisplayName.setText(TApplication.applicationContext.getString(R.string.label_account_deleted));
        } else {
            //load image view with Picasso and CropCircleTransformation
            Picasso.with(TApplication.applicationContext).load(currentItem.getWithUserImageUrl())
                    .placeholder(R.drawable.image_place_holder)
                    .transform(new CropCircleTransformation())
                    .into(holder.mImImageProfile);
            holder.mTxDisplayName.setText(currentItem.getWithUserDisplayName());
        }
        holder.mTxTelepathyStatus.setText(String.format(TApplication.applicationContext.getString(R.string.label_telepathy_live),
                DateUtils.getRelativeTimeSpanString(currentItem.getExpireAt().getTime(), System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS, DateUtils.FORMAT_ABBREV_RELATIVE)));
        holder.mTxTelepathyBody.setText(currentItem.getBody());
        //set prevent corner overlap to false for for under LOLLIPOP SDK ver
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
            holder.mCardViewTelepathy.setPreventCornerOverlap(false);
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

    class TelepathyViewHolder extends RecyclerView.ViewHolder {
        private CardView mCardViewTelepathy;
        private View mTelepathyHeaderView;
        private ImageView mImImageProfile;
        private DetectLTextView mTxDisplayName;
        private FloatingActionButton mFabDeleteTelepathy;
        private AppCompatTextView mTxTelepathyStatus;
        private DetectLTextView mTxTelepathyBody;

        public TelepathyViewHolder(View itemView) {
            super(itemView);
            mCardViewTelepathy = (CardView) itemView.findViewById(R.id.telepathy_card_view);
            mTelepathyHeaderView = itemView.findViewById(R.id.v_telepathy_header);
            mImImageProfile = (ImageView) itemView.findViewById(R.id.im_image_profile);
            mTxDisplayName = (DetectLTextView) itemView.findViewById(R.id.tx_display_name);
            mFabDeleteTelepathy = (FloatingActionButton) itemView.findViewById(R.id.fab_delete_telepathy);
            mTxTelepathyStatus = (AppCompatTextView) itemView.findViewById(R.id.tx_telepathy_status);
            mTxTelepathyBody = (DetectLTextView) itemView.findViewById(R.id.tx_telepathy_body);
            mFabDeleteTelepathy.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mTelepathyEventListener != null) {
                        mTelepathyEventListener.onDeleteTelepathy(mData.get(getAdapterPosition()).getTelepathyId(), getAdapterPosition());
                    }
                }
            });
        }
    }

    public interface TelepathyEventListener {
        void onDeleteTelepathy(String telepathyId, int itemPosition);
    }
}
