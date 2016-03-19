package com.atahani.telepathy.ui.component;

import android.app.Activity;
import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.view.View;

import com.atahani.telepathy.utility.AppPreferenceTools;
import com.atahani.telepathy.TApplication;
import retrofit.RetrofitError;
import mobi.atahani.telepathy.R;
import com.atahani.telepathy.network.TErrorHandler;
import com.atahani.telepathy.ui.SignInActivity;

/**
 * there are common feedback such as connection failed
 * prompt that with snack bar
 */
public class CommonFeedBack {

    private Activity mCurrentActivity;
    private View mParentView;
    private AppPreferenceTools mAppPreferenceTools;


    /**
     * initial for prompt common error
     *
     * @param mParentView      parent view that should display SnackBar on it
     * @param current_activity current activity
     */
    public CommonFeedBack(View mParentView, Activity current_activity) {
        this.mParentView = mParentView;
        this.mCurrentActivity = current_activity;
        mAppPreferenceTools = new AppPreferenceTools(TApplication.applicationContext);
    }

    /**
     * first check if common error happened show snack bar with related error
     * if error is not common one return type of error to handle it in UI
     *
     * @param retrofit_error Retrofit Error
     * @return String un_common_error
     */
    public String checkCommonErrorAndBackUnCommonOne(final RetrofitError retrofit_error) {
        String un_common_error = null;
        String type = retrofit_error.getMessage();
        if (type.equals(TErrorHandler.NO_NETWORK_CONNECTION)) {
            //show  snack bar for no internet connection
            Snackbar.make(this.mParentView, R.string.re_action_no_internet_connection, Snackbar.LENGTH_LONG).show();
        } else if (type.equals(TErrorHandler.SERVER_UNAVAILABLE)) {
            //show snack bar for server un available
            Snackbar.make(this.mParentView, R.string.re_action_server_unavailable, Snackbar.LENGTH_LONG).show();
        } else if (type.equals(TErrorHandler.CLIENT_INFORMATION_IS_NOT_VALID) || type.equals(TErrorHandler.CLIENT_IS_DISABLE)) {
            //in these request such as signin || signup || refreshtoken
            //server check client information and active status, so when client information is not valid or client is disable
            if (mAppPreferenceTools.isAuthorized()) {
                //this mean request from refresh token
                //remove access token information
                mAppPreferenceTools.removeAllOfThePref();
                Intent intent = new Intent(TApplication.applicationContext, SignInActivity.class);
                intent.putExtra("message", TApplication.applicationContext.getResources().getString(R.string.re_action_client_is_invalid));
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                TApplication.applicationContext.startActivity(intent);
            } else {
                //it's in authorize step and should notify with alert dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(mCurrentActivity);
                builder.setTitle(TApplication.applicationContext.getResources().getString(R.string.app_name));
                builder.setMessage(TApplication.applicationContext.getResources().getString(R.string.re_action_client_is_invalid));
                builder.setPositiveButton(TApplication.applicationContext.getResources().getString(R.string.action_ok), null);
                builder.show();
            }

        } else if (type.equals(TErrorHandler.USER_LOCKED)) {
            //mean the user locked by telepathy
            //so should exit from app and alert user
            if (mAppPreferenceTools.isAuthorized()) {
                //that mean request produce when user logged in
                mAppPreferenceTools.removeAllOfThePref();
                Intent intent = new Intent(TApplication.applicationContext, SignInActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("message", TApplication.applicationContext.getResources().getString(R.string.re_action_user_locked));
                TApplication.applicationContext.startActivity(intent);
            } else {
                //it's in authorize steps and should notify with alert dialog that you are blocked by WYW
                AlertDialog.Builder builder = new AlertDialog.Builder(mCurrentActivity);
                builder.setTitle(TApplication.applicationContext.getResources().getString(R.string.app_name));
                builder.setMessage(TApplication.applicationContext.getResources().getString(R.string.re_action_user_locked));
                builder.setPositiveButton(TApplication.applicationContext.getResources().getString(R.string.action_ok), null);
                builder.show();
            }
        } else if (type.equals(TErrorHandler.INTERNAL_SERVER_ERROR) || type.equals(TErrorHandler.UNEXPECTED_ERROR) || type.equals(TErrorHandler.ERROR_HAS_NO_RESPONSE) || type.equals(TErrorHandler.HTTP_ERROR_WITH_NO_MESSAGE)) {
            //show snack bar for internal server error
            Snackbar.make(this.mParentView, R.string.re_action_internal_app_error, Snackbar.LENGTH_LONG).show();
        } else {
            un_common_error = type;
        }
        return un_common_error;
    }
}
