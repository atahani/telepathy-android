package com.atahani.telepathy.utility;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.atahani.telepathy.network.TService;
import com.atahani.telepathy.ui.utility.Constants;

import mobi.atahani.telepathy.R;
import com.atahani.telepathy.TApplication;
import com.atahani.telepathy.network.ClientConfigs;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import uk.co.chrisjenx.calligraphy.CalligraphyTypefaceSpan;
import uk.co.chrisjenx.calligraphy.TypefaceUtils;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utilities for application
 */
public class AndroidUtilities {

    public static float density = 1;
    public static int statusBarHeight = 0;
    public static DisplayMetrics displayMetrics = new DisplayMetrics();
    private static Pattern RTL_CHAR = Pattern.compile("[\u0600-\u06FF\u0750-\u077F\u0590-\u05FF\uFE70-\uFEFF]");

    static {
        density = TApplication.applicationContext.getResources().getDisplayMetrics().density;
    }

    public static void showKeyboard(View view) {
        if (view == null) {
            return;
        }
        InputMethodManager inputManager = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
    }

    public static boolean isKeyboardShowed(View view) {
        if (view == null) {
            return false;
        }
        InputMethodManager inputManager = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        return inputManager.isActive(view);
    }

    public static void hideKeyboard(View view) {
        if (view == null) {
            return;
        }
        InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (!imm.isActive()) {
            return;
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }


    public static int getViewInset(View view) {
        if (view == null || Build.VERSION.SDK_INT < 21) {
            return 0;
        }
        try {
            Field mAttachInfoField = View.class.getDeclaredField("mAttachInfo");
            mAttachInfoField.setAccessible(true);
            Object mAttachInfo = mAttachInfoField.get(view);
            if (mAttachInfo != null) {
                Field mStableInsetsField = mAttachInfo.getClass().getDeclaredField("mStableInsets");
                mStableInsetsField.setAccessible(true);
                Rect insets = (Rect) mStableInsetsField.get(mAttachInfo);
                return insets.bottom;
            }
        } catch (Exception e) {
        }
        return 0;
    }

    private static File getAlbumDir() {
        File storageDir = null;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            storageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "Telepathy");
            if (!storageDir.mkdirs()) {
                if (!storageDir.exists()) {
                    Log.d("android_utilities", "failed to create directory");
                    return null;
                }
            }
        } else {
            Log.d("android_utilities", "External storage is not mounted READ/WRITE.");
        }
        return storageDir;
    }

    /**
     * generate new image path file
     *
     * @return
     */
    public static File generateImagePath() {
        try {
            File storageDir = getAlbumDir();
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
            return new File(storageDir, "IMG_" + timeStamp + ".jpeg");
        } catch (Exception e) {
            Log.d("android_utilities", "the error is " + e);
        }
        return null;
    }

    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    public static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } catch (Exception e) {
            Log.d("android_utilities", "the error is " + e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }


    @SuppressLint("NewApi")
    public static String getPath(final Uri uri) {
        try {
            final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
            if (isKitKat && DocumentsContract.isDocumentUri(TApplication.applicationContext, uri)) {
                if (isExternalStorageDocument(uri)) {
                    final String docId = DocumentsContract.getDocumentId(uri);
                    final String[] split = docId.split(":");
                    final String type = split[0];
                    if ("primary".equalsIgnoreCase(type)) {
                        return Environment.getExternalStorageDirectory() + "/" + split[1];
                    }
                } else if (isDownloadsDocument(uri)) {
                    final String id = DocumentsContract.getDocumentId(uri);
                    final Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
                    return getDataColumn(TApplication.applicationContext, contentUri, null, null);
                } else if (isMediaDocument(uri)) {
                    final String docId = DocumentsContract.getDocumentId(uri);
                    final String[] split = docId.split(":");
                    final String type = split[0];

                    Uri contentUri = null;
                    switch (type) {
                        case "image":
                            contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                            break;
                        case "video":
                            contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                            break;
                        case "audio":
                            contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                            break;
                    }

                    final String selection = "_id=?";
                    final String[] selectionArgs = new String[]{
                            split[1]
                    };

                    return getDataColumn(TApplication.applicationContext, contentUri, selection, selectionArgs);
                }
            } else if ("content".equalsIgnoreCase(uri.getScheme())) {
                return getDataColumn(TApplication.applicationContext, uri, null, null);
            } else if ("file".equalsIgnoreCase(uri.getScheme())) {
                return uri.getPath();
            }
        } catch (Exception e) {
            Log.d("android_utilities", "the error is " + e);
        }
        return null;
    }

    public static CalligraphyTypefaceSpan getCalligraphyTypefaceSpanByText(String text) {
        Matcher matcher = RTL_CHAR.matcher(text);
        if (matcher.find()) {
            //load persian default font
            CalligraphyTypefaceSpan tf = new CalligraphyTypefaceSpan(TypefaceUtils.load(TApplication.applicationContext.getAssets(), TApplication.applicationContext.getString(R.string.normal_persian_font)));
            return tf;

        } else {
            CalligraphyTypefaceSpan tf = new CalligraphyTypefaceSpan(TypefaceUtils.load(TApplication.applicationContext.getAssets(), TApplication.applicationContext.getString(R.string.normal_latin_font)));
            return tf;
        }
    }

    public static int getTypeOfTextDirection(String text) {
        Matcher matcher = RTL_CHAR.matcher(text);
        if (matcher.find()) {
            return View.TEXT_DIRECTION_RTL;
        } else {
            return View.TEXT_DIRECTION_LTR;
        }
    }

    public static Typeface getTypefaceByText(String text) {
        Pattern RTL_CHAR = Pattern.compile("[\u0600-\u06FF\u0750-\u077F\u0590-\u05FF\uFE70-\uFEFF]");
        Matcher matcher = RTL_CHAR.matcher(text);
        if (matcher.find()) {
            //load persian default font
            return TypefaceUtils.load(TApplication.applicationContext.getAssets(), TApplication.applicationContext.getString(R.string.normal_persian_font));
        } else {
            return TypefaceUtils.load(TApplication.applicationContext.getAssets(), TApplication.applicationContext.getString(R.string.normal_latin_font));
        }
    }


    /**
     * when error happening in application save it to pref and send it when user use in application
     *
     * @param ex
     */
    public static void processApplicationError(Exception ex, boolean shouldSendItNow) {
        AppPreferenceTools appPreferenceTools = new AppPreferenceTools(TApplication.applicationContext);
        //save this error in pref
        StringWriter sw = new StringWriter();
        ex.printStackTrace(new PrintWriter(sw));
        String errorWithHeader = appPreferenceTools.saveLastApplicationError(sw.toString());
        if (shouldSendItNow) {
            sendTheMessageToSlack(Constants.SLACK_ERROR_MESSAGE_TYPE, errorWithHeader, Constants.SLACK_ANDROID_CHANNEL_NAME);
        }
    }

    /**
     * process unhandled application error with throwable
     *
     * @param ex
     */
    public static void processUnhandledApplicationError(Throwable ex) {
        final Writer result = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(result);
        ex.printStackTrace(printWriter);
        String stacktrace = result.toString();
        printWriter.close();
        AppPreferenceTools appPreferenceTools = new AppPreferenceTools(TApplication.applicationContext);
        //save this error in pref
        appPreferenceTools.saveLastApplicationError(stacktrace);
    }

    /**
     * check and send app error if exist
     */
    public static void checkAndSendAppErrorIfExist() {
        AppPreferenceTools appPreferenceTools = new AppPreferenceTools(TApplication.applicationContext);
        if (appPreferenceTools.isHaveAnyError()) {
            sendTheMessageToSlack(Constants.SLACK_ERROR_MESSAGE_TYPE, appPreferenceTools.getLastApplicationError(), Constants.SLACK_ANDROID_CHANNEL_NAME);
        }
    }

    /**
     * send the message to slack via android_app_bot
     *
     * @param messageType
     * @param message
     * @param channel
     */
    public static void sendTheMessageToSlack(final int messageType, final String message, String channel) {
        //check connectivity
        ConnectivityManager connectivityManager = (ConnectivityManager) TApplication.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnectedOrConnecting()) {
            RestAdapter slackRestAdapter = new RestAdapter.Builder()
                    .setEndpoint(ClientConfigs.SLACK_END_POINT)
                    .setLogLevel(RestAdapter.LogLevel.FULL)
                    .build();
            TService slackService = slackRestAdapter.create(TService.class);
            //send this message via slack user bot to channel
            slackService.sendLogsToChannelViaBot(ClientConfigs.SLACK_BOT_TOKEN, channel, message, new Callback<Response>() {
                @Override
                public void success(Response response, Response response2) {
                    //remove the error from pref
                    if (messageType == Constants.SLACK_ERROR_MESSAGE_TYPE) {
                        AppPreferenceTools appPreferenceTools = new AppPreferenceTools(TApplication.applicationContext);
                        appPreferenceTools.removeLastApplicationError();
                    }
                }

                @Override
                public void failure(RetrofitError error) {
                    //DO nothing
                    //save the error in pref when fail
                }
            });
        }
    }

    /**
     * get device name for register device model used in store in server
     *
     * @return
     */
    public String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        } else {
            return capitalize(manufacturer) + " " + model;
        }
    }

    private String capitalize(String s) {
        if (s == null || s.length() == 0) {
            return "";
        }
        char first = s.charAt(0);
        if (Character.isUpperCase(first)) {
            return s;
        } else {
            return Character.toUpperCase(first) + s.substring(1);
        }
    }

    /**
     * get application build version
     *
     * @return
     */
    public String getApplicationVersion() {
        try {
            PackageInfo pInfo = TApplication.applicationContext.getPackageManager().getPackageInfo(TApplication.applicationContext.getPackageName(), 0);
            return pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }
}
