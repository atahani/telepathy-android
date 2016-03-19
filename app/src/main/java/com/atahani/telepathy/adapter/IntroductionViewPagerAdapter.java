package com.atahani.telepathy.adapter;


import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v13.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Introduction view pager adapter for dashboard activity
 */
public class IntroductionViewPagerAdapter extends FragmentPagerAdapter {

    private final List<Fragment> mFragmentList =new ArrayList<>();

    public IntroductionViewPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        return mFragmentList.get(position);
    }

    @Override
    public int getCount() {
        return mFragmentList.size();
    }

    /**
     * add fragment to adapter
     * @param fragment fragment
     */
    public void addFragment(Fragment fragment){
        mFragmentList.add(fragment);
    }
}
