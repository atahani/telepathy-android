package com.atahani.telepathy.network.gcm;

import android.app.IntentService;
import android.content.Intent;
import android.os.Build;

import com.google.android.gms.gcm.GcmPubSub;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.atahani.telepathy.network.TRestService;
import com.atahani.telepathy.network.TService;
import com.atahani.telepathy.utility.AndroidUtilities;
import com.atahani.telepathy.utility.AppPreferenceTools;

import mobi.atahani.telepathy.R;
import com.atahani.telepathy.model.RegisterDeviceRequestModel;
import com.atahani.telepathy.model.TOperationResultModel;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

import java.io.IOException;

/**
 * RegistrationIntentService help to get GCM token and send it to server
 */
public class RegistrationIntentService extends IntentService {

    private static final String TAG = "RegIntentService";
    private static final String[] TOPICS = {"global"};

    public RegistrationIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        AppPreferenceTools appPreferenceTools = new AppPreferenceTools(this);
        try {
            // In the (unlikely) event that multiple refresh operations occur simultaneously,
            // ensure that they are processed sequentially.
            synchronized (TAG) {
                // [START register_for_gcm]
                // Initially this call goes out to the network to retrieve the token, subsequent calls
                // are local.
                // [START get_token]
                InstanceID instanceID = InstanceID.getInstance(this);
                String token = instanceID.getToken(getString(R.string.gcm_defaultSenderId),
                        GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
                // [END get_token]
                //send token to server in register device request
                sendRegistrationToServer(appPreferenceTools, token);

                // Subscribe to topic channels
                subscribeTopics(token);

                // You should store a boolean that indicates whether the generated token has been
                // sent to your server. If the boolean is false, send the token to your server,
                // otherwise your server should have already received the token.
                // [END register_for_gcm]
            }
        } catch (Exception e) {
            // If an exception happens while fetching the new token or updating our registration data
            // on a third-party server, this ensures that we'll attempt the update at a later time.
            appPreferenceTools.setIsRegisterDevice(false);
        }
    }


    /**
     * Persist registration to third-party servers.
     * <p/>
     * Modify this method to associate the user's GCM registration token with any server-side account
     * maintained by your application.
     *
     * @param token The new token.
     */
    private void sendRegistrationToServer(final AppPreferenceTools appPreferenceTools, String token) {
        //send request to register device on server
        //build request body
        AndroidUtilities androidUtilities = new AndroidUtilities();
        final String app_version = androidUtilities.getApplicationVersion();
        RegisterDeviceRequestModel registerDeviceRequestModel = new RegisterDeviceRequestModel("GCM_ANDROID", token, androidUtilities.getDeviceName(), Build.VERSION.RELEASE, app_version);
        TRestService restService = new TRestService();
        TService tService = restService.getTService();
        tService.registerDevice(registerDeviceRequestModel, new Callback<TOperationResultModel>() {
            @Override
            public void success(TOperationResultModel tOperationResultModel, Response response) {
                if (tOperationResultModel.type.equals("REGISTER_SUCCESSFULLY")) {
                    //when server know about this device,store the app version number that register on the server
                    appPreferenceTools.setTheLastAppVersion(app_version);
                    appPreferenceTools.setIsRegisterDevice(true);
                } else {
                    appPreferenceTools.setIsRegisterDevice(false);
                }
            }

            @Override
            public void failure(RetrofitError error) {
                appPreferenceTools.setIsRegisterDevice(false);
            }
        });
    }


    /**
     * Subscribe to any GCM topics of interest, as defined by the TOPICS constant.
     *
     * @param token GCM token
     * @throws IOException if unable to reach the GCM PubSub service
     */
    // [START subscribe_topics]
    private void subscribeTopics(String token) throws IOException {
        for (String topic : TOPICS) {
            GcmPubSub pubSub = GcmPubSub.getInstance(this);
            pubSub.subscribe(token, "/topics/" + topic, null);
        }
    }
    // [END subscribe_topics]
}
