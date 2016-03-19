package com.atahani.telepathy.ui.component;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.atahani.telepathy.network.TRestService;
import com.atahani.telepathy.network.TService;
import com.atahani.telepathy.ui.utility.Constants;
import com.atahani.telepathy.ui.utility.SwipeBackInterface;
import com.atahani.telepathy.ui.utility.SwipeBackUtility;
import com.atahani.telepathy.utility.AppPreferenceTools;

import io.realm.Realm;
import mobi.atahani.telepathy.R;
import com.atahani.telepathy.TApplication;
import com.atahani.telepathy.ui.SignInActivity;
import com.atahani.telepathy.ui.utility.SwipeBackHelper;
import com.atahani.telepathy.utility.ThemeUtil;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

/**
 * Telepathy base activity with getTService() method to communicate with rest service api
 * and also swipe back feature
 */
public class TelepathyBaseActivity extends AppCompatActivity implements SwipeBackInterface {

    private SwipeBackHelper mHelper;
    private AppPreferenceTools mAppPreference;
    private PermissionEventListener mPermissionEventListener;
    private Realm mRealm;
    private BroadcastReceiver mTelepathyBaseReceiver;


    //to apply custom font into UI Activity
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    /**
     * get current rest service
     *
     * @return return Rest Service
     */
    public TService getTService() {
        TRestService restService = new TRestService();
        return restService.getTService();
    }

    /**
     * get AppPreference class
     *
     * @return the AppPreferenceTools class
     */
    public AppPreferenceTools getAppPreferenceTools() {
        return mAppPreference;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mAppPreference = new AppPreferenceTools(this);
        mRealm = Realm.getDefaultInstance();
        super.onCreate(savedInstanceState);
        configStatusBar();
        //config swipe back layout base on current layout direction
        mHelper = new SwipeBackHelper(this);
        mHelper.onActivityCreate();
        if (getString(R.string.direction_type).equals("RTL")) {
            mHelper.getSwipeBackLayout().setEdgeTrackingEnabled(SwipeBackLayout.EDGE_RIGHT);
        } else {
            mHelper.getSwipeBackLayout().setEdgeTrackingEnabled(SwipeBackLayout.EDGE_LEFT);
        }
        mTelepathyBaseReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int actionToDo = intent.getIntExtra(Constants.ACTION_TO_DO_PARAM, Constants.DO_NOTHING);
                if (actionToDo == Constants.CLOSE_REALM_DB) {
                    if (!mRealm.isClosed()) {
                        mRealm.close();
                    }
                } else if (actionToDo == Constants.TERMINATE_APPLICATION) {
                    Intent singInIntent = new Intent(TApplication.applicationContext, SignInActivity.class);
                    singInIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    setAnimationOnStart();
                    startActivity(singInIntent);
                    finish();
                }
            }
        };
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        TApplication.updateLocale();
    }

    public Realm getRealm() {
        return this.mRealm;
    }


    /**
     * set theme
     *
     * @param isChildActivity boolean is this a child activity
     */
    public void configTheme(boolean isChildActivity) {
        //first check is application update or not, since the resourceId maybe changed in new application release
        if (mAppPreference.isApplicationUpdate(false)) {
            ThemeUtil themeUtil = new ThemeUtil(TApplication.applicationContext);
            themeUtil.setCurrentTheme(mAppPreference.getCurrentThemeName());
            if (isChildActivity) {
                setTheme(themeUtil.getThemeChildResourceIdByThemeName(mAppPreference.getCurrentThemeName()));
            } else {
                setTheme(themeUtil.getThemeResourceIdByThemeName(mAppPreference.getCurrentThemeName()));
            }
        } else {
            if (isChildActivity) {
                setTheme(mAppPreference.getCurrentThemeChildResourceId());
            } else {
                setTheme(mAppPreference.getCurrentThemeResourceId());
            }
        }
    }

    /**
     * config status bar color
     */
    private void configStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(mAppPreference.getPrimaryDarkColor());
        }
    }

    /**
     * config status bar via primary dark color
     *
     * @param primaryDarkColor int primary dark color
     */
    public void configStatusBar(int primaryDarkColor) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(primaryDarkColor);
        }
    }

    /**
     * config default toolbar and toolbar as setSupportActionBar
     */
    public Toolbar configDefaultToolBar() {
        Toolbar mToolbar = (Toolbar) findViewById(R.id.default_toolbar);
        mToolbar.setBackgroundColor(mAppPreference.getPrimaryColor());
        setSupportActionBar(mToolbar);
        return mToolbar;
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mHelper.onPostCreate();
    }

    @Override
    public View findViewById(int id) {
        View v = super.findViewById(id);
        if (v == null && mHelper != null)
            return mHelper.findViewById(id);
        return v;
    }

    public void setAnimationOnStart() {
        if (getString(R.string.direction_type).equals("RTL")) {
            overridePendingTransition(R.anim.right_to_left_enter, R.anim.no_animation);
        } else {
            overridePendingTransition(R.anim.left_to_right_enter, R.anim.no_animation);
        }
    }

    public void setAnimationOnExit() {
        if (getString(R.string.direction_type).equals("RTL")) {
            overridePendingTransition(R.anim.no_animation, R.anim.right_to_left_exit);
        } else {
            overridePendingTransition(R.anim.no_animation, R.anim.left_to_right_exit);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        setAnimationOnExit();
    }

    public void requestRunTimePermission(String permissionType, int requestCode, PermissionEventListener permissionEventListener) {
        ActivityCompat.requestPermissions(this, new String[]{permissionType}, requestCode);
        mPermissionEventListener = permissionEventListener;
    }

    public boolean checkRunTimePermissionIsGranted(String permissionType) {
        return PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(TApplication.applicationContext, permissionType);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (mPermissionEventListener != null) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mPermissionEventListener.onGranted(requestCode, permissions);
            } else {
                mPermissionEventListener.onFailure(requestCode, permissions);
            }
        }

    }

    /**
     * start app settings intent to manage app permission
     */
    public void startAppSettingsIntent() {
        Intent appSettingsIntent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.fromParts("package", getPackageName(), null));
        appSettingsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(appSettingsIntent);
    }

    /**
     * start gallery intent to pick photo
     */
    public void startGalleryIntent() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), Constants.GALLERY_REQUEST_CODE);
    }

    @Override
    public SwipeBackLayout getSwipeBackLayout() {
        return mHelper.getSwipeBackLayout();
    }

    @Override
    public void setSwipeBackEnable(boolean enable) {
        getSwipeBackLayout().setEnableGesture(enable);
    }

    @Override
    public void scrollToFinishActivity() {
        SwipeBackUtility.convertActivityToTranslucent(this);
        getSwipeBackLayout().scrollToFinishActivity();
    }

    public interface PermissionEventListener {
        void onGranted(int requestCode, String[] permissions);

        void onFailure(int requestCode, String[] permissions);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (!mRealm.isClosed()) {
            mRealm.close();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(this).registerReceiver(mTelepathyBaseReceiver, new IntentFilter(Constants.TELEPATHY_BASE_ACTIVITY_INTENT_FILTER));
    }

    @Override
    protected void onStop() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mTelepathyBaseReceiver);
        super.onStop();
    }
}
