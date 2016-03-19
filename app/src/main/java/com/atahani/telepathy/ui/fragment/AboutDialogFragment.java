package com.atahani.telepathy.ui.fragment;


import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.AppCompatTextView;
import android.view.LayoutInflater;
import android.view.View;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.atahani.telepathy.network.TService;
import com.atahani.telepathy.ui.SignInActivity;
import com.atahani.telepathy.ui.component.CommonFeedBack;
import com.atahani.telepathy.ui.component.TelepathyBaseActivity;
import com.atahani.telepathy.ui.utility.Constants;
import com.atahani.telepathy.utility.AndroidUtilities;
import com.atahani.telepathy.utility.AppPreferenceTools;

import mobi.atahani.telepathy.R;
import com.atahani.telepathy.TApplication;
import com.atahani.telepathy.model.TOperationResultModel;
import com.atahani.telepathy.ui.component.AboutFooterImageView;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * About Dialog Fragment
 */
public class AboutDialogFragment extends DialogFragment {

    private AppPreferenceTools mAppPreferenceTools;

    public AboutDialogFragment() {
        // Required empty public constructor
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View customLayout = inflater.inflate(R.layout.fragment_about_dialog, null);
        //config app version information
        mAppPreferenceTools = new AppPreferenceTools(TApplication.applicationContext);
        AppCompatTextView txAndroidAppVerInfo = (AppCompatTextView) customLayout.findViewById(R.id.tx_android_ver_info);
        txAndroidAppVerInfo.setText(String.format(getString(R.string.label_telepathy_for_android), mAppPreferenceTools.getTheLastAppVersion()));
        txAndroidAppVerInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    //open browser to navigate telepathy website
                    Uri telepathyWebSiteUri = Uri.parse("https://github.com/atahani/telepathy-android.git");
                    startActivity(new Intent(Intent.ACTION_VIEW, telepathyWebSiteUri));
                    ((TelepathyBaseActivity) getActivity()).setAnimationOnStart();
                } catch (Exception ex) {
                    AndroidUtilities.processApplicationError(ex, true);
                    if (isAdded() && getActivity() != null) {
                        Snackbar.make(getActivity().findViewById(android.R.id.content), getString(R.string.re_action_internal_app_error), Snackbar.LENGTH_LONG).show();
                    }
                }
            }
        });
        //config google play store link
        AppCompatTextView txRateUsOnGooglePlayStore = (AppCompatTextView) customLayout.findViewById(R.id.tx_rate_us_on_play_store);
        txRateUsOnGooglePlayStore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //navigate to market for rate application
                Uri uri = Uri.parse("market://details?id=" + TApplication.applicationContext.getPackageName());
                Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
                // To count with Play market backstack, After pressing back button,
                // to taken back to our application, we need to add following flags to intent.
                goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                        Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                try {
                    startActivity(goToMarket);
                    ((TelepathyBaseActivity) getActivity()).setAnimationOnStart();
                } catch (ActivityNotFoundException e) {
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse("http://play.google.com/store/apps/details?id=" + TApplication.applicationContext.getPackageName())));
                }
            }
        });
        //config twitter link
        AppCompatTextView txTwitterLink = (AppCompatTextView) customLayout.findViewById(R.id.tx_twitter_telepathy);
        txTwitterLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    // If Twitter app is not installed, start browser.
                    Uri twitterUri = Uri.parse("http://twitter.com/");
                    startActivity(new Intent(Intent.ACTION_VIEW, twitterUri));
                    ((TelepathyBaseActivity) getActivity()).setAnimationOnStart();
                } catch (Exception ex) {
                    AndroidUtilities.processApplicationError(ex, true);
                    if (isAdded() && getActivity() != null) {
                        Snackbar.make(getActivity().findViewById(android.R.id.content), getString(R.string.re_action_internal_app_error), Snackbar.LENGTH_LONG).show();
                    }
                }
            }
        });
        //config privacy link
        AppCompatTextView txPrivacyLink = (AppCompatTextView) customLayout.findViewById(R.id.tx_privacy);
        txPrivacyLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    //open browser to navigate privacy link
                    Uri privacyPolicyUri = Uri.parse("https://github.com/atahani/telepathy-android/blob/master/LICENSE.md");
                    startActivity(new Intent(Intent.ACTION_VIEW, privacyPolicyUri));
                    ((TelepathyBaseActivity) getActivity()).setAnimationOnStart();
                } catch (Exception ex) {
                    AndroidUtilities.processApplicationError(ex, true);
                    if (isAdded() && getActivity() != null) {
                        Snackbar.make(getActivity().findViewById(android.R.id.content), getString(R.string.re_action_internal_app_error), Snackbar.LENGTH_LONG).show();
                    }
                }
            }
        });
        //config report bug to open mail application and send bugs report
        AppCompatTextView txReportBug = (AppCompatTextView) customLayout.findViewById(R.id.tx_report_bug);
        txReportBug.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    final Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                    emailIntent.setType("plain/text");
                    emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, getString(R.string.label_report_bug_email_subject));
                    emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, emailDeviceInformation());
                    emailIntent.setData(Uri.parse("mailto:" + getString(R.string.telepathy_report_bug_email)));
                    startActivity(Intent.createChooser(emailIntent, getString(R.string.label_report_bug_choose_mail_app)));
                    ((TelepathyBaseActivity) getActivity()).setAnimationOnStart();
                } catch (Exception ex) {
                    AndroidUtilities.processApplicationError(ex, true);
                    if (isAdded() && getActivity() != null) {
                        Snackbar.make(getActivity().findViewById(android.R.id.content), getString(R.string.re_action_internal_app_error), Snackbar.LENGTH_LONG).show();
                    }
                }
            }
        });
        //config the about footer image view
        AboutFooterImageView footerImageView = (AboutFooterImageView) customLayout.findViewById(R.id.im_delete_user_account);
        footerImageView.setTouchOnImageViewEventListener(new AboutFooterImageView.touchOnImageViewEventListener() {
            @Override
            public void onDoubleTab() {
                try {
                    //confirm account delete via alert dialog
                    final AlertDialog.Builder confirmAccountDeleteDialog = new AlertDialog.Builder(getActivity());
                    confirmAccountDeleteDialog.setTitle(getString(R.string.label_delete_user_account));
                    confirmAccountDeleteDialog.setMessage(getString(R.string.label_delete_user_account_description));
                    confirmAccountDeleteDialog.setNegativeButton(getString(R.string.action_no), null);
                    confirmAccountDeleteDialog.setPositiveButton(getString(R.string.action_yes), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //ok to delete account action
                            final ProgressDialog progressDialog = new ProgressDialog(getActivity());
                            progressDialog.setCancelable(false);
                            progressDialog.setCanceledOnTouchOutside(false);
                            progressDialog.setMessage(getString(R.string.re_action_on_deleting_user_account));
                            progressDialog.show();
                            TService tService = ((TelepathyBaseActivity) getActivity()).getTService();
                            tService.deleteUserAccount(new Callback<TOperationResultModel>() {
                                @Override
                                public void success(TOperationResultModel tOperationResultModel, Response response) {
                                    if (getActivity() != null && isAdded()) {
                                        //broad cast to close all of the realm  instance
                                        Intent intentToCloseRealm = new Intent(Constants.TELEPATHY_BASE_ACTIVITY_INTENT_FILTER);
                                        intentToCloseRealm.putExtra(Constants.ACTION_TO_DO_PARAM, Constants.CLOSE_REALM_DB);
                                        LocalBroadcastManager.getInstance(getActivity()).sendBroadcastSync(intentToCloseRealm);
                                        mAppPreferenceTools.removeAllOfThePref();
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
                                                        //also revoke google access
                                                        Auth.GoogleSignInApi.revokeAccess(googleApiClient).setResultCallback(new ResultCallback<Status>() {
                                                            @Override
                                                            public void onResult(Status status) {
                                                                progressDialog.dismiss();
                                                                TelepathyBaseActivity currentActivity = (TelepathyBaseActivity) TApplication.mCurrentActivityInApplication;
                                                                if (currentActivity != null) {
                                                                    Intent intent = new Intent(TApplication.applicationContext, SignInActivity.class);
                                                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                                                    currentActivity.startActivity(intent);
                                                                    currentActivity.setAnimationOnStart();
                                                                    currentActivity.finish();
                                                                }
                                                            }
                                                        });
                                                    }
                                                });
                                            }

                                            @Override
                                            public void onConnectionSuspended(int i) {
                                                //do nothing
                                            }
                                        });
                                    }
                                }

                                @Override
                                public void failure(RetrofitError error) {
                                    progressDialog.dismiss();
                                    if (getActivity() != null && isAdded()) {
                                        CommonFeedBack commonFeedBack = new CommonFeedBack(getActivity().findViewById(android.R.id.content), getActivity());
                                        commonFeedBack.checkCommonErrorAndBackUnCommonOne(error);
                                    }
                                }
                            });
                        }
                    });
                    confirmAccountDeleteDialog.show();
                } catch (Exception ex) {
                    AndroidUtilities.processApplicationError(ex, true);
                    if (isAdded() && getActivity() != null) {
                        Snackbar.make(getActivity().findViewById(android.R.id.content), getString(R.string.re_action_internal_app_error), Snackbar.LENGTH_LONG).show();
                    }
                }
            }
        });
        builder.setView(customLayout);
        return builder.create();
    }

    /**
     * get the email device information for report bug email
     *
     * @return String
     */
    private String emailDeviceInformation() {
        AndroidUtilities androidUtilities = new AndroidUtilities();
        return String.format(getString(R.string.label_report_bug_device_information), androidUtilities.getDeviceName(), Build.VERSION.RELEASE, androidUtilities.getApplicationVersion());
    }


    @Override
    public void onResume() {
        super.onResume();
        if (getResources().getBoolean(R.bool.isTablet)) {
            int width = getResources().getDimensionPixelSize(R.dimen.inner_dialog_width);
            getDialog().getWindow().setLayout(width, -2);
        }
    }
}
