package com.atahani.telepathy;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;

import com.atahani.telepathy.utility.AndroidUtilities;
import com.atahani.telepathy.utility.AppPreferenceTools;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import mobi.atahani.telepathy.R;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

import java.util.Locale;

/**
 * extend application class for config Calligraphy config to set custom fonts
 */
public class TApplication extends Application implements Application.ActivityLifecycleCallbacks {


    public static volatile Context applicationContext;
    public static RealmConfiguration mRealmConfiguration;
    public static Activity mCurrentActivityInApplication;
    private Thread.UncaughtExceptionHandler mDefaultUEH;

    @Override
    public void onCreate() {
        super.onCreate();
        applicationContext = getApplicationContext();
        updateLocale();
        //set Roboto_Regular font as default one
        //but set what font use in different style by region like persian
        initCalligraphyConfig(TApplication.applicationContext.getString(R.string.default_font));
        //config RealmDB
        mRealmConfiguration = new RealmConfiguration.Builder(this)
                .deleteRealmIfMigrationNeeded()
                .build();
        Realm.setDefaultConfiguration(mRealmConfiguration);
        registerActivityLifecycleCallbacks(this);
        //set
        AppPreferenceTools appPreferenceTools = new AppPreferenceTools(applicationContext);
        appPreferenceTools.setShouldGetClassifyMessageWhenAppOpen(true);
        mDefaultUEH = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable ex) {
                AndroidUtilities.processUnhandledApplicationError(ex);
                mDefaultUEH.uncaughtException(thread, ex);
            }
        });
    }

    @Override
    public void onTerminate() {
        unregisterActivityLifecycleCallbacks(this);
        super.onTerminate();
    }

    /**
     * when change language should call it to change default font in application
     *
     * @param defaultFontPath
     */
    private static void initCalligraphyConfig(String defaultFontPath) {
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath(defaultFontPath)
                .setFontAttrId(R.attr.fontPath)
                .build());
    }


    /**
     * update the locale , if not set get current system locale as default if not
     */
    public static void updateLocale() {
        AppPreferenceTools appPreferenceTools = new AppPreferenceTools(applicationContext);
        Locale locale = new Locale(appPreferenceTools.getApplicationLocale());
        //to force change default locale
        Configuration conf = applicationContext.getResources().getConfiguration();
        conf.locale = locale;
        conf.setLayoutDirection(locale);
        applicationContext.getResources().updateConfiguration(conf, applicationContext.getResources().getDisplayMetrics());
        Configuration systemConf = Resources.getSystem().getConfiguration();
        systemConf.locale = locale;
        systemConf.setLayoutDirection(locale);
        Resources.getSystem().updateConfiguration(systemConf, Resources.getSystem().getDisplayMetrics());
        Locale.setDefault(locale);
        initCalligraphyConfig(TApplication.applicationContext.getString(R.string.default_font));
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        mCurrentActivityInApplication = activity;
    }

    @Override
    public void onActivityStarted(Activity activity) {
        mCurrentActivityInApplication = activity;
    }

    @Override
    public void onActivityResumed(Activity activity) {

    }

    @Override
    public void onActivityPaused(Activity activity) {

    }

    @Override
    public void onActivityStopped(Activity activity) {
        mCurrentActivityInApplication = null;
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        mCurrentActivityInApplication = null;
    }
}
