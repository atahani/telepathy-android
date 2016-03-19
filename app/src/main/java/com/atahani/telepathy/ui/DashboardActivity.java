package com.atahani.telepathy.ui;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.appinvite.AppInvite;
import com.google.android.gms.appinvite.AppInviteInvitationResult;
import com.google.android.gms.appinvite.AppInviteReferral;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.atahani.telepathy.adapter.DashboardViewPagerAdapter;
import com.atahani.telepathy.model.UserProfileModel;
import com.atahani.telepathy.network.TService;
import com.atahani.telepathy.ui.component.CommonFeedBack;
import com.atahani.telepathy.ui.fragment.TelepathyFragment;
import com.atahani.telepathy.ui.utility.Constants;
import com.atahani.telepathy.utility.AppPreferenceTools;

import mobi.atahani.telepathy.R;
import com.atahani.telepathy.TApplication;
import com.atahani.telepathy.model.UserModel;
import com.atahani.telepathy.network.gcm.RegistrationIntentService;
import com.atahani.telepathy.realm.NotificationRealmModel;
import com.atahani.telepathy.ui.component.TelepathyBaseActivity;
import com.atahani.telepathy.ui.fragment.FriendFragment;
import com.atahani.telepathy.ui.fragment.MessageFragment;
import com.atahani.telepathy.utility.AndroidUtilities;
import com.atahani.telepathy.utility.ThemeUtil;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class DashboardActivity extends TelepathyBaseActivity {

    private TService mTService;
    private Activity mCurrentActivity;
    private CoordinatorLayout mLyCoordinatorMainLayout;
    private TabLayout mTabDashboard;
    private ViewPager mVpDashboard;
    private DashboardViewPagerAdapter mPagerAdapter;
    private int mSelectedTabColor;
    private int mUnSelectedTabColor;
    private AppPreferenceTools mAppPreferenceTools;
    private BroadcastReceiver mUserProfileBroadCastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        configTheme(false);
        setContentView(R.layout.activity_dashboard);
        //set enable swipe back to false since this is dashboard activity
        setSwipeBackEnable(false);
        mAppPreferenceTools = getAppPreferenceTools();
        mCurrentActivity = this;
        //since this is launcher activity so first check is user authorized
        if (mAppPreferenceTools.isAuthorized() && !mAppPreferenceTools.isInSignUpMode()) {
            //check if device unregistered || application was update so start getGCMInfoServiceIntent to register device on server
            if (!mAppPreferenceTools.isRegisteredDevice() || mAppPreferenceTools.isApplicationUpdate(true)) {
                //retry to start service to get RegistrationIntentService for register device in GCM and got GCM token and send it to server
                Intent getGCMInfoServiceIntent = new Intent(getApplicationContext(), RegistrationIntentService.class);
                startService(getGCMInfoServiceIntent);
            }

            //config default toolbar
            configDefaultToolBar();
            setTitle(getString(R.string.blank));
            mTService = getTService();
            mSelectedTabColor = ContextCompat.getColor(this, R.color.theme_tab_selected_color);
            mUnSelectedTabColor = ContextCompat.getColor(this, R.color.theme_tab_un_selected_color);
            mLyCoordinatorMainLayout = (CoordinatorLayout) findViewById(R.id.ly_coordinator_main);
            mTabDashboard = (TabLayout) findViewById(R.id.tab_dashboard);
            mVpDashboard = (ViewPager) findViewById(R.id.vp_dashboard);
            mPagerAdapter = new DashboardViewPagerAdapter(getSupportFragmentManager(), this);
            mVpDashboard.setAdapter(mPagerAdapter);
            mTabDashboard.setupWithViewPager(mVpDashboard);
            //check is profile loaded for first time if not get it from server
            if (!mAppPreferenceTools.isProfileLoadedForFirstTime()) {
                //get the user profile
                mTService.getUserProfile(new Callback<UserProfileModel>() {
                    @Override
                    public void success(UserProfileModel userProfileModel, Response response) {
                        try {
                            //save the information into pref
                            mAppPreferenceTools.updateUserProfileInformation(userProfileModel);
                            //check is theme changed
                            if (!mAppPreferenceTools.getCurrentThemeName().equals(userProfileModel.theme) || !mAppPreferenceTools.getApplicationLocale().equals(userProfileModel.locale)) {
                                mAppPreferenceTools.setApplicationLocale(userProfileModel.locale);
                                //change the current locale of application
                                TApplication.updateLocale();
                                //change the current theme
                                ThemeUtil themeUtil = new ThemeUtil(TApplication.applicationContext);
                                themeUtil.setCurrentTheme(userProfileModel.theme);
                                //close user message in tablet mode if open already
                                recreateToTakeNewProfileChanges();
                            }
                        } catch (Exception ex) {
                            AndroidUtilities.processApplicationError(ex, true);
                            Snackbar.make(findViewById(android.R.id.content), getString(R.string.re_action_internal_app_error), Snackbar.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        //Do nothing
                    }
                });
            }
            //get drawable of tabs icon
            Drawable messageTabIcon = ContextCompat.getDrawable(this, R.mipmap.ic_telepathy_matched_24dp);
            messageTabIcon.setColorFilter(mUnSelectedTabColor, PorterDuff.Mode.MULTIPLY);
            Drawable telepathyTabIcon = ContextCompat.getDrawable(this, R.mipmap.ic_telepathy_24dp);
            telepathyTabIcon.setColorFilter(mUnSelectedTabColor, PorterDuff.Mode.MULTIPLY);
            Drawable friendTabIcon = ContextCompat.getDrawable(this, R.mipmap.ic_contacts_white_24dp);
            friendTabIcon.setColorFilter(mUnSelectedTabColor, PorterDuff.Mode.MULTIPLY);
            //set these drawable to tabs

            mTabDashboard.getTabAt(Constants.MESSAGES_TAB).setIcon(messageTabIcon);
            mTabDashboard.getTabAt(Constants.TELEPATHY_TAB).setIcon(telepathyTabIcon);
            mTabDashboard.getTabAt(Constants.FRIENDS_TAB).setIcon(friendTabIcon);

            customFontForTabs();
            mTabDashboard.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    try {
                        Drawable selectedTabIconDrawable = tab.getIcon();
                        selectedTabIconDrawable.setColorFilter(mSelectedTabColor, PorterDuff.Mode.MULTIPLY);
                        tab.setIcon(selectedTabIconDrawable);
                        mVpDashboard.setCurrentItem(tab.getPosition());
                        //save the current selected tab inside shared preference
                        mAppPreferenceTools.setCurrentTabInDashboard(tab.getPosition());
                        if (tab.getPosition() == Constants.TELEPATHY_TAB) {
                            //it's mean the selected tab is telepathy so check it
                            TelepathyFragment telepathyFragment = (TelepathyFragment) mPagerAdapter.getRegisteredFragment(Constants.TELEPATHY_TAB);
                            if (telepathyFragment != null) {
                                telepathyFragment.checkWhenTabUnselectedOrSelected();
                            }
                        } else if (tab.getPosition() == Constants.FRIENDS_TAB) {
                            FriendFragment friendFragment = (FriendFragment) mPagerAdapter.getRegisteredFragment(Constants.FRIENDS_TAB);
                            if (friendFragment != null) {
                                friendFragment.checkWhenTabUnselectedOrSelected();
                            }
                        } else if (tab.getPosition() == Constants.MESSAGES_TAB) {
                            MessageFragment messageFragment = (MessageFragment) mPagerAdapter.getRegisteredFragment(Constants.MESSAGES_TAB);
                            if (messageFragment != null) {
                                messageFragment.updateClassifyMessageFromDB();
                                if (mAppPreferenceTools.shouldGetClassifyMessageWhenAppOpen()) {
                                    messageFragment.getClassifyMessageFromServer();
                                }
                            }
                        }
                        customFontForTabs();
                    } catch (Exception ex) {
                        AndroidUtilities.processApplicationError(ex, true);
                        Snackbar.make(findViewById(android.R.id.content), getString(R.string.re_action_internal_app_error), Snackbar.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onTabUnselected(TabLayout.Tab tab) {
                    try {
                        Drawable unSelectedTabIconDrawable = tab.getIcon();
                        unSelectedTabIconDrawable.setColorFilter(mUnSelectedTabColor, PorterDuff.Mode.MULTIPLY);
                        tab.setIcon(unSelectedTabIconDrawable);
                        if (tab.getPosition() == Constants.TELEPATHY_TAB) {
                            //it's mean the un selected tab is telepathy so check it
                            TelepathyFragment telepathyFragment = (TelepathyFragment) mPagerAdapter.getRegisteredFragment(Constants.TELEPATHY_TAB);
                            if (telepathyFragment != null) {
                                telepathyFragment.checkWhenTabUnselectedOrSelected();
                            }
                        } else if (tab.getPosition() == Constants.FRIENDS_TAB) {
                            FriendFragment friendFragment = (FriendFragment) mPagerAdapter.getRegisteredFragment(Constants.FRIENDS_TAB);
                            if (friendFragment != null) {
                                friendFragment.checkWhenTabUnselectedOrSelected();
                            }
                        } else if (tab.getPosition() == Constants.MESSAGES_TAB) {
                            MessageFragment messageFragment = (MessageFragment) mPagerAdapter.getRegisteredFragment(Constants.MESSAGES_TAB);
                            if (messageFragment != null) {
                                messageFragment.updateClassifyMessageFromDB();
                            }
                        }
                        customFontForTabs();
                    } catch (Exception ex) {
                        AndroidUtilities.processApplicationError(ex, true);
                        Snackbar.make(findViewById(android.R.id.content), getString(R.string.re_action_internal_app_error), Snackbar.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onTabReselected(TabLayout.Tab tab) {

                }
            });
            //check is from click on notification
            final Bundle args = getIntent().getExtras();
            if (args != null && args.getInt(Constants.PENDING_INTENT_TYPE, Constants.DO_NOTHING) != Constants.DO_NOTHING && savedInstanceState == null) {
                switch (args.getInt(Constants.PENDING_INTENT_TYPE, Constants.PENDING_DO_NOTHING)) {
                    case Constants.PENDING_OPEN_MESSAGE:
                        try {
                            if (getResources().getBoolean(R.bool.isTablet)) {
                                //navigate to message tab update it and also open user message fragment
                                navigateToPage(Constants.MESSAGES_TAB);
                                Handler handler = new Handler();
                                Runnable newRunnable = new Runnable() {
                                    @Override
                                    public void run() {
                                        MessageFragment messageFragment = (MessageFragment) mPagerAdapter.getRegisteredFragment(Constants.MESSAGES_TAB);
                                        if (messageFragment != null) {
                                            messageFragment.updateClassifyMessageFromDB();
                                            messageFragment.openUserMessageFragmentInTabletMode(args.getString(Constants.WITH_USER_ID_PARAM)
                                                    , args.getString(Constants.WITH_USER_USERNAME_PARAM)
                                                    , args.getString(Constants.WITH_USER_DISPLAY_NAME_PARAM)
                                                    , args.getString(Constants.WITH_USER_IMAGE_URL_PARAM)
                                                    , args.getString(Constants.WITH_USER_THEME_PARAM)
                                                    , args.getLong(Constants.NUMBER_OF_USER_MESSAGE));

                                        }
                                    }
                                };
                                handler.postDelayed(newRunnable, 500);
                            } else {
                                MessageFragment messageFragment = (MessageFragment) mPagerAdapter.getRegisteredFragment(Constants.MESSAGES_TAB);
                                if (messageFragment != null) {
                                    messageFragment.updateClassifyMessageFromDB();
                                }
                                //then open UserMessageActivity
                                Intent userMessagesIntent = new Intent(this, UserMessagesActivity.class);
                                userMessagesIntent.putExtras(args);
                                startActivity(userMessagesIntent);
                                setAnimationOnStart();
                            }
                            break;
                        } catch (Exception ex) {
                            AndroidUtilities.processApplicationError(ex, true);
                            Snackbar.make(findViewById(android.R.id.content), getString(R.string.re_action_internal_app_error), Snackbar.LENGTH_LONG).show();
                            break;
                        }
                    case Constants.PENDING_OPEN_USER_INFO_DIALOG:
                        try {
                            //clear notification with ADD_YOU_AS_FRIEND_PN_TAG tag and this userId
                            if (args.getString(Constants.WITH_USER_ID_PARAM) != null) {
                                NotificationRealmModel notificationRealmModel = getRealm().where(NotificationRealmModel.class).equalTo("tagType", Constants.ADD_YOU_AS_FRIEND_PN_TAG).equalTo("fromUserId", args.getString(Constants.WITH_USER_ID_PARAM)).findFirst();
                                if (notificationRealmModel != null) {
                                    //remove it
                                    getRealm().beginTransaction();
                                    notificationRealmModel.removeFromRealm();
                                    getRealm().commitTransaction();
                                }
                                //start user info activity
                                Intent userInfoIntent = new Intent(mCurrentActivity, UserInfoActivity.class);
                                userInfoIntent.putExtras(args);
                                startActivity(userInfoIntent);
                                setAnimationOnStart();
                            }
                            break;
                        } catch (Exception ex) {
                            AndroidUtilities.processApplicationError(ex, true);
                            Snackbar.make(findViewById(android.R.id.content), getString(R.string.re_action_internal_app_error), Snackbar.LENGTH_LONG).show();
                            break;
                        }
                    case Constants.PENDING_SEND_TELEPATHY_TO_USER:
                        try {
                            //start send telepathy intent
                            Intent telepathyIntent = new Intent(TApplication.applicationContext, SendTelepathyActivity.class);
                            telepathyIntent.putExtras(args);
                            telepathyIntent.putExtra(Constants.IS_FROM_NOTIFICATION_PARAM, true);
                            startActivityForResult(telepathyIntent, Constants.TELEPATHY_REQUEST_CODE);
                            setAnimationOnStart();
                            break;
                        } catch (Exception ex) {
                            AndroidUtilities.processApplicationError(ex, true);
                            Snackbar.make(findViewById(android.R.id.content), getString(R.string.re_action_internal_app_error), Snackbar.LENGTH_LONG).show();
                            break;
                        }
                }
            }
            navigateToPage(mAppPreferenceTools.getCurrentTabInDashboard());
            //set icon color filter for selected tab
            mTabDashboard.getTabAt(mTabDashboard.getSelectedTabPosition()).getIcon().setColorFilter(mSelectedTabColor, PorterDuff.Mode.MULTIPLY);
            //check is from AppInvite
            GoogleApiClient mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(AppInvite.API)
                    .enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
                        @Override
                        public void onConnectionFailed(ConnectionResult connectionResult) {
                            Snackbar.make(mLyCoordinatorMainLayout, getString(R.string.re_action_internal_app_error), Snackbar.LENGTH_SHORT).show();
                        }
                    })
                    .build();

            AppInvite.AppInviteApi.getInvitation(mGoogleApiClient, this, false)
                    .setResultCallback(
                            new ResultCallback<AppInviteInvitationResult>() {
                                @Override
                                public void onResult(final AppInviteInvitationResult result) {
                                    // Because autoLaunchDeepLink = true we don't have to do anything
                                    // here, but we could set that to false and manually choose
                                    // an Activity to launch to handle the deep link here.
                                    //get user information then open dialog
                                    try {
                                        if (AppInviteReferral.hasReferral(result.getInvitationIntent())) {
                                            String[] userGetUrlArray = AppInviteReferral.getDeepLink(result.getInvitationIntent()).split("/");
                                            //get the last part of array
                                            String userId = userGetUrlArray[userGetUrlArray.length - 1];
                                            mTService.findUserWithUserId(userId, new Callback<UserModel>() {
                                                @Override
                                                public void success(UserModel userModel, Response response) {
                                                    try {
                                                        if (mCurrentActivity != null && userModel != null) {
                                                            Intent userInfoIntent = new Intent(mCurrentActivity, UserInfoActivity.class);
                                                            userInfoIntent.putExtra(Constants.WITH_USER_ID_PARAM, userModel.user_id);
                                                            userInfoIntent.putExtra(Constants.WITH_USER_THEME_PARAM, userModel.theme);
                                                            userInfoIntent.putExtra(Constants.WITH_USER_DISPLAY_NAME_PARAM, userModel.display_name);
                                                            userInfoIntent.putExtra(Constants.WITH_USER_IMAGE_URL_PARAM, userModel.image_url);
                                                            userInfoIntent.putExtra(Constants.INVITATION_ID_PARAM, AppInviteReferral.getInvitationId(result.getInvitationIntent()));
                                                            startActivity(userInfoIntent);
                                                            setAnimationOnStart();
                                                        }
                                                    } catch (Exception ex) {
                                                        AndroidUtilities.processApplicationError(ex, true);
                                                        Snackbar.make(findViewById(android.R.id.content), getString(R.string.re_action_internal_app_error), Snackbar.LENGTH_LONG).show();
                                                    }
                                                }

                                                @Override
                                                public void failure(RetrofitError error) {
                                                    if (mCurrentActivity != null) {
                                                        CommonFeedBack commonFeedBack = new CommonFeedBack(mLyCoordinatorMainLayout, mCurrentActivity);
                                                        commonFeedBack.checkCommonErrorAndBackUnCommonOne(error);
                                                    }
                                                }
                                            });
                                        }
                                    } catch (Exception ex) {
                                        AndroidUtilities.processApplicationError(ex, true);
                                        Snackbar.make(findViewById(android.R.id.content), getString(R.string.re_action_internal_app_error), Snackbar.LENGTH_LONG).show();
                                    }
                                }
                            });
        } else {
            //means user not authorized or in signUp mode
            if (mAppPreferenceTools.isInSignUpMode()) {
                startActivity(new Intent(this, CompleteSignUpActivity.class));
                setAnimationOnStart();
                finish();
            } else {
                startActivity(new Intent(this, SignInActivity.class));
                setAnimationOnStart();
                finish();
            }
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        try {
            super.onConfigurationChanged(newConfig);
            customFontForTabs();
        } catch (Exception ex) {
            AndroidUtilities.processApplicationError(ex, true);
            Snackbar.make(findViewById(android.R.id.content), getString(R.string.re_action_internal_app_error), Snackbar.LENGTH_LONG).show();
        }
    }

    /**
     * when user change theme or locale should dashboard activity recreate to take changes
     */
    private void recreateToTakeNewProfileChanges() {
        try {
            //the current language change so should recreate the view
            mAppPreferenceTools.setIsShouldRecreateWhenOtherChangedProfile(false);
            recreate();
        } catch (Exception ex) {
            AndroidUtilities.processApplicationError(ex, true);
            Snackbar.make(findViewById(android.R.id.content), getString(R.string.re_action_internal_app_error), Snackbar.LENGTH_LONG).show();
        }
    }

    private void customFontForTabs() {
        try {
            //apply custom font on TabLayout TextView in FA
            Typeface tf = Typeface.createFromAsset(getAssets(), getString(R.string.default_font));
            ViewGroup vg = (ViewGroup) mTabDashboard.getChildAt(0);
            int tabsCount = vg.getChildCount();
            for (int j = 0; j < tabsCount; j++) {
                ViewGroup vgTab = (ViewGroup) vg.getChildAt(j);
                int tabChildsCount = vgTab.getChildCount();
                for (int i = 0; i < tabChildsCount; i++) {
                    View tabViewChild = vgTab.getChildAt(i);
                    if (tabViewChild instanceof TextView) {
                        ((TextView) tabViewChild).setTypeface(tf);
                    }
                }
            }
        } catch (Exception ex) {
            AndroidUtilities.processApplicationError(ex, true);
            Snackbar.make(findViewById(android.R.id.content), getString(R.string.re_action_internal_app_error), Snackbar.LENGTH_LONG).show();
        }
    }

    /**
     * fix the text of selected tab color
     *
     * @param tabIndex int tab
     */
    private void setSelectedTabTextColor(int tabIndex) {
        try {
            ViewGroup vg = (ViewGroup) mTabDashboard.getChildAt(0);
            int tabsCount = vg.getChildCount();
            for (int j = 0; j < tabsCount; j++) {
                ViewGroup vgTab = (ViewGroup) vg.getChildAt(j);
                int tabChildsCount = vgTab.getChildCount();
                for (int i = 0; i < tabChildsCount; i++) {
                    View tabViewChild = vgTab.getChildAt(i);
                    if (tabViewChild instanceof TextView) {
                        if (tabIndex == j) {
                            ((TextView) tabViewChild).setTextColor(ContextCompat.getColor(TApplication.applicationContext, R.color.theme_tab_selected_color));
                        } else {
                            ((TextView) tabViewChild).setTextColor(ContextCompat.getColor(TApplication.applicationContext, R.color.theme_tab_un_selected_color));
                        }
                    }
                }
            }
        } catch (Exception ex) {
            AndroidUtilities.processApplicationError(ex, true);
            Snackbar.make(findViewById(android.R.id.content), getString(R.string.re_action_internal_app_error), Snackbar.LENGTH_LONG).show();
        }
    }

    /**
     * navigate to page via pageIndex
     *
     * @param pageIndex int pageIndex
     */
    public void navigateToPage(int pageIndex) {
        mVpDashboard.setCurrentItem(pageIndex);
        setSelectedTabTextColor(pageIndex);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if ((requestCode == Constants.TELEPATHY_REQUEST_CODE && resultCode == RESULT_OK) || (requestCode == Constants.USER_MESSAGE_REQUEST && resultCode == Constants.SEND_TELEPATHY_RESULT)) {
            try {
                //show snack bar of telepathy result matched or not
                if (data.getBooleanExtra(Constants.IS_TELEPATHIES_MATCHED_PARAM, false)) {
                    if (getResources().getBoolean(R.bool.isTablet)) {
                        //navigate to message tab update it and also open user message fragment
                        navigateToPage(Constants.MESSAGES_TAB);
                        MessageFragment messageFragment = (MessageFragment) mPagerAdapter.getRegisteredFragment(Constants.MESSAGES_TAB);
                        messageFragment.updateClassifyMessageFromDB();
                        messageFragment.mOpenUserMessageFragmentInStateLoss = false;
                        messageFragment.openUserMessageFragmentInTabletMode(data.getStringExtra(Constants.WITH_USER_ID_PARAM)
                                , data.getStringExtra(Constants.WITH_USER_USERNAME_PARAM)
                                , data.getStringExtra(Constants.WITH_USER_DISPLAY_NAME_PARAM)
                                , data.getStringExtra(Constants.WITH_USER_IMAGE_URL_PARAM)
                                , data.getStringExtra(Constants.WITH_USER_THEME_PARAM)
                                , 0);
                    } else {
                        Intent userMessagesIntent = new Intent(this, UserMessagesActivity.class);
                        userMessagesIntent.putExtras(data.getExtras());
                        startActivity(userMessagesIntent);
                        setAnimationOnStart();
                    }
                } else {
                    //navigate to telepathy tab and show the snack bar
                    navigateToPage(Constants.TELEPATHY_TAB);
                    Snackbar.make(findViewById(android.R.id.content), getString(R.string.re_action_telepathy_successfully_send), Snackbar.LENGTH_LONG).show();
                }
            } catch (Exception ex) {
                AndroidUtilities.processApplicationError(ex, true);
                Snackbar.make(findViewById(android.R.id.content), getString(R.string.re_action_internal_app_error), Snackbar.LENGTH_LONG).show();
            }
        } else if (requestCode == Constants.APP_SETTINGS_REQUEST_CODE && resultCode == RESULT_OK) {
            try {
                //the current language change so should recreate the view
                recreateToTakeNewProfileChanges();
            } catch (Exception ex) {
                AndroidUtilities.processApplicationError(ex, true);
                Snackbar.make(findViewById(android.R.id.content), getString(R.string.re_action_internal_app_error), Snackbar.LENGTH_LONG).show();
            }

        } else if (requestCode == Constants.USER_MESSAGE_REQUEST) {
            try {
                MessageFragment messageFragment = (MessageFragment) mPagerAdapter.getRegisteredFragment(Constants.MESSAGES_TAB);
                if (messageFragment != null) {
                    navigateToPage(Constants.MESSAGES_TAB);
                    messageFragment.updateClassifyMessageFromDB();
                }
            } catch (Exception ex) {
                AndroidUtilities.processApplicationError(ex, true);
                Snackbar.make(findViewById(android.R.id.content), getString(R.string.re_action_internal_app_error), Snackbar.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onStart() {
        try {
            super.onStart();
            if (mAppPreferenceTools.shouldGetClassifyMessageWhenAppOpen()) {
                //get classify message from server
                Handler handler = new Handler();
                Runnable newRunnable = new Runnable() {
                    @Override
                    public void run() {
                        MessageFragment messageFragment = (MessageFragment) mPagerAdapter.getRegisteredFragment(Constants.MESSAGES_TAB);
                        if (messageFragment != null) {
                            messageFragment.getClassifyMessageFromServer();
                        }
                    }
                };
                handler.postDelayed(newRunnable, 500);
            }
        } catch (Exception ex) {
            AndroidUtilities.processApplicationError(ex, true);
            Snackbar.make(findViewById(android.R.id.content), getString(R.string.re_action_internal_app_error), Snackbar.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onResume() {
        try {
            super.onResume();
            //check is theme or locale changed if yes recreate it
            if (mAppPreferenceTools.shouldRecreateWhenOtherChangedProfile()) {
                recreateToTakeNewProfileChanges();
            }
            mUserProfileBroadCastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    int actionToDo = intent.getIntExtra(Constants.ACTION_TO_DO_PARAM, Constants.DO_NOTHING);
                    if (actionToDo == Constants.RECREAT_ACTIVITY_WHEN_LOCALE_OR_THEME_CHANGED) {
                        recreateToTakeNewProfileChanges();
                    }
                }
            };
            LocalBroadcastManager.getInstance(this).registerReceiver((mUserProfileBroadCastReceiver), new IntentFilter(Constants.ACTION_TO_DO_FOR_PROFILE_UPDATE_INTENT_FILTER));
            //check is there error in pref to send it
            AndroidUtilities.checkAndSendAppErrorIfExist();
        } catch (Exception ex) {
            AndroidUtilities.processApplicationError(ex, true);
            Snackbar.make(findViewById(android.R.id.content), getString(R.string.re_action_internal_app_error), Snackbar.LENGTH_LONG).show();
        }
    }


    @Override
    protected void onPause() {
        try {
            mAppPreferenceTools.setCurrentTabInDashboard(mVpDashboard.getCurrentItem());
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mUserProfileBroadCastReceiver);
            super.onPause();
        } catch (Exception ex) {
            AndroidUtilities.processApplicationError(ex, true);
            Snackbar.make(findViewById(android.R.id.content), getString(R.string.re_action_internal_app_error), Snackbar.LENGTH_LONG).show();
        }
    }
}
