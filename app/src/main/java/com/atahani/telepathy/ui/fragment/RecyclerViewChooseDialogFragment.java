package com.atahani.telepathy.ui.fragment;


import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;

import com.atahani.telepathy.ui.component.WrappingLayoutManager;

import mobi.atahani.telepathy.R;
import com.atahani.telepathy.adapter.ChooseItemAdapter;
import com.atahani.telepathy.model.ChooseItemModel;
import com.atahani.telepathy.ui.utility.Constants;
import com.atahani.telepathy.utility.AndroidUtilities;
import com.atahani.telepathy.utility.ThemeUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Recycler view choose dialog framgnet
 */
public class RecyclerViewChooseDialogFragment extends DialogFragment {

    private boolean mIsHaveImage = false;
    private int mChooseListType;
    private RecyclerViewChooseListener mRecyclerViewChooseListener;

    public RecyclerViewChooseDialogFragment() {
        // Required empty public constructor
    }

    /**
     * new instance of fragment
     *
     * @param chooseListType
     * @return fragment
     */
    public static RecyclerViewChooseDialogFragment newInstance(int chooseListType) {
        RecyclerViewChooseDialogFragment fragment = new RecyclerViewChooseDialogFragment();
        Bundle args = new Bundle();
        args.putInt("CHOOSE_LIST_TYPE", chooseListType);
        fragment.setArguments(args);
        return fragment;
    }


    /**
     * set event listener for dialog fragment
     *
     * @param recyclerViewChooseListener
     */
    public void setChangePhotoChooseListener(RecyclerViewChooseListener recyclerViewChooseListener) {
        this.mRecyclerViewChooseListener = recyclerViewChooseListener;
    }

    /**
     * set it CHANGE_PHOTO_CHOOSE_LIST_TYPE
     *
     * @param isHaveImage
     */
    public void setIsHaveImage(boolean isHaveImage) {
        this.mIsHaveImage = isHaveImage;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mChooseListType = getArguments().getInt("CHOOSE_LIST_TYPE");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View customLayout = inflater.inflate(R.layout.fragment_recycler_view_choose_dialog, null);
        try {
            RecyclerView mRyChoose = (RecyclerView) customLayout.findViewById(R.id.ry_choose);
            mRyChoose.setHasFixedSize(true);
            mRyChoose.setLayoutManager(new WrappingLayoutManager(getActivity().getBaseContext(), LinearLayoutManager.VERTICAL, false));
            //define adapter and set adapter
            ChooseItemAdapter chooseAdapter = new ChooseItemAdapter(getActivity());
            if (mChooseListType == Constants.CHANGE_PHOTO_CHOOSE_LIST_TYPE) {
                chooseAdapter.setData(getListOfChooseForChangePhoto());
            } else if (mChooseListType == Constants.CHANGE_LOCALE_TYPE) {
                chooseAdapter.setData(getListOfLanguage());
            } else if (mChooseListType == Constants.CHANGE_THEME_TYPE) {
                ThemeUtil themeUtil = new ThemeUtil(getActivity().getBaseContext());
                chooseAdapter.setData(themeUtil.getColorThemeItemList(ChooseItemModel.ITEM_THEME_COLOR));
            }
            chooseAdapter.setChooseItemClickListener(new ChooseItemAdapter.ChooseItemClickListener() {
                @Override
                public void handleActionWhenClick(int actionType, String valueCode) {
                    if (mRecyclerViewChooseListener != null) {
                        if (actionType != ChooseItemModel.ACTION_DO_NOTHING) {
                            mRecyclerViewChooseListener.onChooseItem(actionType);
                            dismiss();
                        } else if (valueCode != null) {
                            mRecyclerViewChooseListener.onChooseValueCode(valueCode);
                            dismiss();
                        }

                    }
                }

                @Override
                public void handleOnChooseThemeColor(String themeName) {
                    if (mRecyclerViewChooseListener != null) {
                        mRecyclerViewChooseListener.onChooseThemeColor(themeName);
                    }
                }
            });
            mRyChoose.setAdapter(chooseAdapter);
            builder.setView(customLayout);
        } catch (Exception ex) {
            AndroidUtilities.processApplicationError(ex, true);
            if (isAdded() && getActivity() != null) {
                Snackbar.make(getActivity().findViewById(android.R.id.content), getString(R.string.re_action_internal_app_error), Snackbar.LENGTH_LONG).show();
            }
        }
        return builder.create();
    }

    /**
     * get list of bottom sheet item model
     *
     * @return
     */
    public List<ChooseItemModel> getListOfChooseForChangePhoto() {
        List<ChooseItemModel> actionItems = new ArrayList<>();
        try {
            actionItems.add(new ChooseItemModel(ChooseItemModel.ITEM_WITH_ICON, getString(R.string.action_take_photo_by_camera), ChooseItemModel.DEFAULT_ICON_COLOR, R.mipmap.ic_photo_camera_white_24dp, ChooseItemModel.ACTION_TAKE_PHOTO));
            actionItems.add(new ChooseItemModel(ChooseItemModel.ITEM_WITH_ICON, getString(R.string.action_choose_photo_from_gallery), ChooseItemModel.DEFAULT_ICON_COLOR, R.mipmap.ic_photo_library_white_24dp, ChooseItemModel.ACTION_CHOOSE_FROM_GALLERY));
            if (mIsHaveImage) {
                actionItems.add(new ChooseItemModel(ChooseItemModel.ITEM_WITH_ICON, getString(R.string.action_delete_photo), ChooseItemModel.DEFAULT_ICON_COLOR, R.mipmap.ic_delete_white_24dp, ChooseItemModel.ACTION_REMOVE));
            }
        } catch (Exception ex) {
            AndroidUtilities.processApplicationError(ex, true);
            if (isAdded() && getActivity() != null) {
                Snackbar.make(getActivity().findViewById(android.R.id.content), getString(R.string.re_action_internal_app_error), Snackbar.LENGTH_LONG).show();
            }
        }
        return actionItems;
    }

    /**
     * get list of language
     *
     * @return
     */
    public List<ChooseItemModel> getListOfLanguage() {
        List<ChooseItemModel> languageList = new ArrayList<>();
        try {
            String[] languageCodes = getResources().getStringArray(R.array.language_code);
            String[] languageNames = getResources().getStringArray(R.array.language_name);
            for (int i = 0; i < languageCodes.length; i++) {
                languageList.add(new ChooseItemModel(ChooseItemModel.ITEM_ITEM_WITH_OUT_ICON, languageNames[i], languageCodes[i]));
            }
        } catch (Exception ex) {
            AndroidUtilities.processApplicationError(ex, true);
            if (isAdded() && getActivity() != null) {
                Snackbar.make(getActivity().findViewById(android.R.id.content), getString(R.string.re_action_internal_app_error), Snackbar.LENGTH_LONG).show();
            }
        }

        return languageList;
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
     * interface to implement the event listener in the host
     */
    public interface RecyclerViewChooseListener {
        void onChooseItem(int actionTypeId);

        void onChooseValueCode(String valueCode);

        void onChooseThemeColor(String theme);
    }

}
