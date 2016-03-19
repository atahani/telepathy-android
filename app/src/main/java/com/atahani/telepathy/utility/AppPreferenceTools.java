package com.atahani.telepathy.utility;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;

import com.atahani.telepathy.model.AuthorizeResponseModel;

import mobi.atahani.telepathy.R;

import com.atahani.telepathy.model.TokenModel;
import com.atahani.telepathy.model.UserProfileModel;
import com.atahani.telepathy.ui.utility.Constants;

import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * for better management of preference in application
 * like authorize information
 */
public class AppPreferenceTools {

    private SharedPreferences mPreferences;
    private Context mContext;
    public static final String STRING_PREF_UNAVAILABLE = "string preference unavailable";

    /**
     * initial method
     *
     * @param context Context
     */
    public AppPreferenceTools(Context context) {
        this.mContext = context;
        this.mPreferences = this.mContext.getSharedPreferences("app_preference", Context.MODE_PRIVATE);
    }

    /**
     * save token model information to shared preference
     *
     * @param authorizeResponseModel AuthorizeResponseModel
     */
    public void saveUserInformationInSignIn(AuthorizeResponseModel authorizeResponseModel) {
        Calendar openAppCalendar = GregorianCalendar.getInstance();
        mPreferences.edit()
                .putString(mContext.getString(R.string.pref_access_token), authorizeResponseModel.token.access_token)
                .putString(mContext.getString(R.string.pref_refresh_token), authorizeResponseModel.token.refresh_token)
                .putString(mContext.getString(R.string.pref_user_id), authorizeResponseModel.token.user_id)
                .putString(mContext.getString(R.string.pref_app_id), authorizeResponseModel.token.app_id)
                .putBoolean(mContext.getString(R.string.pref_is_in_sign_up_mode), authorizeResponseModel.is_in_sign_up_mode)
                .putString(mContext.getString(R.string.pref_profile_username), authorizeResponseModel.user_profile.username)
                .putString(mContext.getString(R.string.pref_profile_display_name), authorizeResponseModel.user_profile.display_name)
                .putString(mContext.getString(R.string.pref_profile_email), authorizeResponseModel.user_profile.email)
                .putString(mContext.getString(R.string.pref_profile_image_url), authorizeResponseModel.user_profile.image_profile_url)
                .putString(mContext.getString(R.string.pref_profile_locale), authorizeResponseModel.user_profile.locale)
                .putBoolean(mContext.getString(R.string.pref_profile_loaded_in_first_time), true)
                .putLong(mContext.getString(R.string.pref_last_application_open_at), openAppCalendar.getTimeInMillis())
                .apply();
    }

    /**
     * save refresh token model information in refresh token action
     *
     * @param tokenModel Token Model object
     */
    public void saveRefreshTokenInformation(TokenModel tokenModel) {
        mPreferences.edit()
                .putString(mContext.getString(R.string.pref_access_token), tokenModel.access_token)
                .putString(mContext.getString(R.string.pref_refresh_token), tokenModel.refresh_token)
                .apply();
    }


    /**
     * remove all of the preference information in log out
     */
    public void removeAllOfThePref() {
        mPreferences.edit().clear().apply();
    }

    /**
     * update user profile information
     *
     * @param userProfileModel UserProfileModel
     */
    public void updateUserProfileInformation(UserProfileModel userProfileModel) {
        mPreferences.edit()
                .putString(mContext.getString(R.string.pref_profile_username), userProfileModel.username)
                .putString(mContext.getString(R.string.pref_profile_display_name), userProfileModel.display_name)
                .putString(mContext.getString(R.string.pref_profile_email), userProfileModel.email)
                .putString(mContext.getString(R.string.pref_profile_image_url), userProfileModel.image_profile_url)
                .putBoolean(mContext.getString(R.string.pref_profile_loaded_in_first_time), true)
                .apply();
    }

    /**
     * update displayName and image Profile url that comes from PN
     *
     * @param displayName
     * @param imageProfileUrl
     */
    public void updateDisplayNameAndImageProfileUrlFromPN(String displayName, String imageProfileUrl) {
        mPreferences.edit()
                .putString(mContext.getString(R.string.pref_profile_display_name), displayName)
                .putString(mContext.getString(R.string.pref_profile_image_url), imageProfileUrl)
                .apply();
    }

    /**
     * update the username that comes from PN
     *
     * @param username
     */
    public void updateUserNameFromPN(String username) {
        mPreferences.edit()
                .putString(mContext.getString(R.string.pref_profile_username), username)
                .apply();
    }

    /**
     * get Username
     *
     * @return String username
     */
    public String getUsername() {
        return mPreferences.getString(mContext.getString(R.string.pref_profile_username), STRING_PREF_UNAVAILABLE);
    }

    /**
     * get display name
     *
     * @return String display name
     */
    public String getDisplayName() {
        return mPreferences.getString(mContext.getString(R.string.pref_profile_display_name), STRING_PREF_UNAVAILABLE);
    }

    /**
     * get user email
     *
     * @return String user email
     */
    public String getEmail() {
        return mPreferences.getString(mContext.getString(R.string.pref_profile_email), STRING_PREF_UNAVAILABLE);
    }

    /**
     * get user image url
     *
     * @return String user image url
     */
    public String getImageUrl() {
        return mPreferences.getString(mContext.getString(R.string.pref_profile_image_url), STRING_PREF_UNAVAILABLE);
    }

    /**
     * set is register device or not
     *
     * @param isRegisterDevice boolean isRegisterDevice
     */
    public void setIsRegisterDevice(boolean isRegisterDevice) {
        mPreferences.edit()
                .putBoolean(mContext.getString(R.string.pref_is_register_device), isRegisterDevice)
                .apply();
    }

    /**
     * check is device register or not
     *
     * @return boolean true || false
     */
    public boolean isRegisteredDevice() {
        return mPreferences.getBoolean(mContext.getString(R.string.pref_is_register_device), false);
    }

    /**
     * set the last app version name into pref
     *
     * @param versionName String application version name
     */
    public void setTheLastAppVersion(String versionName) {
        mPreferences.edit()
                .putString(mContext.getString(R.string.pref_last_app_version_name), versionName)
                .apply();
    }

    /**
     * get the last app version code name
     *
     * @return String App version code name
     */
    public String getTheLastAppVersion() {
        return mPreferences.getString(mContext.getString(R.string.pref_last_app_version_name), "1.0");
    }

    /**
     * determine is application update and should register device on server again or not
     *
     * @return boolean true if application updated
     */
    public boolean isApplicationUpdate(boolean resetIfUpdated) {
        try {
            //NOTE : since this function run every time open application
            checkTheLastApplicationOpen();
            //check is the last application open time is over 20 days
            PackageInfo pInfo = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0);
            boolean isUpdate = !mPreferences.getString(mContext.getString(R.string.pref_last_app_version_name), "1.0").equals(pInfo.versionName);
            if (isUpdate && resetIfUpdated) {
                resetSomePrefWhenDataBaseDataNotValid();
            }
            return isUpdate;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    /**
     * check the last application open time to validate data in local database
     */
    private void checkTheLastApplicationOpen() {
        Calendar calendar = GregorianCalendar.getInstance();
        Calendar expireAt = GregorianCalendar.getInstance();
        expireAt.add(Calendar.DAY_OF_MONTH, 20);
        if (mPreferences.getLong(mContext.getString(R.string.pref_last_application_open_at), calendar.getTimeInMillis()) >= expireAt.getTimeInMillis()) {
            //reset the pref when database data not valid
            resetSomePrefWhenDataBaseDataNotValid();
        }
        //update the last open application pref
        mPreferences.edit().putLong(mContext.getString(R.string.pref_last_application_open_at), calendar.getTimeInMillis()).apply();
    }

    /**
     * check is user authorized and have access Token
     *
     * @return
     */
    public boolean isAuthorized() {
        return !getAccessToken().equals(STRING_PREF_UNAVAILABLE);
    }

    /**
     * get access token
     *
     * @return string access_token
     */
    public String getAccessToken() {
        return mPreferences.getString(mContext.getString(R.string.pref_access_token), STRING_PREF_UNAVAILABLE);
    }

    /**
     * get refresh token
     *
     * @return string refresh_token
     */
    public String getRefreshToken() {
        return mPreferences.getString(mContext.getString(R.string.pref_refresh_token), STRING_PREF_UNAVAILABLE);
    }

    /**
     * know about is in sign up mode
     * when user sign up it's true, and until complete sign up remain true
     *
     * @return boolean true should navigate to completeSignUpActivity
     */
    public boolean isInSignUpMode() {
        return mPreferences.getBoolean(mContext.getString(R.string.pref_is_in_sign_up_mode), false);
    }

    /**
     * set is in sign up mode
     *
     * @param isInSignUpMode boolean isInSignUpMode
     */
    public void setIsInSignUpMode(boolean isInSignUpMode) {
        mPreferences.edit()
                .putBoolean(mContext.getString(R.string.pref_is_in_sign_up_mode), isInSignUpMode)
                .apply();
    }

    /**
     * set should get classify message when app open
     *
     * @param shouldGetClassifyMessage
     */
    public void setShouldGetClassifyMessageWhenAppOpen(boolean shouldGetClassifyMessage) {
        mPreferences.edit()
                .putBoolean(mContext.getString(R.string.pref_should_get_classify_message_when_app_open), shouldGetClassifyMessage)
                .apply();
    }

    /**
     * check should get classify message when app open
     *
     * @return
     */
    public boolean shouldGetClassifyMessageWhenAppOpen() {
        return mPreferences.getBoolean(mContext.getString(R.string.pref_should_get_classify_message_when_app_open), false);
    }

    /**
     * get user id
     *
     * @return string user_id
     */
    public String getUserId() {
        return mPreferences.getString(mContext.getString(R.string.pref_user_id), STRING_PREF_UNAVAILABLE);
    }

    /**
     * get app id that this application register into server
     *
     * @return string app_id
     */
    public String getAppId() {
        return mPreferences.getString(mContext.getString(R.string.pref_app_id), STRING_PREF_UNAVAILABLE);
    }


    /**
     * set current tab in dashboard when tab is change or leave the dashboard activity
     *
     * @param currentTabInDashboard
     */
    public void setCurrentTabInDashboard(int currentTabInDashboard) {
        mPreferences.edit()
                .putInt(Constants.CURRENT_TAB_IN_DASHBOARD_KEY, currentTabInDashboard)
                .apply();
    }

    /**
     * get current tab in dashboard
     *
     * @return int currentTab
     */
    public int getCurrentTabInDashboard() {
        return mPreferences.getInt(Constants.CURRENT_TAB_IN_DASHBOARD_KEY, Constants.FRIENDS_TAB);
    }


    /**
     * set telepathy in min , used to save last telepathy TTL
     *
     * @param telepathyTTLinMin int telepathy TTL
     */
    public void setTelepathyTTLInMin(int telepathyTTLinMin) {
        mPreferences.edit()
                .putInt(mContext.getString(R.string.pref_telepathy_ttl), telepathyTTLinMin).apply();
    }

    /**
     * get telepathy TTL in min
     *
     * @return int TTL in min
     */
    public int getTelepathyTTlInMin() {
        return mPreferences.getInt(mContext.getString(R.string.pref_telepathy_ttl), 60);
    }

    /**
     * set is should recreate when other changed profile
     *
     * @param value
     */
    public void setIsShouldRecreateWhenOtherChangedProfile(boolean value) {
        mPreferences.edit()
                .putBoolean(mContext.getString(R.string.pref_should_recreate_when_other_change_profile), value)
                .apply();
    }

    /**
     * check should recreate when other change changed profile
     * NOTE : used onResume Dashboard activity to detect should recreate activity to take profile changes
     *
     * @return
     */
    public boolean shouldRecreateWhenOtherChangedProfile() {
        return mPreferences.getBoolean(mContext.getString(R.string.pref_should_recreate_when_other_change_profile), false);
    }

    /**
     * Is Profile loaded for first time
     *
     * @return boolean true || false
     */
    public boolean isProfileLoadedForFirstTime() {
        return mPreferences.getBoolean(mContext.getString(R.string.pref_profile_loaded_in_first_time), false);
    }

    /**
     * know about contact loaded for first time
     */
    public void friendsLoadedForFirstTime() {
        mPreferences.edit()
                .putBoolean(mContext.getString(R.string.pref_friends_loaded_in_first_time), true).apply();
    }

    /**
     * Is Friends Loaded in first time
     *
     * @return boolean true || false
     */
    public boolean isFriendsLoadInFirstTime() {
        return mPreferences.getBoolean(mContext.getString(R.string.pref_friends_loaded_in_first_time), false);
    }

    /**
     * know about telepathies loaded for first time
     */
    public void telepathiesLoadedForFirstTime() {
        mPreferences.edit()
                .putBoolean(mContext.getString(R.string.pref_telepathies_loaded_in_first_time), true).apply();
    }

    /**
     * is telepathies loaded in first time
     *
     * @return boolean true || false
     */
    public boolean isTelepathiesLoadedForFirstTime() {
        return mPreferences.getBoolean(mContext.getString(R.string.pref_telepathies_loaded_in_first_time), false);
    }

    /**
     * reset some pref in delete DB
     */
    public void resetSomePrefWhenDataBaseDataNotValid() {
        mPreferences.edit()
                .putBoolean(mContext.getString(R.string.pref_friends_loaded_in_first_time), false)
                .putBoolean(mContext.getString(R.string.pref_telepathies_loaded_in_first_time), false)
                .putBoolean(mContext.getString(R.string.pref_profile_loaded_in_first_time), false)
                .apply();
    }

    /**
     * set application badger number
     *
     * @param numberOfBadger int numberOfBadger
     */
    public void setApplicationBadgerNumber(int numberOfBadger) {
        mPreferences.edit()
                .putInt(mContext.getString(R.string.pref_badger_number), numberOfBadger).apply();
    }

    /**
     * get Application badger Number
     *
     * @return int the number of Application badger
     */
    public int getApplicationBadgerNumber() {
        return mPreferences.getInt(mContext.getString(R.string.pref_badger_number), 0);
    }

    /**
     * increment Application badger number
     *
     * @return
     */
    public int incrementApplicationBadgerNumber() {
        mPreferences.edit().putInt(mContext.getString(R.string.pref_badger_number), getApplicationBadgerNumber() + 1).apply();
        return getApplicationBadgerNumber();
    }

    /**
     * set vibrate on notification
     *
     * @param vibrateOnNotification
     */
    public void setVibrateOnNotificationReceive(boolean vibrateOnNotification) {
        mPreferences.edit()
                .putBoolean(mContext.getString(R.string.pref_vibrate_on_notifications), vibrateOnNotification)
                .apply();
    }

    /**
     * get the vibrate on notification status
     *
     * @return boolean is vibrate enable or disable
     */
    public boolean vibrateOnNotificationReceive() {
        return mPreferences.getBoolean(mContext.getString(R.string.pref_vibrate_on_notifications), true);
    }

    /**
     * set is notification enable or not when telepathies matched
     *
     * @param isNotificationEnable boolean isNotificationEnable or not
     */
    public void setIsNotificationEnableWhenTelepathiesMatched(boolean isNotificationEnable) {
        mPreferences.edit()
                .putBoolean(mContext.getString(R.string.pref_is_enable_notifications_when_telepathies_matched), isNotificationEnable)
                .apply();
    }

    /**
     * get is notification enable or not when telepathies matched
     *
     * @return boolean true if notifications enable
     */
    public boolean isNotificationEnableWhenTelepathiesMatched() {
        return mPreferences.getBoolean(mContext.getString(R.string.pref_is_enable_notifications_when_telepathies_matched), true);
    }

    /**
     * set is notification enable or not when add you as friend
     *
     * @param isNotificationEnableWhenAddYouAsFriend
     */
    public void setIsNotificationEnableWhenAddYouAsFriend(boolean isNotificationEnableWhenAddYouAsFriend) {
        mPreferences.edit()
                .putBoolean(mContext.getString(R.string.pref_is_enable_notifications_when_add_you_as_friend), isNotificationEnableWhenAddYouAsFriend)
                .apply();
    }

    /**
     * get is notification enable or not when add you as friend
     *
     * @return
     */
    public boolean isNotificationEnableWhenAddYouAsFriend() {
        return mPreferences.getBoolean(mContext.getString(R.string.pref_is_enable_notifications_when_add_you_as_friend), true);
    }

    /**
     * set the application locale
     *
     * @param languageCode String languageCode
     */
    public void setApplicationLocale(String languageCode) {
        mPreferences.edit()
                .putString(mContext.getString(R.string.pref_profile_locale), languageCode).apply();
    }

    /**
     * get application locale
     * if is not set get the default system locale
     *
     * @return String languageCode locale
     */
    public String getApplicationLocale() {
        return mPreferences.getString(mContext.getString(R.string.pref_profile_locale), mContext.getResources().getConfiguration().locale.getLanguage());
    }


    /**
     * set notification ringtone
     *
     * @param notificationRingtone String notification ringtone
     */
    public void setNotificationRingtone(String notificationRingtone) {
        mPreferences.edit()
                .putString(mContext.getString(R.string.pref_notification_ringtone), notificationRingtone)
                .apply();
    }

    /**
     * get notification ringtone URI
     * return DEFAULT_NOTIFICATION_URI is not set yes
     *
     * @return String notification ringtone uri
     */
    public String getNotificationRingtoneUri() {
        return mPreferences.getString(mContext.getString(R.string.pref_notification_ringtone), Settings.System.DEFAULT_NOTIFICATION_URI.toString());
    }

    /**
     * get notification ringtone title
     *
     * @return
     */
    public String getNotificationRingtoneTitle() {
        String ringtoneUri = getNotificationRingtoneUri();
        if (ringtoneUri.length() == 0) {
            return mContext.getString(R.string.setting_notification_silent);
        } else {
            Ringtone ringtone = RingtoneManager.getRingtone(mContext, Uri.parse(ringtoneUri));
            return ringtone.getTitle(mContext);
        }
    }

    /**
     * get the current theme name if not set get the default one
     *
     * @return String the theme name
     */
    public String getCurrentThemeName() {
        return mPreferences.getString(mContext.getString(R.string.pref_profile_theme_name), ThemeUtil.INDIGO_THEME);
    }

    /**
     * get the current Theme Resource Id if not set get the default one
     *
     * @return Int the Theme Resource Id
     */
    public int getCurrentThemeResourceId() {
        return mPreferences.getInt(mContext.getString(R.string.pref_profile_theme_res_id), R.style.Indigo_AppTheme);
    }

    public int getCurrentThemeChildResourceId() {
        return mPreferences.getInt(mContext.getString(R.string.pref_profile_theme_child_res_id), R.style.Indigo_AppTheme_Child);
    }

    /**
     * get primary color if not set get default one
     *
     * @return Int primary color
     */
    public int getPrimaryColor() {
        return mPreferences.getInt(mContext.getString(R.string.pref_theme_primary_color), ContextCompat.getColor(mContext, R.color.indigo_theme_primary));
    }

    /**
     * get primary dark color if not set get default one
     *
     * @return Int primary dark color
     */
    public int getPrimaryDarkColor() {
        return mPreferences.getInt(mContext.getString(R.string.pref_theme_primary_dark_color), ContextCompat.getColor(mContext, R.color.indigo_theme_primary_dark));
    }

    /**
     * get primary light color if not set get default one
     *
     * @return Int primary light color
     */
    public int getPrimaryLightColor() {
        return mPreferences.getInt(mContext.getString(R.string.pref_theme_primary_light_color), ContextCompat.getColor(mContext, R.color.indigo_theme_primary_light));
    }

    /**
     * get accent color if not set get default one
     *
     * @return Int accent color
     */
    public int getAccentColor() {
        return mPreferences.getInt(mContext.getString(R.string.pref_theme_accent_color), ContextCompat.getColor(mContext, R.color.indigo_theme_accent));
    }


    /**
     * save application error in pref
     *
     * @param appError
     * @return the app error with header
     */
    public String saveLastApplicationError(String appError) {
        AndroidUtilities androidUtilities = new AndroidUtilities();
        String userInformation = isAuthorized() ? " user email is " + getEmail() + " with username " + getUsername() + " with name " + getDisplayName() : " user is not logged in ";
        String errorWithHeader = "this is logs of android application error\n" + userInformation + "\nAndroid Device name is: " + androidUtilities.getDeviceName() + " and the android ver is: " + Build.VERSION.RELEASE + " in APP ver : " + androidUtilities.getApplicationVersion() + "\nthe errors stack trace is : \n" + appError;
        mPreferences.edit()
                .putString(this.mContext.getString(R.string.pref_last_application_error), errorWithHeader)
                .apply();
        return errorWithHeader;
    }

    /**
     * get last application error
     *
     * @return
     */
    public String getLastApplicationError() {
        return mPreferences.getString(this.mContext.getString(R.string.pref_last_application_error), STRING_PREF_UNAVAILABLE);
    }

    /**
     * check is have any error to send
     *
     * @return
     */
    public boolean isHaveAnyError() {
        return !mPreferences.getString(this.mContext.getString(R.string.pref_last_application_error), STRING_PREF_UNAVAILABLE).equals(STRING_PREF_UNAVAILABLE);
    }

    /**
     * remove last application error
     */
    public void removeLastApplicationError() {
        mPreferences.edit()
                .putString(this.mContext.getString(R.string.pref_last_application_error), STRING_PREF_UNAVAILABLE)
                .apply();
    }
}
