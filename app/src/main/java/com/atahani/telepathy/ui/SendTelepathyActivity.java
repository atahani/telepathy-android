package com.atahani.telepathy.ui;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.*;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

import com.squareup.picasso.Picasso;
import com.atahani.telepathy.model.PublishTelepathyRequestModel;
import com.atahani.telepathy.realm.MessageModelRealm;
import com.atahani.telepathy.ui.component.CommonFeedBack;
import com.atahani.telepathy.ui.utility.Constants;
import com.atahani.telepathy.ui.utility.CropCircleTransformation;

import io.realm.Realm;
import mobi.atahani.telepathy.R;
import com.atahani.telepathy.TApplication;
import com.atahani.telepathy.model.PublishTelepathyResponse;
import com.atahani.telepathy.realm.NotificationRealmModel;
import com.atahani.telepathy.realm.TelepathyModelRealm;
import com.atahani.telepathy.ui.component.DetectLEditText;
import com.atahani.telepathy.ui.component.DetectLTextView;
import com.atahani.telepathy.ui.component.TelepathyBaseActivity;
import com.atahani.telepathy.ui.fragment.SpecifyTelepathyTTLDialogFragment;
import com.atahani.telepathy.utility.AndroidUtilities;
import com.atahani.telepathy.utility.ThemeUtil;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * used for send telepathy, before telepathy select who want to telepathy
 */
public class SendTelepathyActivity extends TelepathyBaseActivity {

    private final int MAX_MESSAGE_CHAR = 320;
    private DetectLEditText mETxMessageBody;
    private AppCompatTextView mTxCounter;
    private FloatingActionButton mFabSend;
    private String mWithUserId;
    private String mWithUserDisplayName;
    private String mWithUserImageUrl;
    private String mWithUserThemeName;
    private String mWithUserUsername;
    private Activity mCurrentActivity;
    private RelativeLayout mLyAction;
    private ScrollView mScrollEditTextView;
    private int mSoftKeyBoardHeight = 0;
    private int mDelayForObserveView = 900;
    private MenuItem mSendMenuItem;
    private boolean mShouldShowKeyboard = true;
    private boolean mIsFromNotification = false;
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        configTheme(false);
        setContentView(R.layout.activity_send_telepathy);
        //get all of the bundle
        Bundle args = getIntent().getExtras();
        if (args != null) {
            mWithUserId = args.getString(Constants.WITH_USER_ID_PARAM);
            mWithUserDisplayName = args.getString(Constants.WITH_USER_DISPLAY_NAME_PARAM);
            mWithUserImageUrl = args.getString(Constants.WITH_USER_IMAGE_URL_PARAM);
            mWithUserThemeName = args.getString(Constants.WITH_USER_THEME_PARAM);
            mWithUserUsername = args.getString(Constants.WITH_USER_USERNAME_PARAM);
            mIsFromNotification = args.getBoolean(Constants.IS_FROM_NOTIFICATION_PARAM, false);
        }
        //check if have ADD_YOU_AS_FRIEND_PN_TAG notification with this userId remove it
        if (mIsFromNotification) {
            NotificationRealmModel notificationRealmModel = getRealm().where(NotificationRealmModel.class).equalTo("tagType", Constants.ADD_YOU_AS_FRIEND_PN_TAG).equalTo("fromUserId", mWithUserId).findFirst();
            if (notificationRealmModel != null) {
                //clear pn from notifications
                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(TApplication.applicationContext);
                notificationManager.cancel(Constants.ADD_YOU_AS_FRIEND_PN_TAG, notificationRealmModel.getNotificationId());
                //remove the notification model form DB
                getRealm().beginTransaction();
                notificationRealmModel.removeFromRealm();
                getRealm().commitTransaction();
            }
        }
        mCurrentActivity = this;
        ThemeUtil themeUtil = new ThemeUtil(this);
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage(getString(R.string.re_action_on_sending_telepathy));
        mProgressDialog.setCancelable(false);
        mProgressDialog.setCanceledOnTouchOutside(false);
        //config the status bar by mUserThemeName
        configStatusBar(themeUtil.getPrimaryDarkColorByThemeName(mWithUserThemeName));
        //config default toolbar but get the object for get child views
        Toolbar defaultToolbar = configDefaultToolBar();
        defaultToolbar.setBackgroundColor(themeUtil.getPrimaryColorByThemeName(mWithUserThemeName));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mScrollEditTextView = (ScrollView) findViewById(R.id.scroll_for_edit_text);
        mLyAction = (RelativeLayout) findViewById(R.id.ly_action);
        ImageView imImageProfile = (ImageView) defaultToolbar.findViewById(R.id.im_image_profile);
        DetectLTextView txDisplayName = (DetectLTextView) defaultToolbar.findViewById(R.id.tx_display_name);
        mFabSend = (FloatingActionButton) findViewById(R.id.fb_send);
        mFabSend.setBackgroundTintList(ColorStateList.valueOf(themeUtil.getAccentColorByThemeName(mWithUserThemeName)));
        mFabSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doneAction();
            }
        });
        mTxCounter = (AppCompatTextView) defaultToolbar.findViewById(R.id.tx_text_counter);
        txDisplayName.setText(mWithUserDisplayName);
        //load image profile via Glide
        Picasso.with(this).load(mWithUserImageUrl)
                .placeholder(R.drawable.image_place_holder)
                .transform(new CropCircleTransformation()).into(imImageProfile);
        mTxCounter.setText(String.format("%d", MAX_MESSAGE_CHAR));
        mETxMessageBody = (DetectLEditText) findViewById(R.id.etx_telepathy_message);
        //add listener for update text body counter and color if higher that MAX_MESSAGE_CHAR char
        mETxMessageBody.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                mTxCounter.setText(String.format("%d", MAX_MESSAGE_CHAR - s.toString().trim().length()));
                if (TextUtils.isEmpty(s)) {
                    setSwipeBackEnable(true);
                } else {
                    setSwipeBackEnable(false);
                }
            }
        });
        mETxMessageBody.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.hasFocus() && mSoftKeyBoardHeight != 0 && mSendMenuItem.isVisible()) {
                    showKeyboardArea();
                }
            }
        });
        mETxMessageBody.setCustomKeyboardBehavior(new DetectLEditText.CustomKeyboardBehavior() {
            @Override
            public void onDismissingKeyboard() {
                if (!mSendMenuItem.isVisible()) {
                    hideKeyboardArea();
                } else {
                    confirmToLeave();
                }
            }
        });

        //to get height of soft keyboard
        final View rootView = this.getWindow().getDecorView();
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
//                //it's mean not initial at first
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Rect rect = new Rect();
                        rootView.getWindowVisibleDisplayFrame(rect);
                        int heightDifference = rootView.getHeight() - (rect.top != 0 ? AndroidUtilities.statusBarHeight : 0) - AndroidUtilities.getViewInset(rootView);
                        heightDifference = heightDifference - (rect.bottom - rect.top);
                        if (heightDifference > 48 && mShouldShowKeyboard) {
                            mSoftKeyBoardHeight = heightDifference;
                            showKeyboardArea();
                            mDelayForObserveView = 1;
                        }
                    }
                }, mDelayForObserveView);
            }
        });
    }

    /**
     * show the keyboard area
     */
    private void showKeyboardArea() {
        try {
            mShouldShowKeyboard = false;
            mSendMenuItem.setVisible(false);
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
            int actionHeight = getResources().getDimensionPixelOffset(R.dimen.send_telepathy_action_height) + mSoftKeyBoardHeight;
            RelativeLayout.LayoutParams actionLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, actionHeight);
            actionLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            mLyAction.setLayoutParams(actionLayoutParams);
            RelativeLayout.LayoutParams scrollViewLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
            scrollViewLayoutParams.setMargins(0, 0, 0, actionHeight);
            scrollViewLayoutParams.addRule(RelativeLayout.BELOW, R.id.default_toolbar);
            mScrollEditTextView.setLayoutParams(scrollViewLayoutParams);
            mFabSend.setVisibility(View.VISIBLE);
            AndroidUtilities.showKeyboard(mETxMessageBody);
        } catch (Exception ex) {
            AndroidUtilities.processApplicationError(ex, true);
            Snackbar.make(findViewById(android.R.id.content), getString(R.string.re_action_internal_app_error), Snackbar.LENGTH_LONG).show();
        }
    }

    /**
     * hide keyboard area
     */
    private void hideKeyboardArea() {
        try {
            mShouldShowKeyboard = false;
            AndroidUtilities.hideKeyboard(mETxMessageBody);
            mSendMenuItem.setVisible(true);
            int actionHeight = getResources().getDimensionPixelOffset(R.dimen.send_telepathy_action_height);
            RelativeLayout.LayoutParams scrollViewLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
            scrollViewLayoutParams.setMargins(0, 0, 0, actionHeight);
            scrollViewLayoutParams.addRule(RelativeLayout.BELOW, R.id.default_toolbar);
            mScrollEditTextView.setLayoutParams(scrollViewLayoutParams);
            RelativeLayout.LayoutParams actionLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, actionHeight);
            actionLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            mLyAction.setLayoutParams(actionLayoutParams);
            mFabSend.setVisibility(View.GONE);
        } catch (Exception ex) {
            AndroidUtilities.processApplicationError(ex, true);
            Snackbar.make(findViewById(android.R.id.content), getString(R.string.re_action_internal_app_error), Snackbar.LENGTH_LONG).show();
        }
    }

    @Override
    public void onBackPressed() {
        confirmToLeave();
    }

    /**
     * publish telepathy message
     */
    private void doneAction() {
        try {
            //hide keyboard and area
            hideKeyboardArea();
            //check message
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getString(R.string.app_name));
            builder.setPositiveButton(getString(R.string.action_ok), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //show the keyboard
                    AndroidUtilities.showKeyboard(mETxMessageBody);
                    showKeyboardArea();
                }
            });
            if (!TextUtils.isEmpty(mETxMessageBody.getText())) {
                if (mETxMessageBody.getText().toString().trim().length() <= MAX_MESSAGE_CHAR) {
                    //open specify telepathy TTL for pick life time
                    SpecifyTelepathyTTLDialogFragment fragment = new SpecifyTelepathyTTLDialogFragment();
                    fragment.setSpecifyTTLDialogEventListener(new SpecifyTelepathyTTLDialogFragment.SpecifyTTLDialogEventListener() {
                        @Override
                        public void onSend(int TTLInMin) {
                            sendTelepathy(TTLInMin);
                        }

                        @Override
                        public void onCancel() {
                            //show the keyboard
                            mETxMessageBody.requestFocus();
                            AndroidUtilities.showKeyboard(mETxMessageBody);
                            showKeyboardArea();
                        }
                    });
                    fragment.show(getFragmentManager(), Constants.SPECIFY_TELEPATHY_TTL_DIALOG_TAG);
                } else {
                    //open alert dialog to prompt that message is too large
                    builder.setMessage(getString(R.string.re_action_telepathy_is_too_long));
                    builder.show();
                }
            } else {
                //open alert dialog to prompt should set message for this telepathy
                builder.setMessage(getString(R.string.re_action_telepathy_should_have_message));
                builder.show();
            }
        } catch (Exception ex) {
            AndroidUtilities.processApplicationError(ex, true);
            Snackbar.make(findViewById(android.R.id.content), getString(R.string.re_action_internal_app_error), Snackbar.LENGTH_LONG).show();
        }
    }

    /**
     * send telepathy
     *
     * @param TTLInMin int the TTL in min
     */
    private void sendTelepathy(int TTLInMin) {
        try {
            mProgressDialog.show();
            PublishTelepathyRequestModel publishTelepathyRequestModel = new PublishTelepathyRequestModel(mWithUserId, mETxMessageBody.getText().toString(), TTLInMin);
            getTService().publishTelepathy(publishTelepathyRequestModel, new Callback<PublishTelepathyResponse>() {
                @Override
                public void success(final PublishTelepathyResponse publishTelepathyResponse, Response response) {
                    //navigate to dashboard in telepathies list
                    if (!getRealm().isClosed() && mCurrentActivity != null) {
                        try {
                            final Intent resultBundle = new Intent();
                            resultBundle.putExtra(Constants.IS_TELEPATHIES_MATCHED_PARAM, publishTelepathyResponse.is_match);
                            getRealm().executeTransaction(new Realm.Transaction() {
                                @Override
                                public void execute(Realm realm) {
                                    if (publishTelepathyResponse.is_match) {
                                        //save matched message to DB
                                        MessageModelRealm newMessage = realm.createObject(MessageModelRealm.class);
                                        newMessage.setMessageId(publishTelepathyResponse.message.id);
                                        newMessage.setWithUserId(publishTelepathyResponse.message.with_user_id);
                                        newMessage.setBodySend(publishTelepathyResponse.message.body_send);
                                        newMessage.setBodyReceive(publishTelepathyResponse.message.body_receive);
                                        newMessage.setYouAreFirst(publishTelepathyResponse.message.you_are_first);
                                        newMessage.setMatchedAt(publishTelepathyResponse.message.matched_at);
                                        newMessage.setMatchedWithTelepathyId(publishTelepathyResponse.message.matched_with_telepathy_id);
                                        newMessage.setMatchedInSec(publishTelepathyResponse.message.matched_in_sec);
                                        newMessage.setIsReceive(publishTelepathyResponse.message.is_receive);
                                        newMessage.setIsRead(publishTelepathyResponse.message.is_read);
                                        newMessage.setIsSendReadSignal(publishTelepathyResponse.message.is_send_read_signal);
                                        newMessage.setUpdatedAt(publishTelepathyResponse.message.updated_at);
                                        //get and remove matched telepathy
                                        TelepathyModelRealm matchedTelepathyInDB = realm.where(TelepathyModelRealm.class).equalTo("telepathyId", publishTelepathyResponse.message.matched_with_telepathy_id).findFirst();
                                        if (matchedTelepathyInDB != null) {
                                            matchedTelepathyInDB.removeFromRealm();
                                        }
                                        //put information in bundle
                                        resultBundle.putExtra(Constants.MESSAGE_ID_PARAM, publishTelepathyResponse.message.id);
                                        resultBundle.putExtra(Constants.WITH_USER_ID_PARAM, mWithUserId);
                                        resultBundle.putExtra(Constants.WITH_USER_DISPLAY_NAME_PARAM, mWithUserDisplayName);
                                        resultBundle.putExtra(Constants.WITH_USER_IMAGE_URL_PARAM, mWithUserImageUrl);
                                        resultBundle.putExtra(Constants.WITH_USER_THEME_PARAM, mWithUserThemeName);
                                        resultBundle.putExtra(Constants.WITH_USER_USERNAME_PARAM, mWithUserUsername);
                                        resultBundle.putExtra(Constants.NUMBER_OF_USER_MESSAGE, 0);
                                    } else {
                                        //save unmatched telepathy to DB
                                        TelepathyModelRealm newTelepathy = realm.createObject(TelepathyModelRealm.class);
                                        newTelepathy.setTelepathyId(publishTelepathyResponse.telepathy.id);
                                        newTelepathy.setWithUserId(publishTelepathyResponse.telepathy.to_user.user_id);
                                        newTelepathy.setWithUserUsername(publishTelepathyResponse.telepathy.to_user.username);
                                        newTelepathy.setWithUserDisplayName(publishTelepathyResponse.telepathy.to_user.display_name);
                                        newTelepathy.setWithUserImageUrl(publishTelepathyResponse.telepathy.to_user.image_url);
                                        newTelepathy.setWithUserTheme(publishTelepathyResponse.telepathy.to_user.theme);
                                        newTelepathy.setBody(publishTelepathyResponse.telepathy.body);
                                        newTelepathy.setCreatedAt(publishTelepathyResponse.telepathy.created_at);
                                        newTelepathy.setExpireAt(publishTelepathyResponse.telepathy.expire_at);
                                        resultBundle.putExtra(Constants.TELEPATHY_ID_PARAM, publishTelepathyResponse.telepathy.id);
                                    }
                                }
                            }, new Realm.Transaction.Callback() {
                                @Override
                                public void onSuccess() {
                                    //set result bundle and finish this activity
                                    setResult(RESULT_OK, resultBundle);
                                    finish();
                                    setAnimationOnExit();
                                    mProgressDialog.dismiss();
                                }

                                @Override
                                public void onError(Exception e) {
                                    super.onError(e);
                                    setResult(RESULT_CANCELED, resultBundle);
                                    finish();
                                    setAnimationOnExit();
                                    mProgressDialog.dismiss();
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_send, menu);
        mSendMenuItem = menu.findItem(R.id.action_send);
        return true;
    }

    private void confirmToLeave() {
        try {
            //check if have any text message
            if (TextUtils.isEmpty(mETxMessageBody.getText())) {
                finish();
                setAnimationOnExit();
            } else {
                //prompt alert dialog to confirm are you sure to discard this telepathy ?
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(getString(R.string.app_name));
                builder.setMessage(getString(R.string.re_action_discard_send_telepathy_confirm_alert_message));
                builder.setPositiveButton(getString(R.string.action_yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //yes to discard this telepathy message
                        mETxMessageBody.setText("");
                        finish();
                        setAnimationOnExit();
                    }
                });
                builder.setNegativeButton(getString(R.string.action_no), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //show the keyboard to continue
                        showKeyboardArea();
                    }
                });
                builder.show();
            }
        } catch (Exception ex) {
            AndroidUtilities.processApplicationError(ex, true);
            Snackbar.make(findViewById(android.R.id.content), getString(R.string.re_action_internal_app_error), Snackbar.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            confirmToLeave();
        } else if (id == R.id.action_send) {
            doneAction();
        }
        return super.onOptionsItemSelected(item);
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
