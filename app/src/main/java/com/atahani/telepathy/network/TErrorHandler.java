package com.atahani.telepathy.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.atahani.telepathy.TApplication;
import com.atahani.telepathy.model.TErrorResponseModel;

import retrofit.ErrorHandler;
import retrofit.RetrofitError;

/**
 * custom Retrofit error handler for our service
 */
public class TErrorHandler implements ErrorHandler {

    public final static String SERVER_UNAVAILABLE = "SERVER_UNAVAILABLE";
    public final static String NO_NETWORK_CONNECTION = "NO_NETWORK_CONNECTION";
    public final static String ERROR_HAS_NO_RESPONSE = "ERROR_HAS_NO_RESPONSE";
    public final static String UNEXPECTED_ERROR = "UNEXPECTED_ERROR";
    public final static String HTTP_ERROR_WITH_NO_MESSAGE = "HTTP_ERROR_WITH_NO_MESSAGE";
    public final static String INTERNAL_SERVER_ERROR = "INTERNAL_SERVER_ERROR";
    public final static String ACCESS_TOKEN_IS_NOT_VALID = "ACCESS_TOKEN_IS_NOT_VALID";
    public final static String INVALID_REFRESH_TOKEN = "INVALID_REFRESH_TOKEN";
    public final static String CLIENT_INFORMATION_IS_NOT_VALID = "CLIENT_INFORMATION_IS_NOT_VALID";
    public final static String CLIENT_IS_DISABLE = "CLIENT_IS_DISABLE";
    public final static String USER_LOCKED = "USER_LOCKED";

    private ConnectivityManager mConnectivityManager;
    private NetworkInfo mNetworkInfo;

    public TErrorHandler() {
        this.mConnectivityManager = (ConnectivityManager) TApplication.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
    }

    /**
     * override Throwable method to set error message in different situation
     *
     * @param cause RetrofitError
     * @return Throwable Exception
     */
    @Override
    public Throwable handleError(RetrofitError cause) {
        String error_type;
        if (cause.getKind() == RetrofitError.Kind.NETWORK) {
            //check is network available
            if (mNetworkInfo != null && mNetworkInfo.isConnectedOrConnecting()) {
                error_type = SERVER_UNAVAILABLE;
            } else {
                error_type = NO_NETWORK_CONNECTION;
            }
        } else {
            if (cause.getResponse() == null) {
                error_type = ERROR_HAS_NO_RESPONSE;
            } else if (cause.getKind() == RetrofitError.Kind.HTTP) {
                try {
                    TErrorResponseModel errorResponseModel = (TErrorResponseModel) cause.getBodyAs(TErrorResponseModel.class);
                    error_type = errorResponseModel.type;

                } catch (Exception ex) {
                    error_type = HTTP_ERROR_WITH_NO_MESSAGE;
                }
            } else {
                error_type = UNEXPECTED_ERROR;
            }
        }
        return new Exception(error_type);
    }
}
