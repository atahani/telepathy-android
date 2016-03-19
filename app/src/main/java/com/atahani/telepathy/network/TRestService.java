package com.atahani.telepathy.network;

import android.content.Intent;

import com.google.gson.*;
import com.squareup.okhttp.Authenticator;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.atahani.telepathy.utility.AppPreferenceTools;

import com.atahani.telepathy.TApplication;
import com.atahani.telepathy.model.RefreshTokenRequestModel;
import com.atahani.telepathy.model.TokenModel;

import com.atahani.telepathy.network.ClientConfigs;

import com.atahani.telepathy.ui.SignInActivity;

import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.OkClient;
import retrofit.converter.GsonConverter;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.Proxy;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Telepathy rest service that can produce request with REST API endpoint
 */
public class TRestService {

    private TService mMainService;
    private AppPreferenceTools mAppPreferenceTools;
    private TService mServiceForGetRefreshToken;
    private TokenModel mRefreshTokenModel;

    /**
     * initial WRestService class
     */
    public TRestService() {
        RestAdapter restAdapterForGetRefreshToken = new RestAdapter.Builder()
                .setEndpoint(ClientConfigs.REST_END_POINT_URL)
                .build();
        mServiceForGetRefreshToken = restAdapterForGetRefreshToken.create(TService.class);
        mAppPreferenceTools = new AppPreferenceTools(TApplication.applicationContext);
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Date.class, new UTCDateTypeAdapter())
                .create();
        OkHttpClient okHttpClient = new OkHttpClient();
        //add this ok http authenticator when request get 401 http error code refreshToken and retry the request with this new accessToken
        okHttpClient.setAuthenticator(new Authenticator() {
            @Override
            public Request authenticate(Proxy proxy, Response response) throws IOException {
                //refresh access token using sync api request
                try {
                    RefreshTokenRequestModel refreshTokenRequestModel = new RefreshTokenRequestModel(mAppPreferenceTools.getRefreshToken());
                    mRefreshTokenModel = mServiceForGetRefreshToken.refreshToken(refreshTokenRequestModel);
                    if (mRefreshTokenModel != null) {
                        mAppPreferenceTools.saveRefreshTokenInformation(mRefreshTokenModel);
                        return response.request().newBuilder()
                                .removeHeader("authorization")
                                .addHeader("authorization", "bearer " + mRefreshTokenModel.access_token)
                                .build();
                    }
                } catch (RetrofitError error) {
                    TErrorHandler errorHandler = new TErrorHandler();
                    Throwable err = errorHandler.handleError(error);
                    if (err.getMessage().equals(TErrorHandler.INVALID_REFRESH_TOKEN) || err.getMessage().equals(TErrorHandler.CLIENT_IS_DISABLE) || err.getMessage().equals(TErrorHandler.CLIENT_INFORMATION_IS_NOT_VALID)) {
                        //it's mean should authorize
                        mAppPreferenceTools.removeAllOfThePref();
                        Intent intent = new Intent(TApplication.applicationContext, SignInActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        TApplication.applicationContext.startActivity(intent);
                    }
                }
                //if not return so return null
                return null;
            }

            @Override
            public Request authenticateProxy(Proxy proxy, Response response) throws IOException {
                return null;
            }
        });

        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(ClientConfigs.REST_END_POINT_URL)
                .setConverter(new GsonConverter(gson))
                .setClient(new OkClient(okHttpClient))
                .setErrorHandler(new TErrorHandler())
                .setRequestInterceptor(new RequestInterceptor() {
                    @Override
                    public void intercept(RequestFacade request) {
                        if (mAppPreferenceTools.isAuthorized()) {
                            request.addHeader("authorization", "bearer " + mAppPreferenceTools.getAccessToken());
                        }
                    }
                })
                .build();

        this.mMainService = restAdapter.create(TService.class);
    }

    /**
     * get service to call WUWService method
     *
     * @return
     */
    public TService getTService() {
        return this.mMainService;
    }


    /**
     * define UTCDateTypeAdapterDateAdapter as convert JSON DateTime as Date Value with UTC format
     */
    private static class UTCDateTypeAdapter implements JsonSerializer<Date>,
            JsonDeserializer<Date> {
        private final DateFormat dateFormat;

        private UTCDateTypeAdapter() {
            dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
            dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        }

        @Override
        public synchronized JsonElement serialize(Date date, Type type,
                                                  JsonSerializationContext jsonSerializationContext) {
            synchronized (dateFormat) {
                String dateFormatAsString = dateFormat.format(date);
                return new JsonPrimitive(dateFormatAsString);
            }
        }

        @Override
        public synchronized Date deserialize(JsonElement jsonElement, Type type,
                                             JsonDeserializationContext jsonDeserializationContext) {
            try {
                synchronized (dateFormat) {
                    return dateFormat.parse(jsonElement.getAsString());
                }
            } catch (ParseException e) {
                throw new JsonSyntaxException(jsonElement.getAsString(), e);
            }
        }
    }
}