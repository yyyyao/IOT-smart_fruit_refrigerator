package com.example.iot;

import android.net.Uri;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class PageActivity extends AppCompatActivity implements
        tab1.OnFragmentInteractionListener,
        tab2.OnFragmentInteractionListener{

    private ViewPager viewPager;
    private TabLayout tabLayout;
    private MyFragmentAdapter myFragmentAdapter;

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_page);

        viewPager = (ViewPager) findViewById(R.id.pager);
        myFragmentAdapter = new MyFragmentAdapter(getSupportFragmentManager());
        viewPager.setAdapter(myFragmentAdapter);

        tabLayout = (TabLayout) findViewById(R.id.tablayout);
        tabLayout.setupWithViewPager(viewPager);
    }
}
