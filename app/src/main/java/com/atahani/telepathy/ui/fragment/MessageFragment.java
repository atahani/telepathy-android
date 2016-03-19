package com.atahani.telepathy.ui.fragment;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.atahani.telepathy.model.UserModel;
import com.atahani.telepathy.ui.DashboardActivity;
import com.atahani.telepathy.ui.component.TelepathyBaseActivity;
import com.atahani.telepathy.ui.utility.Constants;
import com.atahani.telepathy.utility.AndroidUtilities;
import com.atahani.telepathy.utility.AppPreferenceTools;

import mobi.atahani.telepathy.R;
import com.atahani.telepathy.TApplication;
import com.atahani.telepathy.ui.AppSettingsActivity;
import com.atahani.telepathy.utility.ThemeUtil;

/**
 * Message fragment
 */
public class MessageFragment extends Fragment {

    private ClassifyMessageFragment mClassifyMessageFragment;
    private UserMessagesFragment mUserMessageFragment;
    private View mVSeparator;
    private ThemeUtil mThemeUtil;
    private AppPreferenceTools mAppPreferenceTools;
    private FrameLayout mLyClassifyMessageSection;
    private FrameLayout mLyUserMessagesSection;
    public boolean mOpenUserMessageFragmentInStateLoss = true;


    public MessageFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAppPreferenceTools = new AppPreferenceTools(TApplication.applicationContext);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             final Bundle savedInstanceState) {
        mClassifyMessageFragment = new ClassifyMessageFragment();
        // Inflate the layout for this fragment
        View fragmentView = inflater.inflate(R.layout.fragment_message, container, false);
        try {
            if (getResources().getBoolean(R.bool.isTablet)) {
                //config the
                mVSeparator = fragmentView.findViewById(R.id.v_separator);
                mLyClassifyMessageSection = (FrameLayout) fragmentView.findViewById(R.id.ly_classify_message_section);
                mLyUserMessagesSection = (FrameLayout) fragmentView.findViewById(R.id.ly_user_messages_section);
                mThemeUtil = new ThemeUtil(getActivity().getBaseContext());
                mVSeparator.setBackgroundColor(mAppPreferenceTools.getAccentColor());
                mClassifyMessageFragment.setClassifyMessageFragmentListener(new ClassifyMessageFragment.ClassifyMessageFragmentListener() {
                    @Override
                    public void onClickUserMessage(UserModel userModel, long numberOfUserMessage) {
                        openUserMessageFragmentInTabletMode(userModel.user_id, userModel.username, userModel.display_name, userModel.image_url, userModel.theme, numberOfUserMessage);
                    }

                    @Override
                    public void onEmptyStateChangeInTabletMode(boolean isEmpty) {
                        if (isEmpty) {
                            //assign match parent to mLyClassifyMessageSection
                            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                            mLyClassifyMessageSection.setLayoutParams(layoutParams);
                            mLyUserMessagesSection.setVisibility(View.GONE);
                            mVSeparator.setVisibility(View.GONE);
                        } else {
                            if (mVSeparator.getVisibility() == View.GONE) {
                                //assign classify_message_width to mLyClassifyMessageSection
                                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(TApplication.applicationContext.getResources().getDimensionPixelOffset(R.dimen.classify_message_width), ViewGroup.LayoutParams.MATCH_PARENT);
                                mLyClassifyMessageSection.setLayoutParams(layoutParams);
                                mLyUserMessagesSection.setVisibility(View.VISIBLE);
                                mVSeparator.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                });
                if (getActivity() != null && isAdded()) {
                    getChildFragmentManager()
                            .beginTransaction()
                            .replace(R.id.ly_classify_message_section, mClassifyMessageFragment, Constants.CLASSIFY_MESSAGE_FRAGMENT_TAG)
                            .commitAllowingStateLoss();
                }
                //check is have save instance check is already open user message fragment
                if (savedInstanceState != null && savedInstanceState.getLong(Constants.NUMBER_OF_USER_MESSAGE) != 0) {
                    Handler handler = new Handler();
                    Runnable newRunnable = new Runnable() {
                        @Override
                        public void run() {
                            if (getActivity() != null && isAdded()) {
                                if (mOpenUserMessageFragmentInStateLoss) {
                                    openUserMessageFragmentInTabletMode(savedInstanceState.getString(Constants.WITH_USER_ID_PARAM)
                                            , savedInstanceState.getString(Constants.WITH_USER_USERNAME_PARAM)
                                            , savedInstanceState.getString(Constants.WITH_USER_DISPLAY_NAME_PARAM)
                                            , savedInstanceState.getString(Constants.WITH_USER_IMAGE_URL_PARAM)
                                            , savedInstanceState.getString(Constants.WITH_USER_THEME_PARAM)
                                            , savedInstanceState.getLong(Constants.NUMBER_OF_USER_MESSAGE));
                                } else {
                                    //reset this field if false in this time
                                    mOpenUserMessageFragmentInStateLoss = true;
                                }

                            }
                        }
                    };
                    handler.postDelayed(newRunnable, 500);
                }
            } else {
                if (getActivity() != null && isAdded()) {
                    //is in mobile mode only added classify message fragment
                    getChildFragmentManager()
                            .beginTransaction()
                            .replace(R.id.ly_main_content, mClassifyMessageFragment, Constants.CLASSIFY_MESSAGE_FRAGMENT_TAG)
                            .commitAllowingStateLoss();
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
            if (isAdded() && getActivity() != null) {
                if (getResources().getBoolean(R.bool.isTablet)) {
                    mLyClassifyMessageSection.getLayoutParams().width = getResources().getDimensionPixelOffset(R.dimen.classify_message_width);
                }
            }
        } catch (Exception ex) {
            AndroidUtilities.processApplicationError(ex, true);
            if (isAdded() && getActivity() != null) {
                Snackbar.make(getActivity().findViewById(android.R.id.content), getString(R.string.re_action_internal_app_error), Snackbar.LENGTH_LONG).show();
            }
        }
    }

    public void closeUserMessageFragmentInTabletMode(boolean shouldUpdateClassifyMessages) {
        try {
            if (getActivity() != null && isAdded()) {
                if (mUserMessageFragment != null) {
                    getChildFragmentManager()
                            .beginTransaction()
                            .remove(mUserMessageFragment)
                            .commitAllowingStateLoss();
                    mClassifyMessageFragment.mAdapter.deSelectItemInTablet();
                    mVSeparator.setBackgroundColor(mAppPreferenceTools.getAccentColor());
                    mUserMessageFragment = null;
                    if (shouldUpdateClassifyMessages) {
                        if (mClassifyMessageFragment != null) {
                            mClassifyMessageFragment.updateAdapterData(mClassifyMessageFragment.getRecentClassifyMessagesFromDB());
                        }
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
     * update messages from DB
     */
    public void updateClassifyMessageFromDB() {
        try {
            if (isAdded() && getActivity() != null) {
                if (mClassifyMessageFragment != null) {
                    mClassifyMessageFragment.updateAdapterData(mClassifyMessageFragment.getRecentClassifyMessagesFromDB());
                    if (mUserMessageFragment != null) {
                        mUserMessageFragment.updateFragmentData(false);
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
     * get classify message from server and update \
     * if user message open in tablet mode also update the user message from server
     */
    public void getClassifyMessageFromServer() {
        try {
            if (getActivity() != null && isAdded()) {
                if (mClassifyMessageFragment != null) {
                    mClassifyMessageFragment.getClassifyMessagesFromServer();
                    if (mUserMessageFragment != null) {
                        mUserMessageFragment.updateFragmentData(true);
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
     * open user message fragment in tablet mode
     */
    public void openUserMessageFragmentInTabletMode(String userId, String username, String displayName, String imageUrl, String theme, long numberOfUserMessage) {
        try {
            if (getActivity() != null && isAdded()) {
                mClassifyMessageFragment.mAdapter.changeSelectedItemInTablet(userId);
                //replace the user messages fragment
                mVSeparator.setBackgroundColor(mThemeUtil.getPrimaryColorByThemeName(theme));
                mUserMessageFragment = UserMessagesFragment.newInstance(userId, username, displayName, imageUrl, theme, numberOfUserMessage);
                mUserMessageFragment.setUserMessageFragmentListener(new UserMessagesFragment.UserMessagesFragmentListener() {
                    @Override
                    public void onCloseFragmentInTabletMode(boolean shouldUpdateClassifyMessages) {
                        closeUserMessageFragmentInTabletMode(shouldUpdateClassifyMessages);
                    }

                    @Override
                    public void onShouldUpdateClassifyMessageFromDB() {
                        updateClassifyMessageFromDB();
                    }
                });
                if (isAdded() && getActivity() != null) {
                    getChildFragmentManager()
                            .beginTransaction()
                            .replace(R.id.ly_user_messages_section, mUserMessageFragment, Constants.USER_MESSAGE_FRAGMENT_TAG)
                            .commitAllowingStateLoss();
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
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.menu_dashboard, menu);
        MenuItem telepathyItem = menu.findItem(R.id.action_telepathy);
        telepathyItem.getIcon().mutate().setColorFilter(null);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            try {
                if (getActivity() != null && isAdded()) {
                    //start app settings activity for RESULT
                    getActivity().startActivityForResult(new Intent(TApplication.applicationContext, AppSettingsActivity.class), Constants.APP_SETTINGS_REQUEST_CODE);
                    ((TelepathyBaseActivity) (getActivity())).setAnimationOnStart();
                }
            } catch (Exception ex) {
                AndroidUtilities.processApplicationError(ex, true);
                if (isAdded() && getActivity() != null) {
                    Snackbar.make(getActivity().findViewById(android.R.id.content), getString(R.string.re_action_internal_app_error), Snackbar.LENGTH_LONG).show();
                }
            }
        } else if (id == R.id.action_telepathy) {
            try {
                if (getActivity() != null && isAdded()) {
                    //navigate to friends tab
                    ((DashboardActivity) (getActivity())).navigateToPage(Constants.FRIENDS_TAB);
                }
            } catch (Exception ex) {
                AndroidUtilities.processApplicationError(ex, true);
                if (isAdded() && getActivity() != null) {
                    Snackbar.make(getActivity().findViewById(android.R.id.content), getString(R.string.re_action_internal_app_error), Snackbar.LENGTH_LONG).show();
                }
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        try {
            if (mUserMessageFragment != null) {
                outState.putString(Constants.WITH_USER_ID_PARAM, mUserMessageFragment.mWithUserId);
                outState.putString(Constants.WITH_USER_DISPLAY_NAME_PARAM, mUserMessageFragment.mWithUserDisplayName);
                outState.putString(Constants.WITH_USER_IMAGE_URL_PARAM, mUserMessageFragment.mWithUserImageUrl);
                outState.putString(Constants.WITH_USER_USERNAME_PARAM, mUserMessageFragment.mWithUserUsername);
                outState.putString(Constants.WITH_USER_THEME_PARAM, mUserMessageFragment.mWithUserTheme);
                outState.putLong(Constants.NUMBER_OF_USER_MESSAGE, mUserMessageFragment.mTotalItem);
            } else {
                outState.putLong(Constants.NUMBER_OF_USER_MESSAGE, 0);
            }
            super.onSaveInstanceState(outState);
        } catch (Exception ex) {
            AndroidUtilities.processApplicationError(ex, true);
            if (isAdded() && getActivity() != null) {
                Snackbar.make(getActivity().findViewById(android.R.id.content), getString(R.string.re_action_internal_app_error), Snackbar.LENGTH_LONG).show();
            }
        }
    }
}