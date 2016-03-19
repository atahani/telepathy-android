package com.atahani.telepathy.ui;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.view.View;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
import com.atahani.telepathy.adapter.IntroductionViewPagerAdapter;
import com.atahani.telepathy.model.AuthorizeResponseModel;
import com.atahani.telepathy.model.SignInRequestModel;
import com.atahani.telepathy.realm.MessageModelRealm;
import com.atahani.telepathy.realm.TelepathyModelRealm;
import com.atahani.telepathy.realm.UserModelRealm;
import com.atahani.telepathy.ui.component.CommonFeedBack;
import com.atahani.telepathy.ui.component.TelepathyBaseActivity;
import com.atahani.telepathy.ui.fragment.IntroductionFragment;
import com.atahani.telepathy.ui.utility.Constants;
import com.atahani.telepathy.utility.AndroidUtilities;
import com.atahani.telepathy.utility.AppPreferenceTools;
import com.atahani.telepathy.utility.ThemeUtil;

import me.relex.circleindicator.CircleIndicator;
import mobi.atahani.telepathy.R;
import com.atahani.telepathy.TApplication;

import com.atahani.telepathy.network.ClientConfigs;

import com.atahani.telepathy.realm.NotificationRealmModel;
import com.atahani.telepathy.realm.UserMessageNotificationRealmModel;

import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Sign in activity sign in with google button
 */
public class SignInActivity extends TelepathyBaseActivity implements GoogleApiClient.OnConnectionFailedListener {

    private GoogleApiClient mGoogleApiClient;
    private ProgressDialog mProgressDialog;
    private AppPreferenceTools mAppPreferenceTools;
    private Activity mCurrentActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            configTheme(false);
            setSwipeBackEnable(false);
            configStatusBar(ContextCompat.getColor(this, R.color.theme_sign_in_primary_color));
            mCurrentActivity = this;
            setContentView(R.layout.activity_sign_in);
            mAppPreferenceTools = new AppPreferenceTools(TApplication.applicationContext);
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage(getString(R.string.re_action_on_sign_in));
            mProgressDialog.setCancelable(false);
            mProgressDialog.setCanceledOnTouchOutside(false);
            ViewPager mViewPagerIntroduction = (ViewPager) findViewById(R.id.vp_introduction);
            IntroductionViewPagerAdapter viewPagerAdapter = new IntroductionViewPagerAdapter(getFragmentManager());
            CircleIndicator circleIndicator = (CircleIndicator) findViewById(R.id.indicator_default);
            viewPagerAdapter.addFragment(IntroductionFragment.newInstance(1));
            viewPagerAdapter.addFragment(IntroductionFragment.newInstance(2));
            viewPagerAdapter.addFragment(IntroductionFragment.newInstance(3));
            mViewPagerIntroduction.setAdapter(viewPagerAdapter);
            circleIndicator.setViewPager(mViewPagerIntroduction);

            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestScopes(new Scope(Scopes.PROFILE))
                    .requestIdToken(ClientConfigs.GOOGLE_SERVER_CLIENT_ID)
                    .requestEmail()
                    .build();

            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                    .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                    .build();

            SignInButton btnGoogleSignIn = (SignInButton) findViewById(R.id.btn_google_sign_in);
            btnGoogleSignIn.setSize(SignInButton.SIZE_WIDE);
            btnGoogleSignIn.setColorScheme(SignInButton.COLOR_DARK);

            btnGoogleSignIn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        //first check internet connectivity then sign in if internet is connect
                        ConnectivityManager connectivityManager = (ConnectivityManager) TApplication.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE);
                        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
                        if (networkInfo != null && networkInfo.isConnectedOrConnecting()) {
                            Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
                            startActivityForResult(signInIntent, Constants.RC_GET_AUTH_CODE);
                            setAnimationOnStart();
                        } else {
                            Snackbar.make(findViewById(android.R.id.content), getString(R.string.re_action_no_internet_connection), Snackbar.LENGTH_LONG).show();
                        }
                    } catch (Exception ex) {
                        AndroidUtilities.processApplicationError(ex, true);
                        Snackbar.make(findViewById(android.R.id.content), getString(R.string.re_action_internal_app_error), Snackbar.LENGTH_LONG).show();
                    }
                }
            });
        } catch (Exception ex) {
            AndroidUtilities.processApplicationError(ex, true);
            Snackbar.make(findViewById(android.R.id.content), getString(R.string.re_action_internal_app_error), Snackbar.LENGTH_LONG).show();
        }
    }

    /**
     * send sign in request into server from different provider
     *
     * @param provider     String pre define provider
     * @param access_token access_token to get user information from provide API
     */
    private void signIn(final String provider, String access_token) {
        try {
            AndroidUtilities androidUtilities = new AndroidUtilities();
            SignInRequestModel signInRequestModel = new SignInRequestModel(ClientConfigs.TELEPATHY_APP_ID, ClientConfigs.TELEPATHY_APP_KEY, provider, access_token, androidUtilities.getDeviceName());
            getTService().signIn(signInRequestModel, new retrofit.Callback<AuthorizeResponseModel>() {
                @Override
                public void success(AuthorizeResponseModel authorizeResponseModel, Response response) {
                    try {
                        if (!getRealm().isClosed() && mCurrentActivity != null) {
                            mProgressDialog.dismiss();
                            //remove shared prefs
                            mAppPreferenceTools.removeAllOfThePref();
                            //save telepathy access token
                            mAppPreferenceTools.saveUserInformationInSignIn(authorizeResponseModel);
                            //before save any object in DB
                            getRealm().beginTransaction();
                            getRealm().clear(UserModelRealm.class);
                            getRealm().clear(MessageModelRealm.class);
                            getRealm().clear(TelepathyModelRealm.class);
                            getRealm().clear(UserMessageNotificationRealmModel.class);
                            getRealm().clear(NotificationRealmModel.class);
                            getRealm().commitTransaction();
                            mAppPreferenceTools.setIsInSignUpMode(authorizeResponseModel.is_in_sign_up_mode);
                            mAppPreferenceTools.setShouldGetClassifyMessageWhenAppOpen(true);
                            if (authorizeResponseModel.is_in_sign_up_mode) {
                                startActivity(new Intent(mCurrentActivity, CompleteSignUpActivity.class));
                                setAnimationOnStart();
                                finish();
                            } else {
                                //since the user already registered set the theme
                                ThemeUtil themeUtil = new ThemeUtil(TApplication.applicationContext);
                                themeUtil.setCurrentTheme(authorizeResponseModel.user_profile.theme);
                                TApplication.updateLocale();
                                startActivity(new Intent(mCurrentActivity, DashboardActivity.class));
                                setAnimationOnStart();
                                finish();
                            }
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
                    try {
                        if (mCurrentActivity != null) {
                            mProgressDialog.dismiss();
                            //user want try again so should disconnect and connect in signInButton click event
                            AlertDialog.Builder builder = new AlertDialog.Builder(mCurrentActivity);
                            builder.setTitle(getString(R.string.app_name));
                            CommonFeedBack commonFeedBack = new CommonFeedBack(findViewById(android.R.id.content), mCurrentActivity);
                            String unCommonErrorType = commonFeedBack.checkCommonErrorAndBackUnCommonOne(error);
                            if (unCommonErrorType != null) {
                                if (unCommonErrorType.equals("USER_LOCKED")) {
                                    builder.setMessage(getString(R.string.re_action_user_locked));
                                    builder.setPositiveButton(getString(R.string.action_ok), null);
                                    builder.show();
                                } else if (unCommonErrorType.equals("EMAIL_ADDRESS_IS_NOT_VERIFIED")) {
                                    builder.setMessage(String.format(getString(R.string.re_action_email_not_verified), getString(R.string.label_google_provider)))
                                            .setPositiveButton(getString(R.string.action_ok), null)
                                            .show();
                                }
                            }
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
        } catch (Exception ex) {
            if (mProgressDialog != null && mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
            }
            AndroidUtilities.processApplicationError(ex, true);
            Snackbar.make(findViewById(android.R.id.content), getString(R.string.re_action_internal_app_error), Snackbar.LENGTH_LONG).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            if (requestCode == Constants.RC_GET_AUTH_CODE) {
                GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
                if (result.isSuccess()) {
                    mProgressDialog.show();
                    //start authentication sign in
                    GoogleSignInAccount acct = result.getSignInAccount();
                    signIn(Constants.GOOGLE_PROVIDER, acct.getIdToken());
                } else {
                    if (mGoogleApiClient.isConnected()) {
                        Auth.GoogleSignInApi.signOut(mGoogleApiClient);
                    }
                    Snackbar.make(findViewById(android.R.id.content), getString(R.string.re_action_on_canceling_sign_in), Snackbar.LENGTH_LONG).show();
                }
            }
            super.onActivityResult(requestCode, resultCode, data);
        } catch (Exception ex) {
            if (mProgressDialog != null && mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
            }
            AndroidUtilities.processApplicationError(ex, true);
            Snackbar.make(findViewById(android.R.id.content), getString(R.string.re_action_internal_app_error), Snackbar.LENGTH_LONG).show();
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
        Snackbar.make(findViewById(android.R.id.content), getString(R.string.re_action_internal_app_error), Snackbar.LENGTH_LONG).show();
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
