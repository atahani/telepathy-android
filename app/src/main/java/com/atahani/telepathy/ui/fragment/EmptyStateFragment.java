package com.atahani.telepathy.ui.fragment;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatTextView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.atahani.telepathy.ui.utility.Constants;
import com.atahani.telepathy.utility.AndroidUtilities;

import mobi.atahani.telepathy.R;
import com.atahani.telepathy.TApplication;

public class EmptyStateFragment extends Fragment {

    private static final String EMPTY_STATE_TYPE_PARAM = "EMPTY_STATE_TYPE";

    private int mEmptyStateType = Constants.DO_NOT_HAVE_CLASSIFY_MESSAGE_EMPTY_STATE;

    public EmptyStateFragment() {
        // Required empty public constructor
    }

    /**
     * use for new instance of fragment
     */
    public static EmptyStateFragment newInstance(int emptyStateType) {
        EmptyStateFragment fragment = new EmptyStateFragment();
        Bundle args = new Bundle();
        args.putInt(EMPTY_STATE_TYPE_PARAM, emptyStateType);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mEmptyStateType = getArguments().getInt(EMPTY_STATE_TYPE_PARAM, Constants.DO_NOT_HAVE_CLASSIFY_MESSAGE_EMPTY_STATE);
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View fragmentView = inflater.inflate(R.layout.fragment_empty_state, container, false);
        try {
            AppCompatTextView txEmptyMessage = (AppCompatTextView) fragmentView.findViewById(R.id.tx_empty_message);
            ImageView imEmptyImageView = (ImageView) fragmentView.findViewById(R.id.im_empty_state);
            int goToFriendTabResourceTabId = R.drawable.go_to_friend_tab;
            if (getResources().getString(R.string.locale).equals("fa")) {
                goToFriendTabResourceTabId = R.drawable.go_to_friend_tab_fa;
            }
            switch (mEmptyStateType) {
                case Constants.NOT_FOUND_EMPTY_STATE:
                    Picasso.with(TApplication.applicationContext)
                            .load(R.drawable.not_found)
                            .into(imEmptyImageView);
                    txEmptyMessage.setText(getString(R.string.label_not_found_any_person));
                    break;
                case Constants.DO_NOT_HAVE_CLASSIFY_MESSAGE_EMPTY_STATE:
                    Picasso.with(TApplication.applicationContext)
                            .load(goToFriendTabResourceTabId)
                            .into(imEmptyImageView);
                    txEmptyMessage.setText(getString(R.string.label_do_not_have_any_message));
                    break;
                case Constants.DO_NOT_HAVE_TELEPATHY_EMPTY_STATE:
                    Picasso.with(TApplication.applicationContext)
                            .load(goToFriendTabResourceTabId)
                            .into(imEmptyImageView);
                    txEmptyMessage.setText(getString(R.string.label_do_not_have_any_telepathy));
                    break;
            }
        } catch (Exception ex) {
            AndroidUtilities.processApplicationError(ex, true);
            if (isAdded() && getActivity() != null) {
                Snackbar.make(getActivity().findViewById(android.R.id.content), getString(R.string.re_action_internal_app_error), Snackbar.LENGTH_LONG).show();
            }
        }
        return fragmentView;
    }
}
