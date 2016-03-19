package com.atahani.telepathy.ui;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import com.atahani.telepathy.model.UserProfileModel;
import com.atahani.telepathy.network.TService;
import com.atahani.telepathy.ui.component.CommonFeedBack;
import com.atahani.telepathy.ui.component.TelepathyBaseActivity;
import com.atahani.telepathy.utility.AndroidUtilities;
import com.atahani.telepathy.utility.AppPreferenceTools;

import mobi.atahani.telepathy.R;
import com.atahani.telepathy.model.UpdateProfileRequestModel;
import com.atahani.telepathy.ui.component.DetectLEditText;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class EditDisplayNameActivity extends TelepathyBaseActivity {

    private TService mTService;
    private DetectLEditText mEtxDisplayName;
    private boolean mIsValidForm = true;
    private Activity mCurrentActivity;
    private AppPreferenceTools mAppPreferenceTools;
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        configTheme(true);
        setContentView(R.layout.activity_edit_display_name_acitivty);
        //config defaults on create activity
        configDefaultToolBar();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mAppPreferenceTools = getAppPreferenceTools();
        mEtxDisplayName = (DetectLEditText) findViewById(R.id.etx_display_name);
        mTService = getTService();
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage(getString(R.string.re_action_on_updating_display_name));
        mProgressDialog.setCancelable(false);
        mProgressDialog.setCanceledOnTouchOutside(false);
        //bind data
        mEtxDisplayName.setText(mAppPreferenceTools.getDisplayName());
        mEtxDisplayName.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    doneAction();
                }
                return false;
            }
        });
        mCurrentActivity = this;
    }

    private void doneAction() {
        try {
            //for update user profile first check inputs
            if (getCurrentFocus() != null) {
                AndroidUtilities.hideKeyboard(mEtxDisplayName);
            }
            TextInputLayout mTilDisplayName = (TextInputLayout) findViewById(R.id.til_display_name);
            if (mEtxDisplayName.getText().toString().trim().length() == 0) {
                mIsValidForm = false;
                mTilDisplayName.setError(getString(R.string.re_action_enter_display_name));
            } else {
                mTilDisplayName.setError(null);
            }
            if (mIsValidForm) {
                //now send request to update user profile
                mProgressDialog.show();
                //create update profile request model without username
                UpdateProfileRequestModel updateProfileRequestModel = new UpdateProfileRequestModel(mEtxDisplayName.getText().toString(), mAppPreferenceTools.getApplicationLocale(), mAppPreferenceTools.getCurrentThemeName());
                mTService.updateProfile(updateProfileRequestModel, new Callback<UserProfileModel>() {
                    @Override
                    public void success(UserProfileModel userProfileModel, Response response) {
                        try {
                            if (mCurrentActivity != null) {
                                mProgressDialog.dismiss();
                                mAppPreferenceTools.updateUserProfileInformation(userProfileModel);
                                //set result back to settings activity
                                Intent resultIntent = new Intent();
                                resultIntent.putExtra("message", getString(R.string.re_action_display_name_successfully_update));
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
            if(mProgressDialog!=null && mProgressDialog.isShowing()){
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
