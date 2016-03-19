package com.atahani.telepathy.ui.fragment;


import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.*;
import android.support.v7.widget.SearchView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.*;
import android.widget.*;

import com.google.android.gms.appinvite.AppInviteInvitation;
import com.pnikosis.materialishprogress.ProgressWheel;
import com.atahani.telepathy.adapter.UserAdapter;
import com.atahani.telepathy.model.UserModel;
import com.atahani.telepathy.network.TService;
import com.atahani.telepathy.realm.MessageModelRealm;
import com.atahani.telepathy.realm.UserModelRealm;
import com.atahani.telepathy.realm.adapter.RealmFriendAdapter;
import com.atahani.telepathy.ui.SendTelepathyActivity;
import com.atahani.telepathy.ui.component.CommonFeedBack;
import com.atahani.telepathy.ui.component.TelepathyBaseActivity;
import com.atahani.telepathy.ui.utility.Constants;
import com.atahani.telepathy.ui.utility.MarginDecoration;
import com.atahani.telepathy.utility.AndroidUtilities;
import com.atahani.telepathy.utility.AppPreferenceTools;

import io.realm.Realm;
import io.realm.RealmResults;
import mobi.atahani.telepathy.R;
import com.atahani.telepathy.TApplication;
import com.atahani.telepathy.adapter.FriendAdapter;
import com.atahani.telepathy.model.TOperationResultModel;
import com.atahani.telepathy.ui.AppSettingsActivity;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Friend fragment
 */
public class FriendFragment extends Fragment {

    private Realm mRealmDB;
    private TService mTService;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mUserRecyclerView;
    private RecyclerView.ItemDecoration mItemDecoration;
    private ProgressWheel mProgressWheel;
    private AppPreferenceTools mAppPreferenceTools;
    private RealmResults<UserModelRealm> mFriendResult;
    private FriendAdapter mFriendAdapter;
    private UserAdapter mUserAdapter;
    private FrameLayout mLyEmptyStateContent;
    private ProgressDialog mProgressDialog;
    private boolean mIsInQueryMode = false;
    private BroadcastReceiver mFriendUpdateReceiver;
    private String mDirectionType = "LTR";

    public FriendFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View fragmentView = inflater.inflate(R.layout.fragment_friend, container, false);
        try {
            mDirectionType = getResources().getString(R.string.direction_type);
            mRealmDB = ((TelepathyBaseActivity) getActivity()).getRealm();
            mTService = ((TelepathyBaseActivity) getActivity()).getTService();
            mAppPreferenceTools = ((TelepathyBaseActivity) getActivity()).getAppPreferenceTools();
            mLyEmptyStateContent = (FrameLayout) fragmentView.findViewById(R.id.ly_empty_state_content);
            mProgressDialog = new ProgressDialog(getActivity());
            mProgressDialog.setCancelable(false);
            mProgressDialog.setCanceledOnTouchOutside(false);
            mSwipeRefreshLayout = (SwipeRefreshLayout) fragmentView.findViewById(R.id.swipe_refresh_layout);
            mSwipeRefreshLayout.setColorSchemeColors(mAppPreferenceTools.getAccentColor(), mAppPreferenceTools.getPrimaryColor(), mAppPreferenceTools.getPrimaryDarkColor());
            mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    if (!mIsInQueryMode) {
                        getFriendsListFromServer();
                    } else {
                        mSwipeRefreshLayout.setRefreshing(false);
                    }
                }
            });
            mProgressWheel = (ProgressWheel) fragmentView.findViewById(R.id.progress_wheel);
            mProgressWheel.setBarColor(mAppPreferenceTools.getAccentColor());
            mUserRecyclerView = (RecyclerView) fragmentView.findViewById(R.id.ry_users);
            mUserRecyclerView.setPaddingRelative(getResources().getDimensionPixelOffset(R.dimen.friend_recycler_view_side_margin), getResources().getDimensionPixelOffset(R.dimen.friend_recycler_view_top_margin), getResources().getDimensionPixelOffset(R.dimen.friend_recycler_view_side_margin), 0);
            //get data and config recycler view in load
            mUserRecyclerView.setHasFixedSize(true);
            mItemDecoration = new MarginDecoration(TApplication.applicationContext, R.dimen.user_column_view_side_margin, R.dimen.user_column_view_side_margin, R.dimen.user_column_view_bottom_margin);
            mUserRecyclerView.addItemDecoration(mItemDecoration);
            //set layout manager
            mUserRecyclerView.setLayoutManager(new GridLayoutManager(TApplication.applicationContext, getResources().getInteger(R.integer.user_span_size), OrientationHelper.VERTICAL, false));
            if (!mRealmDB.isClosed()) {
                mFriendResult = mRealmDB.where(UserModelRealm.class).equalTo("isFriend", true).findAllSorted("displayName");
            }
            RealmFriendAdapter mRealmFriendAdapter = new RealmFriendAdapter(TApplication.applicationContext, mFriendResult, true);
            mFriendAdapter = new FriendAdapter(TApplication.applicationContext, new FriendAdapter.FriendEventListener() {
                @Override
                public void onStartTelepathy(String userId, String username, String displayName, String imageUrl, String themeName) {
                    //start telepathy
                    startTelepathy(userId, username, displayName, imageUrl, themeName);
                }

                @Override
                public void onRemoveAsFriend(String userId, int itemPosition) {
                    removeAsFriend(userId, itemPosition, false);
                }
            });
            mFriendAdapter.setRealmAdapter(mRealmFriendAdapter);
            mUserRecyclerView.setAdapter(mFriendAdapter);
            mUserAdapter = new UserAdapter(TApplication.applicationContext, new UserAdapter.UserItemEventListener() {
                @Override
                public void onStartTelepathy(String userId, String username, String displayName, String imageUrl, String themeName) {
                    //start telepathy
                    startTelepathy(userId, username, displayName, imageUrl, themeName);
                }

                @Override
                public void onAddAsFriend(String userId, int itemPosition) {
                    addAsFriend(userId, itemPosition);
                }

                @Override
                public void onRemoveAsFriend(String userId, int itemPosition) {
                    removeAsFriend(userId, itemPosition, true);
                }
            });
            //check is load in first time
            if (!mAppPreferenceTools.isFriendsLoadInFirstTime()) {
                getFriendsListFromServer();
            } else {
                //check is have not friend load empty state
                if (mFriendResult.size() == 0) {
                    changeEmptyState(Constants.DO_NOT_HAVE_FRIEND_EMPTY_STATE);
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
        return fragmentView;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        try {
            mUserRecyclerView.setPaddingRelative(getResources().getDimensionPixelOffset(R.dimen.friend_recycler_view_side_margin), getResources().getDimensionPixelOffset(R.dimen.friend_recycler_view_top_margin), getResources().getDimensionPixelOffset(R.dimen.friend_recycler_view_side_margin), 0);
            mUserRecyclerView.setLayoutManager(new GridLayoutManager(TApplication.applicationContext, getResources().getInteger(R.integer.user_span_size), OrientationHelper.VERTICAL, false));
            if (mDirectionType.equals("RTL")) {
                mUserRecyclerView.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
            } else {
                mUserRecyclerView.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
            }
            mUserRecyclerView.removeItemDecoration(mItemDecoration);
            mItemDecoration = new MarginDecoration(TApplication.applicationContext, R.dimen.user_column_view_side_margin, R.dimen.user_column_view_side_margin, R.dimen.user_column_view_bottom_margin);
            mUserRecyclerView.addItemDecoration(mItemDecoration);
        } catch (Exception ex) {
            AndroidUtilities.processApplicationError(ex, true);
            if (isAdded() && getActivity() != null) {
                Snackbar.make(getActivity().findViewById(android.R.id.content), getString(R.string.re_action_internal_app_error), Snackbar.LENGTH_LONG).show();
            }
        }
    }

    /**
     * get list of all friend list
     */
    private void getFriendsListFromServer() {
        try {
            mSwipeRefreshLayout.setRefreshing(true);
            mTService.getListOfFriends("", new Callback<List<UserModel>>() {
                @Override
                public void success(final List<UserModel> friendModels, Response response) {
                    try {
                        if (!mRealmDB.isClosed() && getActivity() != null && isAdded()) {
                            // un friend all of the user models
                            mRealmDB.executeTransaction(new Realm.Transaction() {
                                @Override
                                public void execute(Realm realm) {
                                    //un friend all of users
                                    RealmResults<UserModelRealm> userModelResult = realm.where(UserModelRealm.class).findAll();
                                    ArrayList<String> listOfUserId = new ArrayList<>();
                                    for (int i = 0; i < userModelResult.size(); i++) {
                                        listOfUserId.add(userModelResult.get(i).getUserId());
                                    }
                                    for (int i = 0; i < listOfUserId.size(); i++) {
                                        UserModelRealm currentUserModel = realm.where(UserModelRealm.class).equalTo("userId", listOfUserId.get(i)).findFirst();
                                        if (currentUserModel != null) {
                                            currentUserModel.setIsFriend(false);
                                            //check if do not have any message with this userId remove it from DB
                                            long numberOfUserMessage = realm.where(MessageModelRealm.class).equalTo("withUserId", currentUserModel.getUserId()).count();
                                            if (numberOfUserMessage == 0) {
                                                currentUserModel.removeFromRealm();
                                            }
                                        }
                                    }
                                    for (UserModel friend : friendModels) {
                                        //check is already in db
                                        UserModelRealm userModelRealm = realm.where(UserModelRealm.class).equalTo("userId", friend.user_id).findFirst();
                                        if (userModelRealm == null) {
                                            //add into DB
                                            UserModelRealm friendModelRealm = realm.createObject(UserModelRealm.class);
                                            friendModelRealm.setUserId(friend.user_id);
                                            friendModelRealm.setUsername(friend.username);
                                            friendModelRealm.setDisplayName(friend.display_name);
                                            friendModelRealm.setImageUrl(friend.image_url);
                                            friendModelRealm.setTheme(friend.theme);
                                            friendModelRealm.setIsFriend(true);
                                        } else {
                                            userModelRealm.setUsername(friend.username);
                                            userModelRealm.setDisplayName(friend.display_name);
                                            userModelRealm.setImageUrl(friend.image_url);
                                            userModelRealm.setTheme(friend.theme);
                                            userModelRealm.setIsFriend(true);
                                        }
                                    }
                                }
                            }, new Realm.Transaction.Callback() {
                                @Override
                                public void onSuccess() {
                                    super.onSuccess();
                                    if (getActivity() != null && isAdded()) {
                                        mFriendAdapter.notifyDataSetChanged();
                                        mAppPreferenceTools.friendsLoadedForFirstTime();
                                        mSwipeRefreshLayout.setRefreshing(false);
                                        //check if have no friend load empty state
                                        if (mFriendResult.size() == 0) {
                                            changeEmptyState(Constants.DO_NOT_HAVE_FRIEND_EMPTY_STATE);
                                        } else {
                                            changeEmptyState(Constants.DISAPPEAR_EMPTY_STATE);
                                        }
                                    }
                                }

                                @Override
                                public void onError(Exception e) {
                                    super.onError(e);
                                    if (getActivity() != null && isAdded()) {
                                        mSwipeRefreshLayout.setRefreshing(false);
                                        //check if have no friend load empty state
                                        if (mFriendResult.size() == 0) {
                                            changeEmptyState(Constants.DO_NOT_HAVE_FRIEND_EMPTY_STATE);
                                        } else {
                                            changeEmptyState(Constants.DISAPPEAR_EMPTY_STATE);
                                        }
                                    }
                                }
                            });
                        }
                    } catch (Exception ex) {
                        if (mSwipeRefreshLayout != null && mSwipeRefreshLayout.isRefreshing()) {
                            mSwipeRefreshLayout.setRefreshing(false);
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
                        if (mSwipeRefreshLayout != null && mSwipeRefreshLayout.isRefreshing()) {
                            mSwipeRefreshLayout.setRefreshing(false);
                        }
                        CommonFeedBack commonFeedBack = new CommonFeedBack(getActivity().findViewById(android.R.id.content), getActivity());
                        commonFeedBack.checkCommonErrorAndBackUnCommonOne(error);
                    }

                }
            });
        } catch (Exception ex) {
            if (mSwipeRefreshLayout != null && mSwipeRefreshLayout.isRefreshing()) {
                mSwipeRefreshLayout.setRefreshing(false);
            }
            AndroidUtilities.processApplicationError(ex, true);
            if (isAdded() && getActivity() != null) {
                Snackbar.make(getActivity().findViewById(android.R.id.content), getString(R.string.re_action_internal_app_error), Snackbar.LENGTH_LONG).show();
            }
        }
    }


    /**
     * add user as friend in query mode
     *
     * @param userId       String userId
     * @param itemPosition String itemPosition in recycler view
     */
    private void addAsFriend(final String userId, final int itemPosition) {
        try {
            mProgressDialog.setMessage(getString(R.string.re_action_on_add_in_friend_list));
            mProgressDialog.show();
            mTService.addNewFriend(userId, new Callback<UserModel>() {
                @Override
                public void success(final UserModel userModel, Response response) {
                    try {
                        if (!mRealmDB.isClosed() && getActivity() != null && isAdded()) {
                            mRealmDB.executeTransaction(new Realm.Transaction() {
                                @Override
                                public void execute(Realm realm) {
                                    //check is already have this user
                                    UserModelRealm userModelRealm = realm.where(UserModelRealm.class).equalTo("userId", userModel.user_id).findFirst();
                                    if (userModelRealm != null) {
                                        userModelRealm.setIsFriend(true);
                                    } else {
                                        //add this user as friend into DB
                                        UserModelRealm friendModelRealm = realm.createObject(UserModelRealm.class);
                                        friendModelRealm.setUserId(userModel.user_id);
                                        friendModelRealm.setUsername(userModel.username);
                                        friendModelRealm.setDisplayName(userModel.display_name);
                                        friendModelRealm.setImageUrl(userModel.image_url);
                                        friendModelRealm.setTheme(userModel.theme);
                                        friendModelRealm.setIsFriend(true);
                                    }
                                }
                            }, new Realm.Transaction.Callback() {
                                @Override
                                public void onSuccess() {
                                    super.onSuccess();
                                    if (getActivity() != null && isAdded()) {
                                        mUserAdapter.notifyToAddedFriend(userId, itemPosition);
                                        mProgressDialog.dismiss();
                                    }
                                }

                                @Override
                                public void onError(Exception e) {
                                    super.onError(e);
                                    if (getActivity() != null && isAdded()) {
                                        mProgressDialog.dismiss();
                                    }
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
     * remove user as friend
     *
     * @param userId        String userId
     * @param itemPosition  String itemPosition in recycler view
     * @param isInQueryMode boolean if in search mode true
     */
    private void removeAsFriend(final String userId, final int itemPosition, final boolean isInQueryMode) {
        try {
            //first check confirm dialog message
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(getString(R.string.app_name));
            builder.setMessage(getString(R.string.re_action_remove_user_as_friend_confirm_alert_message));
            builder.setPositiveButton(getString(R.string.action_yes), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //yes remove this user as friend
                    mProgressDialog.setMessage(getString(R.string.re_action_on_remove_from_friend_list));
                    mProgressDialog.show();
                    mTService.removeFriend(userId, new Callback<TOperationResultModel>() {
                        @Override
                        public void success(TOperationResultModel tOperationResultModel, Response response) {
                            try {
                                if (!mRealmDB.isClosed() && getActivity() != null && isAdded()) {
                                    mRealmDB.executeTransaction(new Realm.Transaction() {
                                        @Override
                                        public void execute(Realm realm) {
                                            //un friend this user in DB
                                            UserModelRealm friendModelRealm = realm.where(UserModelRealm.class).equalTo("isFriend", true).equalTo("userId", userId).findFirst();
                                            friendModelRealm.setIsFriend(false);
                                            //check if do not have any message with this userId remove it from DB
                                            long numberOfUserMessage = realm.where(MessageModelRealm.class).equalTo("withUserId", userId).count();
                                            if (numberOfUserMessage == 0) {
                                                friendModelRealm.removeFromRealm();
                                            }
                                        }
                                    }, new Realm.Transaction.Callback() {
                                        @Override
                                        public void onSuccess() {
                                            super.onSuccess();
                                            if (getActivity() != null && isAdded()) {
                                                //notify recycler view
                                                if (isInQueryMode) {
                                                    mUserAdapter.notifyToRemovedFriend(userId, itemPosition);
                                                } else {
                                                    mFriendAdapter.notifyDataSetChanged();
                                                }
                                                //check is have any item or not
                                                if (mFriendResult.size() == 0 && !mIsInQueryMode) {
                                                    changeEmptyState(Constants.DO_NOT_HAVE_FRIEND_EMPTY_STATE);
                                                }
                                                mProgressDialog.dismiss();
                                            }
                                        }

                                        @Override
                                        public void onError(Exception e) {
                                            super.onError(e);
                                            if (getActivity() != null && isAdded()) {
                                                mProgressDialog.dismiss();
                                            }
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
     * start telepathy create new SendTelepathyDialogFragment
     * and set listener when telepathy send it
     *
     * @param userId      String userId
     * @param displayName String displayName
     * @param imageUrl    String imageUrl
     */
    private void startTelepathy(String userId, String username, String displayName, String imageUrl, String themeName) {
        try {
            if (getActivity() != null && isAdded()) {
                Intent telepathyIntent = new Intent(TApplication.applicationContext, SendTelepathyActivity.class);
                telepathyIntent.putExtra(Constants.WITH_USER_ID_PARAM, userId);
                telepathyIntent.putExtra(Constants.WITH_USER_DISPLAY_NAME_PARAM, displayName);
                telepathyIntent.putExtra(Constants.WITH_USER_IMAGE_URL_PARAM, imageUrl);
                telepathyIntent.putExtra(Constants.WITH_USER_THEME_PARAM, themeName);
                telepathyIntent.putExtra(Constants.WITH_USER_USERNAME_PARAM, username);
                getActivity().startActivityForResult(telepathyIntent, Constants.TELEPATHY_REQUEST_CODE);
                ((TelepathyBaseActivity) getActivity()).setAnimationOnStart();
            }
        } catch (Exception ex) {
            AndroidUtilities.processApplicationError(ex, true);
            if (isAdded() && getActivity() != null) {
                Snackbar.make(getActivity().findViewById(android.R.id.content), getString(R.string.re_action_internal_app_error), Snackbar.LENGTH_LONG).show();
            }
        }
    }

    /**
     * get array list of friendIds
     *
     * @return ArrayList of friendIds
     */
    private ArrayList<String> getFriendsList() {
        ArrayList<String> list = new ArrayList<>();
        if (mFriendResult != null) {
            for (UserModelRealm modelRealm : mFriendResult) {
                list.add(modelRealm.getUserId());
            }
        }
        return list;
    }

    /**
     * change the empty state
     *
     * @param stateType int type of state
     */
    private void changeEmptyState(int stateType) {
        if (stateType == Constants.DO_NOT_HAVE_FRIEND_EMPTY_STATE) {
            if (isAdded() && getActivity() != null) {
                //load friend empty state fragment
                getChildFragmentManager().beginTransaction().replace(R.id.ly_empty_state_content, new FriendEmptyStateFragment(), Constants.EMPTY_FRAGMENT_TAG).commitAllowingStateLoss();
            }
            mLyEmptyStateContent.setVisibility(View.VISIBLE);
        } else if (stateType == Constants.NOT_FOUND_EMPTY_STATE && mIsInQueryMode) {
            if (isAdded() && getActivity() != null) {
                //load empty fragment into this side
                EmptyStateFragment emptyStateFragment = EmptyStateFragment.newInstance(Constants.NOT_FOUND_EMPTY_STATE);
                //add this fragment to view
                getChildFragmentManager().beginTransaction().replace(R.id.ly_empty_state_content, emptyStateFragment, Constants.EMPTY_FRAGMENT_TAG).commitAllowingStateLoss();
                mLyEmptyStateContent.setVisibility(View.VISIBLE);
            }
        } else {
            if (isAdded() && getActivity() != null) {
                Fragment currentFragment = getChildFragmentManager().findFragmentByTag(Constants.EMPTY_FRAGMENT_TAG);
                if (currentFragment != null) {
                    getChildFragmentManager().beginTransaction().remove(currentFragment).commitAllowingStateLoss();
                }
            }
            mLyEmptyStateContent.setVisibility(View.GONE);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.menu_friends, menu);
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
        search_view.setQueryHint(getString(R.string.hint_search_user));
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
        search_container.setHint(getResources().getString(R.string.hint_search_user));
        Drawable transparent_drawable = new ColorDrawable(Color.TRANSPARENT);
        search_plate.setBackground(transparent_drawable);
        //set custom cursor for search_container
        try {
            Field mCursorDrawableRes = TextView.class.getDeclaredField("mCursorDrawableRes");
            mCursorDrawableRes.setAccessible(true);
            mCursorDrawableRes.set(search_container, R.drawable.search_view_cursor);
        } catch (Exception e) {
            //nothing to do
        }
        //set query text change listener for search_view to bind item request into server and get values
        search_view.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                AndroidUtilities.hideKeyboard(search_container);
                searchUserByQuery(s.trim());
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                if (s.length() == 0 && mIsInQueryMode) {
                    //means clear all of the text
                    mUserAdapter.clearItems();
                    changeEmptyState(Constants.DISAPPEAR_EMPTY_STATE);
                    mProgressWheel.setVisibility(View.GONE);
                }
                return false;
            }
        });
        MenuItemCompat.setOnActionExpandListener(action_search_item, new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                if (isAdded()) {
                    changeEmptyState(Constants.DISAPPEAR_EMPTY_STATE);
                    mSwipeRefreshLayout.setRefreshing(false);
                    mIsInQueryMode = true;
                    mUserAdapter.setFriendIdList(getFriendsList());
                    mUserRecyclerView.setAdapter(mUserAdapter);
                }
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                if (isAdded()) {
                    mIsInQueryMode = false;
                    mUserAdapter.clearItems();
                    mUserRecyclerView.setAdapter(mFriendAdapter);
                    if (mFriendResult.size() == 0) {
                        changeEmptyState(Constants.DO_NOT_HAVE_FRIEND_EMPTY_STATE);
                    } else {
                        changeEmptyState(Constants.DISAPPEAR_EMPTY_STATE);
                    }
                    mProgressWheel.setVisibility(View.GONE);
                }
                return true;
            }
        });
    }

    /**
     * search user by query by request to server
     *
     * @param query String part of query
     */
    private void searchUserByQuery(String query) {
        try {
            changeEmptyState(Constants.DISAPPEAR_EMPTY_STATE);
            mProgressWheel.setBarColor(mAppPreferenceTools.getAccentColor());
            mProgressWheel.setVisibility(View.VISIBLE);
            mTService.searchInUsers(query, new Callback<List<UserModel>>() {
                @Override
                public void success(List<UserModel> userModels, Response response) {
                    if (getActivity() != null && isAdded()) {
                        mProgressWheel.setVisibility(View.GONE);
                        mUserAdapter.updateUserData(userModels);
                        //check is have no user to show
                        if (userModels.size() == 0) {
                            changeEmptyState(Constants.NOT_FOUND_EMPTY_STATE);
                        } else {
                            changeEmptyState(Constants.DISAPPEAR_EMPTY_STATE);
                        }
                    }
                }

                @Override
                public void failure(RetrofitError error) {
                    if (getActivity() != null && isAdded()) {
                        mProgressWheel.setVisibility(View.GONE);
                        CommonFeedBack commonFeedBack = new CommonFeedBack(getActivity().findViewById(android.R.id.content), getActivity());
                        commonFeedBack.checkCommonErrorAndBackUnCommonOne(error);
                    }
                }
            });
        } catch (Exception ex) {
            AndroidUtilities.processApplicationError(ex, true);
            if (isAdded() && getActivity() != null) {
                Snackbar.make(getActivity().findViewById(android.R.id.content), getString(R.string.re_action_internal_app_error), Snackbar.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_invite) {
            if (getActivity() != null && isAdded()) {
                inviteFriends();
            }
        } else if (id == R.id.action_share_app_link) {
            if (getActivity() != null && isAdded()) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_SEND);
                //# change the type of data you need to share,
                //# for image use "image/*"
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.application_share_link));
                startActivity(Intent.createChooser(intent, getString(R.string.action_share_app_link)));
            }
        } else if (id == R.id.action_settings) {
            if (getActivity() != null && isAdded()) {
                //start app settings activity for RESULT
                getActivity().startActivityForResult(new Intent(TApplication.applicationContext, AppSettingsActivity.class), Constants.APP_SETTINGS_REQUEST_CODE);
                ((TelepathyBaseActivity) (getActivity())).setAnimationOnStart();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * check when tab un selected
     */
    public void checkWhenTabUnselectedOrSelected() {
        try {
            if (getActivity() != null && isAdded()) {
                mUserRecyclerView.setAdapter(mFriendAdapter);
                //check is have any item or not
                if (mFriendResult.size() == 0) {
                    changeEmptyState(Constants.DO_NOT_HAVE_FRIEND_EMPTY_STATE);
                } else {
                    changeEmptyState(Constants.DISAPPEAR_EMPTY_STATE);
                }
                if (mIsInQueryMode) {
                    mIsInQueryMode = false;
                }
            }

        } catch (Exception ex) {
            if (getActivity() != null && isAdded()) {
                //start app settings activity for RESULT
                getActivity().startActivityForResult(new Intent(TApplication.applicationContext, AppSettingsActivity.class), Constants.APP_SETTINGS_REQUEST_CODE);
                ((TelepathyBaseActivity) (getActivity())).setAnimationOnStart();
            }
        }
    }


    /**
     * open app invite intent with custom configuration
     */
    private void inviteFriends() {
        try {
            Intent intent = new AppInviteInvitation.IntentBuilder(getString(R.string.label_invitation_title))
                    .setMessage(getString(R.string.label_invitation_message))
                    .setAndroidMinimumVersionCode(Build.VERSION_CODES.JELLY_BEAN_MR1)
                    .setDeepLink(Uri.parse(String.format(getString(R.string.invite_deep_link_template), mAppPreferenceTools.getUserId())))
                    .setCustomImage(Uri.parse(mAppPreferenceTools.getImageUrl()))
                    .setCallToActionText(getString(R.string.action_install_and_send_telepathy))
                    .build();
            getActivity().startActivityForResult(intent, Constants.REQUEST_INVITE);
            ((TelepathyBaseActivity) getActivity()).setAnimationOnStart();
        } catch (Exception ex) {
            if (getActivity() != null && isAdded()) {
                //start app settings activity for RESULT
                getActivity().startActivityForResult(new Intent(TApplication.applicationContext, AppSettingsActivity.class), Constants.APP_SETTINGS_REQUEST_CODE);
                ((TelepathyBaseActivity) (getActivity())).setAnimationOnStart();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        try {
            mFriendUpdateReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    int actionToDo = intent.getIntExtra(Constants.ACTION_TO_DO_PARAM, Constants.UPDATE_FROM_DB);
                    if (!mRealmDB.isClosed() && isAdded()) {
                        if (actionToDo == Constants.UPDATE_FROM_DB) {
                            mUserRecyclerView.setAdapter(mFriendAdapter);
                            //check is have any item or not
                            if (mFriendResult.size() == 0) {
                                changeEmptyState(Constants.DO_NOT_HAVE_FRIEND_EMPTY_STATE);
                            } else {
                                changeEmptyState(Constants.DISAPPEAR_EMPTY_STATE);
                            }
                        } else if (actionToDo == Constants.UPDATE_FROM_NET) {
                            getFriendsListFromServer();
                        }
                    }
                }
            };
            LocalBroadcastManager.getInstance(getActivity()).registerReceiver((mFriendUpdateReceiver), new IntentFilter(Constants.ACTION_TO_DO_FOR_FRIEND_UPDATE_INTENT_FILTER));
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
            LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mFriendUpdateReceiver);
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
}