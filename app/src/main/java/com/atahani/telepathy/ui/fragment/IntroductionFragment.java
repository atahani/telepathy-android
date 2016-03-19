package com.atahani.telepathy.ui.fragment;


import android.app.Fragment;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatTextView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.atahani.telepathy.ui.utility.Constants;

import mobi.atahani.telepathy.R;
import com.atahani.telepathy.TApplication;
import com.atahani.telepathy.utility.AndroidUtilities;

/**
 * Introduction Fragment used in sign in activity view pager
 */
public class IntroductionFragment extends Fragment {

    private int mStepNumber;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment EmptyStateFragment.
     */
    public static IntroductionFragment newInstance(int stepNumber) {
        IntroductionFragment fragment = new IntroductionFragment();
        Bundle args = new Bundle();
        args.putInt(Constants.STEP_NUMBER_PARAM, stepNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public IntroductionFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mStepNumber = getArguments().getInt(Constants.STEP_NUMBER_PARAM);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View mainLayout = inflater.inflate(R.layout.fragment_introduction, container, false);
        try {
            ImageView mImageState = (ImageView) mainLayout.findViewById(R.id.im_empty_state);
            AppCompatTextView mTxStateMessage = (AppCompatTextView) mainLayout.findViewById(R.id.tx_empty_message);
            if (mStepNumber == 1) {
                mImageState.setImageDrawable(ContextCompat.getDrawable(TApplication.applicationContext, R.drawable.intro_slide_one));
                mTxStateMessage.setText(getString(R.string.label_intro_slide_one));
            } else if (mStepNumber == 2) {
                mImageState.setImageDrawable(ContextCompat.getDrawable(TApplication.applicationContext, R.drawable.intro_slide_two));
                mTxStateMessage.setText(getString(R.string.label_intro_slide_two));
            } else if (mStepNumber == 3) {
                mImageState.setImageDrawable(ContextCompat.getDrawable(TApplication.applicationContext, R.drawable.intro_slide_three));
                mTxStateMessage.setText(getString(R.string.label_intro_slide_three));
            }
        } catch (Exception ex) {
            AndroidUtilities.processApplicationError(ex, true);
            if (isAdded() && getActivity() != null) {
                Snackbar.make(getActivity().findViewById(android.R.id.content), getString(R.string.re_action_internal_app_error), Snackbar.LENGTH_LONG).show();
            }
        }
        return mainLayout;
    }
}
