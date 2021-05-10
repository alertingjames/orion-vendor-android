package com.app.orion_vendor.main;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.app.orion_vendor.R;
import com.app.orion_vendor.base.BaseActivity;
import com.app.orion_vendor.commons.Commons;
import com.app.orion_vendor.fragments.EditBrandFragment;
import com.app.orion_vendor.fragments.MyBrandProductsFragment;
import com.app.orion_vendor.fragments.StoreRatingsFragment;
import com.ogaclejapan.smarttablayout.SmartTabLayout;

public class MyBrandDetailActivity extends BaseActivity {

    FrameLayout countFrame;
    TextView countBox;
    TextView title;

    ViewPager viewPager;
    SmartTabLayout viewPagerTab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_brand_detail);

        // Get the ViewPager and set it's PagerAdapter so that it can display items
        viewPager = (ViewPager) findViewById(R.id.viewPager);
        viewPager.setAdapter(new SampleFragmentPagerAdapter(getSupportFragmentManager()));

        viewPagerTab = (SmartTabLayout) findViewById(R.id.viewPagerTab);
        viewPagerTab.setViewPager(viewPager);
        setCustomFont();

        title = (TextView)findViewById(R.id.title);
        title.setText(Commons.brand.getName());
        title.setTypeface(bold);

        countFrame = (FrameLayout)findViewById(R.id.countFrame);
        countBox = (TextView)findViewById(R.id.countBox);

    }

    public void setCustomFont() {

        ViewGroup vg = (ViewGroup) viewPagerTab.getChildAt(0);
        int tabsCount = vg.getChildCount();

        for (int i = 0; i < tabsCount; i++) {
            View tabViewChild = vg.getChildAt(i);
            if (tabViewChild instanceof TextView) {
                //Put your font in assests folder
                //assign name of the font here (Must be case sensitive)
                ((TextView) tabViewChild).setTypeface(bold);
            }
        }
    }

    public void back(View view){
        onBackPressed();
    }

    class SampleFragmentPagerAdapter extends FragmentPagerAdapter {
        final int PAGE_COUNT = 3;
        private String tabTitles[] = new String[] { "PROFILE", "PRODUCTS", "RATINGS" };

        public SampleFragmentPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return PAGE_COUNT;
        }

        @Override
        public Fragment getItem(int position) {
            switch (position){
                case 0:
                    return EditBrandFragment.newInstance(position);
                case 1:
                    return MyBrandProductsFragment.newInstance(position);
                case 2:
                    return StoreRatingsFragment.newInstance(position);
                default:
                    return EditBrandFragment.newInstance(position);
            }
        }

        @Override
        public CharSequence getPageTitle(int position) {
            // Generate title based on item position
            return tabTitles[position];
        }
    }


}
























