package com.atahani.telepathy.ui;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.squareup.picasso.Picasso;
import com.atahani.telepathy.TApplication;
import com.atahani.telepathy.adapter.AppSettingsAdapter;
import com.atahani.telepathy.model.AppSettingItemModel;
import com.atahani.telepathy.model.ChooseItemModel;
import com.atahani.telepathy.model.UpdateProfileRequestModel;
import com.atahani.telepathy.model.UserProfileModel;
import com.atahani.telepathy.network.TService;
import com.atahani.telepathy.ui.component.CommonFeedBack;
import com.atahani.telepathy.ui.component.TelepathyBaseActivity;
import com.atahani.telepathy.ui.fragment.AboutDialogFragment;
import com.atahani.telepathy.ui.fragment.RecyclerViewChooseDialogFragment;
import com.atahani.telepathy.ui.utility.Constants;
import com.atahani.telepathy.ui.utility.CropCircleTransformation;
import com.atahani.telepathy.utility.AndroidUtilities;
import com.atahani.telepathy.utility.AppPreferenceTools;
import com.atahani.telepathy.utility.Crop;
import com.atahani.telepathy.utility.ThemeUtil;

import mobi.atahani.telepathy.R;

import com.atahani.telepathy.model.TOperationResultModel;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.mime.TypedFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AppSettingsActivity extends TelepathyBaseActivity {

    private TService mTService;
    private ImageView mImImageProfile;
    private AppSettingsAdapter mAppSettingsAdapter;
    private String mCurrentImagePath;
    private Activity mCurrentActivity;
    private AppPreferenceTools mAppPreferenceTools;
    private ProgressDialog mProgressDialog;
    private BroadcastReceiver mUserProfileBroadCastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        configTheme(true);
        setContentView(R.layout.activity_app_settings);
        //config toolbar
        Toolbar customToolbar = (Toolbar) findViewById(R.id.custom_toolbar);
        setSupportActionBar(customToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        if (getResources().getBoolean(R.bool.isTablet)) {
            setSwipeBackEnable(false);
        }
        mImImageProfile = (ImageView) findViewById(R.id.im_image_profile);
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setCanceledOnTouchOutside(false);
        mTService = getTService();
        mAppPreferenceTools = getAppPreferenceTools();
        //set primary theme color to collapsingToolbarSettings
        CollapsingToolbarLayout collapsingToolbarSettings = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar_settings);
        collapsingToolbarSettings.setContentScrimColor(mAppPreferenceTools.getPrimaryColor());
        //set primary theme color to profile cover layout
        RelativeLayout lyProfileCoverLayout = (RelativeLayout) findViewById(R.id.ly_profile_cover);
        lyProfileCoverLayout.setBackgroundColor(mAppPreferenceTools.getPrimaryColor());
        //load user image with picasso
        Picasso.with(this).load(mAppPreferenceTools.getImageUrl())
                .placeholder(R.drawable.image_place_holder)
                .transform(new CropCircleTransformation()).into(mImImageProfile);
        //config settings adapter and recycler view
        RecyclerView mRySettingsItem = (RecyclerView) findViewById(R.id.ry_settings_item);
        mAppSettingsAdapter = new AppSettingsAdapter(this, getSettingList());
        mAppSettingsAdapter.setSettingsItemClickListener(new AppSettingsAdapter.SettingsItemClickListener() {
            @Override
            public void onLogOut() {
                try {
                    //first open alert dialog to confirm the log out form application
                    AlertDialog.Builder builder = new AlertDialog.Builder(mCurrentActivity);
                    builder.setTitle(getString(R.string.label_log_out));
                    builder.setMessage(getString(R.string.label_log_out_description));
                    builder.setNegativeButton(getString(R.string.action_no), null);
                    builder.setPositiveButton(getString(R.string.action_yes), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //exit from application
                            mProgressDialog.setMessage(getString(R.string.re_action_on_logging_out));
                            mProgressDialog.show();
                            mTService.terminateApp(mAppPreferenceTools.getAppId(), new Callback<TOperationResultModel>() {
                                @Override
                                public void success(TOperationResultModel tOperationResultModel, Response response) {
                                    try {
                                        //have one successful response that mean can terminated
                                        //broad cast to close all of the realm  instance
                                        Intent intentToCloseRealm = new Intent(Constants.TELEPATHY_BASE_ACTIVITY_INTENT_FILTER);
                                        intentToCloseRealm.putExtra(Constants.ACTION_TO_DO_PARAM, Constants.CLOSE_REALM_DB);
                                        LocalBroadcastManager.getInstance(mCurrentActivity).sendBroadcastSync(intentToCloseRealm);
                                        mAppPreferenceTools.removeAllOfThePref();
//                                //change locale to system default
//                                 for sign out google first build GoogleAPIClient
                                        final GoogleApiClient googleApiClient = new GoogleApiClient.Builder(TApplication.applicationContext)
                                                .addApi(Auth.GOOGLE_SIGN_IN_API)
                                                .build();
                                        googleApiClient.connect();
                                        googleApiClient.registerConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                                            @Override
                                            public void onConnected(Bundle bundle) {
                                                Auth.GoogleSignInApi.signOut(googleApiClient).setResultCallback(new ResultCallback<Status>() {
                                                    @Override
                                                    public void onResult(Status status) {
                                                        Intent intent = new Intent(TApplication.applicationContext, SignInActivity.class);
                                                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                                        startActivity(intent);
                                                        setAnimationOnStart();
                                                        finish();
                                                        mProgressDialog.dismiss();
                                                    }
                                                });
                                            }

                                            @Override
                                            public void onConnectionSuspended(int i) {
                                                //do nothing
                                            }
                                        });
                                    } catch (Exception ex) {
                                        if (mProgressDialog != null && mProgressDialog.isShowing()) {
                                            mProgressDialog.dismiss();
                                        }
                                        AndroidUtilities.processApplicationError(ex, true);
                                        Snackbar.make(findViewById(android.R.id.content), getString(R.string.re_action_internal_app_error), Snackbar.LENGTH_LONG).show();
                                    }

                                }

                                @Override
                                public void failure(RetrofitError error) {
                                    if (mCurrentActivity != null) {
                                        mProgressDialog.dismiss();
                                        //handle error, since have no un_common error
                                        CommonFeedBack commonFeedBack = new CommonFeedBack(findViewById(android.R.id.content), mCurrentActivity);
                                        commonFeedBack.checkCommonErrorAndBackUnCommonOne(error);
                                    }
                                }
                            });
                        }
                    });
                    builder.show();
                } catch (Exception ex) {
                    if (mProgressDialog != null && mProgressDialog.isShowing()) {
                        mProgressDialog.dismiss();
                    }
                    AndroidUtilities.processApplicationError(ex, true);
                    Snackbar.make(findViewById(android.R.id.content), getString(R.string.re_action_internal_app_error), Snackbar.LENGTH_LONG).show();
                }
            }

            @Override
            public void openActivity(Class targetActivityClass) {
                try {
                    Intent childIntent = new Intent(mCurrentActivity, targetActivityClass);
                    startActivityForResult(childIntent, Constants.START_ACTIVITY_FOR_SETTING_RESULT_MESSAGE);
                    setAnimationOnStart();
                } catch (Exception ex) {
                    AndroidUtilities.processApplicationError(ex, true);
                    Snackbar.make(findViewById(android.R.id.content), getString(R.string.re_action_internal_app_error), Snackbar.LENGTH_LONG).show();
                }
            }

            @Override
            public void onOpenDialog(AppSettingItemModel selectedItem) {
                try {
                    if (selectedItem.getDialogSettingType() == AppSettingItemModel.DIALOG_EDIT_LOCALE) {
                        RecyclerViewChooseDialogFragment chooseLocaleFragment = RecyclerViewChooseDialogFragment.newInstance(Constants.CHANGE_LOCALE_TYPE);
                        chooseLocaleFragment.setChangePhotoChooseListener(new RecyclerViewChooseDialogFragment.RecyclerViewChooseListener() {
                            @Override
                            public void onChooseItem(int actionTypeId) {
                                //DO NOTHING
                            }

                            @Override
                            public void onChooseValueCode(String valueCode) {
                                //check is the valueCode different from current locale send request to server
                                if (!valueCode.equals(mAppPreferenceTools.getApplicationLocale())) {
                                    updateUserProfile(valueCode, null);
                                }
                            }

                            @Override
                            public void onChooseThemeColor(String theme) {
                                //DO NOTHING
                            }
                        });
                        chooseLocaleFragment.show(getFragmentManager(), Constants.RECYCLER_VIEW_CHOOSE_DIALOG_TAG);
                    } else if (selectedItem.getDialogSettingType() == AppSettingItemModel.DIALOG_SET_NOTIFICATION_SOUND) {
                        Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
                        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION);
                        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);
                        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, true);
                        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI, Settings.System.DEFAULT_NOTIFICATION_URI);
                        String existingValue = mAppPreferenceTools.getNotificationRingtoneUri();
                        if (existingValue.length() == 0) {
                            // Select "Silent"
                            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, (Uri) null);
                        } else {
                            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, Uri.parse(existingValue));
                        }
                        //start activity for result
                        startActivityForResult(intent, Constants.REQUEST_CODE_ALERT_RINGTONE);
                        setAnimationOnStart();
                    } else if (selectedItem.getDialogSettingType() == AppSettingItemModel.DIALOG_EDIT_THEME) {
                        //open dialog to choose theme color
                        RecyclerViewChooseDialogFragment chooseThemeDialogFragment = RecyclerViewChooseDialogFragment.newInstance(Constants.CHANGE_THEME_TYPE);
                        chooseThemeDialogFragment.setChangePhotoChooseListener(new RecyclerViewChooseDialogFragment.RecyclerViewChooseListener() {
                            @Override
                            public void onChooseItem(int actionTypeId) {
                                //DO NOTHING
                            }

                            @Override
                            public void onChooseValueCode(String valueCode) {
                                //DO NOTHING
                            }

                            @Override
                            public void onChooseThemeColor(String theme) {
                                //change the theme
                                updateUserProfile(null, theme);
                            }
                        });
                        chooseThemeDialogFragment.show(getFragmentManager(), Constants.RECYCLER_VIEW_CHOOSE_DIALOG_TAG);
                    } else if (selectedItem.getDialogSettingType() == AppSettingItemModel.DIALOG_ABOUT) {
                        //open about dialog
                        AboutDialogFragment fragment = new AboutDialogFragment();
                        fragment.show(getFragmentManager(), Constants.ABOUT_DIALOG_FRAGMENT_TAG);
                    }
                } catch (Exception ex) {
                    AndroidUtilities.processApplicationError(ex, true);
                    Snackbar.make(findViewById(android.R.id.content), getString(R.string.re_action_internal_app_error), Snackbar.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCheckItem(int checkBoxType, boolean isChecked) {
                try {
                    if (checkBoxType == AppSettingItemModel.CHECKBOX_VIBRATE) {
                        mAppPreferenceTools.setVibrateOnNotificationReceive(isChecked);
                    } else if (checkBoxType == AppSettingItemModel.CHECKBOX_NOTIFICATION_WHEN_TELEPATHIES_MATCHED) {
                        mAppPreferenceTools.setIsNotificationEnableWhenTelepathiesMatched(isChecked);
                    } else if (checkBoxType == AppSettingItemModel.CHECKBOX_NOTIFICATION_WHEN_ADD_YOU_AS_FRIEND) {
                        mAppPreferenceTools.setIsNotificationEnableWhenAddYouAsFriend(isChecked);
                    }
                } catch (Exception ex) {
                    AndroidUtilities.processApplicationError(ex, true);
                    Snackbar.make(findViewById(android.R.id.content), getString(R.string.re_action_internal_app_error), Snackbar.LENGTH_LONG).show();
                }
            }
        });
        mRySettingsItem.setLayoutManager(new LinearLayoutManager(TApplication.applicationContext));
        mRySettingsItem.setHasFixedSize(true);
        mRySettingsItem.setAdapter(mAppSettingsAdapter);
        mCurrentActivity = this;
        FloatingActionButton mFabEditImage = (FloatingActionButton) findViewById(R.id.fab_edit_image);
        mFabEditImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //open RecyclerViewChooseDialog Fragment
                RecyclerViewChooseDialogFragment fragment = RecyclerViewChooseDialogFragment.newInstance(Constants.CHANGE_PHOTO_CHOOSE_LIST_TYPE);
                fragment.setIsHaveImage(!mAppPreferenceTools.getImageUrl().contains(Constants.DEFAULT_IMAGE_PROFILE_NAME));
                fragment.setChangePhotoChooseListener(new RecyclerViewChooseDialogFragment.RecyclerViewChooseListener() {
                    @Override
                    public void onChooseItem(int actionTypeId) {
                        if (actionTypeId == ChooseItemModel.ACTION_TAKE_PHOTO) {
                            try {
                                //create image capture intent and start activity
                                //before start camera intent check storage permission
                                if (checkRunTimePermissionIsGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                                    mCurrentImagePath = startCameraIntent();
                                } else {
                                    //request write external permission for open camera intent
                                    requestRunTimePermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, Constants.FOR_TAKE_PHOTO_REQUEST_WRITE_EXTERNAL_STORAGE_PER, new PermissionEventListener() {
                                        @Override
                                        public void onGranted(int requestCode, String[] permissions) {
                                            //now start camera intent
                                            mCurrentImagePath = startCameraIntent();
                                        }

                                        @Override
                                        public void onFailure(int requestCode, String[] permissions) {
                                            Snackbar.make(findViewById(android.R.id.content), getString(R.string.re_action_on_deny_external_storage_per_for_take_photo), Snackbar.LENGTH_LONG)
                                                    .setAction(getString(R.string.action_manage), new View.OnClickListener() {
                                                        @Override
                                                        public void onClick(View v) {
                                                            startAppSettingsIntent();
                                                        }
                                                    }).show();
                                        }
                                    });
                                }
                            } catch (Exception ex) {
                                AndroidUtilities.processApplicationError(ex, true);
                                Snackbar.make(findViewById(android.R.id.content), getString(R.string.re_action_internal_app_error), Snackbar.LENGTH_LONG).show();
                            }
                        } else if (actionTypeId == ChooseItemModel.ACTION_CHOOSE_FROM_GALLERY) {
                            try {
                                //open gallery to select single picture as image profile
                                //before that check runtime permission
                                if (checkRunTimePermissionIsGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                                    startGalleryIntent();
                                } else {
                                    //request write external permission for open camera intent
                                    requestRunTimePermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, Constants.FOR_OPEN_GALLARY_REQUEST_WRITE_EXTERNAL_STORAGE_PER, new PermissionEventListener() {
                                        @Override
                                        public void onGranted(int requestCode, String[] permissions) {
                                            startGalleryIntent();
                                        }

                                        @Override
                                        public void onFailure(int requestCode, String[] permissions) {
                                            Snackbar.make(findViewById(android.R.id.content), getString(R.string.re_action_on_deny_external_storage_per_for_pick_photo), Snackbar.LENGTH_LONG)
                                                    .setAction(getString(R.string.action_manage), new View.OnClickListener() {
                                                        @Override
                                                        public void onClick(View v) {
                                                            startAppSettingsIntent();
                                                        }
                                                    }).show();
                                        }
                                    });
                                }
                            } catch (Exception ex) {
                                AndroidUtilities.processApplicationError(ex, true);
                                Snackbar.make(findViewById(android.R.id.content), getString(R.string.re_action_internal_app_error), Snackbar.LENGTH_LONG).show();
                            }
                        } else if (actionTypeId == ChooseItemModel.ACTION_REMOVE) {
                            try {
                                //send request to remove the current image profile
                                mProgressDialog.setMessage(getString(R.string.re_action_on_deleting_current_image_profile));
                                mProgressDialog.show();
                                mTService.removeImageProfile(new Callback<UserProfileModel>() {
                                    @Override
                                    public void success(UserProfileModel userProfileModel, Response response) {
                                        try {
                                            if (mCurrentActivity != null) {
                                                mAppPreferenceTools.updateUserProfileInformation(userProfileModel);
                                                Picasso.with(mCurrentActivity).load(userProfileModel.image_profile_url)
                                                        .placeholder(R.drawable.image_place_holder)
                                                        .transform(new CropCircleTransformation()).into(mImImageProfile);
                                                mProgressDialog.dismiss();
                                            }
                                        } catch (Exception ex) {
                                            if (mProgressDialog != null && mProgressDialog.isShowing()) {
                                                mProgressDialog.dismiss();
                                            }
                                            AndroidUtilities.processApplicationError(ex, true);
                                            Snackbar.make(findViewById(android.R.id.content), getString(R.string.re_action_internal_app_error), Snackbar.LENGTH_LONG).show();
                                        }
                                    }

                                    @Override
                                    public void failure(RetrofitError error) {
                                        //handle error
                                        if (mCurrentActivity != null) {
                                            mProgressDialog.dismiss();
                                            CommonFeedBack commonFeedBack = new CommonFeedBack(findViewById(android.R.id.content), mCurrentActivity);
                                            commonFeedBack.checkCommonErrorAndBackUnCommonOne(error);
                                        }
                                    }
                                });

                            } catch (Exception ex) {
                                if (mProgressDialog != null && mProgressDialog.isShowing()) {
                                    mProgressDialog.dismiss();
                                }
                                AndroidUtilities.processApplicationError(ex, true);
                                Snackbar.make(findViewById(android.R.id.content), getString(R.string.re_action_internal_app_error), Snackbar.LENGTH_LONG).show();
                            }
                        }

                    }

                    @Override
                    public void onChooseValueCode(String valueCode) {
                        //DO NOTHING
                    }

                    @Override
                    public void onChooseThemeColor(String theme) {
                        //DO NOTHING
                    }
                });
                fragment.show(getFragmentManager(), Constants.RECYCLER_VIEW_CHOOSE_DIALOG_TAG);
            }
        });

        //config broadcast receiver
        mUserProfileBroadCastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int actionToDo = intent.getIntExtra(Constants.ACTION_TO_DO_PARAM, Constants.DO_NOTHING);
                try {
                    if (actionToDo == Constants.UPDATE_USER_PROFILE) {
                        //only update the user profile
                        mAppSettingsAdapter.updateWhenProfileUpdate(getSettingList());
                        //load user image with picasso
                        Picasso.with(mCurrentActivity).load(mAppPreferenceTools.getImageUrl())
                                .placeholder(R.drawable.image_place_holder)
                                .transform(new CropCircleTransformation()).into(mImImageProfile);
                    } else if (actionToDo == Constants.RECREAT_ACTIVITY_WHEN_LOCALE_OR_THEME_CHANGED) {
                        Intent finishIntent = new Intent();
                        mCurrentActivity.setResult(DashboardActivity.RESULT_OK, finishIntent);
                        mCurrentActivity.finish();
                    }
                } catch (Exception ex) {
                    AndroidUtilities.processApplicationError(ex, true);
                    Snackbar.make(findViewById(android.R.id.content), getString(R.string.re_action_internal_app_error), Snackbar.LENGTH_LONG).show();
                }
            }
        };
    }

    /**
     * start camera and return generated image path
     *
     * @return String CurrentImagePath
     */
    private String startCameraIntent() {
        String imagePath = null;
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            File image = AndroidUtilities.generateImagePath();
            if (image != null) {
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(image));
                imagePath = image.getAbsolutePath();
            }
            startActivityForResult(cameraIntent, Constants.CAMERA_REQUEST_CODE);
            setAnimationOnStart();
        } catch (Exception ex) {
            AndroidUtilities.processApplicationError(ex, true);
            Snackbar.make(findViewById(android.R.id.content), getString(R.string.re_action_internal_app_error), Snackbar.LENGTH_LONG).show();
        }
        return imagePath;
    }

    /**
     * update user profile
     * change locale or theme
     *
     * @param locale String locale
     */
    private void updateUserProfile(final String locale, final String theme) {
        try {
            String themeValue = theme;
            String localeValue = locale;
            //produce request for update user profile information
            if (theme != null) {
                mProgressDialog.setMessage(getString(R.string.re_action_on_changing_theme));
            } else {
                mProgressDialog.setMessage(getString(R.string.re_action_on_changing_locale));
            }
            if (localeValue == null) {
                localeValue = mAppPreferenceTools.getApplicationLocale();
            }
            if (themeValue == null) {
                themeValue = mAppPreferenceTools.getCurrentThemeName();
            }
            UpdateProfileRequestModel updateProfileRequestModel = new UpdateProfileRequestModel(mAppPreferenceTools.getDisplayName(), localeValue, themeValue);
            mProgressDialog.show();
            mTService.updateProfile(updateProfileRequestModel, new Callback<UserProfileModel>() {
                @Override
                public void success(UserProfileModel userProfileModel, Response response) {
                    try {
                        if (locale != null) {
                            mAppPreferenceTools.updateUserProfileInformation(userProfileModel);
                            mAppSettingsAdapter.changeOpenDialogSettingStatus(AppSettingItemModel.DIALOG_EDIT_LOCALE, getCurrentLanguageName());
                            mAppPreferenceTools.setApplicationLocale(userProfileModel.locale);
                            //change the current locale of application
                            TApplication.updateLocale();
                            //recreate the activity
                            Intent intent = new Intent();
                            setResult(DashboardActivity.RESULT_OK, intent);
                            finish();
                            setAnimationOnExit();
                        } else {
                            //change the current theme
                            ThemeUtil themeUtil = new ThemeUtil(TApplication.applicationContext);
                            themeUtil.setCurrentTheme(theme);
                            //set result for finish activity
                            Intent intent = new Intent();
                            setResult(DashboardActivity.RESULT_OK, intent);
                            finish();
                            setAnimationOnExit();
                        }
                        mProgressDialog.dismiss();
                    } catch (Exception ex) {
                        if (mProgressDialog != null && mProgressDialog.isShowing()) {
                            mProgressDialog.dismiss();
                        }
                        AndroidUtilities.processApplicationError(ex, true);
                        Snackbar.make(findViewById(android.R.id.content), getString(R.string.re_action_internal_app_error), Snackbar.LENGTH_LONG).show();
                    }
                }

                @Override
                public void failure(RetrofitError error) {
                    if (mCurrentActivity != null) {
                        mProgressDialog.dismiss();
                        //handle common errors , in this request don't have any un common errors
                        CommonFeedBack commonFeedBack = new CommonFeedBack(findViewById(android.R.id.content), mCurrentActivity);
                        commonFeedBack.checkCommonErrorAndBackUnCommonOne(error);
                    }
                }
            });
        } catch (Exception ex) {
            if (mProgressDialog != null && mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
            }
            AndroidUtilities.processApplicationError(ex, true);
            Snackbar.make(findViewById(android.R.id.content), getString(R.string.re_action_internal_app_error), Snackbar.LENGTH_LONG).show();
        }
    }


    /**
     * initial settings list
     */
    private List<AppSettingItemModel> getSettingList() {
        List<AppSettingItemModel> mSettingsList = new ArrayList<>();
        try {
            String usernameStatusMessage = getString(R.string.re_action_username_not_defined);
            if (!mAppPreferenceTools.getUsername().equals(AppPreferenceTools.STRING_PREF_UNAVAILABLE)) {
                usernameStatusMessage = "@" + mAppPreferenceTools.getUsername();
            }

            //Info Group
            mSettingsList.add(new AppSettingItemModel(AppSettingItemModel.HEADER_ITEM_TYPE, getString(R.string.setting_group_title_info)));
            mSettingsList.add(new AppSettingItemModel(AppSettingItemModel.ITEM_OPEN_ACTIVITY, getString(R.string.setting_username), EditUsernameActivity.class, usernameStatusMessage));
            mSettingsList.add(new AppSettingItemModel(AppSettingItemModel.ITEM_DIVIDER_TYPE));
            mSettingsList.add(new AppSettingItemModel(AppSettingItemModel.ITEM_OPEN_ACTIVITY, getString(R.string.setting_display_name), EditDisplayNameActivity.class, mAppPreferenceTools.getDisplayName()));
            //Notifications Group
            mSettingsList.add(new AppSettingItemModel(AppSettingItemModel.HEADER_ITEM_TYPE, getString(R.string.setting_group_title_notifications)));
            mSettingsList.add(new AppSettingItemModel(AppSettingItemModel.ITEM_WITH_CHECKBOX, getString(R.string.setting_got_pn_telepathies_matched), AppSettingItemModel.CHECKBOX_NOTIFICATION_WHEN_TELEPATHIES_MATCHED, mAppPreferenceTools.isNotificationEnableWhenTelepathiesMatched()));
            mSettingsList.add(new AppSettingItemModel(AppSettingItemModel.ITEM_DIVIDER_TYPE));
            mSettingsList.add(new AppSettingItemModel(AppSettingItemModel.ITEM_WITH_CHECKBOX, getString(R.string.setting_got_pn_add_you_as_friend), AppSettingItemModel.CHECKBOX_NOTIFICATION_WHEN_ADD_YOU_AS_FRIEND, mAppPreferenceTools.isNotificationEnableWhenAddYouAsFriend()));


            mSettingsList.add(new AppSettingItemModel(AppSettingItemModel.ITEM_DIVIDER_TYPE));
            mSettingsList.add(new AppSettingItemModel(AppSettingItemModel.ITEM_TO_OPEN_DIALOG, getString(R.string.setting_sound), mAppPreferenceTools.getNotificationRingtoneTitle(), AppSettingItemModel.DIALOG_SET_NOTIFICATION_SOUND));
            mSettingsList.add(new AppSettingItemModel(AppSettingItemModel.ITEM_DIVIDER_TYPE));
            mSettingsList.add(new AppSettingItemModel(AppSettingItemModel.ITEM_WITH_CHECKBOX, getString(R.string.setting_vibrate), AppSettingItemModel.CHECKBOX_VIBRATE, mAppPreferenceTools.vibrateOnNotificationReceive()));
            //General Group
            mSettingsList.add(new AppSettingItemModel(AppSettingItemModel.HEADER_ITEM_TYPE, getString(R.string.setting_group_title_general)));
            mSettingsList.add(new AppSettingItemModel(AppSettingItemModel.ITEM_TO_OPEN_DIALOG, getString(R.string.setting_language), getCurrentLanguageName(), AppSettingItemModel.DIALOG_EDIT_LOCALE));
            mSettingsList.add(new AppSettingItemModel(AppSettingItemModel.ITEM_DIVIDER_TYPE));
            mSettingsList.add(new AppSettingItemModel(AppSettingItemModel.ITEM_TO_OPEN_DIALOG, getString(R.string.setting_theme), null, AppSettingItemModel.DIALOG_EDIT_THEME));
            mSettingsList.add(new AppSettingItemModel(AppSettingItemModel.ITEM_DIVIDER_TYPE));
            mSettingsList.add(new AppSettingItemModel(AppSettingItemModel.ITEM_TO_OPEN_DIALOG, getString(R.string.setting_about_telepathy), null, AppSettingItemModel.DIALOG_ABOUT));
            mSettingsList.add(new AppSettingItemModel(AppSettingItemModel.ITEM_DIVIDER_TYPE));
            mSettingsList.add(new AppSettingItemModel(AppSettingItemModel.ITEM_TO_LOG_OUT, getString(R.string.setting_log_out)));
        } catch (Exception ex) {
            AndroidUtilities.processApplicationError(ex, true);
            Snackbar.make(findViewById(android.R.id.content), getString(R.string.re_action_internal_app_error), Snackbar.LENGTH_LONG).show();
        }
        return mSettingsList;
    }

    /**
     * get the current language name via mUserProfileModelRealm locale
     *
     * @return String
     */
    private String getCurrentLanguageName() {
        String[] languageCodes = getResources().getStringArray(R.array.language_code);
        String[] languageNames = getResources().getStringArray(R.array.language_name);
        for (int i = 0; i < languageCodes.length; i++) {
            if (mAppPreferenceTools.getApplicationLocale().equals(languageCodes[i])) {
                return languageNames[i];
            }
        }
        return languageNames[0];
    }

    /**
     * on activity result
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.START_ACTIVITY_FOR_SETTING_RESULT_MESSAGE && resultCode == RESULT_OK) {
            try {
                //get message and show with snack bar
                String message = data.getStringExtra("message");
                if (message != null) {
                    if (message.equals(getString(R.string.re_action_display_name_successfully_update))) {
                        //update display name status in editDisplayName appSettings item
                        mAppSettingsAdapter.changeOpenActivitySettingStatus(EditDisplayNameActivity.class, mAppPreferenceTools.getDisplayName());
                    } else if (message.equals(getString(R.string.re_action_username_successfully_update))) {
                        //update username status in editUsername appSettings item
                        mAppSettingsAdapter.changeOpenActivitySettingStatus(EditUsernameActivity.class, "@" + mAppPreferenceTools.getUsername());
                    }
                    Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT).show();
                }
            } catch (Exception ex) {
                AndroidUtilities.processApplicationError(ex, true);
                Snackbar.make(findViewById(android.R.id.content), getString(R.string.re_action_internal_app_error), Snackbar.LENGTH_LONG).show();
            }
        } else if (requestCode == Crop.REQUEST_CROP) {
            try {
                if (resultCode == RESULT_OK) {
                    //after crop should send to server
                    Uri editedImageUri = Crop.getOutput(data);
                    File imageFile = new File(editedImageUri.getPath());
                    if (imageFile.exists()) {
                        uploadImageProfile(imageFile);
                    } else {
                        //show snack bar to error happened
                        Snackbar.make(findViewById(android.R.id.content), getString(R.string.re_action_internal_app_error), Snackbar.LENGTH_LONG).show();
                    }
                } else if (resultCode == Crop.RESULT_ERROR) {
                    //show snack bar to error happened
                    Snackbar.make(findViewById(android.R.id.content), getString(R.string.re_action_internal_app_error), Snackbar.LENGTH_LONG).show();
                }
            } catch (Exception ex) {
                AndroidUtilities.processApplicationError(ex, true);
                Snackbar.make(findViewById(android.R.id.content), getString(R.string.re_action_internal_app_error), Snackbar.LENGTH_LONG).show();
            }
        } else if (requestCode == Constants.CAMERA_REQUEST_CODE && resultCode == RESULT_OK) {
            try {
                navigateToPhotoCropActivity();
            } catch (Exception ex) {
                AndroidUtilities.processApplicationError(ex, true);
                Snackbar.make(findViewById(android.R.id.content), getString(R.string.re_action_internal_app_error), Snackbar.LENGTH_LONG).show();
            }
        } else if (requestCode == Constants.GALLERY_REQUEST_CODE && resultCode == RESULT_OK) {
            try {
                Uri selectedImageUri = data.getData();
                String extractUriFrom = selectedImageUri.toString();
                //check is from google photos or google drive
                if (extractUriFrom.contains("com.google.android.apps.photos.contentprovider") || extractUriFrom.contains("com.google.android.apps.docs.storage")) {
                    final int chunkSize = 1024;  // We'll read in one kB at a time
                    byte[] imageData = new byte[chunkSize];
                    File imageFile = AndroidUtilities.generateImagePath();
                    InputStream in = null;
                    OutputStream out = null;
                    try {
                        in = getContentResolver().openInputStream(selectedImageUri);
                        out = new FileOutputStream(imageFile);
                        int bytesRead;
                        while ((bytesRead = in.read(imageData)) > 0) {
                            out.write(Arrays.copyOfRange(imageData, 0, Math.max(0, bytesRead)));
                        }

                        mCurrentImagePath = imageFile.getAbsolutePath();
                        navigateToPhotoCropActivity();
                    } catch (Exception ex) {
                        AndroidUtilities.processApplicationError(ex, true);
                        Snackbar.make(findViewById(android.R.id.content), getString(R.string.re_action_internal_app_error), Snackbar.LENGTH_LONG).show();
                        Log.e("Something went wrong.", ex.toString());
                    } finally {
                        if (in != null) {
                            in.close();
                        }
                        if (out != null) {
                            out.close();
                        }
                    }
                } else {
                    mCurrentImagePath = AndroidUtilities.getPath(selectedImageUri);
                    //now navigate to photoCropActivity
                    navigateToPhotoCropActivity();
                }
            } catch (Exception ex) {
                AndroidUtilities.processApplicationError(ex, true);
                Snackbar.make(findViewById(android.R.id.content), getString(R.string.re_action_internal_app_error), Snackbar.LENGTH_LONG).show();
            }
        } else if (requestCode == Constants.REQUEST_CODE_ALERT_RINGTONE && data != null) {
            try {
                Uri ringtone = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
                if (ringtone != null) {
                    mAppPreferenceTools.setNotificationRingtone(ringtone.toString());
                } else {
                    //silent was selected
                    mAppPreferenceTools.setNotificationRingtone("");
                }
                mAppSettingsAdapter.changeOpenDialogSettingStatus(AppSettingItemModel.DIALOG_SET_NOTIFICATION_SOUND, mAppPreferenceTools.getNotificationRingtoneTitle());
            } catch (Exception ex) {
                AndroidUtilities.processApplicationError(ex, true);
                Snackbar.make(findViewById(android.R.id.content), getString(R.string.re_action_internal_app_error), Snackbar.LENGTH_LONG).show();
            }
        }
    }


    /**
     * after select photo from gallery or take photo by camera
     * navigate to PhotoCropActivity to crop selected image profile
     */
    private void navigateToPhotoCropActivity() {
        //produce good file name to edit as CashDir+file_name_without_type+'_edited'
        File selected_image_file = new File(mCurrentImagePath);
        String file_name_without_type = selected_image_file.getName().substring(0, selected_image_file.getName().lastIndexOf("."));
        Uri source = Uri.fromFile(selected_image_file);
        Uri destination = Uri.fromFile(new File(getCacheDir(), file_name_without_type + "_edited"));
        //start crop activity to crop current image
        Crop.of(source, destination).withMaxSize(512, 512).asSquare().start(this);
        setAnimationOnStart();
    }

    /**
     * internal function for upload image profile
     *
     * @param imageFile File imageFile
     */
    private void uploadImageProfile(File imageFile) {
        try {
            TypedFile typedFile = new TypedFile("image/jpeg", imageFile);
            mProgressDialog.setMessage(getString(R.string.re_action_on_uploading_image_profile));
            mProgressDialog.show();
            mTService.updateImageProfile(typedFile, new Callback<UserProfileModel>() {
                @Override
                public void success(UserProfileModel userProfileModel, Response response) {
                    try {
                        //update in realmDB
                        mAppPreferenceTools.updateUserProfileInformation(userProfileModel);
                        //load new image profile
                        Picasso.with(mCurrentActivity).load(userProfileModel.image_profile_url)
                                .placeholder(R.drawable.image_place_holder)
                                .transform(new CropCircleTransformation()).into(mImImageProfile);
                        mProgressDialog.dismiss();
                    } catch (Exception ex) {
                        if (mProgressDialog != null && mProgressDialog.isShowing()) {
                            mProgressDialog.dismiss();
                        }
                        AndroidUtilities.processApplicationError(ex, true);
                        Snackbar.make(findViewById(android.R.id.content), getString(R.string.re_action_internal_app_error), Snackbar.LENGTH_LONG).show();
                    }
                }

                @Override
                public void failure(RetrofitError error) {
                    if (mCurrentActivity != null) {
                        mProgressDialog.dismiss();
                        CommonFeedBack commonFeedBack = new CommonFeedBack(findViewById(android.R.id.content), mCurrentActivity);
                        commonFeedBack.checkCommonErrorAndBackUnCommonOne(error);
                    }
                }
            });
        } catch (Exception ex) {
            if (mProgressDialog != null && mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
            }
            AndroidUtilities.processApplicationError(ex, true);
            Snackbar.make(findViewById(android.R.id.content), getString(R.string.re_action_internal_app_error), Snackbar.LENGTH_LONG).show();
        }
    }

    /**
     * don't have any menu for action
     * just handle home as up
     *
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            //handle up action like on back pressed
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        try {
            saveSelfArgs(outState);
            super.onSaveInstanceState(outState);
        } catch (Exception ex) {
            AndroidUtilities.processApplicationError(ex, true);
            Snackbar.make(findViewById(android.R.id.content), getString(R.string.re_action_internal_app_error), Snackbar.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        try {
            restoreSelfArgs(savedInstanceState);
            super.onRestoreInstanceState(savedInstanceState);
        } catch (Exception ex) {
            AndroidUtilities.processApplicationError(ex, true);
            Snackbar.make(findViewById(android.R.id.content), getString(R.string.re_action_internal_app_error), Snackbar.LENGTH_LONG).show();
        }
    }

    /**
     * save m_current_image_path to bundle
     *
     * @param args Bundle
     */
    public void saveSelfArgs(Bundle args) {
        if (mCurrentImagePath != null) {
            args.putString("path", mCurrentImagePath);
        }
    }

    /**
     * restore m_current_image_path from bundle
     *
     * @param args Bundle
     */
    public void restoreSelfArgs(Bundle args) {
        mCurrentImagePath = args.getString("path");
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            LocalBroadcastManager.getInstance(this).registerReceiver((mUserProfileBroadCastReceiver), new IntentFilter(Constants.ACTION_TO_DO_FOR_PROFILE_UPDATE_INTENT_FILTER));
        } catch (Exception ex) {
            AndroidUtilities.processApplicationError(ex, true);
            Snackbar.make(findViewById(android.R.id.content), getString(R.string.re_action_internal_app_error), Snackbar.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onPause() {
        try {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mUserProfileBroadCastReceiver);
        } catch (Exception ex) {
            AndroidUtilities.processApplicationError(ex, true);
            Snackbar.make(findViewById(android.R.id.content), getString(R.string.re_action_internal_app_error), Snackbar.LENGTH_LONG).show();
        }
        super.onPause();
    }

    @Override
    protected void onStop() {
        try {
            if (mProgressDialog != null && mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
            }
        } catch (Exception ex) {
            AndroidUtilities.processApplicationError(ex, true);
            Snackbar.make(findViewById(android.R.id.content), getString(R.string.re_action_internal_app_error), Snackbar.LENGTH_LONG).show();
        }
        super.onStop();
    }
}
