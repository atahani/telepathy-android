package com.atahani.telepathy.ui;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import com.atahani.telepathy.ui.component.UsernameETX;
import com.atahani.telepathy.utility.AppPreferenceTools;

import mobi.atahani.telepathy.R;
import com.atahani.telepathy.model.CheckUsernameRequestModel;
import com.atahani.telepathy.model.UpdateProfileRequestModel;
import com.atahani.telepathy.model.UserProfileModel;
import com.atahani.telepathy.model.UsernameCheckingResponse;
import com.atahani.telepathy.network.TService;
import com.atahani.telepathy.ui.component.CommonFeedBack;
import com.atahani.telepathy.ui.component.TelepathyBaseActivity;
import com.atahani.telepathy.utility.AndroidUtilities;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class EditUsernameActivity extends TelepathyBaseActivity {

    public static final String USERNAME_REGEX = "^[a-zA-Z0-9_]{4,20}$";
    private UsernameETX mEtxUsername;
    private TextInputLayout mTilUsername;
    private TService mTService;
    private boolean mIsValidForm = true;
    private Activity mCurrentActivity;
    private AppPreferenceTools mAppPreferenceTools;
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        configTheme(true);
        setContentView(R.layout.activity_edit_username);
        configDefaultToolBar();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mAppPreferenceTools = getAppPreferenceTools();
        mEtxUsername = (UsernameETX) findViewById(R.id.ext_username);
        mTilUsername = (TextInputLayout) findViewById(R.id.til_username);
        mTService = getTService();
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage(getString(R.string.re_action_on_updating_username));
        mProgressDialog.setCancelable(false);
        mProgressDialog.setCanceledOnTouchOutside(false);

        if (!mAppPreferenceTools.getUsername().equals(AppPreferenceTools.STRING_PREF_UNAVAILABLE)) {
            mEtxUsername.setText("@" + mAppPreferenceTools.getUsername());
        } else {
            mEtxUsername.setText("@");
        }
        mEtxUsername.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    if (s.length() > 1) {
                        //first check regex on username
                        String username = mEtxUsername.getText().toString().substring(1);
                        if (username.matches(USERNAME_REGEX)) {
                            //check username availability by server
                            mTilUsername.setError(null);
                            CheckUsernameRequestModel checkUsernameRequestModel = new CheckUsernameRequestModel(username);
                            mTService.checkUsernameAvailability(checkUsernameRequestModel, new Callback<UsernameCheckingResponse>() {
                                @Override
                                public void success(UsernameCheckingResponse usernameCheckingResponse, Response response) {
                                    try {
                                        if (mCurrentActivity != null) {
                                            mIsValidForm = usernameCheckingResponse.is_valid;
                                            if (!usernameCheckingResponse.is_valid) {
                                                mTilUsername.setError(getString(R.string.re_action_username_should_be));
                                            }
                                            if (!usernameCheckingResponse.is_unique) {
                                                mTilUsername.setError(getString(R.string.re_action_username_is_not_unique));
                                            }
                                        }
                                    } catch (Exception ex) {
                                        AndroidUtilities.processApplicationError(ex, true);
                                        Snackbar.make(findViewById(android.R.id.content), getString(R.string.re_action_internal_app_error), Snackbar.LENGTH_LONG).show();
                                    }
                                }

                                @Override
                                public void failure(RetrofitError error) {
                                    if (mCurrentActivity != null) {
                                        CommonFeedBack commonFeedBack = new CommonFeedBack(findViewById(android.R.id.content), mCurrentActivity);
                                        commonFeedBack.checkCommonErrorAndBackUnCommonOne(error);
                                    }
                                }
                            });

                        } else {
                            mIsValidForm = false;
                            if (s.length() >= 5) {
                                mTilUsername.setError(getString(R.string.re_action_username_should_be));
                            } else {
                                mTilUsername.setError(getString(R.string.re_action_username_should_be_at_least_4_character));
                            }
                        }
                    }
                } catch (Exception ex) {
                    AndroidUtilities.processApplicationError(ex, true);
                    Snackbar.make(findViewById(android.R.id.content), getString(R.string.re_action_internal_app_error), Snackbar.LENGTH_LONG).show();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        mEtxUsername.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    //in the last input action done
                    doneAction();
                }
                return false;
            }
        });
        mCurrentActivity = this;
    }

    /**
     * done action to update username
     */
    private void doneAction() {
        try {
            //for update user profile first check inputs
            if (getCurrentFocus() != null) {
                AndroidUtilities.hideKeyboard(mEtxUsername);
            }
            if (mIsValidForm) {
                //now send request to update user profile
                mProgressDialog.show();
                // create update profile request model with username
                UpdateProfileRequestModel updateProfileRequestModel = new UpdateProfileRequestModel(mEtxUsername.getText().toString().substring(1), mAppPreferenceTools.getDisplayName(), mAppPreferenceTools.getApplicationLocale(), mAppPreferenceTools.getCurrentThemeName());
                mTService.updateProfile(updateProfileRequestModel, new Callback<UserProfileModel>() {
                    @Override
                    public void success(UserProfileModel userProfileModel, Response response) {
                        try {
                            if (mCurrentActivity != null) {
                                mProgressDialog.dismiss();
                                mAppPreferenceTools.updateUserProfileInformation(userProfileModel);
                                //set result back to settings application
                                Intent resultIntent = new Intent();
                                resultIntent.putExtra("message", getString(R.string.re_action_username_successfully_update));
                                setResult(RESULT_OK, resultIntent);
                                finish();
                                setAnimationOnExit();
                            }
                        } catch (Exception ex) {
                            if(mProgressDialog!=null && mProgressDialog.isShowing()){
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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_done, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == android.R.id.home) {
            //handle up action like on back pressed
            onBackPressed();
        } else if (id == R.id.action_done) {
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
