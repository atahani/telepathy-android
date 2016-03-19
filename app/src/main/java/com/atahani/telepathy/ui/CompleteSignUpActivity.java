package com.atahani.telepathy.ui;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import mobi.atahani.telepathy.R;
import com.atahani.telepathy.TApplication;
import com.atahani.telepathy.adapter.ChooseItemAdapter;
import com.atahani.telepathy.model.ChooseItemModel;
import com.atahani.telepathy.model.UpdateProfileRequestModel;
import com.atahani.telepathy.model.UserProfileModel;
import com.atahani.telepathy.network.TService;
import com.atahani.telepathy.ui.component.CommonFeedBack;
import com.atahani.telepathy.ui.component.TelepathyBaseActivity;
import com.atahani.telepathy.ui.fragment.RecyclerViewChooseDialogFragment;
import com.atahani.telepathy.ui.utility.Constants;
import com.atahani.telepathy.ui.utility.CropCircleTransformation;
import com.atahani.telepathy.utility.AndroidUtilities;
import com.atahani.telepathy.utility.AppPreferenceTools;
import com.atahani.telepathy.utility.Crop;
import com.atahani.telepathy.utility.ThemeUtil;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.mime.TypedFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Locale;

/**
 * Complete Sign up activity
 */
public class CompleteSignUpActivity extends TelepathyBaseActivity {

    private View mHeaderView;
    private TService mTService;
    private AppPreferenceTools mAppPreferenceTools;
    private ImageView mImImageProfile;
    private AppCompatEditText mEtxDisplayName;
    private String mCurrentImagePath;
    private Activity mCurrentActivity;
    private ThemeUtil mThemeUtil;
    private FloatingActionButton mFabActionDone;
    private FloatingActionButton mFabEditImage;
    private ProgressDialog mProgressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        configTheme(false);
        setSwipeBackEnable(false);
        setContentView(R.layout.activity_complete_sign_up);
        if (getResources().getBoolean(R.bool.isTablet) && Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            CardView mainCardView = (CardView) findViewById(R.id.main_card_view);
            mainCardView.setPreventCornerOverlap(false);
        }
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setCanceledOnTouchOutside(false);
        mHeaderView = findViewById(R.id.header_view);
        mFabEditImage = (FloatingActionButton) findViewById(R.id.fab_edit_image);
        mFabActionDone = (FloatingActionButton) findViewById(R.id.fab_action_done);
        mImImageProfile = (ImageView) findViewById(R.id.im_image_profile);
        mEtxDisplayName = (AppCompatEditText) findViewById(R.id.etx_display_name);
        mAppPreferenceTools = new AppPreferenceTools(TApplication.applicationContext);
        //set the colors
        mHeaderView.setBackgroundColor(mAppPreferenceTools.getPrimaryColor());
        mFabActionDone.setBackgroundTintList(ColorStateList.valueOf(mAppPreferenceTools.getAccentColor()));
        mFabEditImage.setBackgroundTintList(ColorStateList.valueOf(mAppPreferenceTools.getAccentColor()));
        if (getResources().getBoolean(R.bool.isTablet)) {
            RelativeLayout lyMainLayout = (RelativeLayout) findViewById(R.id.ly_main_content);
            lyMainLayout.setBackgroundColor(mAppPreferenceTools.getPrimaryLightColor());
        }
        mCurrentActivity = this;
        mTService = getTService();
        mThemeUtil = new ThemeUtil(this);
        //bind data
        mEtxDisplayName.setText(mAppPreferenceTools.getDisplayName());
        mEtxDisplayName.setSelection(mEtxDisplayName.getText().length());
        Picasso.with(this).load(mAppPreferenceTools.getImageUrl())
                .placeholder(R.drawable.image_place_holder)
                .transform(new CropCircleTransformation()).into(mImImageProfile);
        mFabActionDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doneAction();
            }
        });
        mEtxDisplayName.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    doneAction();
                    return true;
                }
                return false;
            }
        });
        mFabEditImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    //open RecyclerViewChooseDialog Fragment
                    RecyclerViewChooseDialogFragment fragment = RecyclerViewChooseDialogFragment.newInstance(Constants.CHANGE_PHOTO_CHOOSE_LIST_TYPE);
                    fragment.setIsHaveImage(!mAppPreferenceTools.getImageUrl().contains(Constants.DEFAULT_IMAGE_PROFILE_NAME));
                    fragment.setChangePhotoChooseListener(new RecyclerViewChooseDialogFragment.RecyclerViewChooseListener() {
                        @Override
                        public void onChooseItem(int actionTypeId) {
                            if (actionTypeId == ChooseItemModel.ACTION_TAKE_PHOTO) {
                                //create image capture intent and start activity
                                //before start camera intent check storage permission
                                if (checkRunTimePermissionIsGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                                    mCurrentImagePath = startCameraIntent();
                                } else {
                                    //request write external permission for open camera intent
                                    requestRunTimePermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, Constants.FOR_TAKE_PHOTO_REQUEST_WRITE_EXTERNAL_STORAGE_PER, new PermissionEventListener() {
                                        @Override
                                        public void onGranted(int requestCode, String[] permissions) {
                                            //now start camera intent
                                            mCurrentImagePath = startCameraIntent();
                                        }

                                        @Override
                                        public void onFailure(int requestCode, String[] permissions) {
                                            Snackbar.make(findViewById(android.R.id.content), getString(R.string.re_action_on_deny_external_storage_per_for_take_photo), Snackbar.LENGTH_LONG)
                                                    .setAction(getString(R.string.action_manage), new View.OnClickListener() {
                                                        @Override
                                                        public void onClick(View v) {
                                                            startAppSettingsIntent();
                                                        }
                                                    }).show();
                                        }
                                    });
                                }
                            } else if (actionTypeId == ChooseItemModel.ACTION_CHOOSE_FROM_GALLERY) {
                                //open gallery to select single picture as image profile
                                //before that check runtime permission
                                if (checkRunTimePermissionIsGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                                    startGalleryIntent();
                                } else {
                                    //request write external permission for open camera intent
                                    requestRunTimePermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, Constants.FOR_OPEN_GALLARY_REQUEST_WRITE_EXTERNAL_STORAGE_PER, new PermissionEventListener() {
                                        @Override
                                        public void onGranted(int requestCode, String[] permissions) {
                                            startGalleryIntent();
                                        }

                                        @Override
                                        public void onFailure(int requestCode, String[] permissions) {
                                            Snackbar.make(findViewById(android.R.id.content), getString(R.string.re_action_on_deny_external_storage_per_for_pick_photo), Snackbar.LENGTH_LONG)
                                                    .setAction(getString(R.string.action_manage), new View.OnClickListener() {
                                                        @Override
                                                        public void onClick(View v) {
                                                            startAppSettingsIntent();
                                                        }
                                                    }).show();
                                        }
                                    });
                                }

                            } else if (actionTypeId == ChooseItemModel.ACTION_REMOVE) {
                                try {
                                    //send request to remove the current image profile
                                    mProgressDialog.setMessage(getString(R.string.re_action_on_deleting_current_image_profile));
                                    mProgressDialog.show();
                                    mTService.removeImageProfile(new Callback<UserProfileModel>() {
                                        @Override
                                        public void success(UserProfileModel userProfileModel, Response response) {
                                            try {

                                                if (mCurrentActivity != null) {
                                                    mProgressDialog.dismiss();
                                                    mAppPreferenceTools.updateUserProfileInformation(userProfileModel);
                                                    Picasso.with(mCurrentActivity).load(userProfileModel.image_profile_url)
                                                            .placeholder(R.drawable.image_place_holder)
                                                            .transform(new CropCircleTransformation()).into(mImImageProfile);
                                                }
                                            } catch (Exception ex) {
                                                if (mProgressDialog != null && mProgressDialog.isShowing()) {
                                                    mProgressDialog.dismiss();
                                                }
                                                AndroidUtilities.processApplicationError(ex, true);
                                                Snackbar.make(findViewById(android.R.id.content), getString(R.string.re_action_internal_app_error), Snackbar.LENGTH_LONG).show();
                                            }
                                        }

                                        @Override
                                        public void failure(RetrofitError error) {
                                            //handle error
                                            if (mCurrentActivity != null) {
                                                mProgressDialog.dismiss();
                                                CommonFeedBack commonFeedBack = new CommonFeedBack(findViewById(android.R.id.content), mCurrentActivity);
                                                commonFeedBack.checkCommonErrorAndBackUnCommonOne(error);
                                            }
                                        }
                                    });
                                } catch (Exception ex) {
                                    if (mProgressDialog != null && mProgressDialog.isShowing()) {
                                        mProgressDialog.dismiss();
                                    }
                                    AndroidUtilities.processApplicationError(ex, true);
                                    Snackbar.make(findViewById(android.R.id.content), getString(R.string.re_action_internal_app_error), Snackbar.LENGTH_LONG).show();
                                }
                            }

                        }

                        @Override
                        public void onChooseValueCode(String valueCode) {
                            //DO NOTHING
                        }

                        @Override
                        public void onChooseThemeColor(String theme) {

                        }

                    });
                    fragment.show(getFragmentManager(), Constants.RECYCLER_VIEW_CHOOSE_DIALOG_TAG);
                } catch (Exception ex) {
                    AndroidUtilities.processApplicationError(ex, true);
                    Snackbar.make(findViewById(android.R.id.content), getString(R.string.re_action_internal_app_error), Snackbar.LENGTH_LONG).show();
                }
            }
        });
        //config theme recycler view choose
        RecyclerView ryChooseTheme = (RecyclerView) findViewById(R.id.ry_choose_theme);
        ryChooseTheme.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        ryChooseTheme.setHasFixedSize(true);
        ChooseItemAdapter adapter = new ChooseItemAdapter(this);
        adapter.setData(mThemeUtil.getColorThemeItemList(ChooseItemModel.ITEM_THEME_HR_COLOR));
        adapter.setChooseItemClickListener(new ChooseItemAdapter.ChooseItemClickListener() {
            @Override
            public void handleActionWhenClick(int actionType, String valueCode) {
                //DO NOTHING
            }

            @Override
            public void handleOnChooseThemeColor(String themeName) {
                try {
                    //change theme and change the color of header and float action buttons
                    mThemeUtil.setCurrentTheme(themeName);
                    mHeaderView.setBackgroundColor(mAppPreferenceTools.getPrimaryColor());
                    mFabActionDone.setBackgroundTintList(ColorStateList.valueOf(mAppPreferenceTools.getAccentColor()));
                    mFabEditImage.setBackgroundTintList(ColorStateList.valueOf(mAppPreferenceTools.getAccentColor()));
                    configStatusBar(mAppPreferenceTools.getPrimaryDarkColor());
                    if (getResources().getBoolean(R.bool.isTablet)) {
                        RelativeLayout lyMainLayout = (RelativeLayout) findViewById(R.id.ly_main_content);
                        lyMainLayout.setBackgroundColor(mAppPreferenceTools.getPrimaryLightColor());
                    }
                } catch (Exception ex) {
                    AndroidUtilities.processApplicationError(ex, true);
                    Snackbar.make(findViewById(android.R.id.content), getString(R.string.re_action_internal_app_error), Snackbar.LENGTH_LONG).show();
                }
            }
        });
        ryChooseTheme.setAdapter(adapter);
    }

    /**
     * start camera and return generated image path
     *
     * @return String CurrentImagePath
     */
    private String startCameraIntent() {
        String imagePath = null;
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            File image = AndroidUtilities.generateImagePath();
            if (image != null) {
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(image));
                imagePath = image.getAbsolutePath();
            }
            startActivityForResult(cameraIntent, Constants.CAMERA_REQUEST_CODE);
            setAnimationOnStart();
        } catch (Exception e) {
            Snackbar.make(findViewById(android.R.id.content), getString(R.string.re_action_internal_app_error), Snackbar.LENGTH_LONG).show();
        }
        return imagePath;
    }

    /**
     * validate locale
     * check if system locale is not support then
     * return locale as default
     *
     * @param systemLocale String system default locale
     * @return String valid locale
     */
    private String validateLocale(String systemLocale) {
        String[] languageCodes = getResources().getStringArray(R.array.language_code);
        for (String languageCode : languageCodes) {
            if (languageCode.equals(systemLocale)) {
                return systemLocale;
            }
        }
        //return en as default locale
        return "en";
    }

    /**
     * done action update profile
     */
    private void doneAction() {
        try {
            //for update user profile first check inputs
            if (getCurrentFocus() != null) {
                AndroidUtilities.hideKeyboard(mEtxDisplayName);
            }
            if (mEtxDisplayName.getText().toString().trim().length() > 0) {
                //now send request to update user profile
                mProgressDialog.setMessage(getString(R.string.re_action_on_complete_sign_up_progress));
                mProgressDialog.show();
                Locale currentLocale = getResources().getConfiguration().locale;
                mAppPreferenceTools.setApplicationLocale(currentLocale.getLanguage());
                //create update profile request model
                UpdateProfileRequestModel updateProfileRequestModel = new UpdateProfileRequestModel(mEtxDisplayName.getText().toString(), validateLocale(currentLocale.getLanguage()), mAppPreferenceTools.getCurrentThemeName());
                mTService.updateProfile(updateProfileRequestModel, new Callback<UserProfileModel>() {
                    @Override
                    public void success(UserProfileModel userProfileModel, Response response) {
                        try {
                            if (mCurrentActivity != null) {
                                mProgressDialog.dismiss();
                                mAppPreferenceTools.updateUserProfileInformation(userProfileModel);
                                //also set false to sign up in mode
                                mAppPreferenceTools.setIsInSignUpMode(false);
                                //clear all of the other activities
                                Intent intent = new Intent(TApplication.applicationContext, DashboardActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                setAnimationOnStart();
                                finish();
                            }
                        } catch (Exception ex) {
                            if (mProgressDialog != null && mProgressDialog.isShowing()) {
                                mProgressDialog.dismiss();
                            }
                            AndroidUtilities.processApplicationError(ex, true);
                            Snackbar.make(findViewById(android.R.id.content), getString(R.string.re_action_internal_app_error), Snackbar.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        if (mCurrentActivity != null) {
                            mProgressDialog.dismiss();
                            //handle common errors , in this request don't have any un common errors
                            CommonFeedBack commonFeedBack = new CommonFeedBack(findViewById(android.R.id.content), mCurrentActivity);
                            commonFeedBack.checkCommonErrorAndBackUnCommonOne(error);
                        }
                    }
                });
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(getString(R.string.app_name));
                builder.setMessage(getString(R.string.re_action_enter_display_name));
                builder.setPositiveButton(getString(R.string.action_ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //show the soft keyboard
                        AndroidUtilities.showKeyboard(mEtxDisplayName);
                    }
                });
                builder.show();
            }
        } catch (Exception ex) {
            if (mProgressDialog != null && mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
            }
            AndroidUtilities.processApplicationError(ex, true);
            Snackbar.make(findViewById(android.R.id.content), getString(R.string.re_action_internal_app_error), Snackbar.LENGTH_LONG).show();
        }
    }

    /**
     * on activity result
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Crop.REQUEST_CROP) {
            try {
                if (resultCode == Activity.RESULT_OK) {
                    //after crop should send to server
                    Uri editedImageUri = Crop.getOutput(data);
                    File imageFile = new File(editedImageUri.getPath());
                    if (imageFile.exists()) {
                        uploadImageProfile(imageFile);
                    } else {
                        //show snack bar to error happened
                        Snackbar.make(findViewById(android.R.id.content), getString(R.string.re_action_internal_app_error), Snackbar.LENGTH_LONG).show();
                    }
                } else if (resultCode == Crop.RESULT_ERROR) {
                    //show snack bar to error happened
                    Snackbar.make(findViewById(android.R.id.content), getString(R.string.re_action_internal_app_error), Snackbar.LENGTH_LONG).show();
                }
            } catch (Exception ex) {
                AndroidUtilities.processApplicationError(ex, true);
                Snackbar.make(findViewById(android.R.id.content), getString(R.string.re_action_internal_app_error), Snackbar.LENGTH_LONG).show();
            }
        } else if (requestCode == Constants.CAMERA_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            try {
                navigateToPhotoCropActivity();
            } catch (Exception ex) {
                AndroidUtilities.processApplicationError(ex, true);
                Snackbar.make(findViewById(android.R.id.content), getString(R.string.re_action_internal_app_error), Snackbar.LENGTH_LONG).show();
            }
        } else if (requestCode == Constants.GALLERY_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            try {
                Uri selectedImageUri = data.getData();
                String extractUriFrom = selectedImageUri.toString();
                //check is from google photos or google drive
                if (extractUriFrom.contains("com.google.android.apps.photos.contentprovider") || extractUriFrom.contains("com.google.android.apps.docs.storage")) {
                    final int chunkSize = 1024;  // We'll read in one kB at a time
                    byte[] imageData = new byte[chunkSize];
                    File imageFile = AndroidUtilities.generateImagePath();
                    InputStream in = null;
                    OutputStream out = null;
                    mProgressDialog.setMessage(getString(R.string.re_action_loading));
                    mProgressDialog.show();
                    try {
                        in = getContentResolver().openInputStream(selectedImageUri);
                        out = new FileOutputStream(imageFile);
                        int bytesRead;
                        while ((bytesRead = in.read(imageData)) > 0) {
                            out.write(Arrays.copyOfRange(imageData, 0, Math.max(0, bytesRead)));
                        }
                        mCurrentImagePath = imageFile.getAbsolutePath();
                        if (mProgressDialog.isShowing()) {
                            mProgressDialog.dismiss();
                        }
                        navigateToPhotoCropActivity();
                    } catch (Exception ex) {
                        if (mProgressDialog.isShowing()) {
                            mProgressDialog.dismiss();
                        }
                        AndroidUtilities.processApplicationError(ex, true);
                        Snackbar.make(findViewById(android.R.id.content), getString(R.string.re_action_internal_app_error), Snackbar.LENGTH_LONG).show();
                        Log.e("Something went wrong.", ex.toString());
                    } finally {
                        if (in != null) {
                            in.close();
                        }
                        if (out != null) {
                            out.close();
                        }
                        if (mProgressDialog.isShowing()) {
                            mProgressDialog.dismiss();
                        }
                    }
                } else {
                    mCurrentImagePath = AndroidUtilities.getPath(selectedImageUri);
                    //now navigate to photoCropActivity
                    navigateToPhotoCropActivity();
                }
            } catch (Exception ex) {
                AndroidUtilities.processApplicationError(ex, true);
                Snackbar.make(findViewById(android.R.id.content), getString(R.string.re_action_internal_app_error), Snackbar.LENGTH_LONG).show();
            }
        }
    }

    /**
     * after select photo from gallery or take photo by camera
     * navigate to PhotoCropActivity to crop selected image profile
     */
    private void navigateToPhotoCropActivity() {
        //produce good file name to edit as CashDir+file_name_without_type+'_edited'
        File selected_image_file = new File(mCurrentImagePath);
        String file_name_without_type = selected_image_file.getName().substring(0, selected_image_file.getName().lastIndexOf("."));
        Uri source = Uri.fromFile(selected_image_file);
        Uri destination = Uri.fromFile(new File(getCacheDir(), file_name_without_type + "_edited"));
        //start crop activity to crop current image
        Crop.of(source, destination).withMaxSize(512, 512).asSquare().start(this);
        setAnimationOnStart();
    }

    /**
     * internal function for upload image profile
     *
     * @param imageFile File imageFile
     */
    private void uploadImageProfile(File imageFile) {
        try {
            TypedFile typedFile = new TypedFile("image/jpeg", imageFile);
            mProgressDialog.setMessage(getString(R.string.re_action_on_uploading_image_profile));
            mProgressDialog.show();
            mTService.updateImageProfile(typedFile, new Callback<UserProfileModel>() {
                @Override
                public void success(UserProfileModel userProfileModel, Response response) {
                    try {
                        if (mCurrentActivity != null) {
                            mProgressDialog.dismiss();
                            mAppPreferenceTools.updateUserProfileInformation(userProfileModel);
                            //load new image profile
                            Picasso.with(mCurrentActivity).load(userProfileModel.image_profile_url)
                                    .placeholder(R.drawable.image_place_holder)
                                    .transform(new CropCircleTransformation()).into(mImImageProfile);
                        }
                    } catch (Exception ex) {
                        if (mProgressDialog != null && mProgressDialog.isShowing()) {
                            mProgressDialog.dismiss();
                        }
                        AndroidUtilities.processApplicationError(ex, true);
                        Snackbar.make(findViewById(android.R.id.content), getString(R.string.re_action_internal_app_error), Snackbar.LENGTH_LONG).show();
                    }
                }

                @Override
                public void failure(RetrofitError error) {
                    if (mCurrentActivity != null) {
                        mProgressDialog.dismiss();
                        //handle errors
                        CommonFeedBack commonFeedBack = new CommonFeedBack(findViewById(android.R.id.content), mCurrentActivity);
                        commonFeedBack.checkCommonErrorAndBackUnCommonOne(error);
                    }
                }
            });
        } catch (Exception ex) {
            if (mProgressDialog != null && mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
            }
            AndroidUtilities.processApplicationError(ex, true);
            Snackbar.make(findViewById(android.R.id.content), getString(R.string.re_action_internal_app_error), Snackbar.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        try {
            saveSelfArgs(outState);
            super.onSaveInstanceState(outState);
        } catch (Exception ex) {
            AndroidUtilities.processApplicationError(ex, true);
            Snackbar.make(findViewById(android.R.id.content), getString(R.string.re_action_internal_app_error), Snackbar.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        try {
            restoreSelfArgs(savedInstanceState);
            super.onRestoreInstanceState(savedInstanceState);
        } catch (Exception ex) {
            AndroidUtilities.processApplicationError(ex, true);
            Snackbar.make(findViewById(android.R.id.content), getString(R.string.re_action_internal_app_error), Snackbar.LENGTH_LONG).show();
        }
    }

    /**
     * save m_current_image_path to bundle
     *
     * @param args Bundle
     */
    public void saveSelfArgs(Bundle args) {
        if (mCurrentImagePath != null) {
            args.putString("path", mCurrentImagePath);
        }
    }

    /**
     * restore m_current_image_path from bundle
     *
     * @param args Bundle
     */
    public void restoreSelfArgs(Bundle args) {
        mCurrentImagePath = args.getString("path");
    }

    @Override
    protected void onStop() {
        try {
            if (mProgressDialog != null && mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
            }
        } catch (Exception ex) {
            AndroidUtilities.processApplicationError(ex, true);
            Snackbar.make(findViewById(android.R.id.content), getString(R.string.re_action_internal_app_error), Snackbar.LENGTH_LONG).show();
        }
        super.onStop();
    }
}
