package com.atahani.telepathy.ui.fragment;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatTextView;
import android.view.LayoutInflater;
import android.view.View;

import com.atahani.telepathy.ui.component.spinnerwheel.AbstractWheel;
import com.atahani.telepathy.utility.AndroidUtilities;
import com.atahani.telepathy.utility.AppPreferenceTools;

import mobi.atahani.telepathy.R;
import com.atahani.telepathy.TApplication;
import com.atahani.telepathy.ui.component.spinnerwheel.adapters.NumericWheelAdapter;

/**
 * Specify TTL Dialog for telepathy
 */
public class SpecifyTelepathyTTLDialogFragment extends DialogFragment {

    private AppPreferenceTools mAppPreferenceTools;
    private AbstractWheel mDay;
    private AbstractWheel mHour;
    private AbstractWheel mMin;
    private SpecifyTTLDialogEventListener mSpecifyTTLDialogEventListener;

    /**
     * set event listener
     *
     * @param specifyTTLDialogEventListener SpecifyTTLDialogEventListener
     */
    public void setSpecifyTTLDialogEventListener(SpecifyTTLDialogEventListener specifyTTLDialogEventListener) {
        this.mSpecifyTTLDialogEventListener = specifyTTLDialogEventListener;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        try {
            //get layout inflater
            mAppPreferenceTools = new AppPreferenceTools(TApplication.applicationContext);
            LayoutInflater inflater = getActivity().getLayoutInflater();
            View customLayout = inflater.inflate(R.layout.dialog_specify_telepathy_ttl, null);
            AppCompatTextView txDialogTitle = (AppCompatTextView) customLayout.findViewById(R.id.tx_dialog_title);
            txDialogTitle.setBackgroundColor(mAppPreferenceTools.getPrimaryColor());
            AppCompatTextView txDayLabel = (AppCompatTextView) customLayout.findViewById(R.id.tx_day_label);
            txDayLabel.setTextColor(mAppPreferenceTools.getPrimaryColor());
            AppCompatTextView txHourLabel = (AppCompatTextView) customLayout.findViewById(R.id.tx_hour_label);
            txHourLabel.setTextColor(mAppPreferenceTools.getPrimaryColor());
            AppCompatTextView txMinLabel = (AppCompatTextView) customLayout.findViewById(R.id.tx_min_label);
            txMinLabel.setTextColor(mAppPreferenceTools.getPrimaryColor());
            mDay = (AbstractWheel) customLayout.findViewById(R.id.wheel_day);
            mDay.setViewAdapter(new NumericWheelAdapter(TApplication.applicationContext, 0, 7, "%d"));
            mDay.setCyclic(true);
            mHour = (AbstractWheel) customLayout.findViewById(R.id.wheel_hour);
            mHour.setViewAdapter(new NumericWheelAdapter(TApplication.applicationContext, 0, 23, "%d"));
            mHour.setCyclic(true);
            mMin = (AbstractWheel) customLayout.findViewById(R.id.wheel_min);
            mMin.setViewAdapter(new NumericWheelAdapter(TApplication.applicationContext, 0, 59, "%d"));
            mMin.setCyclic(true);
            //get the last TTLMin
            int lastTTLinMin = mAppPreferenceTools.getTelepathyTTlInMin();
            if (lastTTLinMin < 11520) {
                if (lastTTLinMin >= 1440) {
                    int numberOfDay = Math.abs(lastTTLinMin / 1440);
                    lastTTLinMin = lastTTLinMin - (numberOfDay * 1440);
                    mDay.setCurrentItem(numberOfDay);
                } else {
                    mDay.setCurrentItem(0);
                }
                //check
                if (lastTTLinMin >= 60) {
                    int numberOfHour = Math.abs(lastTTLinMin / 60);
                    lastTTLinMin = lastTTLinMin - (numberOfHour * 60);
                    mHour.setCurrentItem(numberOfHour);
                } else {
                    mHour.setCurrentItem(0);
                }
                //set min of remain
                mMin.setCurrentItem(lastTTLinMin);
            } else {
                mDay.setCurrentItem(0);
                mHour.setCurrentItem(0);
                mMin.setCurrentItem(0);
            }
            builder.setView(customLayout);
            builder.setPositiveButton(getString(R.string.action_send), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (mSpecifyTTLDialogEventListener != null) {
                        int min = (mDay.getCurrentItem() * 1440) + (mHour.getCurrentItem() * 60) + (mMin.getCurrentItem());
                        if (min >= 1) {
                            //save the lastTTLInMin
                            mAppPreferenceTools.setTelepathyTTLInMin(min);
                            mSpecifyTTLDialogEventListener.onSend(min);
                            dismiss();
                        } else {
                            //show the error should be at least one min
                            Snackbar.make(getActivity().findViewById(android.R.id.content), getString(R.string.re_action_should_be_at_least_one_min), Snackbar.LENGTH_LONG).show();
                        }
                    }
                }
            });
            builder.setNegativeButton(getString(R.string.action_cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (mSpecifyTTLDialogEventListener != null) {
                        mSpecifyTTLDialogEventListener.onCancel();
                        dismiss();
                    }
                }
            });
        } catch (Exception ex) {
            AndroidUtilities.processApplicationError(ex, true);
            if (isAdded() && getActivity() != null) {
                Snackbar.make(getActivity().findViewById(android.R.id.content), getString(R.string.re_action_internal_app_error), Snackbar.LENGTH_LONG).show();
            }
        }
        return builder.create();
    }

    @Override
    public void onResume() {
        super.onResume();
        try {
            if (getResources().getBoolean(R.bool.isTablet)) {
                int width = getResources().getDimensionPixelSize(R.dimen.inner_dialog_width);
                getDialog().getWindow().setLayout(width, -2);
            }
        } catch (Exception ex) {
            AndroidUtilities.processApplicationError(ex, true);
            if (isAdded() && getActivity() != null) {
                Snackbar.make(getActivity().findViewById(android.R.id.content), getString(R.string.re_action_internal_app_error), Snackbar.LENGTH_LONG).show();
            }
        }

    }

    /**
     * interface to implement event listener
     */
    public interface SpecifyTTLDialogEventListener {
        void onSend(int TTLInMin);

        void onCancel();
    }
}
