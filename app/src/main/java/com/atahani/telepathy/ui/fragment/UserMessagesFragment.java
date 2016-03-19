package com.atahani.telepathy.ui.fragment;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.pnikosis.materialishprogress.ProgressWheel;
import com.squareup.picasso.Picasso;
import com.atahani.telepathy.adapter.UserMessageAdapter;
import com.atahani.telepathy.realm.MessageModelRealm;

import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;
import me.leolin.shortcutbadger.ShortcutBadger;
import mobi.atahani.telepathy.R;
import com.atahani.telepathy.TApplication;
import com.atahani.telepathy.model.MessagesModel;
import com.atahani.telepathy.model.TOperationResultModel;
import com.atahani.telepathy.network.TService;
import com.atahani.telepathy.realm.TelepathyModelRealm;
import com.atahani.telepathy.realm.UserMessageNotificationRealmModel;
import com.atahani.telepathy.realm.UserModelRealm;
import com.atahani.telepathy.ui.SendTelepathyActivity;
import com.atahani.telepathy.ui.component.CommonFeedBack;
import com.atahani.telepathy.ui.component.DetectLTextView;
import com.atahani.telepathy.ui.component.TelepathyBaseActivity;
import com.atahani.telepathy.ui.utility.Constants;
import com.atahani.telepathy.ui.utility.CropCircleTransformation;
import com.atahani.telepathy.ui.utility.MarginDecoration;
import com.atahani.telepathy.utility.AndroidUtilities;
import com.atahani.telepathy.utility.AppPreferenceTools;
import com.atahani.telepathy.utility.ThemeUtil;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * the user messages fragment that contain messages recycler view and loader used in MessageFragment and UserMessageActivity
 */
public class UserMessagesFragment extends Fragment {

    private RealmResults<MessageModelRealm> mMessageRealmResult;
    private Realm mRealmDB;
    private TService mTService;
    private FrameLayout mLyEmptyStateContent;
    private Toolbar mToolbar;
    private ImageView mImUserImageView;
    private DetectLTextView mTxUserDisplayName;
    private ProgressDialog mProgressDialog;
    private UserMessageAdapter mAdapter;
    private ProgressWheel mPrPagerLoad;
    private int mCurrentPageNumber = 1;
    public long mTotalItem = 0;
    private boolean mIsInQueryMode = false;
    private AppPreferenceTools mAppPreferenceTools;
    public String mWithUserId;
    public String mWithUserDisplayName;
    public String mWithUserImageUrl;
    public String mWithUserTheme;
    public String mWithUserUsername;
    private UserMessagesFragmentListener mUserMessageFragmentListener;
    private BroadcastReceiver mMessageUpdateReceiver;
    private ThemeUtil mThemeUtil;

    public UserMessagesFragment() {
        // Required empty public constructor
    }


    public static UserMessagesFragment newInstance(String withUserId, String withUserUsername, String withUserDisplayName, String withUserImageUrl, String withUserTheme, long numberOfUserMessage) {
        UserMessagesFragment fragment = new UserMessagesFragment();
        Bundle args = new Bundle();
        args.putString(Constants.WITH_USER_ID_PARAM, withUserId);
        args.putString(Constants.WITH_USER_DISPLAY_NAME_PARAM, withUserDisplayName);
        args.putString(Constants.WITH_USER_IMAGE_URL_PARAM, withUserImageUrl);
        args.putString(Constants.WITH_USER_USERNAME_PARAM, withUserUsername);
        args.putString(Constants.WITH_USER_THEME_PARAM, withUserTheme);
        args.putLong(Constants.NUMBER_OF_USER_MESSAGE, numberOfUserMessage);
        fragment.setArguments(args);
        return fragment;
    }

    public void setUserMessageFragmentListener(UserMessagesFragmentListener userMessageFragmentListener) {
        this.mUserMessageFragmentListener = userMessageFragmentListener;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mWithUserId = getArguments().getString(Constants.WITH_USER_ID_PARAM);
            mWithUserDisplayName = getArguments().getString(Constants.WITH_USER_DISPLAY_NAME_PARAM);
            mWithUserImageUrl = getArguments().getString(Constants.WITH_USER_IMAGE_URL_PARAM);
            mWithUserUsername = getArguments().getString(Constants.WITH_USER_USERNAME_PARAM);
            mWithUserTheme = getArguments().getString(Constants.WITH_USER_THEME_PARAM);
            mTotalItem = getArguments().getLong(Constants.NUMBER_OF_USER_MESSAGE);
        }
        if (!getResources().getBoolean(R.bool.isTablet)) {
            setHasOptionsMenu(true);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View fragmentView = inflater.inflate(R.layout.fragment_user_messages, container, false);
        try {
            mRealmDB = ((TelepathyBaseActivity) getActivity()).getRealm();
            mTService = ((TelepathyBaseActivity) getActivity()).getTService();
            mAppPreferenceTools = new AppPreferenceTools(TApplication.applicationContext);
            mLyEmptyStateContent = (FrameLayout) fragmentView.findViewById(R.id.ly_empty_state_content);
            mProgressDialog = new ProgressDialog(getActivity());
            mProgressDialog.setCancelable(false);
            mProgressDialog.setCanceledOnTouchOutside(false);
            mPrPagerLoad = (ProgressWheel) fragmentView.findViewById(R.id.progress_wheel_load_page);
            mPrPagerLoad.setBarColor(mAppPreferenceTools.getAccentColor());
            mThemeUtil = new ThemeUtil(TApplication.applicationContext);
            //config the toolbar
            mToolbar = (Toolbar) fragmentView.findViewById(R.id.user_messages_toolbar);
            mToolbar.setBackgroundColor(mThemeUtil.getPrimaryColorByThemeName(mWithUserTheme));
            mImUserImageView = (ImageView) mToolbar.findViewById(R.id.im_image_profile);
            mTxUserDisplayName = (DetectLTextView) mToolbar.findViewById(R.id.tx_display_name);
            if (mWithUserDisplayName != null && mWithUserDisplayName.equals(Constants.DELETED_ACCOUNT_VALUE)) {
                mTxUserDisplayName.setText(getString(R.string.label_account_deleted));
                Picasso.with(TApplication.applicationContext)
                        .load(R.drawable.user_deleted)
                        .transform(new CropCircleTransformation())
                        .into(mImUserImageView);
            } else {
                mTxUserDisplayName.setText(mWithUserDisplayName);
                Picasso.with(TApplication.applicationContext).load(mWithUserImageUrl)
                        .placeholder(R.drawable.image_place_holder)
                        .transform(new CropCircleTransformation())
                        .into(mImUserImageView);
            }
            if (getResources().getBoolean(R.bool.isTablet)) {
                mToolbar.inflateMenu(R.menu.menu_user_message);
                mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        onMenuItemSelected(item);
                        return true;
                    }
                });
                onCreateOptionMenu(mToolbar.getMenu());
                mToolbar.setNavigationIcon(R.mipmap.ic_close_white_24dp);
                mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //close this fragment via event listener
                        if (mUserMessageFragmentListener != null) {
                            mUserMessageFragmentListener.onCloseFragmentInTabletMode(false);
                        }
                    }
                });
            } else {
                ((AppCompatActivity) getActivity()).setSupportActionBar(mToolbar);
                ((TelepathyBaseActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
            //get user messages by withUserId
            if (!mRealmDB.isClosed()) {
                mMessageRealmResult = mRealmDB.where(MessageModelRealm.class).equalTo("withUserId", mWithUserId).findAllSorted("matchedAt", Sort.DESCENDING);
            }
            RecyclerView mRecyclerView = (RecyclerView) fragmentView.findViewById(R.id.ry_user_messages);
            mRecyclerView.setPaddingRelative(0, getResources().getDimensionPixelOffset(R.dimen.dashboard_recycler_view_top_margin), 0, 0);
            //START Adapter
            mAdapter = new UserMessageAdapter(inflater, mWithUserTheme, mAppPreferenceTools, mMessageRealmResult, new UserMessageAdapter.UserMessageEventListener() {
                @Override
                public void onDeleteMessage(final String messageId, int itemPosition) {
                    //open alert dialog to confirm this action
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle(getString(R.string.label_delete_message));
                    builder.setMessage(getString(R.string.label_delete_message_description));
                    builder.setPositiveButton(getString(R.string.action_yes), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //remove this message send request to server
                            mProgressDialog.setMessage(getString(R.string.re_action_on_deleting_message));
                            mProgressDialog.show();
                            mTService.deleteMessageById(messageId, new Callback<TOperationResultModel>() {
                                @Override
                                public void success(TOperationResultModel tOperationResultModel, Response response) {
                                    try {
                                        //remove this object from DB
                                        if (getActivity() != null && !mRealmDB.isClosed() && isAdded()) {
                                            mRealmDB.executeTransaction(new Realm.Transaction() {
                                                @Override
                                                public void execute(Realm realm) {
                                                    MessageModelRealm messageModelRealm = realm.where(MessageModelRealm.class).equalTo("messageId", messageId).findFirst();
                                                    if (messageModelRealm != null) {
                                                        messageModelRealm.removeFromRealm();
                                                    }
                                                }
                                            }, new Realm.Transaction.Callback() {
                                                @Override
                                                public void onSuccess() {
                                                    updateFromDB();
                                                    if (mUserMessageFragmentListener != null) {
                                                        mUserMessageFragmentListener.onShouldUpdateClassifyMessageFromDB();
                                                    }
                                                }

                                                @Override
                                                public void onError(Exception e) {
                                                    super.onError(e);
                                                    updateFromDB();
                                                }
                                            });
                                            mProgressDialog.dismiss();
                                        }
                                    } catch (Exception ex) {
                                        if (mProgressDialog != null && mProgressDialog.isShowing()) {
                                            mProgressDialog.dismiss();
                                        }
                                        AndroidUtilities.processApplicationError(ex, true);
                                        if (isAdded() && getActivity() != null) {
                                            Snackbar.make(getActivity().findViewById(android.R.id.content), getString(R.string.re_action_internal_app_error), Snackbar.LENGTH_LONG).show();
                                        }
                                    }

                                }

                                @Override
                                public void failure(RetrofitError error) {
                                    if (getActivity() != null && isAdded()) {
                                        mProgressDialog.dismiss();
                                        CommonFeedBack commonFeedBack = new CommonFeedBack(getActivity().findViewById(android.R.id.content), getActivity());
                                        commonFeedBack.checkCommonErrorAndBackUnCommonOne(error);
                                    }
                                }
                            });
                        }
                    });
                    builder.setNegativeButton(getString(R.string.action_no), null);
                    builder.show();
                }
            });
            //END adapter
            mRecyclerView.setLayoutManager(new LinearLayoutManager(TApplication.applicationContext));
            RecyclerView.ItemDecoration mItemDecoration = new MarginDecoration(TApplication.applicationContext, R.dimen.message_column_view_bottom_margin);
            mRecyclerView.addItemDecoration(mItemDecoration);
            mRecyclerView.setAdapter(mAdapter);
            setUnReadMessageAsRead();
            //check total number at first load
            if (mTotalItem > mMessageRealmResult.size()) {
                getUserMessagesFromServer(1);
            } else if (mTotalItem == -1) {//it's true when from notification click
                getUserMessagesFromServer(1);
            }
            mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    if (mTotalItem > mMessageRealmResult.size()) {
                        mPrPagerLoad.spin();
                        //it's mean we have messages but do not have in DB
                        mCurrentPageNumber++;
                        getUserMessagesFromServer(mCurrentPageNumber);
                    }
                }
            });
            //check if in tablet mode, when we touch the fragment should set as read all user message
            if (getResources().getBoolean(R.bool.isTablet)) {
                mRecyclerView.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        setUnReadMessageAsRead();
                        return false;
                    }
                });
            }
        } catch (Exception ex) {
            if (mProgressDialog != null && mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
            }
            AndroidUtilities.processApplicationError(ex, true);
            if (isAdded() && getActivity() != null) {
                Snackbar.make(getActivity().findViewById(android.R.id.content), getString(R.string.re_action_internal_app_error), Snackbar.LENGTH_LONG).show();
            }
        }
        return fragmentView;
    }

    /**
     * get user messages from server
     *
     * @param pageNumber
     */
    public void getUserMessagesFromServer(int pageNumber) {
        try {
            int itemPerPage = getResources().getInteger(R.integer.number_of_user_messages_for_each_request);
            mTService.getMessages(mWithUserId, itemPerPage, pageNumber, new Callback<MessagesModel>() {
                @Override
                public void success(final MessagesModel messagesModel, Response response) {
                    try {

                        if (getActivity() != null && !mRealmDB.isClosed() && isAdded()) {
                            mRealmDB.executeTransaction(new Realm.Transaction() {
                                @Override
                                public void execute(Realm realm) {
                                    for (int i = 0; i < messagesModel.data.size(); i++) {
                                        //check is exist message in DB or not
                                        MessageModelRealm messageModelFromDB = realm.where(MessageModelRealm.class).equalTo("messageId", messagesModel.data.get(i).id).findFirst();
                                        if (messageModelFromDB == null) {
                                            //it's mean not in DB and should add it
                                            MessageModelRealm newMessageModelForDB = realm.createObject(MessageModelRealm.class);
                                            newMessageModelForDB.setMessageId(messagesModel.data.get(i).id);
                                            newMessageModelForDB.setWithUserId(messagesModel.data.get(i).with_user_id);
                                            newMessageModelForDB.setBodySend(messagesModel.data.get(i).body_send);
                                            newMessageModelForDB.setBodyReceive(messagesModel.data.get(i).body_receive);
                                            newMessageModelForDB.setYouAreFirst(messagesModel.data.get(i).you_are_first);
                                            newMessageModelForDB.setMatchedAt(messagesModel.data.get(i).matched_at);
                                            newMessageModelForDB.setMatchedWithTelepathyId(messagesModel.data.get(i).matched_with_telepathy_id);
                                            newMessageModelForDB.setMatchedInSec(messagesModel.data.get(i).matched_in_sec);
                                            newMessageModelForDB.setIsReceive(messagesModel.data.get(i).is_receive);
                                            newMessageModelForDB.setIsRead(messagesModel.data.get(i).is_read);
                                            newMessageModelForDB.setIsSendReadSignal(messagesModel.data.get(i).is_send_read_signal);
                                            newMessageModelForDB.setUpdatedAt(messagesModel.data.get(i).updated_at);
                                            //check if have any telepathy with this matched_telepathy_id remove it
                                            TelepathyModelRealm matchedTelepathy = realm.where(TelepathyModelRealm.class).equalTo("telepathyId", newMessageModelForDB.getMatchedWithTelepathyId()).findFirst();
                                            if (matchedTelepathy != null) {
                                                matchedTelepathy.removeFromRealm();
                                            }
                                        }
                                    }
                                }
                            }, new Realm.Transaction.Callback() {
                                @Override
                                public void onSuccess() {
                                    if (isAdded() && getActivity() != null) {
                                        mCurrentPageNumber = messagesModel.page;
                                        mTotalItem = messagesModel.total;
                                        updateFromDB();
                                    }
                                }

                                @Override
                                public void onError(Exception e) {
                                    super.onError(e);
                                    if (isAdded() && getActivity() != null) {
                                        updateFromDB();
                                    }
                                }
                            });
                        }
                        if (mPrPagerLoad.isSpinning()) {
                            mPrPagerLoad.stopSpinning();
                        }
                    } catch (Exception ex) {
                        if (mProgressDialog != null && mProgressDialog.isShowing()) {
                            mProgressDialog.dismiss();
                        }
                        AndroidUtilities.processApplicationError(ex, true);
                        if (isAdded() && getActivity() != null) {
                            Snackbar.make(getActivity().findViewById(android.R.id.content), getString(R.string.re_action_internal_app_error), Snackbar.LENGTH_LONG).show();
                        }
                    }
                }

                @Override
                public void failure(RetrofitError error) {
                    if (mPrPagerLoad.isSpinning()) {
                        mPrPagerLoad.stopSpinning();
                    }
                    if (getActivity() != null && isAdded()) {
                        CommonFeedBack commonFeedBack = new CommonFeedBack(getActivity().findViewById(android.R.id.content), getActivity());
                        commonFeedBack.checkCommonErrorAndBackUnCommonOne(error);
                    }
                }

            });
        } catch (Exception ex) {
            if (mProgressDialog != null && mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
            }
            AndroidUtilities.processApplicationError(ex, true);
            if (isAdded() && getActivity() != null) {
                Snackbar.make(getActivity().findViewById(android.R.id.content), getString(R.string.re_action_internal_app_error), Snackbar.LENGTH_LONG).show();
            }
        }
    }


    /**
     * update from DB and check empty state
     */
    public void updateFromDB() {
        try {
            if (!mRealmDB.isClosed() && isAdded() && getActivity() != null) {
                mMessageRealmResult = mRealmDB.where(MessageModelRealm.class).equalTo("withUserId", mWithUserId).findAllSorted("matchedAt", Sort.DESCENDING);
                mAdapter.updateRealmResult(mMessageRealmResult);
                if (mMessageRealmResult.size() == 0) {
                    if (mIsInQueryMode) {
                        changeEmptyState(Constants.NOT_FOUND_EMPTY_STATE);
                    } else {
                        //mean's don't have any user message form this user should close the user message fragment or activity
                        if (getResources().getBoolean(R.bool.isTablet)) {
                            if (mUserMessageFragmentListener != null) {
                                mUserMessageFragmentListener.onCloseFragmentInTabletMode(true);
                            }
                        } else {
                            //in mobile mode should finish activity
                            getActivity().setResult(Activity.RESULT_OK);
                            getActivity().finish();
                            ((TelepathyBaseActivity) getActivity()).setAnimationOnExit();
                        }
                    }
                } else {
                    changeEmptyState(Constants.DISAPPEAR_EMPTY_STATE);
                }
            }
        } catch (Exception ex) {
            AndroidUtilities.processApplicationError(ex, true);
            if (isAdded() && getActivity() != null) {
                Snackbar.make(getActivity().findViewById(android.R.id.content), getString(R.string.re_action_internal_app_error), Snackbar.LENGTH_LONG).show();
            }
        }
    }

    /**
     * update the fragment fields such as user theme and user messages
     */
    public void updateFragmentData(boolean isFromServer) {
        try {
            //first update the user fields such as user theme and all other fields
            if (mRealmDB != null && !mRealmDB.isClosed() && mWithUserId != null && isAdded()) {
                UserModelRealm userModelRealm = mRealmDB.where(UserModelRealm.class).equalTo("userId", mWithUserId).findFirst();
                if (userModelRealm != null) {
                    if (userModelRealm.getDisplayName().equals(Constants.DELETED_ACCOUNT_VALUE)) {
                        //mean's the user account was delete
                        mTxUserDisplayName.setText(getString(R.string.label_account_deleted));
                        Picasso.with(TApplication.applicationContext)
                                .load(R.drawable.user_deleted)
                                .transform(new CropCircleTransformation())
                                .into(mImUserImageView);
                    } else {
                        mTxUserDisplayName.setText(userModelRealm.getDisplayName());
                        Picasso.with(TApplication.applicationContext)
                                .load(userModelRealm.getImageUrl())
                                .transform(new CropCircleTransformation())
                                .into(mImUserImageView);
                    }
                    //update the toolbar color by name
                    mToolbar.setBackgroundColor(mThemeUtil.getPrimaryColorByThemeName(userModelRealm.getTheme()));
                    //finally update the user messages from DB
                    if (isFromServer) {
                        getUserMessagesFromServer(1);
                    } else {
                        updateFromDB();
                    }
                }
            }
        } catch (Exception ex) {
            AndroidUtilities.processApplicationError(ex, true);
            if (isAdded() && getActivity() != null) {
                Snackbar.make(getActivity().findViewById(android.R.id.content), getString(R.string.re_action_internal_app_error), Snackbar.LENGTH_LONG).show();
            }
        }
    }

    /**
     * change empty sate
     *
     * @param stateType int type of state
     */
    private void changeEmptyState(int stateType) {
        try {
            if (getActivity() != null && isAdded()) {
                if (stateType == Constants.NOT_FOUND_EMPTY_STATE) {
                    //load empty fragment into this side
                    EmptyStateFragment emptyStateFragment = EmptyStateFragment.newInstance(Constants.NOT_FOUND_EMPTY_STATE);
                    //add this fragment to view
                    getChildFragmentManager().beginTransaction().replace(R.id.ly_empty_state_content, emptyStateFragment, Constants.EMPTY_FRAGMENT_TAG).commitAllowingStateLoss();
                    mLyEmptyStateContent.setVisibility(View.VISIBLE);
                } else {
                    Fragment currentFragment = getChildFragmentManager().findFragmentByTag(Constants.EMPTY_FRAGMENT_TAG);
                    if (currentFragment != null && getActivity() != null && isAdded()) {
                        getChildFragmentManager().beginTransaction().remove(currentFragment).commitAllowingStateLoss();
                    }
                    mLyEmptyStateContent.setVisibility(View.GONE);
                }
            }
        } catch (Exception ex) {
            AndroidUtilities.processApplicationError(ex, true);
            if (isAdded() && getActivity() != null) {
                Snackbar.make(getActivity().findViewById(android.R.id.content), getString(R.string.re_action_internal_app_error), Snackbar.LENGTH_LONG).show();
            }
        }
    }

    /**
     * set all of the messages as read
     */
    private void setUnReadMessageAsRead() {
        try {
            if (!mRealmDB.isClosed() && isAdded() && getActivity() != null) {
                //clear notification and Badger number
                UserMessageNotificationRealmModel notificationRealmModelByUserId = mRealmDB.where(UserMessageNotificationRealmModel.class).equalTo("fromUserId", mWithUserId).findFirst();
                if (notificationRealmModelByUserId != null) {
                    mRealmDB.beginTransaction();
                    notificationRealmModelByUserId.setNumberOfNotification(0);
                    notificationRealmModelByUserId.setNotificationBody("");
                    mRealmDB.commitTransaction();
                    NotificationManagerCompat notificationManager = NotificationManagerCompat.from(TApplication.applicationContext);
                    notificationManager.cancel(notificationRealmModelByUserId.getNotificationId());
                }
                mAppPreferenceTools.setApplicationBadgerNumber(0);
                ShortcutBadger.with(TApplication.applicationContext).count(0);
                //get list of unRead Messages
                RealmResults<MessageModelRealm> unreadMessageForIterating = mRealmDB.where(MessageModelRealm.class).equalTo("withUserId", mWithUserId).equalTo("youAreFirst", true).equalTo("isSendReadSignal", false).findAll();
                final ArrayList<String> listOfMessageId = new ArrayList<>();
                for (int i = 0; i < unreadMessageForIterating.size(); i++) {
                    listOfMessageId.add(unreadMessageForIterating.get(i).getMessageId());
                }
                if (listOfMessageId.size() > 0) {
                    mTService.patchAllUserMessageAsRead(mWithUserId, new Callback<TOperationResultModel>() {
                        @Override
                        public void success(TOperationResultModel tOperationResultModel, Response response) {
                            // update all of the unreadMessage as send read signal
                            if (getActivity() != null && !mRealmDB.isClosed() && isAdded()) {
                                mRealmDB.executeTransaction(new Realm.Transaction() {
                                    @Override
                                    public void execute(Realm realm) {
                                        for (int i = 0; i < listOfMessageId.size(); i++) {
                                            //find message and update it
                                            MessageModelRealm messageModelRealm = realm.where(MessageModelRealm.class).equalTo("messageId", listOfMessageId.get(i)).findFirst();
                                            messageModelRealm.setIsSendReadSignal(true);
                                        }
                                    }
                                }, new Realm.Transaction.Callback() {
                                    @Override
                                    public void onSuccess() {
                                        if (mUserMessageFragmentListener != null) {
                                            mUserMessageFragmentListener.onShouldUpdateClassifyMessageFromDB();
                                        }
                                    }
                                });
                            }
                        }

                        @Override
                        public void failure(RetrofitError error) {
                            //do nothing
                        }
                    });
                }
            }
        } catch (Exception ex) {
            AndroidUtilities.processApplicationError(ex, true);
            if (isAdded() && getActivity() != null) {
                Snackbar.make(getActivity().findViewById(android.R.id.content), getString(R.string.re_action_internal_app_error), Snackbar.LENGTH_LONG).show();
            }
        }
    }

    /**
     * search message by query in DB result
     *
     * @param searchQuery String query
     */
    private void searchMessageByQueryInDB(String searchQuery) {
        try {
            if (!mRealmDB.isClosed() && isAdded()) {
                RealmResults<MessageModelRealm> messageResult = mRealmDB.where(MessageModelRealm.class)
                        .contains("bodySend", searchQuery)
                        .or()
                        .contains("bodyReceive", searchQuery)
                        .findAllSorted("matchedAt", Sort.DESCENDING);
                mAdapter.updateRealmResult(messageResult);
                //check not found state
                if (messageResult.size() == 0) {
                    changeEmptyState(Constants.NOT_FOUND_EMPTY_STATE);
                } else {
                    changeEmptyState(Constants.DISAPPEAR_EMPTY_STATE);
                }
            }
        } catch (Exception ex) {
            AndroidUtilities.processApplicationError(ex, true);
            if (isAdded() && getActivity() != null) {
                Snackbar.make(getActivity().findViewById(android.R.id.content), getString(R.string.re_action_internal_app_error), Snackbar.LENGTH_LONG).show();
            }
        }
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        //inflate menu
        inflater.inflate(R.menu.menu_user_message, menu);
        onCreateOptionMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        onMenuItemSelected(item);
        return super.onOptionsItemSelected(item);
    }

    private void onCreateOptionMenu(Menu menu) {
        MenuItem actionTelepathyItem = menu.findItem(R.id.action_telepathy);
        if (mWithUserUsername.equals(Constants.DELETED_ACCOUNT_VALUE)) {
            actionTelepathyItem.setVisible(false);
        } else {
            actionTelepathyItem.setVisible(true);
        }
        //get action_search_item, search_view, search_plate, search_container, search_clear_action object
        MenuItem action_search_item = menu.findItem(R.id.action_search);
        final SearchView search_view = (SearchView) MenuItemCompat.getActionView(action_search_item);
        final EditText search_container = (EditText) search_view.findViewById(R.id.search_src_text);
        search_container.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!TextUtils.isEmpty(s)) {
                    search_container.setTypeface(AndroidUtilities.getTypefaceByText(s.toString()));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        View search_plate = search_view.findViewById(R.id.search_plate);
        final ImageView search_clear_action = (ImageView) search_view.findViewById(R.id.search_close_btn);
        search_view.setQueryHint(getString(R.string.hint_search));
        //config items for better UX
        search_clear_action.setVisibility(View.VISIBLE);
        search_clear_action.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (search_container.getText().toString().trim().length() != 0) {
                    changeEmptyState(Constants.DISAPPEAR_EMPTY_STATE);
                    search_container.setText("");
                    //and if keyboard hide , show it
                    AndroidUtilities.showKeyboard(search_container);
                    search_clear_action.setVisibility(View.VISIBLE);
                }
            }
        });
        search_view.setMaxWidth(100000);
        search_container.setHint(getResources().getString(R.string.hint_search));
        Drawable transparent_drawable = new ColorDrawable(Color.TRANSPARENT);
        search_plate.setBackground(transparent_drawable);
        //set query text change listener for search_view to bind item request into server and get values
        search_view.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                AndroidUtilities.hideKeyboard(search_container);
                searchMessageByQueryInDB(s.trim());
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                if (s.length() == 0) {
                    //means clear all of the text
                    updateFromDB();
                    changeEmptyState(Constants.DISAPPEAR_EMPTY_STATE);
                }
                return false;
            }
        });
        MenuItemCompat.setOnActionExpandListener(action_search_item, new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                changeEmptyState(Constants.DISAPPEAR_EMPTY_STATE);
                mIsInQueryMode = true;
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                updateFromDB();
                mIsInQueryMode = false;
                return true;
            }
        });
    }

    private void onMenuItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_telepathy) {
            try {
                if (getActivity() != null && isAdded()) {
                    if (mWithUserUsername != null && !mWithUserUsername.equals(Constants.DELETED_ACCOUNT_VALUE)) {
                        //open send telepathy with this user
                        Intent telepathyIntent = new Intent(TApplication.applicationContext, SendTelepathyActivity.class);
                        telepathyIntent.putExtra(Constants.WITH_USER_ID_PARAM, mWithUserId);
                        telepathyIntent.putExtra(Constants.WITH_USER_DISPLAY_NAME_PARAM, mWithUserDisplayName);
                        telepathyIntent.putExtra(Constants.WITH_USER_IMAGE_URL_PARAM, mWithUserImageUrl);
                        telepathyIntent.putExtra(Constants.WITH_USER_THEME_PARAM, mWithUserTheme);
                        telepathyIntent.putExtra(Constants.WITH_USER_USERNAME_PARAM, mWithUserUsername);
                        getActivity().startActivityForResult(telepathyIntent, Constants.TELEPATHY_REQUEST_CODE);
                        ((TelepathyBaseActivity) getActivity()).setAnimationOnStart();
                    }
                }
            } catch (Exception ex) {
                AndroidUtilities.processApplicationError(ex, true);
                if (isAdded() && getActivity() != null) {
                    Snackbar.make(getActivity().findViewById(android.R.id.content), getString(R.string.re_action_internal_app_error), Snackbar.LENGTH_LONG).show();
                }
            }
        } else if (id == R.id.action_delete_messages) {
            try {
                if (getActivity() != null && isAdded()) {
                    //first prompt user to delete all of the messages
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle(getString(R.string.label_delete_all_messages));
                    builder.setMessage(getString(R.string.label_delete_message_description));
                    builder.setPositiveButton(getString(R.string.action_yes), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //remove this message send request to server
                            mProgressDialog.setMessage(getString(R.string.re_action_on_deleting_messages));
                            mProgressDialog.show();
                            //delete all of the messages with this user
                            mTService.deleteUserMessages(mWithUserId, new Callback<TOperationResultModel>() {
                                @Override
                                public void success(TOperationResultModel tOperationResultModel, Response response) {
                                    try {
                                        if (getActivity() != null && !mRealmDB.isClosed()) {
                                            mRealmDB.executeTransaction(new Realm.Transaction() {
                                                @Override
                                                public void execute(Realm realm) {
                                                    RealmResults<MessageModelRealm> messageFromThisUserResult = realm.where(MessageModelRealm.class).equalTo("withUserId", mWithUserId).findAll();
                                                    messageFromThisUserResult.clear();
                                                }
                                            }, new Realm.Transaction.Callback() {
                                                @Override
                                                public void onSuccess() {
                                                    updateFromDB();
                                                }

                                                @Override
                                                public void onError(Exception e) {
                                                    super.onError(e);
                                                    updateFromDB();
                                                }
                                            });
                                            mProgressDialog.dismiss();
                                        }
                                    } catch (Exception ex) {
                                        if(mProgressDialog!=null && mProgressDialog.isShowing()){
                                            mProgressDialog.dismiss();
                                        }
                                        AndroidUtilities.processApplicationError(ex, true);
                                        if (isAdded() && getActivity() != null) {
                                            Snackbar.make(getActivity().findViewById(android.R.id.content), getString(R.string.re_action_internal_app_error), Snackbar.LENGTH_LONG).show();
                                        }
                                    }
                                }

                                @Override
                                public void failure(RetrofitError error) {
                                    if (getActivity() != null) {
                                        mProgressDialog.dismiss();
                                        CommonFeedBack commonFeedBack = new CommonFeedBack(getActivity().findViewById(android.R.id.content), getActivity());
                                        commonFeedBack.checkCommonErrorAndBackUnCommonOne(error);
                                    }
                                }
                            });
                        }
                    });
                    builder.setNegativeButton(getString(R.string.action_no), null);
                    builder.show();
                }
            } catch (Exception ex) {
                if(mProgressDialog!=null && mProgressDialog.isShowing()){
                    mProgressDialog.dismiss();
                }
                AndroidUtilities.processApplicationError(ex, true);
                if (isAdded() && getActivity() != null) {
                    Snackbar.make(getActivity().findViewById(android.R.id.content), getString(R.string.re_action_internal_app_error), Snackbar.LENGTH_LONG).show();
                }
            }

        } else if (id == android.R.id.home) {
            if (getActivity() != null) {
                //handle up action like on back pressed
                getActivity().onBackPressed();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        try {
            mMessageUpdateReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    int actionToDo = intent.getIntExtra(Constants.ACTION_TO_DO_PARAM, Constants.UPDATE_FROM_DB);
                    if (!mRealmDB.isClosed() && isAdded()) {
                        if (actionToDo == Constants.UPDATE_FROM_DB) {
                            updateFromDB();
                        } else if (actionToDo == Constants.UPDATE_FROM_NET) {
                            getUserMessagesFromServer(1);
                        }
                    }
                }
            };
            LocalBroadcastManager.getInstance(getActivity()).registerReceiver((mMessageUpdateReceiver), new IntentFilter(Constants.ACTION_TO_DO_FOR_MESSAGE_UPDATE_INTENT_FILTER));

        } catch (Exception ex) {
            AndroidUtilities.processApplicationError(ex, true);
            if (isAdded() && getActivity() != null) {
                Snackbar.make(getActivity().findViewById(android.R.id.content), getString(R.string.re_action_internal_app_error), Snackbar.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onStop() {
        try {
            LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mMessageUpdateReceiver);
            if (mProgressDialog != null && mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
            }
        } catch (Exception ex) {
            AndroidUtilities.processApplicationError(ex, true);
            if (isAdded() && getActivity() != null) {
                Snackbar.make(getActivity().findViewById(android.R.id.content), getString(R.string.re_action_internal_app_error), Snackbar.LENGTH_LONG).show();
            }
        }
        super.onStop();
    }

    public interface UserMessagesFragmentListener {
        void onCloseFragmentInTabletMode(boolean shouldUpdateClassifyMessages);

        void onShouldUpdateClassifyMessageFromDB();
    }
}
