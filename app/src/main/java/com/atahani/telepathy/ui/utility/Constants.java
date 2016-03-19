package com.atahani.telepathy.ui.utility;

/**
 * Constants used in telepathy application
 */
public class Constants {

    //other stuff
    public static final String SLACK_ANDROID_CHANNEL_NAME = "#android";
    public static final int SLACK_ERROR_MESSAGE_TYPE = 1;
    public static final int SLACK_NON_ERROR_MESSAGE_TYPE = 2;

    public static final String DEFAULT_IMAGE_PROFILE_NAME = "default_image_profile";
    public static final String INVITATION_ID_PARAM = "INVITATION_ID";
    public static final String NUMBER_OF_USER_MESSAGE = "NUMBER_OF_USER_MESSAGE";
    public static final String WITH_USER_ID_PARAM = "WITH_USER_ID";
    public static final String WITH_USER_DISPLAY_NAME_PARAM = "WITH_USER_DISPLAY_NAME";
    public static final String WITH_USER_IMAGE_URL_PARAM = "WITH_USER_IMAGE_URL";
    public static final String WITH_USER_THEME_PARAM = "WITH_USER_THEME";
    public static final String WITH_USER_USERNAME_PARAM = "WITH_USER_USERNAME";
    public static final String MESSAGE_ID_PARAM = "MESSAGE_ID";
    public static final String TELEPATHY_ID_PARAM = "TELEPATHY_ID";
    public static final String IS_FROM_NOTIFICATION_PARAM = "IS_FROM_NOTIFICATION";
    public static final String IS_TELEPATHIES_MATCHED_PARAM = "IS_TELEPATHIES_MATCHED";
    public static final String STEP_NUMBER_PARAM = "STEP_NUMBER";
    public static final String GOOGLE_PROVIDER = "google";
    public static final String CURRENT_TAB_IN_DASHBOARD_KEY = "CURRENT_TAB_IN_DASHBOARD";
    public static final String DEFAULT_THEME_NAME = "INDIGO_THEME";
    public static final String DELETED_ACCOUNT_VALUE = "DELETED_ACCOUNT";

    //intent filter and related to broad case message
    public static final String ACTION_TO_DO_FOR_MESSAGE_UPDATE_INTENT_FILTER = "ACTION_TO_DO_FOR_MESSAGE_UPDATE_INTENT_FILTER";
    public static final String ACTION_TO_DO_FOR_CLASSIFY_MESSAGE_UPDATE_INTENT_FILTER = "ACTION_TO_DO_FOR_CLASSIFY_MESSAGE_UPDATE_INTENT_FILTER";
    public static final String ACTION_TO_DO_FOR_TELEPATHY_UPDATE_INTENT_FILTER = "ACTION_TO_DO_FOR_TELEPATHY_UPDATE_INTENT_FILTER";
    public static final String ACTION_TO_DO_FOR_FRIEND_UPDATE_INTENT_FILTER = "ACTION_TO_DO_FOR_FRIEND_UPDATE_INTENT_FILTER";
    public static final String ACTION_TO_DO_FOR_PROFILE_UPDATE_INTENT_FILTER = "ACTION_TO_DO_FOR_PROFILE_UPDATE_INTENT_FILTER";
    public static final String TELEPATHY_BASE_ACTIVITY_INTENT_FILTER = "TELEPATHY_BASE_ACTIVITY_INTENT_FILTER";
    public static final String ACTION_TO_DO_PARAM = "ACTION_TO_DO";

    public static final String PENDING_INTENT_TYPE = "PENDING_INTENT_TYPE";


    //Pending intent type
    public static final int PENDING_DO_NOTHING = 0;
    public static final int PENDING_OPEN_MESSAGE = 1;
    public static final int PENDING_OPEN_USER_INFO_DIALOG = 2;
    public static final int PENDING_SEND_TELEPATHY_TO_USER = 3;

    //types
    public static final int CHANGE_PHOTO_CHOOSE_LIST_TYPE = 1;
    public static final int CHANGE_LOCALE_TYPE = 2;
    public static final int CHANGE_THEME_TYPE = 3;
    public static final int DO_NOTHING = 0;
    public static final int UPDATE_FROM_DB = 1;
    public static final int UPDATE_FROM_NET = 2;
    public static final int CLOSE_REALM_DB = 5;
    public static final int TERMINATE_APPLICATION = 7;
    public static final int RECREAT_ACTIVITY_WHEN_LOCALE_OR_THEME_CHANGED = 3;
    public static final int UPDATE_USER_PROFILE = 4;

    public static final int FRIENDS_TAB = 0;
    public static final int TELEPATHY_TAB = 1;
    public static final int MESSAGES_TAB = 2;


    //request codes
    public static final int RC_GET_AUTH_CODE = 9003;
    public static final int FOR_TAKE_PHOTO_REQUEST_WRITE_EXTERNAL_STORAGE_PER = 12;
    public static final int FOR_OPEN_GALLARY_REQUEST_WRITE_EXTERNAL_STORAGE_PER = 13;
    public static final int CAMERA_REQUEST_CODE = 1888;
    public static final int GALLERY_REQUEST_CODE = 12;
    public static final int REQUEST_CODE_ALERT_RINGTONE = 17;
    public static final int START_ACTIVITY_FOR_SETTING_RESULT_MESSAGE = 0;
    public static final int TELEPATHY_REQUEST_CODE = 7;
    public static final int APP_SETTINGS_REQUEST_CODE = 9;
    public static final int REQUEST_INVITE = 23;
    public static final int USER_MESSAGE_REQUEST = 71;
    public static final int SEND_TELEPATHY_RESULT = 21;

    //states
    public static final int DISAPPEAR_EMPTY_STATE = 0;
    public static final int NOT_FOUND_EMPTY_STATE = 1;
    public static final int DO_NOT_HAVE_CLASSIFY_MESSAGE_EMPTY_STATE = 2;
    public static final int DO_NOT_HAVE_FRIEND_EMPTY_STATE = 3;
    public static final int DO_NOT_HAVE_TELEPATHY_EMPTY_STATE = 4;

    //fragment tags
    public static final String ABOUT_DIALOG_FRAGMENT_TAG = "ABOUT_DIALOG_FRAGMENT";
    public static final String RECYCLER_VIEW_CHOOSE_DIALOG_TAG = "RECYCLER_VIEW_CHOOSE_DIALOG";
    public static final String SPECIFY_TELEPATHY_TTL_DIALOG_TAG = "SPECIFY_TELEPATHY_TTL_DIALOG";
    public static final String CLASSIFY_MESSAGE_FRAGMENT_TAG = "CLASSIFY_MESSAGE_FRAGMENT_TAG";
    public static final String USER_MESSAGE_FRAGMENT_TAG = "USER_MESSAGE_FRAGMENT_TAG";
    public static final String EMPTY_FRAGMENT_TAG = "EMPTY_FRAGMENT_TAG";


    //notification tags
    public static final String ADD_YOU_AS_FRIEND_PN_TAG = "ADD_YOU_AS_FRIEND_PN_TAG";

    //PN TYPES
    public static final int TELEPATHIES_MATCHED_PN_TYPE = 0;
    public static final int MESSAGE_RECEIVE_PN_TYPE = 1;
    public static final int MESSAGE_READ_PN_TYPE = 2;
    public static final int ALL_USER_MESSAGE_READ_PN_TYPE = 3;
    public static final int FRIEND_UPDATE_PROFILE_PN_TYPE = 4;
    public static final int USER_PROFILE_UPDATE_PN_TYPE = 5;
    public static final int NEW_TELEPATHY_SEND_IT_PN_TYPE = 6;
    public static final int TELEPATHY_DELETED_PN_TYPE = 7;
    public static final int MESSAGE_DELETED_PN_TYPE = 8;
    public static final int USER_MESSAGE_DELETED_PN_TYPE = 9;
    public static final int USER_MESSAGE_ALREADY_READ_BY_USER_PN_TYPE = 10;
    public static final int USER_ADD_AS_FRIEND_PN_TYPE = 11;
    public static final int USER_REMOVE_AS_FRIEND_PN_TYPE = 12;
    public static final int USER_DELETE_PN_TYPE = 13;
    public static final int TERMINATE_APP_PN_TYPE = 14;
    public static final int GOT_USER_MESSAGE_PN_TYPE = 15;
    public static final int ADD_YOU_AS_FRIEND = 16;

}
