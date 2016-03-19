package com.atahani.telepathy.ui.fragment;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.*;
import android.widget.FrameLayout;

import com.pnikosis.materialishprogress.ProgressWheel;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;
import mobi.atahani.telepathy.R;
import com.atahani.telepathy.TApplication;
import com.atahani.telepathy.adapter.TelepathyAdapter;
import com.atahani.telepathy.model.TOperationResultModel;
import com.atahani.telepathy.model.TelepathiesModel;
import com.atahani.telepathy.network.TService;
import com.atahani.telepathy.realm.TelepathyModelRealm;
import com.atahani.telepathy.ui.AppSettingsActivity;
import com.atahani.telepathy.ui.DashboardActivity;
import com.atahani.telepathy.ui.component.CommonFeedBack;
import com.atahani.telepathy.ui.component.TelepathyBaseActivity;
import com.atahani.telepathy.ui.utility.Constants;
import com.atahani.telepathy.ui.utility.MarginDecoration;
import com.atahani.telepathy.utility.AndroidUtilities;
import com.atahani.telepathy.utility.AppPreferenceTools;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

import java.util.Date;

/**
 * Telepathy fragment
 * load resent telepathies that not matched yet
 */
public class TelepathyFragment extends Fragment {

    private Realm mRealmDB;
    private TService mTService;
    private AppPreferenceTools mAppPreferenceTools;
    private FrameLayout mLyEmptyStateContent;
    private ProgressDialog mProgressDialog;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private TelepathyAdapter mAdapter;
    private RecyclerView.ItemDecoration mItemDecoration;
    private RecyclerView mRecyclerView;
    private StaggeredGridLayoutManager mStaggeredGridLayoutManager;
    private LinearLayoutManager mLinearLayoutManager;
    private ProgressWheel mPrPagerLoad;
    private RealmResults<TelepathyModelRealm> mTelepathiesResult;
    private int mCurrentPageNumber = 1;
    private int mTotalItem = 0;
    private String mToUserId = "";
    private String mSearchQueryText = "";
    private BroadcastReceiver mMessageUpdateReceiver;
    private String mDirectionType = "LTR";


    public TelepathyFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View fragmentView = inflater.inflate(R.layout.fragment_telepathy, container, false);
        try {
            mDirectionType = getResources().getString(R.string.direction_type);
            mRealmDB = ((TelepathyBaseActivity) (getActivity())).getRealm();
            mTService = ((TelepathyBaseActivity) (getActivity())).getTService();
            mAppPreferenceTools = ((TelepathyBaseActivity) (getActivity())).getAppPreferenceTools();
            mProgressDialog = new ProgressDialog(getActivity());
            mProgressDialog.setCancelable(false);
            mProgressDialog.setCanceledOnTouchOutside(false);
            mLyEmptyStateContent = (FrameLayout) fragmentView.findViewById(R.id.ly_empty_state_content);
            mSwipeRefreshLayout = (SwipeRefreshLayout) fragmentView.findViewById(R.id.swipe_refresh_layout);
            mSwipeRefreshLayout.setColorSchemeColors(mAppPreferenceTools.getAccentColor(), mAppPreferenceTools.getPrimaryColor(), mAppPreferenceTools.getPrimaryDarkColor());
            mRecyclerView = (RecyclerView) fragmentView.findViewById(R.id.ry_telepathies);
            mPrPagerLoad = (ProgressWheel) fragmentView.findViewById(R.id.progress_wheel_load_page);
            mPrPagerLoad.setBarColor(mAppPreferenceTools.getAccentColor());
            mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    getTelepathiesFromServer(1, "", "");
                }
            });
            mRecyclerView.setPaddingRelative(getResources().getDimensionPixelOffset(R.dimen.dashboard_recycler_view_side_margin), getResources().getDimensionPixelOffset(R.dimen.dashboard_recycler_view_top_margin), getResources().getDimensionPixelOffset(R.dimen.dashboard_recycler_view_side_margin), 0);
            //get the telepathies from DB
            if (!mRealmDB.isClosed()) {
                mTelepathiesResult = mRealmDB.where(TelepathyModelRealm.class).findAllSorted("expireAt", Sort.ASCENDING);
            }
            //START config adapter
            mAdapter = new TelepathyAdapter(inflater, mTelepathiesResult, new TelepathyAdapter.TelepathyEventListener() {
                @Override
                public void onDeleteTelepathy(final String telepathyId, final int itemPosition) {
                    //open alert dialog to confirm this action
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle(getString(R.string.label_disappear_telepathy));
                    builder.setMessage(getString(R.string.label_disappear_telepathy_description));
                    builder.setPositiveButton(getString(R.string.action_yes), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //remove this telepathy send request to server
                            mProgressDialog.setMessage(getString(R.string.re_action_on_disappearing_telepathy));
                            mProgressDialog.show();
                            mTService.disappearTelepathy(telepathyId, new Callback<TOperationResultModel>() {
                                @Override
                                public void success(TOperationResultModel tOperationResultModel, Response response) {
                                    try {
                                        //remove this object from DB
                                        if (!mRealmDB.isClosed() && getActivity() != null && isAdded()) {
                                            mRealmDB.executeTransaction(new Realm.Transaction() {
                                                @Override
                                                public void execute(Realm realm) {
                                                    TelepathyModelRealm telepathyModelRealm = realm.where(TelepathyModelRealm.class).equalTo("telepathyId", telepathyId).findFirst();
                                                    if (telepathyModelRealm != null) {
                                                        telepathyModelRealm.removeFromRealm();
                                                    }
                                                }
                                            }, new Realm.Transaction.Callback() {
                                                @Override
                                                public void onSuccess() {
                                                    super.onSuccess();
                                                    if (getActivity() != null && isAdded()) {
                                                        updateFromDB();
                                                        mProgressDialog.dismiss();
                                                    }
                                                }

                                                @Override
                                                public void onError(Exception e) {
                                                    super.onError(e);
                                                    if (getActivity() != null && isAdded()) {
                                                        mProgressDialog.dismiss();
                                                    }
                                                }
                                            });
                                        }

                                    } catch (Exception ex) {
                                        if (mProgressDialog != null && mProgressDialog.isShowing()) {
                                            mProgressDialog.dismiss();
                                        }
                                        AndroidUtilities.processApplicationError(ex, true);
                                        if (isAdded() && getActivity() != null) {
                                            Snackbar.make(getActivity().findViewById(android.R.id.content), getString(R.string.re_action_internal_app_error), Snackbar.LENGTH_LONG).show();
                                        }
                                    }
                                }

                                @Override
                                public void failure(RetrofitError error) {
                                    if (getActivity() != null && isAdded()) {
                                        mProgressDialog.dismiss();
                                        CommonFeedBack commonFeedBack = new CommonFeedBack(getActivity().findViewById(android.R.id.content), getActivity());
                                        commonFeedBack.checkCommonErrorAndBackUnCommonOne(error);
                                    }
                                }
                            });
                        }
                    });
                    builder.setNegativeButton(getString(R.string.action_no), null);
                    builder.show();
                }
            });
            //END of adapter
            if (getResources().getBoolean(R.bool.isTablet)) {
                //set staggered grid layout manager for tablet
                mStaggeredGridLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
                mStaggeredGridLayoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS);
                mRecyclerView.setLayoutManager(mStaggeredGridLayoutManager);
                mAdapter.setStaggeredGridLayoutManager(mStaggeredGridLayoutManager);
            } else {
                //set simple linear layout manager
                mLinearLayoutManager = new LinearLayoutManager(TApplication.applicationContext);
                mRecyclerView.setLayoutManager(mLinearLayoutManager);
            }
            mItemDecoration = new MarginDecoration(TApplication.applicationContext, R.dimen.message_column_view_side_margin, R.dimen.message_column_view_side_margin, R.dimen.message_column_view_bottom_margin);
            mRecyclerView.addItemDecoration(mItemDecoration);
            mRecyclerView.setAdapter(mAdapter);
            if (!mAppPreferenceTools.isTelepathiesLoadedForFirstTime()) {
                getTelepathiesFromServer(1, "", "");
            } else {
                updateFromDB();
            }
            mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    if (mTotalItem > mTelepathiesResult.size()) {
                        mPrPagerLoad.spin();
                        //it's mean we have telepathies but do not have in DB
                        mCurrentPageNumber++;
                        getTelepathiesFromServer(mCurrentPageNumber, mToUserId, mSearchQueryText);
                    }
                }
            });
        } catch (Exception ex) {
            if (mProgressDialog != null && mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
            }
            AndroidUtilities.processApplicationError(ex, true);
            if (isAdded() && getActivity() != null) {
                Snackbar.make(getActivity().findViewById(android.R.id.content), getString(R.string.re_action_internal_app_error), Snackbar.LENGTH_LONG).show();
            }
        }
        return fragmentView;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        try {
            mRecyclerView.setPaddingRelative(getResources().getDimensionPixelOffset(R.dimen.dashboard_recycler_view_side_margin), getResources().getDimensionPixelOffset(R.dimen.dashboard_recycler_view_top_margin), getResources().getDimensionPixelOffset(R.dimen.dashboard_recycler_view_side_margin), 0);
            mRecyclerView.removeItemDecoration(mItemDecoration);
            if (mDirectionType.equals("RTL")) {
                mRecyclerView.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
            } else {
                mRecyclerView.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
            }
            mItemDecoration = new MarginDecoration(TApplication.applicationContext, R.dimen.message_column_view_side_margin, R.dimen.message_column_view_side_margin, R.dimen.message_column_view_bottom_margin);
            mRecyclerView.addItemDecoration(mItemDecoration);
        } catch (Exception ex) {
            AndroidUtilities.processApplicationError(ex, true);
            if (isAdded() && getActivity() != null) {
                Snackbar.make(getActivity().findViewById(android.R.id.content), getString(R.string.re_action_internal_app_error), Snackbar.LENGTH_LONG).show();
            }
        }
    }

    /**
     * get telepathies from server
     */
    private void getTelepathiesFromServer(int pageNumber, final String toUserId, final String searchQueryText) {
        if (getActivity() != null && isAdded()) {
            try {
                int itemPerPage = getResources().getInteger(R.integer.number_of_telepathies_for_each_request);
                mSwipeRefreshLayout.setRefreshing(true);
                mTService.getTelepathies(itemPerPage, pageNumber, toUserId, searchQueryText, new Callback<TelepathiesModel>() {
                    @Override
                    public void success(final TelepathiesModel telepathiesModel, Response response) {
                        try {

                            if (!mRealmDB.isClosed() && getActivity() != null && isAdded()) {
                                mRealmDB.executeTransaction(new Realm.Transaction() {
                                    @Override
                                    public void execute(Realm realm) {
                                        for (int i = 0; i < telepathiesModel.data.size(); i++) {
                                            //check is exist telepathy in DB or not
                                            TelepathyModelRealm telepathyModelFromDB = realm.where(TelepathyModelRealm.class).equalTo("telepathyId", telepathiesModel.data.get(i).id).findFirst();
                                            if (telepathyModelFromDB == null) {
                                                //it's mean not in DB and should add it
                                                TelepathyModelRealm newTelepathy = realm.createObject(TelepathyModelRealm.class);
                                                newTelepathy.setTelepathyId(telepathiesModel.data.get(i).id);
                                                if (telepathiesModel.data.get(i).to_user != null) {
                                                    newTelepathy.setWithUserId(telepathiesModel.data.get(i).to_user.user_id);
                                                    newTelepathy.setWithUserUsername(telepathiesModel.data.get(i).to_user.username);
                                                    newTelepathy.setWithUserDisplayName(telepathiesModel.data.get(i).to_user.display_name);
                                                    newTelepathy.setWithUserImageUrl(telepathiesModel.data.get(i).to_user.image_url);
                                                    newTelepathy.setWithUserTheme(telepathiesModel.data.get(i).to_user.theme);
                                                } else {
                                                    newTelepathy.setWithUserId(Constants.DELETED_ACCOUNT_VALUE);
                                                    newTelepathy.setWithUserUsername(Constants.DELETED_ACCOUNT_VALUE);
                                                    newTelepathy.setWithUserDisplayName(Constants.DELETED_ACCOUNT_VALUE);
                                                    newTelepathy.setWithUserImageUrl(Constants.DELETED_ACCOUNT_VALUE);
                                                    newTelepathy.setWithUserTheme(Constants.DEFAULT_THEME_NAME);
                                                }
                                                newTelepathy.setBody(telepathiesModel.data.get(i).body);
                                                newTelepathy.setCreatedAt(telepathiesModel.data.get(i).created_at);
                                                newTelepathy.setExpireAt(telepathiesModel.data.get(i).expire_at);
                                            } else if (telepathiesModel.data.get(i).to_user != null) {
                                                telepathyModelFromDB.setWithUserId(telepathiesModel.data.get(i).to_user.user_id);
                                                telepathyModelFromDB.setWithUserUsername(telepathiesModel.data.get(i).to_user.username);
                                                telepathyModelFromDB.setWithUserDisplayName(telepathiesModel.data.get(i).to_user.display_name);
                                                telepathyModelFromDB.setWithUserImageUrl(telepathiesModel.data.get(i).to_user.image_url);
                                                telepathyModelFromDB.setWithUserTheme(telepathiesModel.data.get(i).to_user.theme);
                                            } else {
                                                telepathyModelFromDB.setWithUserId(Constants.DELETED_ACCOUNT_VALUE);
                                                telepathyModelFromDB.setWithUserUsername(Constants.DELETED_ACCOUNT_VALUE);
                                                telepathyModelFromDB.setWithUserDisplayName(Constants.DELETED_ACCOUNT_VALUE);
                                                telepathyModelFromDB.setWithUserImageUrl(Constants.DELETED_ACCOUNT_VALUE);
                                                telepathyModelFromDB.setWithUserTheme(Constants.DEFAULT_THEME_NAME);
                                            }
                                        }
                                    }
                                }, new Realm.Transaction.Callback() {
                                    @Override
                                    public void onSuccess() {
                                        super.onSuccess();
                                        if (isAdded() && getActivity() != null) {
                                            mAppPreferenceTools.telepathiesLoadedForFirstTime();
                                            mCurrentPageNumber = telepathiesModel.page;
                                            mTotalItem = telepathiesModel.total;
                                            updateFromDB();
                                            if (mPrPagerLoad.isSpinning()) {
                                                mPrPagerLoad.stopSpinning();
                                            }
                                            mSwipeRefreshLayout.setRefreshing(false);
                                        }
                                    }

                                    @Override
                                    public void onError(Exception e) {
                                        super.onError(e);
                                        if (isAdded() && getActivity() != null) {
                                            if (mPrPagerLoad.isSpinning()) {
                                                mPrPagerLoad.stopSpinning();
                                            }
                                            mSwipeRefreshLayout.setRefreshing(false);
                                        }
                                    }
                                });
                            }
                        } catch (Exception ex) {
                            if (mSwipeRefreshLayout != null && mSwipeRefreshLayout.isRefreshing()) {
                                mSwipeRefreshLayout.setRefreshing(false);
                            }
                            if (mPrPagerLoad != null && mPrPagerLoad.isSpinning()) {
                                mPrPagerLoad.stopSpinning();
                            }
                            AndroidUtilities.processApplicationError(ex, true);
                            if (isAdded() && getActivity() != null) {
                                Snackbar.make(getActivity().findViewById(android.R.id.content), getString(R.string.re_action_internal_app_error), Snackbar.LENGTH_LONG).show();
                            }
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        if (getActivity() != null && isAdded()) {
                            if (mPrPagerLoad != null && mPrPagerLoad.isSpinning()) {
                                mPrPagerLoad.stopSpinning();
                            }
                            if (mSwipeRefreshLayout != null && mSwipeRefreshLayout.isRefreshing()) {
                                mSwipeRefreshLayout.setRefreshing(false);
                            }
                            CommonFeedBack commonFeedBack = new CommonFeedBack(getActivity().findViewById(android.R.id.content), getActivity());
                            commonFeedBack.checkCommonErrorAndBackUnCommonOne(error);
                        }
                    }
                });
            } catch (Exception ex) {
                if (mSwipeRefreshLayout != null && mSwipeRefreshLayout.isRefreshing()) {
                    mSwipeRefreshLayout.setRefreshing(false);
                }
                if (mPrPagerLoad != null && mPrPagerLoad.isSpinning()) {
                    mPrPagerLoad.stopSpinning();
                }
                AndroidUtilities.processApplicationError(ex, true);
                if (isAdded() && getActivity() != null) {
                    Snackbar.make(getActivity().findViewById(android.R.id.content), getString(R.string.re_action_internal_app_error), Snackbar.LENGTH_LONG).show();
                }
            }
        }
    }

    private void clearExpiredTelepathies() {
        try {
            if (!mRealmDB.isClosed() && getActivity() != null && isAdded()) {
                mRealmDB.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        RealmResults<TelepathyModelRealm> expiredResult = realm.where(TelepathyModelRealm.class).lessThanOrEqualTo("expireAt", new Date()).findAll();
                        for (int i = 0; i < expiredResult.size(); i++) {
                            //disappear the telepathy from server
                            mTService.disappearTelepathy(expiredResult.get(i).getTelepathyId(), new retrofit.Callback<TOperationResultModel>() {
                                @Override
                                public void success(TOperationResultModel tOperationResultModel, Response response) {
                                    //do nothing , since it's removed
                                }

                                @Override
                                public void failure(RetrofitError error) {
                                    if (getActivity() != null && isAdded()) {
                                        CommonFeedBack commonFeedBack = new CommonFeedBack(getActivity().findViewById(android.R.id.content), getActivity());
                                        commonFeedBack.checkCommonErrorAndBackUnCommonOne(error);
                                    }
                                }
                            });
                        }
                        //remove result from RealmDB
                        if (expiredResult.size() > 0) {
                            expiredResult.clear();
                        }
                    }
                }, new Realm.Transaction.Callback() {
                    @Override
                    public void onSuccess() {
                        super.onSuccess();
                        if (!mRealmDB.isClosed() && isAdded()) {
                            mAdapter.updateRealmResult(mTelepathiesResult);
                            //now should check empty state
                            if (mTelepathiesResult.size() == 0) {
                                changeEmptyState(Constants.DO_NOT_HAVE_TELEPATHY_EMPTY_STATE);
                            } else {
                                changeEmptyState(Constants.DISAPPEAR_EMPTY_STATE);
                            }
                        }
                    }

                    @Override
                    public void onError(Exception e) {
                        super.onError(e);
                        if (!mRealmDB.isClosed() && isAdded()) {
                            mAdapter.updateRealmResult(mTelepathiesResult);
                            //now should check empty state
                            if (mTelepathiesResult.size() == 0) {
                                changeEmptyState(Constants.DO_NOT_HAVE_TELEPATHY_EMPTY_STATE);
                            } else {
                                changeEmptyState(Constants.DISAPPEAR_EMPTY_STATE);
                            }
                        }
                    }
                });

            }
        } catch (Exception ex) {
            AndroidUtilities.processApplicationError(ex, true);
            if (isAdded() && getActivity() != null) {
                Snackbar.make(getActivity().findViewById(android.R.id.content), getString(R.string.re_action_internal_app_error), Snackbar.LENGTH_LONG).show();
            }
        }
    }

    /**
     * change empty sate
     *
     * @param stateType int type of state
     */
    private void changeEmptyState(int stateType) {
        try {
            if (getActivity() != null && isAdded()) {
                if (stateType == Constants.DO_NOT_HAVE_TELEPATHY_EMPTY_STATE) {
                    //load empty fragment into this side
                    EmptyStateFragment emptyStateFragment = EmptyStateFragment.newInstance(Constants.DO_NOT_HAVE_TELEPATHY_EMPTY_STATE);
                    //add this fragment to view
                    getChildFragmentManager().beginTransaction().replace(R.id.ly_empty_state_content, emptyStateFragment).commitAllowingStateLoss();
                    mLyEmptyStateContent.setVisibility(View.VISIBLE);
                } else {
                    mLyEmptyStateContent.setVisibility(View.GONE);
                }
            }
        } catch (Exception ex) {
            AndroidUtilities.processApplicationError(ex, true);
            if (isAdded() && getActivity() != null) {
                Snackbar.make(getActivity().findViewById(android.R.id.content), getString(R.string.re_action_internal_app_error), Snackbar.LENGTH_LONG).show();
            }
        }
    }

    /**
     * update DB and check empty state
     */
    public void updateFromDB() {
        clearExpiredTelepathies();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.menu_dashboard, menu);
        MenuItem telepathyItem = menu.findItem(R.id.action_telepathy);
        telepathyItem.getIcon().mutate().setColorFilter(null);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            try {
                //start app settings activity for RESULT
                getActivity().startActivityForResult(new Intent(TApplication.applicationContext, AppSettingsActivity.class), Constants.APP_SETTINGS_REQUEST_CODE);
                ((TelepathyBaseActivity) getActivity()).setAnimationOnStart();
            } catch (Exception ex) {
                AndroidUtilities.processApplicationError(ex, true);
                if (isAdded() && getActivity() != null) {
                    Snackbar.make(getActivity().findViewById(android.R.id.content), getString(R.string.re_action_internal_app_error), Snackbar.LENGTH_LONG).show();
                }
            }
        } else if (id == R.id.action_telepathy) {
            try {
                //navigate to friends tab
                ((DashboardActivity) (getActivity())).navigateToPage(Constants.FRIENDS_TAB);
            } catch (Exception ex) {
                AndroidUtilities.processApplicationError(ex, true);
                if (isAdded() && getActivity() != null) {
                    Snackbar.make(getActivity().findViewById(android.R.id.content), getString(R.string.re_action_internal_app_error), Snackbar.LENGTH_LONG).show();
                }
            }
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * check when tab un selected
     */
    public void checkWhenTabUnselectedOrSelected() {
        updateFromDB();
    }

    @Override
    public void onResume() {
        super.onResume();
        try {
            mMessageUpdateReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    int actionToDo = intent.getIntExtra(Constants.ACTION_TO_DO_PARAM, Constants.UPDATE_FROM_DB);
                    if (!mRealmDB.isClosed() && isAdded() && getActivity() != null) {
                        if (actionToDo == Constants.UPDATE_FROM_DB) {
                            updateFromDB();
                        } else if (actionToDo == Constants.UPDATE_FROM_NET) {
                            getTelepathiesFromServer(1, "", "");
                        }
                    }
                }
            };
            LocalBroadcastManager.getInstance(getActivity()).registerReceiver((mMessageUpdateReceiver), new IntentFilter(Constants.ACTION_TO_DO_FOR_TELEPATHY_UPDATE_INTENT_FILTER));
        } catch (Exception ex) {
            AndroidUtilities.processApplicationError(ex, true);
            if (isAdded() && getActivity() != null) {
                Snackbar.make(getActivity().findViewById(android.R.id.content), getString(R.string.re_action_internal_app_error), Snackbar.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onStop() {
        try {
            LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mMessageUpdateReceiver);
            if (mProgressDialog != null && mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
            }
        } catch (Exception ex) {
            AndroidUtilities.processApplicationError(ex, true);
            if (isAdded() && getActivity() != null) {
                Snackbar.make(getActivity().findViewById(android.R.id.content), getString(R.string.re_action_internal_app_error), Snackbar.LENGTH_LONG).show();
            }
        }
        super.onStop();
    }
}
