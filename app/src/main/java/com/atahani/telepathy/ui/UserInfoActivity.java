package com.atahani.telepathy.ui;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatImageButton;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.squareup.picasso.Picasso;

import io.realm.Realm;
import mobi.atahani.telepathy.R;
import com.atahani.telepathy.TApplication;
import com.atahani.telepathy.model.TOperationResultModel;
import com.atahani.telepathy.model.UserModel;
import com.atahani.telepathy.network.TService;
import com.atahani.telepathy.realm.UserModelRealm;
import com.atahani.telepathy.ui.component.CommonFeedBack;
import com.atahani.telepathy.ui.component.DetectLTextView;
import com.atahani.telepathy.ui.component.TelepathyBaseActivity;
import com.atahani.telepathy.ui.utility.Constants;
import com.atahani.telepathy.ui.utility.CropCircleTransformation;
import com.atahani.telepathy.utility.AndroidUtilities;
import com.atahani.telepathy.utility.ThemeUtil;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class UserInfoActivity extends TelepathyBaseActivity {

    private String mUserId;
    private String mUserTheme;
    private String mUserDisplayName;
    private String mUserImageProfileURL;
    private String mInvitationId;
    private TService mTService;
    private ProgressDialog mProgressDialog;
    private Activity mCurrentActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);
        Bundle args = getIntent().getExtras();
        if (args != null) {
            mUserId = args.getString(Constants.WITH_USER_ID_PARAM);
            mUserTheme = args.getString(Constants.WITH_USER_THEME_PARAM);
            mUserDisplayName = args.getString(Constants.WITH_USER_DISPLAY_NAME_PARAM);
            mUserImageProfileURL = args.getString(Constants.WITH_USER_IMAGE_URL_PARAM);
            mInvitationId = args.getString(Constants.INVITATION_ID_PARAM, "");
        }
        setFinishOnTouchOutside(false);
        mTService = getTService();
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setCanceledOnTouchOutside(false);
        mCurrentActivity = this;
        ThemeUtil themeUtil = new ThemeUtil(this);
        //assign primary color from user theme
        RelativeLayout mMainLayout = (RelativeLayout) findViewById(R.id.ly_main_content);
        mMainLayout.setBackgroundColor(themeUtil.getPrimaryColorByThemeName(mUserTheme));
        //load user display name
        DetectLTextView mTxDisplayName = (DetectLTextView) findViewById(R.id.tx_display_name);
        mTxDisplayName.setText(mUserDisplayName);
        //load image with Glide
        ImageView mImImageProfile = (ImageView) findViewById(R.id.im_image_profile);
        Picasso.with(this).load(mUserImageProfileURL)
                .placeholder(R.drawable.image_place_holder)
                .transform(new CropCircleTransformation())
                .into(mImImageProfile);
        //config close button
        AppCompatImageButton mBtnClose = (AppCompatImageButton) findViewById(R.id.btn_close);
        mBtnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setAnimationOnExit();
                finish();
            }
        });
        //config start telepathy button
        AppCompatImageButton mBtnTelepathy = (AppCompatImageButton) findViewById(R.id.btn_telepathy);
        mBtnTelepathy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    //start telepathy
                    Intent telepathyIntent = new Intent(mCurrentActivity, SendTelepathyActivity.class);
                    telepathyIntent.putExtra(Constants.WITH_USER_ID_PARAM, mUserId);
                    telepathyIntent.putExtra(Constants.WITH_USER_DISPLAY_NAME_PARAM, mUserDisplayName);
                    telepathyIntent.putExtra(Constants.WITH_USER_IMAGE_URL_PARAM, mUserImageProfileURL);
                    telepathyIntent.putExtra(Constants.WITH_USER_THEME_PARAM, mUserTheme);
                    startActivityForResult(telepathyIntent, Constants.TELEPATHY_REQUEST_CODE);
                    setAnimationOnStart();
                    finish();
                } catch (Exception ex) {
                    AndroidUtilities.processApplicationError(ex, true);
                    Snackbar.make(findViewById(android.R.id.content), getString(R.string.re_action_internal_app_error), Snackbar.LENGTH_LONG).show();
                }
            }
        });
        mBtnTelepathy.getDrawable().mutate().setColorFilter(null);
        mBtnTelepathy.getDrawable().setColorFilter(ContextCompat.getColor(TApplication.applicationContext, android.R.color.white), PorterDuff.Mode.MULTIPLY);
        //config the image button friend action
        final AppCompatImageButton mBtnFriendAction = (AppCompatImageButton) findViewById(R.id.btn_friend_action);
        UserModelRealm friendModelRealm = getRealm().where(UserModelRealm.class).equalTo("userId", mUserId).equalTo("isFriend", true).findFirst();
        if (friendModelRealm != null) {
            mBtnFriendAction.setImageDrawable(ContextCompat.getDrawable(this, R.mipmap.ic_person_remove_white_24dp));
            mBtnFriendAction.setContentDescription(getString(R.string.action_remove_from_friends));
        } else {
            mBtnFriendAction.setImageDrawable(ContextCompat.getDrawable(this, R.mipmap.ic_person_add_white_24dp));
            mBtnFriendAction.setContentDescription(getString(R.string.action_add_as_friend));
        }
        mBtnFriendAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    //check is user already friend or not
                    UserModelRealm friendModelRealm = getRealm().where(UserModelRealm.class).equalTo("userId", mUserId).equalTo("isFriend", true).findFirst();
                    if (friendModelRealm != null) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(mCurrentActivity);
                        builder.setTitle(getString(R.string.app_name));
                        builder.setMessage(getString(R.string.re_action_remove_user_as_friend_confirm_alert_message));
                        builder.setPositiveButton(getString(R.string.action_yes), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //remove as friend
                                mProgressDialog.setMessage(getString(R.string.re_action_on_remove_from_friend_list));
                                mProgressDialog.show();
                                mTService.removeFriend(mUserId, new Callback<TOperationResultModel>() {
                                    @Override
                                    public void success(TOperationResultModel tOperationResultModel, Response response) {
                                        try {
                                            if (!getRealm().isClosed() && mCurrentActivity != null) {
                                                //remove this object friend from DB
                                                getRealm().executeTransaction(new Realm.Transaction() {
                                                    @Override
                                                    public void execute(Realm realm) {
                                                        UserModelRealm friendModelRealmInTran = realm.where(UserModelRealm.class).equalTo("userId", mUserId).equalTo("isFriend", true).findFirst();
                                                        friendModelRealmInTran.setIsFriend(false);
                                                    }
                                                }, new Realm.Transaction.Callback() {
                                                    @Override
                                                    public void onSuccess() {
                                                        super.onSuccess();
                                                        //change the friend action icon
                                                        mBtnFriendAction.setImageDrawable(ContextCompat.getDrawable(getBaseContext(), R.mipmap.ic_person_add_white_24dp));
                                                        mBtnFriendAction.setContentDescription(getString(R.string.action_add_as_friend));
                                                        mProgressDialog.dismiss();
                                                        Intent messageIntent = new Intent(Constants.ACTION_TO_DO_FOR_FRIEND_UPDATE_INTENT_FILTER);
                                                        messageIntent.putExtra(Constants.ACTION_TO_DO_PARAM, Constants.UPDATE_FROM_DB);
                                                        LocalBroadcastManager mLocalBroadcastManager = LocalBroadcastManager.getInstance(TApplication.applicationContext);
                                                        mLocalBroadcastManager.sendBroadcast(messageIntent);
                                                    }

                                                    @Override
                                                    public void onError(Exception e) {
                                                        super.onError(e);
                                                        mProgressDialog.dismiss();
                                                    }
                                                });
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
                                        if (mCurrentActivity != null && getBaseContext() != null) {
                                            mProgressDialog.dismiss();
                                            CommonFeedBack commonFeedBack = new CommonFeedBack(findViewById(android.R.id.content), mCurrentActivity);
                                            commonFeedBack.checkCommonErrorAndBackUnCommonOne(error);
                                        }
                                    }
                                });
                            }
                        });
                        builder.setNegativeButton(getString(R.string.action_no), null);
                        builder.show();
                    } else {
                        //add as friend
                        mProgressDialog.setMessage(getString(R.string.re_action_on_add_in_friend_list));
                        mProgressDialog.show();
                        mTService.addNewFriend(mUserId, new Callback<UserModel>() {
                            @Override
                            public void success(final UserModel userModel, Response response) {
                                try {
                                    if (!getRealm().isClosed() && mCurrentActivity != null) {
                                        getRealm().executeTransaction(new Realm.Transaction() {
                                            @Override
                                            public void execute(Realm realm) {
                                                //if is not exist
                                                UserModelRealm userModelRealm = realm.where(UserModelRealm.class).equalTo("userId", userModel.user_id).findFirst();
                                                if (userModelRealm != null) {
                                                    //update it as friend
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
                                                mBtnFriendAction.setImageDrawable(ContextCompat.getDrawable(getBaseContext(), R.mipmap.ic_person_remove_white_24dp));
                                                mBtnFriendAction.setContentDescription(getString(R.string.action_remove_from_friends));
                                                Intent messageIntent = new Intent(Constants.ACTION_TO_DO_FOR_FRIEND_UPDATE_INTENT_FILTER);
                                                messageIntent.putExtra(Constants.ACTION_TO_DO_PARAM, Constants.UPDATE_FROM_DB);
                                                LocalBroadcastManager mLocalBroadcastManager = LocalBroadcastManager.getInstance(TApplication.applicationContext);
                                                mLocalBroadcastManager.sendBroadcast(messageIntent);
                                                mProgressDialog.dismiss();

                                            }

                                            @Override
                                            public void onError(Exception e) {
                                                super.onError(e);
                                                mProgressDialog.dismiss();
                                            }
                                        });
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
                                if (mCurrentActivity != null && getBaseContext() != null) {
                                    mProgressDialog.dismiss();
                                    CommonFeedBack commonFeedBack = new CommonFeedBack(findViewById(android.R.id.content), mCurrentActivity);
                                    commonFeedBack.checkCommonErrorAndBackUnCommonOne(error);
                                }
                            }
                        });
                    }
                } catch (Exception ex) {
                    if (mProgressDialog != null && mProgressDialog.isShowing()) {
                        mProgressDialog.dismiss();
                    }
                    AndroidUtilities.processApplicationError(ex, true);
                    Snackbar.make(findViewById(android.R.id.content), getString(R.string.re_action_internal_app_error), Snackbar.LENGTH_LONG).show();
                }
            }
        });
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
