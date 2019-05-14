package com.example.iot;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

public class MyFragmentAdapter extends FragmentStatePagerAdapter {
    private String[] tabTitles = new String[]{"My Fruits","Recommendation"};

    public MyFragmentAdapter(FragmentManager fm){
        super(fm);
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return tabTitles[position];
    }

    @Override
    public Fragment getItem(int i) {
        switch(i){
            case 0: return new tab1();
            case 1: return new tab2();
        }
        return null;
    }

    @Override
    public int getCount() {
        return 2;
    }
}

