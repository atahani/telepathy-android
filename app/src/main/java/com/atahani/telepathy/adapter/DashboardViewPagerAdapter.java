package com.atahani.telepathy.adapter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.SparseArray;
import android.view.ViewGroup;

import mobi.atahani.telepathy.R;
import com.atahani.telepathy.ui.fragment.FriendFragment;
import com.atahani.telepathy.ui.fragment.MessageFragment;
import com.atahani.telepathy.ui.fragment.TelepathyFragment;

/**
 * Dashboard view pager adapter for 3 fragment page
 */
public class DashboardViewPagerAdapter extends FragmentStatePagerAdapter {


    private Context mContext;
    SparseArray<Fragment> registeredFragments = new SparseArray<Fragment>();

    public DashboardViewPagerAdapter(FragmentManager fm, Context context) {
        super(fm);
        this.mContext = context;
    }

    @Override
    public Fragment getItem(int position) {

        switch (position) {
            case 0:
                return new FriendFragment();
            case 1:
                return new TelepathyFragment();
            case 2:
                return new MessageFragment();
            default:
                return new FriendFragment();
        }
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return this.mContext.getResources().getString(R.string.title_tab_friend);
            case 1:
                return this.mContext.getResources().getString(R.string.title_tab_telepathy);
            case 2:
                return this.mContext.getResources().getString(R.string.title_tab_message);
            default:
                return this.mContext.getResources().getString(R.string.title_tab_friend);
        }
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Fragment fragment = (Fragment) super.instantiateItem(container, position);
        registeredFragments.put(position, fragment);
        return fragment;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        registeredFragments.remove(position);
        super.destroyItem(container, position, object);
    }

    public Fragment getRegisteredFragment(int position) {
        return registeredFragments.get(position);
    }
}
