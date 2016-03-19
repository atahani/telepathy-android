package com.atahani.telepathy.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;

import com.atahani.telepathy.ui.component.TelepathyBaseActivity;
import com.atahani.telepathy.ui.fragment.UserMessagesFragment;
import com.atahani.telepathy.ui.utility.Constants;
import com.atahani.telepathy.utility.AndroidUtilities;
import com.atahani.telepathy.utility.ThemeUtil;

import mobi.atahani.telepathy.R;

/**
 * the user messages activity
 */
public class UserMessagesActivity extends TelepathyBaseActivity {


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //get all of the bundle
        Bundle args = getIntent().getExtras();
        if (args != null) {
            //config status bar
            try {
                ThemeUtil themeUtil = new ThemeUtil(this);
                configStatusBar(themeUtil.getPrimaryDarkColorByThemeName(args.getString(Constants.WITH_USER_THEME_PARAM)));
                //add UserMessageFragment to this activity
                UserMessagesFragment mUserMessageFragment = UserMessagesFragment.newInstance(args.getString(Constants.WITH_USER_ID_PARAM)
                        , args.getString(Constants.WITH_USER_USERNAME_PARAM)
                        , args.getString(Constants.WITH_USER_DISPLAY_NAME_PARAM)
                        , args.getString(Constants.WITH_USER_IMAGE_URL_PARAM)
                        , args.getString(Constants.WITH_USER_THEME_PARAM)
                        , args.getLong(Constants.NUMBER_OF_USER_MESSAGE));
                getSupportFragmentManager().beginTransaction()
                        .replace(android.R.id.content, mUserMessageFragment)
                        .commit();
            } catch (Exception ex) {
                AndroidUtilities.processApplicationError(ex, true);
                Snackbar.make(findViewById(android.R.id.content), getString(R.string.re_action_internal_app_error), Snackbar.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.TELEPATHY_REQUEST_CODE && resultCode == RESULT_OK) {
            setResult(Constants.SEND_TELEPATHY_RESULT, data);
            finish();
            setAnimationOnExit();
        }
    }
}
