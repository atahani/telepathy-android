package com.atahani.telepathy.ui.fragment;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.*;
import android.widget.FrameLayout;

import com.atahani.telepathy.adapter.ClassifyMessageAdapter;
import com.atahani.telepathy.model.ClassifyMessageModel;
import com.atahani.telepathy.model.UserModel;
import com.atahani.telepathy.network.TService;
import com.atahani.telepathy.realm.MessageModelRealm;
import com.atahani.telepathy.realm.TelepathyModelRealm;
import com.atahani.telepathy.realm.UserModelRealm;
import com.atahani.telepathy.ui.component.CommonFeedBack;
import com.atahani.telepathy.ui.component.TelepathyBaseActivity;
import com.atahani.telepathy.ui.utility.Constants;
import com.atahani.telepathy.utility.AndroidUtilities;
import com.atahani.telepathy.utility.AppPreferenceTools;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;
import mobi.atahani.telepathy.R;
import com.atahani.telepathy.TApplication;
import com.atahani.telepathy.model.MessageModel;
import com.atahani.telepathy.ui.UserMessagesActivity;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

import java.util.ArrayList;
import java.util.List;

/**
 * Classify Message Fragment
 * load the all of the user that you have message with it
 */
public class ClassifyMessageFragment extends Fragment {

    private Realm mRealmDB;
    private TService mTService;
    private FrameLayout mLyEmptyStateContent;
    public ClassifyMessageAdapter mAdapter;
    public SwipeRefreshLayout mSwipeRefreshLayout;
    public boolean mIsInQueryMode = false;
    public ClassifyMessageFragmentListener mClassifyMessageFragmentListener;
    private AppPreferenceTools mAppPreferenceTools;
    private BroadcastReceiver mMessageUpdateReceiver;

    public ClassifyMessageFragment() {
        // Required empty public constructor
    }

    public void setClassifyMessageFragmentListener(ClassifyMessageFragmentListener classifyMessageFragmentListener) {
        this.mClassifyMessageFragmentListener = classifyMessageFragmentListener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View fragmentView = inflater.inflate(R.layout.fragment_classify_message, container, false);
        try {
            mRealmDB = ((TelepathyBaseActivity) getActivity()).getRealm();
            mTService = ((TelepathyBaseActivity) getActivity()).getTService();
            mAppPreferenceTools = ((TelepathyBaseActivity) getActivity()).getAppPreferenceTools();
            mLyEmptyStateContent = (FrameLayout) fragmentView.findViewById(R.id.ly_empty_state_content);
            mSwipeRefreshLayout = (SwipeRefreshLayout) fragmentView.findViewById(R.id.swipe_refresh_layout);
            mSwipeRefreshLayout.setColorSchemeColors(mAppPreferenceTools.getAccentColor(), mAppPreferenceTools.getPrimaryColor(), mAppPreferenceTools.getPrimaryDarkColor());
            RecyclerView mRecyclerView = (RecyclerView) fragmentView.findViewById(R.id.ry_classify_messages);
            mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    if (!mIsInQueryMode) {
                        getClassifyMessagesFromServer();
                    } else {
                        mSwipeRefreshLayout.setRefreshing(false);
                    }
                }
            });
            //get the classify messages from DB
            mAdapter = new ClassifyMessageAdapter(inflater, getRecentClassifyMessagesFromDB(), new ClassifyMessageAdapter.ClassifyEventListener() {
                @Override
                public void onClickClassifyMessage(UserModel userModel, long numberOfUserMessage) {
                    if (getResources().getBoolean(R.bool.isTablet)) {
                        //in tablet mode
                        if (mClassifyMessageFragmentListener != null) {
                            mClassifyMessageFragmentListener.onClickUserMessage(userModel, numberOfUserMessage);
                            mAdapter.changeSelectedItemInTablet(userModel.user_id);
                        }
                    } else {
                        //start UserMessagesActivity
                        Intent userMessagesIntent = new Intent(TApplication.applicationContext, UserMessagesActivity.class);
                        userMessagesIntent.putExtra(Constants.WITH_USER_ID_PARAM, userModel.user_id);
                        userMessagesIntent.putExtra(Constants.WITH_USER_DISPLAY_NAME_PARAM, userModel.display_name);
                        userMessagesIntent.putExtra(Constants.WITH_USER_IMAGE_URL_PARAM, userModel.image_url);
                        userMessagesIntent.putExtra(Constants.WITH_USER_USERNAME_PARAM, userModel.username);
                        userMessagesIntent.putExtra(Constants.WITH_USER_THEME_PARAM, userModel.theme);
                        userMessagesIntent.putExtra(Constants.NUMBER_OF_USER_MESSAGE, numberOfUserMessage);
                        getActivity().startActivityForResult(userMessagesIntent, Constants.USER_MESSAGE_REQUEST);
                        ((TelepathyBaseActivity) getActivity()).setAnimationOnStart();
                    }
                }
            });
            mRecyclerView.setLayoutManager(new LinearLayoutManager(TApplication.applicationContext));
            mRecyclerView.setAdapter(mAdapter);
            mAdapter.setRealmDB(mRealmDB);
            if (mAdapter.getItemCount() == 0) {
                changeEmptyState(Constants.DO_NOT_HAVE_CLASSIFY_MESSAGE_EMPTY_STATE);
                if (getResources().getBoolean(R.bool.isTablet) && mClassifyMessageFragmentListener != null) {
                    mClassifyMessageFragmentListener.onEmptyStateChangeInTabletMode(true);
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

    /**
     * get classify messages from server
     */
    public void getClassifyMessagesFromServer() {
        if (!mRealmDB.isClosed() && isAdded()) {
            try {
                mSwipeRefreshLayout.setRefreshing(true);
                mTService.getClassifyMessages(new Callback<List<ClassifyMessageModel>>() {
                    @Override
                    public void success(final List<ClassifyMessageModel> classifyMessageModels, Response response) {
                        //check and store message if not there
                        if (!mRealmDB.isClosed() && getActivity() != null && isAdded()) {
                            mRealmDB.executeTransaction(new Realm.Transaction() {
                                @Override
                                public void execute(Realm realm) {
                                    for (int i = 0; i < classifyMessageModels.size(); i++) {
                                        MessageModelRealm messageModelRealm = realm.where(MessageModelRealm.class).equalTo("messageId", classifyMessageModels.get(i).last_message.id).findFirst();
                                        if (messageModelRealm == null) {
                                            //so add it to DB
                                            MessageModelRealm newMessageModelRealm = realm.createObject(MessageModelRealm.class);
                                            newMessageModelRealm.setMessageId(classifyMessageModels.get(i).last_message.id);
                                            newMessageModelRealm.setWithUserId(classifyMessageModels.get(i).last_message.with_user_id);
                                            newMessageModelRealm.setBodySend(classifyMessageModels.get(i).last_message.body_send);
                                            newMessageModelRealm.setBodyReceive(classifyMessageModels.get(i).last_message.body_receive);
                                            newMessageModelRealm.setYouAreFirst(classifyMessageModels.get(i).last_message.you_are_first);
                                            newMessageModelRealm.setMatchedAt(classifyMessageModels.get(i).last_message.matched_at);
                                            newMessageModelRealm.setMatchedWithTelepathyId(classifyMessageModels.get(i).last_message.matched_with_telepathy_id);
                                            newMessageModelRealm.setMatchedInSec(classifyMessageModels.get(i).last_message.matched_in_sec);
                                            newMessageModelRealm.setIsReceive(classifyMessageModels.get(i).last_message.is_receive);
                                            newMessageModelRealm.setIsRead(classifyMessageModels.get(i).last_message.is_read);
                                            newMessageModelRealm.setIsSendReadSignal(classifyMessageModels.get(i).last_message.is_send_read_signal);
                                            newMessageModelRealm.setUpdatedAt(classifyMessageModels.get(i).last_message.updated_at);
                                            //check if have any telepathy with this matched_telepathy_id remove it
                                            TelepathyModelRealm matchedTelepathy = realm.where(TelepathyModelRealm.class).equalTo("telepathyId", classifyMessageModels.get(i).last_message.matched_with_telepathy_id).findFirst();
                                            if (matchedTelepathy != null) {
                                                matchedTelepathy.removeFromRealm();
                                            }
                                        } else {
                                            //update the message information inside DB
                                            messageModelRealm.setIsReceive(classifyMessageModels.get(i).last_message.is_receive);
                                            messageModelRealm.setIsRead(classifyMessageModels.get(i).last_message.is_read);
                                            messageModelRealm.setIsSendReadSignal(classifyMessageModels.get(i).last_message.is_send_read_signal);
                                            messageModelRealm.setUpdatedAt(classifyMessageModels.get(i).last_message.updated_at);
                                        }
                                        if (classifyMessageModels.get(i).with_user != null) {
                                            //check is user information in DB
                                            UserModelRealm userModelRealm = realm.where(UserModelRealm.class).equalTo("userId", classifyMessageModels.get(i).with_user.user_id).findFirst();
                                            if (userModelRealm == null) {
                                                //add new user information in DB
                                                UserModelRealm newUserModelRealm = realm.createObject(UserModelRealm.class);
                                                newUserModelRealm.setUserId(classifyMessageModels.get(i).with_user.user_id);
                                                newUserModelRealm.setUsername(classifyMessageModels.get(i).with_user.username);
                                                newUserModelRealm.setDisplayName(classifyMessageModels.get(i).with_user.display_name);
                                                newUserModelRealm.setImageUrl(classifyMessageModels.get(i).with_user.image_url);
                                                newUserModelRealm.setTheme(classifyMessageModels.get(i).with_user.theme);
                                                newUserModelRealm.setNumberOfUserMessageFromServer(classifyMessageModels.get(i).message_count);
                                                newUserModelRealm.setIsFriend(false);
                                            } else {
                                                //update the user information
                                                userModelRealm.setUsername(classifyMessageModels.get(i).with_user.username);
                                                userModelRealm.setDisplayName(classifyMessageModels.get(i).with_user.display_name);
                                                userModelRealm.setImageUrl(classifyMessageModels.get(i).with_user.image_url);
                                                userModelRealm.setTheme(classifyMessageModels.get(i).with_user.theme);
                                                userModelRealm.setNumberOfUserMessageFromServer(classifyMessageModels.get(i).message_count);
                                            }
                                        } else {
                                            //it's mean this user account deleted and should un friend and set username as null
                                            UserModelRealm deletedAccountUserRealmModel = realm.where(UserModelRealm.class).equalTo("userId", classifyMessageModels.get(i).last_message.with_user_id).findFirst();
                                            if (deletedAccountUserRealmModel != null) {
                                                deletedAccountUserRealmModel.setIsFriend(false);
                                                deletedAccountUserRealmModel.setTheme(Constants.DEFAULT_THEME_NAME);
                                                deletedAccountUserRealmModel.setUsername(Constants.DELETED_ACCOUNT_VALUE);
                                                deletedAccountUserRealmModel.setImageUrl(Constants.DELETED_ACCOUNT_VALUE);
                                                deletedAccountUserRealmModel.setDisplayName(Constants.DELETED_ACCOUNT_VALUE);
                                                deletedAccountUserRealmModel.setNumberOfUserMessageFromServer(classifyMessageModels.get(i).message_count);
                                            } else {
                                                //create new deleted account user realm model
                                                UserModelRealm newDeletedUserRealmModel = realm.createObject(UserModelRealm.class);
                                                newDeletedUserRealmModel.setUserId(classifyMessageModels.get(i).last_message.with_user_id);
                                                newDeletedUserRealmModel.setUsername(Constants.DELETED_ACCOUNT_VALUE);
                                                newDeletedUserRealmModel.setDisplayName(Constants.DELETED_ACCOUNT_VALUE);
                                                newDeletedUserRealmModel.setImageUrl(Constants.DELETED_ACCOUNT_VALUE);
                                                newDeletedUserRealmModel.setTheme(Constants.DEFAULT_THEME_NAME);
                                                newDeletedUserRealmModel.setNumberOfUserMessageFromServer(classifyMessageModels.get(i).message_count);
                                                newDeletedUserRealmModel.setIsFriend(false);
                                            }
                                            //also should update the telepathies with this user
                                            RealmResults<TelepathyModelRealm> telepathiesWithThisUser = realm.where(TelepathyModelRealm.class).equalTo("withUserId", classifyMessageModels.get(i).last_message.with_user_id).findAll();
                                            for (int j = 0; j < telepathiesWithThisUser.size(); j++) {
                                                telepathiesWithThisUser.get(j).setWithUserUsername(Constants.DELETED_ACCOUNT_VALUE);
                                                telepathiesWithThisUser.get(j).setWithUserDisplayName(Constants.DELETED_ACCOUNT_VALUE);
                                                telepathiesWithThisUser.get(j).setWithUserImageUrl(Constants.DELETED_ACCOUNT_VALUE);
                                                telepathiesWithThisUser.get(j).setWithUserTheme(Constants.DEFAULT_THEME_NAME);
                                            }
                                        }
                                    }
                                }
                            }, new Realm.Transaction.Callback() {
                                @Override
                                public void onSuccess() {
                                    super.onSuccess();
                                    if (getActivity() != null && isAdded()) {
                                        mAppPreferenceTools.setShouldGetClassifyMessageWhenAppOpen(false);
                                        updateAdapterData(getRecentClassifyMessagesFromDB());
                                        mSwipeRefreshLayout.setRefreshing(false);
                                    }
                                }

                                @Override
                                public void onError(Exception e) {
                                    super.onError(e);
                                    if (getActivity() != null && isAdded()) {
                                        updateAdapterData(getRecentClassifyMessagesFromDB());
                                        mSwipeRefreshLayout.setRefreshing(false);
                                    }
                                }
                            });
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        if (getActivity() != null) {
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
    }


    /**
     * get recent classify message from DB group by users
     *
     * @return List of MessageModel
     */
    public List<ClassifyMessageModel> getRecentClassifyMessagesFromDB() {
        List<ClassifyMessageModel> result = new ArrayList<>();
        if (!mRealmDB.isClosed() && isAdded()) {
            try {
                ArrayList<String> userIds = new ArrayList<>();
                RealmResults<MessageModelRealm> resultFromDB = mRealmDB.where(MessageModelRealm.class).findAllSorted("matchedAt", Sort.DESCENDING);
                for (MessageModelRealm modelRealm : resultFromDB) {
                    if (userIds.indexOf(modelRealm.getWithUserId()) == -1) {
                        //get user information from DB
                        UserModelRealm userModelRealm = mRealmDB.where(UserModelRealm.class).equalTo("userId", modelRealm.getWithUserId()).findFirst();
                        userIds.add(modelRealm.getWithUserId());
                        ClassifyMessageModel classifyMessageModel = new ClassifyMessageModel();
                        if (userModelRealm != null) {
                            //create userModel and assign it
                            UserModel userModel = new UserModel();
                            userModel.user_id = userModelRealm.getUserId();
                            userModel.username = userModelRealm.getUsername();
                            userModel.display_name = userModelRealm.getDisplayName();
                            userModel.image_url = userModelRealm.getImageUrl();
                            userModel.theme = userModelRealm.getTheme();
                            classifyMessageModel.with_user = userModel;
                            //create messageModel and assign it
                            MessageModel messageModel = new MessageModel();
                            messageModel.with_user_id = modelRealm.getWithUserId();
                            messageModel.is_receive = modelRealm.isReceive();
                            messageModel.is_read = modelRealm.isRead();
                            messageModel.is_send_read_signal = modelRealm.isSendReadSignal();
                            messageModel.you_are_first = modelRealm.isYouAreFirst();
                            messageModel.body_receive = modelRealm.getBodyReceive();
                            messageModel.body_send = modelRealm.getBodySend();
                            messageModel.id = modelRealm.getMessageId();
                            messageModel.matched_at = modelRealm.getMatchedAt();
                            messageModel.matched_in_sec = modelRealm.getMatchedInSec();
                            messageModel.matched_with_telepathy_id = modelRealm.getMatchedWithTelepathyId();
                            messageModel.updated_at = modelRealm.getUpdatedAt();
                            classifyMessageModel.last_message = messageModel;
                            //assign number of message in DB
                            classifyMessageModel.message_count = userModelRealm.getNumberOfUserMessageFromServer();
                            //added to list
                            result.add(classifyMessageModel);
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
        return result;
    }

    /**
     * update adapter data
     */
    public void updateAdapterData(List<ClassifyMessageModel> data) {
        if (isAdded() && getActivity() != null) {
            try {
                mAdapter.updateDate(data);
                //now should check empty state
                if (data.size() == 0) {
                    if (mIsInQueryMode) {
                        changeEmptyState(Constants.NOT_FOUND_EMPTY_STATE);
                    } else {
                        changeEmptyState(Constants.DO_NOT_HAVE_CLASSIFY_MESSAGE_EMPTY_STATE);
                    }
                } else {
                    changeEmptyState(Constants.DISAPPEAR_EMPTY_STATE);
                }
            } catch (Exception ex) {
                AndroidUtilities.processApplicationError(ex, true);
                if (isAdded() && getActivity() != null) {
                    Snackbar.make(getActivity().findViewById(android.R.id.content), getString(R.string.re_action_internal_app_error), Snackbar.LENGTH_LONG).show();
                }
            }
        }
    }

    /**
     * change empty sate
     *
     * @param stateType int type of state
     */
    public void changeEmptyState(int stateType) {
        if (isAdded() && getActivity() != null) {
            try {
                if (stateType == Constants.DO_NOT_HAVE_CLASSIFY_MESSAGE_EMPTY_STATE) {
                    //load empty fragment into this side
                    EmptyStateFragment emptyStateFragment = EmptyStateFragment.newInstance(Constants.DO_NOT_HAVE_CLASSIFY_MESSAGE_EMPTY_STATE);
                    //add this fragment to view
                    getChildFragmentManager().beginTransaction().replace(R.id.ly_empty_state_content, emptyStateFragment).commitAllowingStateLoss();
                    mLyEmptyStateContent.setVisibility(View.VISIBLE);
                    if (TApplication.applicationContext.getResources().getBoolean(R.bool.isTablet) && mClassifyMessageFragmentListener != null) {
                        mClassifyMessageFragmentListener.onEmptyStateChangeInTabletMode(true);
                    }

                } else {
                    if (TApplication.applicationContext.getResources().getBoolean(R.bool.isTablet) && mClassifyMessageFragmentListener != null) {
                        mClassifyMessageFragmentListener.onEmptyStateChangeInTabletMode(false);
                    }
                    mLyEmptyStateContent.setVisibility(View.GONE);
                }
            } catch (Exception ex) {
                AndroidUtilities.processApplicationError(ex, true);
                if (isAdded() && getActivity() != null) {
                    Snackbar.make(getActivity().findViewById(android.R.id.content), getString(R.string.re_action_internal_app_error), Snackbar.LENGTH_LONG).show();
                }
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
                            updateAdapterData(getRecentClassifyMessagesFromDB());
                        } else if (actionToDo == Constants.UPDATE_FROM_NET) {
                            getClassifyMessagesFromServer();
                        }
                    }
                }
            };
            LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mMessageUpdateReceiver, new IntentFilter(Constants.ACTION_TO_DO_FOR_MESSAGE_UPDATE_INTENT_FILTER));
            LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mMessageUpdateReceiver, new IntentFilter(Constants.ACTION_TO_DO_FOR_CLASSIFY_MESSAGE_UPDATE_INTENT_FILTER));
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
        } catch (Exception ex) {
            AndroidUtilities.processApplicationError(ex, true);
            if (isAdded() && getActivity() != null) {
                Snackbar.make(getActivity().findViewById(android.R.id.content), getString(R.string.re_action_internal_app_error), Snackbar.LENGTH_LONG).show();
            }
        }
        super.onStop();
    }

    public interface ClassifyMessageFragmentListener {
        void onClickUserMessage(UserModel userModel, long numberOfUserMessage);

        void onEmptyStateChangeInTabletMode(boolean isEmpty);
    }
}
