package com.atahani.telepathy.network.gcm;

import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.LocalBroadcastManager;

import com.google.android.gms.gcm.GcmListenerService;
import com.squareup.picasso.Picasso;
import com.atahani.telepathy.model.TelepathyModel;
import com.atahani.telepathy.network.TRestService;
import com.atahani.telepathy.network.TService;
import com.atahani.telepathy.realm.MessageModelRealm;
import com.atahani.telepathy.realm.TelepathyModelRealm;
import com.atahani.telepathy.realm.UserModelRealm;
import com.atahani.telepathy.ui.DashboardActivity;
import com.atahani.telepathy.ui.utility.Constants;
import com.atahani.telepathy.ui.utility.CropCircleTransformation;
import com.atahani.telepathy.ui.utility.TimeUtils;
import com.atahani.telepathy.utility.AppPreferenceTools;
import com.atahani.telepathy.utility.ThemeUtil;

import io.realm.Realm;
import io.realm.RealmResults;
import me.leolin.shortcutbadger.ShortcutBadger;
import mobi.atahani.telepathy.R;
import com.atahani.telepathy.TApplication;
import com.atahani.telepathy.model.MessageModel;
import com.atahani.telepathy.model.TOperationResultModel;
import com.atahani.telepathy.realm.NotificationRealmModel;
import com.atahani.telepathy.realm.UserMessageNotificationRealmModel;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

/**
 * the telepathy GCM listener to get GCM message and send notifications
 */
public class TGcmListenerService extends GcmListenerService {


    private AppPreferenceTools mAppPreferenceTools;
    private LocalBroadcastManager mLocalBroadcastManager;

    @Override
    public void onCreate() {
        super.onCreate();
        mAppPreferenceTools = new AppPreferenceTools(this);
        mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);
    }

    /**
     * Called when message is received
     *
     * @param from SenderID of the sender.
     * @param data Data bundle containing message data as key/value pairs.
     *             For Set of keys use data.keySet().
     */
    @Override
    public void onMessageReceived(String from, Bundle data) {
        try {
            int pnType = Integer.parseInt(data.getString("type"));
            switch (pnType) {
                case Constants.TELEPATHIES_MATCHED_PN_TYPE:
                    if (data.getString("message_id") != null && data.getString("from_user_display_name") != null && data.getString("from_user_image_url") != null && data.getString("matched_at") != null && data.getString("matched_in_sec") != null) {
                        processTelepathiesMatchedPN(data);
                    }
                    break;
                case Constants.MESSAGE_RECEIVE_PN_TYPE:
                    if (data.getString("message_id") != null && data.getString("updated_at") != null) {
                        updateMessageStatusInDB(data.getString("message_id"), pnType, new Date(Long.parseLong(data.getString("updated_at", "0"))));
                    }
                    break;
                case Constants.MESSAGE_READ_PN_TYPE:
                    if (data.getString("message_id") != null && data.getString("updated_at") != null) {
                        updateMessageStatusInDB(data.getString("message_id"), pnType, new Date(Long.parseLong(data.getString("updated_at", "0"))));
                    }
                    break;
                case Constants.ALL_USER_MESSAGE_READ_PN_TYPE:
                    if (data.getString("from_user_id") != null && data.getString("updated_at") != null) {
                        updateAllMessageAsReadFromUserId(data.getString("from_user_id"), new Date(Long.parseLong(data.getString("updated_at", "0"))));
                    }
                    break;
                case Constants.FRIEND_UPDATE_PROFILE_PN_TYPE:
                    //it only update friends information in DB
                    updateFriendInformationInDB(data.getString("friend_user_id", ""),
                            data.getString("friend_username", ""),
                            data.getString("friend_display_name", ""),
                            data.getString("friend_image_profile_url", ""),
                            data.getString("friend_theme", ""));
                    break;
                case Constants.USER_PROFILE_UPDATE_PN_TYPE:
                    if (data.getString("display_name") != null && data.getString("image_profile_url") != null && data.getString("locale") != null && data.getString("theme") != null) {
                        updateUserProfile(data.getString("username"), data.getString("display_name"), data.getString("image_profile_url"), data.getString("locale"), data.getString("theme"));
                    }
                    break;
                case Constants.NEW_TELEPATHY_SEND_IT_PN_TYPE:
                    if (data.getString("new_telepathy_id") != null) {
                        shouldInsertNewTelepathy(data.getString("new_telepathy_id"));
                    }
                    break;
                case Constants.TELEPATHY_DELETED_PN_TYPE:
                    if (data.getString("deleted_telepathy_id") != null) {
                        shouldDeleteTelepathy(data.getString("deleted_telepathy_id"));
                    }
                    break;
                case Constants.MESSAGE_DELETED_PN_TYPE:
                    if (data.getString("deleted_message_id") != null) {
                        oneMessageShouldBeDeleteById(data.getString("deleted_message_id"));
                    }
                    break;
                case Constants.USER_MESSAGE_DELETED_PN_TYPE:
                    if (data.getString("user_id") != null) {
                        deleteAllUserMessageByUserId(data.getString("user_id"));
                    }
                    break;
                case Constants.USER_MESSAGE_ALREADY_READ_BY_USER_PN_TYPE:
                    if (data.getString("user_id") != null) {
                        allUserMessageAlreadyRead(data.getString("user_id"));
                    }
                    break;
                case Constants.USER_ADD_AS_FRIEND_PN_TYPE:
                    if (data.getString("friend_user_id") != null && data.getString("friend_display_name") != null && data.getString("friend_image_profile_url") != null && data.getString("friend_theme") != null) {
                        shouldAddThisUserAsFriend(data.getString("friend_user_id"),
                                data.getString("friend_username"),
                                data.getString("friend_display_name"),
                                data.getString("friend_image_profile_url"),
                                data.getString("friend_theme"));
                    }
                    break;
                case Constants.USER_REMOVE_AS_FRIEND_PN_TYPE:
                    if (data.getString("friend_user_id") != null) {
                        shouldRemoveThisUserAsFriend(data.getString("friend_user_id"));
                    }
                    break;
                case Constants.USER_DELETE_PN_TYPE:
                    if (data.getString("deleted_user_id") != null) {
                        userAccountDeleted(data.getString("deleted_user_id"));
                    }
                    break;
                case Constants.TERMINATE_APP_PN_TYPE:
                    terminateThisAndSignOut();
                    break;
                case Constants.GOT_USER_MESSAGE_PN_TYPE:
                    if (data.getString("new_message_id") != null) {
                        shouldUpdateUserMessage(data.getString("new_message_id"));
                    }
                    break;
                case Constants.ADD_YOU_AS_FRIEND:
                    if (data.getString("friend_user_id") != null && data.getString("friend_username") != null && data.getString("friend_display_name") != null && data.getString("friend_image_url") != null && data.getString("friend_theme") != null && data.getString("add_friend_at") != null) {
                        processPNWhenAddYouAsFriend(
                                data.getString("friend_user_id"),
                                data.getString("friend_username"),
                                data.getString("friend_display_name"),
                                data.getString("friend_image_url"),
                                data.getString("friend_theme"),
                                Long.parseLong(data.getString("add_friend_at", "0")));
                    }
                    break;
            }
        } catch (NumberFormatException ex) {
        }
    }

    private void processTelepathiesMatchedPN(final Bundle data) {
        TRestService tRestService = new TRestService();
        //send receive token
        TService tService = tRestService.getTService();
        tService.patchMessageAsReceive(data.getString("matched_with_telepathy_id"), data.getString("from_user_id"), new Callback<TOperationResultModel>() {
            @Override
            public void success(TOperationResultModel tOperationResultModel, Response response) {
                //do nothing
            }

            @Override
            public void failure(RetrofitError error) {
                //do nothing
            }
        });

        Realm mRealmDB = Realm.getDefaultInstance();
        //increment badger number then push notification
        incrementBadgerNumber();
        //if user not in dashboard activity so show the notification
        if (mAppPreferenceTools.getCurrentTabInDashboard() == Constants.MESSAGES_TAB) {
            //it's mean the user currently on dashboard activity and should send signal to update from net
            Intent telepathyIntent = new Intent(Constants.ACTION_TO_DO_FOR_TELEPATHY_UPDATE_INTENT_FILTER);
            telepathyIntent.putExtra(Constants.ACTION_TO_DO_PARAM, Constants.UPDATE_FROM_NET);
            mLocalBroadcastManager.sendBroadcast(telepathyIntent);

            Intent messageIntent = new Intent(Constants.ACTION_TO_DO_FOR_MESSAGE_UPDATE_INTENT_FILTER);
            messageIntent.putExtra(Constants.ACTION_TO_DO_PARAM, Constants.UPDATE_FROM_NET);
            mLocalBroadcastManager.sendBroadcast(messageIntent);
        } else {
            //first check have any message with this message_id or not
            MessageModelRealm existMessage = mRealmDB.where(MessageModelRealm.class).equalTo("messageId", data.getString("message_id")).findFirst();
            //send notification when notification is enable and message do'nt exist in DB
            if (mAppPreferenceTools.isNotificationEnableWhenTelepathiesMatched() && existMessage == null) {
                sendTelepathyNotification(data.getString("from_user_id")
                        , data.getString("from_user_username")
                        , data.getString("from_user_display_name")
                        , data.getString("from_user_image_url")
                        , data.getString("from_user_theme")
                        , Long.parseLong(data.getString("matched_at", "0"))
                        , Integer.parseInt(data.getString("matched_in_sec", "0")));
            }
            //then since the application not open at this time get message detail information and save it to DB
            //then get message from server by messageId and update it to RealmDB
            if (existMessage == null) {
                tService.getMessageById(data.getString("message_id"), new Callback<MessageModel>() {
                    @Override
                    public void success(final MessageModel messageModel, Response response) {
                        final Realm realmInner = Realm.getDefaultInstance();
                        realmInner.executeTransaction(new Realm.Transaction() {
                            @Override
                            public void execute(Realm realm) {
                                //remove matched telepathy if exist
                                TelepathyModelRealm matchedTelepathy = realm.where(TelepathyModelRealm.class).equalTo("telepathyId", messageModel.matched_with_telepathy_id).findFirst();
                                if (matchedTelepathy != null) {
                                    matchedTelepathy.removeFromRealm();
                                }
                                //check is message model already exist
                                MessageModelRealm existMessageModel = realm.where(MessageModelRealm.class).equalTo("messageId", messageModel.id).findFirst();
                                if (existMessageModel == null) {
                                    MessageModelRealm newMessageModelForDB = realm.createObject(MessageModelRealm.class);
                                    newMessageModelForDB.setMessageId(messageModel.id);
                                    newMessageModelForDB.setWithUserId(messageModel.with_user_id);
                                    newMessageModelForDB.setBodySend(messageModel.body_send);
                                    newMessageModelForDB.setBodyReceive(messageModel.body_receive);
                                    newMessageModelForDB.setYouAreFirst(messageModel.you_are_first);
                                    newMessageModelForDB.setMatchedAt(messageModel.matched_at);
                                    newMessageModelForDB.setMatchedWithTelepathyId(messageModel.matched_with_telepathy_id);
                                    newMessageModelForDB.setMatchedInSec(messageModel.matched_in_sec);
                                    newMessageModelForDB.setIsReceive(messageModel.is_receive);
                                    newMessageModelForDB.setIsRead(messageModel.is_read);
                                    newMessageModelForDB.setIsSendReadSignal(messageModel.is_send_read_signal);
                                    newMessageModelForDB.setUpdatedAt(messageModel.updated_at);
                                }
                            }
                        }, new Realm.Transaction.Callback() {
                            @Override
                            public void onSuccess() {
                                super.onSuccess();
                                realmInner.close();
                                Intent telepathyIntent = new Intent(Constants.ACTION_TO_DO_FOR_TELEPATHY_UPDATE_INTENT_FILTER);
                                telepathyIntent.putExtra(Constants.ACTION_TO_DO_PARAM, Constants.UPDATE_FROM_DB);
                                mLocalBroadcastManager.sendBroadcast(telepathyIntent);

                                Intent messageIntent = new Intent(Constants.ACTION_TO_DO_FOR_MESSAGE_UPDATE_INTENT_FILTER);
                                messageIntent.putExtra(Constants.ACTION_TO_DO_PARAM, Constants.UPDATE_FROM_DB);
                                mLocalBroadcastManager.sendBroadcast(messageIntent);
                            }

                            @Override
                            public void onError(Exception e) {
                                super.onError(e);
                                realmInner.close();
                            }
                        });
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        if (mLocalBroadcastManager != null) {
                            Intent telepathyIntent = new Intent(Constants.ACTION_TO_DO_FOR_TELEPATHY_UPDATE_INTENT_FILTER);
                            telepathyIntent.putExtra(Constants.ACTION_TO_DO_PARAM, Constants.UPDATE_FROM_NET);
                            mLocalBroadcastManager.sendBroadcast(telepathyIntent);

                            Intent messageIntent = new Intent(Constants.ACTION_TO_DO_FOR_MESSAGE_UPDATE_INTENT_FILTER);
                            messageIntent.putExtra(Constants.ACTION_TO_DO_PARAM, Constants.UPDATE_FROM_NET);
                            mLocalBroadcastManager.sendBroadcast(messageIntent);
                        }
                    }
                });
            } else {
                Intent telepathyIntent = new Intent(Constants.ACTION_TO_DO_FOR_TELEPATHY_UPDATE_INTENT_FILTER);
                telepathyIntent.putExtra(Constants.ACTION_TO_DO_PARAM, Constants.UPDATE_FROM_DB);
                mLocalBroadcastManager.sendBroadcast(telepathyIntent);

                Intent messageIntent = new Intent(Constants.ACTION_TO_DO_FOR_MESSAGE_UPDATE_INTENT_FILTER);
                messageIntent.putExtra(Constants.ACTION_TO_DO_PARAM, Constants.UPDATE_FROM_DB);
                mLocalBroadcastManager.sendBroadcast(messageIntent);
            }
        }
        mRealmDB.close();
    }

    /**
     * send telepathy notification notification
     * set in message update with this messageId in AppKnowAppTools
     */
    private void sendTelepathyNotification(String fromUserId, String fromUserUsername, String fromUserDisplayName, String fromUserImageURL, String fromUserTheme, Long matchedAt, int matchedInSec) {
        TimeUtils timeUtils = new TimeUtils(this);
        Realm realm = Realm.getDefaultInstance();
        UserMessageNotificationRealmModel userNotificationRealmModel;
        userNotificationRealmModel = realm.where(UserMessageNotificationRealmModel.class).equalTo("fromUserId", fromUserId).findFirst();
        realm.beginTransaction();
        if (userNotificationRealmModel == null) {
            userNotificationRealmModel = realm.createObject(UserMessageNotificationRealmModel.class);
            userNotificationRealmModel.setNotificationId(getLastNotificationId(realm));
            userNotificationRealmModel.setFromUserId(fromUserId);
            userNotificationRealmModel.setNumberOfNotification(1);
            userNotificationRealmModel.setNotificationBody(String.format(getString(R.string.label_telepathies_matched), timeUtils.getMatchedInTime(matchedInSec)));
        } else {
            userNotificationRealmModel.setNumberOfNotification(userNotificationRealmModel.getNumberOfNotification() + 1);
            userNotificationRealmModel.setNotificationBody(userNotificationRealmModel.getNotificationBody() + "\n" + String.format(getString(R.string.label_telepathies_matched), timeUtils.getMatchedInTime(matchedInSec)));
        }
        realm.commitTransaction();
        Intent intentForOpenMessage = new Intent(this, DashboardActivity.class);
        intentForOpenMessage.putExtra(Constants.PENDING_INTENT_TYPE, Constants.PENDING_OPEN_MESSAGE);
        intentForOpenMessage.putExtra(Constants.WITH_USER_ID_PARAM, fromUserId);
        intentForOpenMessage.putExtra(Constants.WITH_USER_DISPLAY_NAME_PARAM, fromUserDisplayName);
        intentForOpenMessage.putExtra(Constants.WITH_USER_USERNAME_PARAM, fromUserUsername);
        intentForOpenMessage.putExtra(Constants.WITH_USER_IMAGE_URL_PARAM, fromUserImageURL);
        intentForOpenMessage.putExtra(Constants.WITH_USER_THEME_PARAM, fromUserTheme);
        intentForOpenMessage.putExtra(Constants.NUMBER_OF_USER_MESSAGE, -1);
        //add flag to clear top
        intentForOpenMessage.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intentForOpenMessage.setAction(Long.toString(System.currentTimeMillis()));
        PendingIntent pendingIntent = PendingIntent.getActivity(this, Constants.PENDING_OPEN_MESSAGE, intentForOpenMessage,
                PendingIntent.FLAG_UPDATE_CURRENT);
        Uri notificationRingtone = null;
        if (mAppPreferenceTools.getNotificationRingtoneUri().length() != 0) {
            notificationRingtone = Uri.parse(mAppPreferenceTools.getNotificationRingtoneUri());
        }
        final NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_telepathy_notification)
                .setContentTitle(fromUserDisplayName)
                .setContentText(String.format(getString(R.string.label_telepathies_matched), timeUtils.getMatchedInTime(matchedInSec)))
                .setTicker(getString(R.string.label_telepathy_matched_notification))
                .setShowWhen(true)
                .setWhen(matchedAt)
                .setAutoCancel(true)
                .setSound(notificationRingtone)
                .setContentIntent(pendingIntent);
        notificationBuilder.setStyle(new NotificationCompat.BigTextStyle());
        notificationBuilder.setColor(mAppPreferenceTools.getPrimaryColor());
        if (userNotificationRealmModel.getNumberOfNotification() > 1) {
            notificationBuilder.setNumber(userNotificationRealmModel.getNumberOfNotification());
            notificationBuilder.setGroupSummary(true);
            notificationBuilder.setGroup(fromUserId);
            NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
            String notificationBodyList[] = userNotificationRealmModel.getNotificationBody().split("\n");
            for (String body : notificationBodyList) {
                inboxStyle.addLine(body);
            }
            inboxStyle.setSummaryText(String.format(getString(R.string.label_telepathy_matched_notification_summary_text), userNotificationRealmModel.getNumberOfNotification()));
            inboxStyle.setBigContentTitle(getString(R.string.label_telepathy_matched_notification));
            notificationBuilder.setStyle(inboxStyle);
        }
        Bitmap userImageProfileBitmap = null;
        try {
            userImageProfileBitmap = Picasso.with(this).load(fromUserImageURL).transform(new CropCircleTransformation()).get();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (userImageProfileBitmap != null) {
                notificationBuilder.setLargeIcon(userImageProfileBitmap);
            }
        }
        if (mAppPreferenceTools.vibrateOnNotificationReceive()) {
            notificationBuilder.setVibrate(new long[]{0l});
        }
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(TApplication.applicationContext);
        //notify with notificationId
        notificationManager.notify(userNotificationRealmModel.getNotificationId(), notificationBuilder.build());
        realm.close();
    }

    private int getLastNotificationId(Realm realm) {
        return realm.where(UserMessageNotificationRealmModel.class).max("notificationId").intValue() + 1;
    }

    /**
     * when got new push notification for telepathy increment the badger number
     */
    private void incrementBadgerNumber() {
        ShortcutBadger.with(getApplicationContext()).count(mAppPreferenceTools.incrementApplicationBadgerNumber());
    }

    /**
     * update message status in DB
     *
     * @param messageId String messageId should update in DB
     * @param pnType    the pnType
     * @param updatedAt Date the updatedAt message status
     */
    private void updateMessageStatusInDB(String messageId, int pnType, Date updatedAt) {
        Realm mRealmDB = Realm.getDefaultInstance();
        MessageModelRealm messageModelRealm = mRealmDB.where(MessageModelRealm.class).equalTo("messageId", messageId).findFirst();
        if (messageModelRealm != null) {
            mRealmDB.beginTransaction();
            if (pnType == Constants.MESSAGE_READ_PN_TYPE) {
                messageModelRealm.setIsRead(true);
                messageModelRealm.setIsReceive(true);
                messageModelRealm.setUpdatedAt(updatedAt);
            } else if (pnType == Constants.MESSAGE_RECEIVE_PN_TYPE) {
                messageModelRealm.setIsReceive(true);
                messageModelRealm.setUpdatedAt(updatedAt);
            }
            mRealmDB.commitTransaction();
            //set signal for update from DB
            Intent messageIntent = new Intent(Constants.ACTION_TO_DO_FOR_MESSAGE_UPDATE_INTENT_FILTER);
            messageIntent.putExtra(Constants.ACTION_TO_DO_PARAM, Constants.UPDATE_FROM_DB);
            mLocalBroadcastManager.sendBroadcast(messageIntent);
        }
        mRealmDB.close();
    }

    /**
     * update all of the messages as read from user id
     *
     * @param from_user_id
     * @param updatedAt
     */
    private void updateAllMessageAsReadFromUserId(String from_user_id, Date updatedAt) {
        Realm realm = Realm.getDefaultInstance();
        ArrayList<String> listOfMessageId = new ArrayList<>();
        RealmResults<MessageModelRealm> messageModelRealmResult = realm.where(MessageModelRealm.class).equalTo("withUserId", from_user_id).equalTo("isRead", false).findAll();
        for (int i = 0; i < messageModelRealmResult.size(); i++) {
            listOfMessageId.add(messageModelRealmResult.get(i).getMessageId());
        }
        realm.beginTransaction();
        for (int j = 0; j < listOfMessageId.size(); j++) {
            //get the realm message in DB by id
            MessageModelRealm messageModelRealm = realm.where(MessageModelRealm.class).equalTo("messageId", listOfMessageId.get(j)).findFirst();
            messageModelRealm.setIsRead(true);
            messageModelRealm.setIsReceive(true);
            messageModelRealm.setUpdatedAt(updatedAt);
        }
        realm.commitTransaction();
        realm.close();
        //set signal for update from DB
        Intent messageIntent = new Intent(Constants.ACTION_TO_DO_FOR_MESSAGE_UPDATE_INTENT_FILTER);
        messageIntent.putExtra(Constants.ACTION_TO_DO_PARAM, Constants.UPDATE_FROM_DB);
        mLocalBroadcastManager.sendBroadcast(messageIntent);
    }

    /**
     * update friend information in realDB after got notification with data values
     * also update messages with user information in messages in DB
     *
     * @param friendId    String friendId
     * @param username    String username
     * @param displayName String displayName
     * @param imageUrl    String imageUrl
     */
    private void updateFriendInformationInDB(String friendId, String username, String displayName, String imageUrl, String theme) {
        Realm mRealmDB = Realm.getDefaultInstance();
        UserModelRealm friendModelRealm = mRealmDB.where(UserModelRealm.class).equalTo("userId", friendId).findFirst();
        RealmResults<TelepathyModelRealm> telepathyResultByThisUser = mRealmDB.where(TelepathyModelRealm.class).equalTo("withUserId", friendId).findAll();
        if (!displayName.equals("") && !imageUrl.equals("") && !theme.equals("")) {
            mRealmDB.beginTransaction();
            if (friendModelRealm != null) {
                if (!username.equals("")) {
                    friendModelRealm.setUsername(username);
                }
                friendModelRealm.setDisplayName(displayName);
                friendModelRealm.setImageUrl(imageUrl);
                friendModelRealm.setTheme(theme);
            }
            for (int i = 0; i < telepathyResultByThisUser.size(); i++) {
                if (!username.equals("")) {
                    telepathyResultByThisUser.get(i).setWithUserUsername(username);
                }
                telepathyResultByThisUser.get(i).setWithUserDisplayName(displayName);
                telepathyResultByThisUser.get(i).setWithUserImageUrl(imageUrl);
                telepathyResultByThisUser.get(i).setWithUserTheme(theme);
            }
            mRealmDB.commitTransaction();
            mRealmDB.close();
        }
    }

    /**
     * update user profile values in pref
     *
     * @param username
     * @param displayName
     * @param imageProfileUrl
     * @param theme
     */
    private void updateUserProfile(String username, String displayName, String imageProfileUrl, String locale, String theme) {
        //save the information into pref
        mAppPreferenceTools.updateDisplayNameAndImageProfileUrlFromPN(displayName, imageProfileUrl);
        if (!username.equals("")) {
            mAppPreferenceTools.updateUserNameFromPN(username);
        }
        Intent userProfileUpdateIntent = new Intent(Constants.ACTION_TO_DO_FOR_PROFILE_UPDATE_INTENT_FILTER);
        //check is theme changed
        if (!mAppPreferenceTools.getCurrentThemeName().equals(theme) || !mAppPreferenceTools.getApplicationLocale().equals(locale)) {
            mAppPreferenceTools.setApplicationLocale(locale);
            //change the current locale of application
            TApplication.updateLocale();
            //change the current theme
            ThemeUtil themeUtil = new ThemeUtil(getBaseContext());
            themeUtil.setCurrentTheme(theme);
            mAppPreferenceTools.setIsShouldRecreateWhenOtherChangedProfile(true);
            userProfileUpdateIntent.putExtra(Constants.ACTION_TO_DO_PARAM, Constants.RECREAT_ACTIVITY_WHEN_LOCALE_OR_THEME_CHANGED);
        } else {
            userProfileUpdateIntent.putExtra(Constants.ACTION_TO_DO_PARAM, Constants.UPDATE_USER_PROFILE);
        }
        mLocalBroadcastManager.sendBroadcast(userProfileUpdateIntent);
        //send signal to update profile recreate or just update it
    }

    /**
     * have new telepathy and should get it from server and insert into locale DB
     * and send update signal to update from DB in telepathy fragment
     *
     * @param newTelepathyId String new telepathy id
     */
    private void shouldInsertNewTelepathy(String newTelepathyId) {
        //check is have already this telepathy in DB
        Realm realm = Realm.getDefaultInstance();
        TelepathyModelRealm telepathyModelRealm = realm.where(TelepathyModelRealm.class).equalTo("telepathyId", newTelepathyId).findFirst();
        realm.close();
        if (telepathyModelRealm == null) {
            if (mAppPreferenceTools.getCurrentTabInDashboard() != Constants.TELEPATHY_TAB) {
                //get the telepathy by id from server and add it to db
                TRestService tRestService = new TRestService();
                //send receive token
                final TService tService = tRestService.getTService();
                tService.getTelepathyById(newTelepathyId, new Callback<TelepathyModel>() {
                    @Override
                    public void success(final TelepathyModel telepathyModel, Response response) {
                        //save it to database and send signal to update from DB
                        //check is already in db or not
                        final Realm realmInner = Realm.getDefaultInstance();
                        realmInner.executeTransaction(new Realm.Transaction() {
                            @Override
                            public void execute(Realm realm) {
                                TelepathyModelRealm telepathyModelRealmInner = realm.where(TelepathyModelRealm.class).equalTo("telepathyId", telepathyModel.id).findFirst();
                                if (telepathyModelRealmInner == null) {
                                    TelepathyModelRealm newTelepathyModelRealm = realm.createObject(TelepathyModelRealm.class);
                                    newTelepathyModelRealm.setTelepathyId(telepathyModel.id);
                                    newTelepathyModelRealm.setWithUserId(telepathyModel.to_user.user_id);
                                    newTelepathyModelRealm.setWithUserUsername(telepathyModel.to_user.username);
                                    newTelepathyModelRealm.setWithUserDisplayName(telepathyModel.to_user.display_name);
                                    newTelepathyModelRealm.setWithUserImageUrl(telepathyModel.to_user.image_url);
                                    newTelepathyModelRealm.setWithUserTheme(telepathyModel.to_user.theme);
                                    newTelepathyModelRealm.setBody(telepathyModel.body);
                                    newTelepathyModelRealm.setCreatedAt(telepathyModel.created_at);
                                    newTelepathyModelRealm.setExpireAt(telepathyModel.expire_at);
                                }
                            }
                        }, new Realm.Transaction.Callback() {
                            @Override
                            public void onSuccess() {
                                super.onSuccess();
                                realmInner.close();
                                //should get the telepathy from DB
                                Intent telepathyUpdateIntent = new Intent(Constants.ACTION_TO_DO_FOR_TELEPATHY_UPDATE_INTENT_FILTER);
                                telepathyUpdateIntent.putExtra(Constants.ACTION_TO_DO_PARAM, Constants.UPDATE_FROM_DB);
                                mLocalBroadcastManager.sendBroadcast(telepathyUpdateIntent);
                            }

                            @Override
                            public void onError(Exception e) {
                                super.onError(e);
                                realmInner.close();
                            }
                        });
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        //should get the recent telepathy from server
                        Intent telepathyUpdateIntent = new Intent(Constants.ACTION_TO_DO_FOR_TELEPATHY_UPDATE_INTENT_FILTER);
                        telepathyUpdateIntent.putExtra(Constants.ACTION_TO_DO_PARAM, Constants.UPDATE_FROM_NET);
                        mLocalBroadcastManager.sendBroadcast(telepathyUpdateIntent);
                    }
                });
            } else {
                //should get the recent telepathy from server
                Intent telepathyUpdateIntent = new Intent(Constants.ACTION_TO_DO_FOR_TELEPATHY_UPDATE_INTENT_FILTER);
                telepathyUpdateIntent.putExtra(Constants.ACTION_TO_DO_PARAM, Constants.UPDATE_FROM_NET);
                mLocalBroadcastManager.sendBroadcast(telepathyUpdateIntent);

            }
        }
    }

    /**
     * the telepathy deleted in other device should delete telepathy from locale DB
     *
     * @param deletedTelepathyId
     */
    private void shouldDeleteTelepathy(String deletedTelepathyId) {
        //first check is have this telepathy in DB or not
        Realm realm = Realm.getDefaultInstance();
        TelepathyModelRealm telepathyModelRealm = realm.where(TelepathyModelRealm.class).equalTo("telepathyId", deletedTelepathyId).findFirst();
        if (telepathyModelRealm != null) {
            realm.beginTransaction();
            telepathyModelRealm.removeFromRealm();
            realm.commitTransaction();
        }
        realm.close();
        //should get the telepathy from DB
        Intent telepathyUpdateIntent = new Intent(Constants.ACTION_TO_DO_FOR_TELEPATHY_UPDATE_INTENT_FILTER);
        telepathyUpdateIntent.putExtra(Constants.ACTION_TO_DO_PARAM, Constants.UPDATE_FROM_DB);
        mLocalBroadcastManager.sendBroadcast(telepathyUpdateIntent);
    }

    /**
     * the message deleted in other device so should delete message by id from locale DB
     *
     * @param deletedMessageId
     */
    private void oneMessageShouldBeDeleteById(String deletedMessageId) {
        //first check is have this message in DB or not
        Realm realm = Realm.getDefaultInstance();
        MessageModelRealm messageModelRealm = realm.where(MessageModelRealm.class).equalTo("messageId", deletedMessageId).findFirst();
        if (messageModelRealm != null) {
            realm.beginTransaction();
            messageModelRealm.removeFromRealm();
            realm.commitTransaction();
        }
        realm.close();
        //check if user in message tab should update classify message from server
        if (mAppPreferenceTools.getCurrentTabInDashboard() == Constants.MESSAGES_TAB) {
            Intent classifyMessageIntent = new Intent(Constants.ACTION_TO_DO_FOR_CLASSIFY_MESSAGE_UPDATE_INTENT_FILTER);
            classifyMessageIntent.putExtra(Constants.ACTION_TO_DO_PARAM, Constants.UPDATE_FROM_NET);
            mLocalBroadcastManager.sendBroadcast(classifyMessageIntent);
        }
        //set signal for update from DB
        Intent messageIntent = new Intent(Constants.ACTION_TO_DO_FOR_MESSAGE_UPDATE_INTENT_FILTER);
        messageIntent.putExtra(Constants.ACTION_TO_DO_PARAM, Constants.UPDATE_FROM_DB);
        mLocalBroadcastManager.sendBroadcast(messageIntent);
    }

    /**
     * the all user message deleted in other device so should delete all message from this user
     *
     * @param userId
     */
    private void deleteAllUserMessageByUserId(String userId) {
        Realm realm = Realm.getDefaultInstance();
        RealmResults<MessageModelRealm> messageFromThisUserResult = realm.where(MessageModelRealm.class).equalTo("withUserId", userId).findAll();
        if (messageFromThisUserResult.size() > 0) {
            realm.beginTransaction();
            messageFromThisUserResult.clear();
            realm.commitTransaction();
        }
        realm.close();
        //set signal for update from DB
        Intent messageIntent = new Intent(Constants.ACTION_TO_DO_FOR_MESSAGE_UPDATE_INTENT_FILTER);
        messageIntent.putExtra(Constants.ACTION_TO_DO_PARAM, Constants.UPDATE_FROM_DB);
        mLocalBroadcastManager.sendBroadcast(messageIntent);
    }

    /**
     * user read all user message in other device so should clear unread indicator from all user message
     *
     * @param userId
     */
    private void allUserMessageAlreadyRead(String userId) {
        Realm realm = Realm.getDefaultInstance();
        RealmResults<MessageModelRealm> unreadMessageForIterating = realm.where(MessageModelRealm.class).equalTo("withUserId", userId).equalTo("youAreFirst", true).equalTo("isSendReadSignal", false).findAll();
        ArrayList<String> listOfMessageId = new ArrayList<>();
        for (int i = 0; i < unreadMessageForIterating.size(); i++) {
            listOfMessageId.add(unreadMessageForIterating.get(i).getMessageId());
        }
        if (listOfMessageId.size() > 0) {
            realm.beginTransaction();
            for (int i = 0; i < listOfMessageId.size(); i++) {
                MessageModelRealm messageModelRealm = realm.where(MessageModelRealm.class).equalTo("messageId", listOfMessageId.get(i)).findFirst();
                if (messageModelRealm != null) {
                    messageModelRealm.setIsSendReadSignal(true);
                }
            }
            realm.commitTransaction();
        }
        //clear notification and badger number for this user
        UserMessageNotificationRealmModel notificationRealmModelByUserId = realm.where(UserMessageNotificationRealmModel.class).equalTo("fromUserId", userId).findFirst();
        if (notificationRealmModelByUserId != null) {
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(TApplication.applicationContext);
            notificationManager.cancel(notificationRealmModelByUserId.getNotificationId());
            realm.beginTransaction();
            notificationRealmModelByUserId.setNumberOfNotification(0);
            notificationRealmModelByUserId.setNotificationBody("");
            realm.commitTransaction();
        }
        mAppPreferenceTools.setApplicationBadgerNumber(0);
        ShortcutBadger.with(TApplication.applicationContext).count(0);
        realm.close();
        //set signal for update from DB
        Intent messageIntent = new Intent(Constants.ACTION_TO_DO_FOR_MESSAGE_UPDATE_INTENT_FILTER);
        messageIntent.putExtra(Constants.ACTION_TO_DO_PARAM, Constants.UPDATE_FROM_DB);
        mLocalBroadcastManager.sendBroadcast(messageIntent);
    }

    /**
     * should add this user as friend since in other device this user added as friend
     *
     * @param friendId
     * @param friendUsername
     * @param friendDisplayName
     * @param friendImageProfileUrl
     * @param friendTheme
     */
    private void shouldAddThisUserAsFriend(String friendId, String friendUsername, String friendDisplayName, String friendImageProfileUrl, String friendTheme) {
        //check is already in db
        Realm realm = Realm.getDefaultInstance();
        UserModelRealm userModelRealm = realm.where(UserModelRealm.class).equalTo("userId", friendId).findFirst();
        if (userModelRealm == null) {
            //add this user as friend to db
            realm.beginTransaction();
            UserModelRealm friendUserModelRealm = realm.createObject(UserModelRealm.class);
            friendUserModelRealm.setUserId(friendId);
            friendUserModelRealm.setUsername(friendUsername);
            friendUserModelRealm.setDisplayName(friendDisplayName);
            friendUserModelRealm.setImageUrl(friendImageProfileUrl);
            friendUserModelRealm.setTheme(friendTheme);
            friendUserModelRealm.setIsFriend(true);
            realm.commitTransaction();
        } else {
            realm.beginTransaction();
            userModelRealm.setIsFriend(true);
            realm.commitTransaction();
        }
        realm.close();
        //set signal for update friend from DB
        Intent messageIntent = new Intent(Constants.ACTION_TO_DO_FOR_FRIEND_UPDATE_INTENT_FILTER);
        messageIntent.putExtra(Constants.ACTION_TO_DO_PARAM, Constants.UPDATE_FROM_DB);
        mLocalBroadcastManager.sendBroadcast(messageIntent);
    }

    /**
     * should remove this user as friend since in other device this user removed as friend
     *
     * @param friendUserId
     */
    private void shouldRemoveThisUserAsFriend(String friendUserId) {
        //check if have it in db remove it as friend
        Realm realm = Realm.getDefaultInstance();
        UserModelRealm friendModelRealm = realm.where(UserModelRealm.class).equalTo("isFriend", true).equalTo("userId", friendUserId).findFirst();
        if (friendModelRealm != null) {
            realm.beginTransaction();
            friendModelRealm.setIsFriend(false);
            //check if do not have any message with this userId remove it from DB
            long numberOfUserMessage = realm.where(MessageModelRealm.class).equalTo("withUserId", friendUserId).count();
            if (numberOfUserMessage == 0) {
                friendModelRealm.removeFromRealm();
            }
            realm.commitTransaction();
        }
        realm.close();
        //set signal for update friend from DB
        Intent messageIntent = new Intent(Constants.ACTION_TO_DO_FOR_FRIEND_UPDATE_INTENT_FILTER);
        messageIntent.putExtra(Constants.ACTION_TO_DO_PARAM, Constants.UPDATE_FROM_DB);
        mLocalBroadcastManager.sendBroadcast(messageIntent);
    }

    /**
     * user account deleted and should take changes in DB
     *
     * @param deletedUserId
     */
    private void userAccountDeleted(String deletedUserId) {
        Realm realm = Realm.getDefaultInstance();
        UserModelRealm deletedUserRealmModel = realm.where(UserModelRealm.class).equalTo("userId", deletedUserId).findFirst();
        realm.beginTransaction();
        if (deletedUserRealmModel != null) {
            //un friend this user and update values
            deletedUserRealmModel.setIsFriend(false);
            deletedUserRealmModel.setUsername(Constants.DELETED_ACCOUNT_VALUE);
            deletedUserRealmModel.setImageUrl(Constants.DELETED_ACCOUNT_VALUE);
            deletedUserRealmModel.setDisplayName(Constants.DELETED_ACCOUNT_VALUE);
            deletedUserRealmModel.setTheme(Constants.DEFAULT_THEME_NAME);
            //check in telepathy model and set as deleted user
            RealmResults<TelepathyModelRealm> telepathiesWithThisUser = realm.where(TelepathyModelRealm.class).equalTo("withUserId", deletedUserId).findAll();
            for (int j = 0; j < telepathiesWithThisUser.size(); j++) {
                telepathiesWithThisUser.get(j).setWithUserUsername(Constants.DELETED_ACCOUNT_VALUE);
                telepathiesWithThisUser.get(j).setWithUserDisplayName(Constants.DELETED_ACCOUNT_VALUE);
                telepathiesWithThisUser.get(j).setWithUserImageUrl(Constants.DELETED_ACCOUNT_VALUE);
                telepathiesWithThisUser.get(j).setWithUserTheme(Constants.DEFAULT_THEME_NAME);
            }
        }
        realm.commitTransaction();
        realm.close();
        //send signal for update friend and update telepathies from server and also update classify messages from DB
        Intent messageIntent = new Intent(Constants.ACTION_TO_DO_FOR_FRIEND_UPDATE_INTENT_FILTER);
        messageIntent.putExtra(Constants.ACTION_TO_DO_PARAM, Constants.UPDATE_FROM_DB);
        mLocalBroadcastManager.sendBroadcast(messageIntent);
        Intent telepathyIntent = new Intent(Constants.ACTION_TO_DO_FOR_TELEPATHY_UPDATE_INTENT_FILTER);
        telepathyIntent.putExtra(Constants.ACTION_TO_DO_PARAM, Constants.UPDATE_FROM_DB);
        mLocalBroadcastManager.sendBroadcast(telepathyIntent);
        Intent classifyMessageIntent = new Intent(Constants.ACTION_TO_DO_FOR_CLASSIFY_MESSAGE_UPDATE_INTENT_FILTER);
        classifyMessageIntent.putExtra(Constants.ACTION_TO_DO_PARAM, Constants.UPDATE_FROM_DB);
        mLocalBroadcastManager.sendBroadcast(classifyMessageIntent);
    }

    /**
     * terminate this application when got related PN
     */
    private void terminateThisAndSignOut() {
        mAppPreferenceTools.removeAllOfThePref();
        //send signal to base telepathy activity to terminate application
        Intent terminateIntent = new Intent(Constants.TELEPATHY_BASE_ACTIVITY_INTENT_FILTER);
        terminateIntent.putExtra(Constants.ACTION_TO_DO_PARAM, Constants.TERMINATE_APPLICATION);
        mLocalBroadcastManager.sendBroadcast(terminateIntent);
    }


    /**
     * when user got matched message in other device should update the user message in other devices
     *
     * @param newMessageId String newMessageId that should got from server
     */
    private void shouldUpdateUserMessage(final String newMessageId) {
        TRestService tRestService = new TRestService();
        TService tService = tRestService.getTService();
        Realm realm = Realm.getDefaultInstance();
        //if user not in dashboard activity so show the notification
        if (mAppPreferenceTools.getCurrentTabInDashboard() == Constants.MESSAGES_TAB) {
            //it's mean the user currently on dashboard activity and should send signal to update from net
            Intent messageIntent = new Intent(Constants.ACTION_TO_DO_FOR_MESSAGE_UPDATE_INTENT_FILTER);
            messageIntent.putExtra(Constants.ACTION_TO_DO_PARAM, Constants.UPDATE_FROM_NET);
            mLocalBroadcastManager.sendBroadcast(messageIntent);
        } else {
            //first check have any message with this message_id or not
            MessageModelRealm existMessage = realm.where(MessageModelRealm.class).equalTo("messageId", newMessageId).findFirst();
            if (existMessage == null) {
                tService.getMessageById(newMessageId, new Callback<MessageModel>() {
                    @Override
                    public void success(final MessageModel messageModel, Response response) {
                        final Realm realmInner = Realm.getDefaultInstance();
                        realmInner.executeTransaction(new Realm.Transaction() {
                            @Override
                            public void execute(Realm realm) {
                                //check is message model realm already exist
                                MessageModelRealm existMessageModelRealmInner = realm.where(MessageModelRealm.class).equalTo("messageId", newMessageId).findFirst();
                                if (existMessageModelRealmInner == null) {
                                    MessageModelRealm newMessageModelForDB = realm.createObject(MessageModelRealm.class);
                                    newMessageModelForDB.setMessageId(messageModel.id);
                                    newMessageModelForDB.setWithUserId(messageModel.with_user_id);
                                    newMessageModelForDB.setBodySend(messageModel.body_send);
                                    newMessageModelForDB.setBodyReceive(messageModel.body_receive);
                                    newMessageModelForDB.setYouAreFirst(messageModel.you_are_first);
                                    newMessageModelForDB.setMatchedAt(messageModel.matched_at);
                                    newMessageModelForDB.setMatchedWithTelepathyId(messageModel.matched_with_telepathy_id);
                                    newMessageModelForDB.setMatchedInSec(messageModel.matched_in_sec);
                                    newMessageModelForDB.setIsReceive(messageModel.is_receive);
                                    newMessageModelForDB.setIsRead(messageModel.is_read);
                                    newMessageModelForDB.setIsSendReadSignal(messageModel.is_send_read_signal);
                                    newMessageModelForDB.setUpdatedAt(messageModel.updated_at);
                                }
                            }
                        }, new Realm.Transaction.Callback() {
                            @Override
                            public void onSuccess() {
                                super.onSuccess();
                                realmInner.close();
                                Intent messageIntent = new Intent(Constants.ACTION_TO_DO_FOR_MESSAGE_UPDATE_INTENT_FILTER);
                                messageIntent.putExtra(Constants.ACTION_TO_DO_PARAM, Constants.UPDATE_FROM_DB);
                                mLocalBroadcastManager.sendBroadcast(messageIntent);
                            }

                            @Override
                            public void onError(Exception e) {
                                super.onError(e);
                                realmInner.close();
                            }
                        });
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        if (mLocalBroadcastManager != null) {
                            Intent messageIntent = new Intent(Constants.ACTION_TO_DO_FOR_MESSAGE_UPDATE_INTENT_FILTER);
                            messageIntent.putExtra(Constants.ACTION_TO_DO_PARAM, Constants.UPDATE_FROM_NET);
                            mLocalBroadcastManager.sendBroadcast(messageIntent);
                        }
                    }
                });
            } else {
                Intent messageIntent = new Intent(Constants.ACTION_TO_DO_FOR_MESSAGE_UPDATE_INTENT_FILTER);
                messageIntent.putExtra(Constants.ACTION_TO_DO_PARAM, Constants.UPDATE_FROM_DB);
                mLocalBroadcastManager.sendBroadcast(messageIntent);
            }
        }
        realm.close();
    }

    /**
     * send push notification when added you as friend
     * NOTE : friend check is notification added you as friend enabled or not default is enabled
     *
     * @param friendUserId
     * @param friendUsername
     * @param friendDisplayName
     * @param friendImageUrl
     * @param friendTheme
     */
    private void processPNWhenAddYouAsFriend(String friendUserId, String friendUsername, String friendDisplayName, String friendImageUrl, String friendTheme, Long addFriendAt) {
        //check is add you as friend notification enable or not
        if (mAppPreferenceTools.isNotificationEnableWhenAddYouAsFriend()) {
            Realm realm = Realm.getDefaultInstance();

            //check is already have this type of notification
            NotificationRealmModel notificationRealmModel = realm.where(NotificationRealmModel.class).equalTo("tagType", Constants.ADD_YOU_AS_FRIEND_PN_TAG).equalTo("fromUserId", friendUserId).findFirst();
            if (notificationRealmModel == null) {
                realm.beginTransaction();
                //create new notification realm model to store notification values
                NotificationRealmModel newNotificationRealmModel = realm.createObject(NotificationRealmModel.class);
                int lastNotificationId = realm.where(NotificationRealmModel.class).max("notificationId").intValue() + 1;
                newNotificationRealmModel.setNotificationId(lastNotificationId);
                newNotificationRealmModel.setTagType(Constants.ADD_YOU_AS_FRIEND_PN_TAG);
                newNotificationRealmModel.setFromUserId(friendUserId);
                realm.commitTransaction();

                Intent DashboardForUserInfoActivity = new Intent(this, DashboardActivity.class);
                DashboardForUserInfoActivity.putExtra(Constants.WITH_USER_ID_PARAM, friendUserId);
                DashboardForUserInfoActivity.putExtra(Constants.WITH_USER_DISPLAY_NAME_PARAM, friendDisplayName);
                DashboardForUserInfoActivity.putExtra(Constants.WITH_USER_USERNAME_PARAM, friendUsername);
                DashboardForUserInfoActivity.putExtra(Constants.WITH_USER_IMAGE_URL_PARAM, friendImageUrl);
                DashboardForUserInfoActivity.putExtra(Constants.WITH_USER_THEME_PARAM, friendTheme);
                DashboardForUserInfoActivity.putExtra(Constants.PENDING_INTENT_TYPE, Constants.PENDING_OPEN_USER_INFO_DIALOG);
                //add flag to clear top
                DashboardForUserInfoActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                DashboardForUserInfoActivity.setAction(Long.toString(System.currentTimeMillis()));
                PendingIntent pendingIntentForOpenUserInfoDialog = PendingIntent.getActivity(this, Constants.PENDING_OPEN_USER_INFO_DIALOG, DashboardForUserInfoActivity,
                        PendingIntent.FLAG_UPDATE_CURRENT);
                Intent DashboardForStartTelepathy = new Intent(this, DashboardActivity.class);
                DashboardForStartTelepathy.putExtra(Constants.WITH_USER_ID_PARAM, friendUserId);
                DashboardForStartTelepathy.putExtra(Constants.WITH_USER_DISPLAY_NAME_PARAM, friendDisplayName);
                DashboardForStartTelepathy.putExtra(Constants.WITH_USER_USERNAME_PARAM, friendUsername);
                DashboardForStartTelepathy.putExtra(Constants.WITH_USER_IMAGE_URL_PARAM, friendImageUrl);
                DashboardForStartTelepathy.putExtra(Constants.WITH_USER_THEME_PARAM, friendTheme);
                DashboardForStartTelepathy.putExtra(Constants.PENDING_INTENT_TYPE, Constants.PENDING_SEND_TELEPATHY_TO_USER);
                //add flag to clear top
                DashboardForStartTelepathy.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                DashboardForStartTelepathy.setAction(Long.toString(System.currentTimeMillis()));
                PendingIntent pendingIntentForStartTelepathy = PendingIntent.getActivity(this, Constants.PENDING_SEND_TELEPATHY_TO_USER, DashboardForStartTelepathy,
                        PendingIntent.FLAG_UPDATE_CURRENT);
                Uri notificationRingtone = null;
                if (mAppPreferenceTools.getNotificationRingtoneUri().length() != 0) {
                    notificationRingtone = Uri.parse(mAppPreferenceTools.getNotificationRingtoneUri());
                }
                NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle();
                bigTextStyle.bigText(getString(R.string.label_add_you_as_friend));
                final NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_telepathy_notification)
                        .setContentTitle(friendDisplayName)
                        .setContentText(getString(R.string.label_add_you_as_friend))
                        .setStyle(bigTextStyle)
                        .setShowWhen(true)
                        .setWhen(addFriendAt)
                        .setAutoCancel(true)
                        .setSound(notificationRingtone)
                        .setContentIntent(pendingIntentForOpenUserInfoDialog);
                notificationBuilder.setColor(mAppPreferenceTools.getPrimaryColor());
                notificationBuilder.setAutoCancel(true);
                notificationBuilder.addAction(R.mipmap.ic_telepathy_24dp, getString(R.string.action_telepathy), pendingIntentForStartTelepathy);
                Bitmap userImageProfileBitmap = null;
                try {
                    userImageProfileBitmap = Picasso.with(this).load(friendImageUrl).transform(new CropCircleTransformation()).get();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (userImageProfileBitmap != null) {
                        notificationBuilder.setLargeIcon(userImageProfileBitmap);
                    }
                }
                if (mAppPreferenceTools.vibrateOnNotificationReceive()) {
                    notificationBuilder.setVibrate(new long[]{0l});
                }

                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(TApplication.applicationContext);
                //notify with notificationId and TAG
                notificationManager.notify(Constants.ADD_YOU_AS_FRIEND_PN_TAG, lastNotificationId, notificationBuilder.build());
            }
            realm.close();

        }
    }

}