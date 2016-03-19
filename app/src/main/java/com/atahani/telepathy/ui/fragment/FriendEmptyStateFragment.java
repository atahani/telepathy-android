package com.atahani.telepathy.ui.fragment;


import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import mobi.atahani.telepathy.R;
import com.atahani.telepathy.TApplication;
import com.atahani.telepathy.utility.AndroidUtilities;

/***
 *
 */
public class FriendEmptyStateFragment extends Fragment {

    public FriendEmptyStateFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View fragmentView = inflater.inflate(R.layout.fragment_friend_empty_state, container, false);
        try {
            ImageView imSearchYourFriendHelper = (ImageView) fragmentView.findViewById(R.id.im_search_your_friend_help);
            ImageView imAppInviteHelper = (ImageView) fragmentView.findViewById(R.id.im_app_invite_help);
            if (getString(R.string.locale).equals("fa")) {
                Picasso.with(TApplication.applicationContext)
                        .load(R.drawable.search_your_friend_helper_fa)
                        .into(imSearchYourFriendHelper);
            } else {
                Picasso.with(TApplication.applicationContext)
                        .load(R.drawable.search_your_friend_helper)
                        .into(imSearchYourFriendHelper);
            }
            if (getString(R.string.locale).equals("fa")) {
                Picasso.with(TApplication.applicationContext)
                        .load(R.drawable.app_invite_helper_fa)
                        .into(imAppInviteHelper);
            } else {
                Picasso.with(TApplication.applicationContext)
                        .load(R.drawable.app_invite_helper)
                        .into(imAppInviteHelper);
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
