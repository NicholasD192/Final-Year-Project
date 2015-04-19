package com.nicholasdavies.bitalinosensorapplication;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.app.FragmentManager;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;

/**
 * This is the Main Menu Activity which the Fragments Use
 *
 * @author Nick Davies
 */

public class Main extends FragmentActivity {

    ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        viewPager = (ViewPager) findViewById(R.id.pager);
        PagerAdapter padapter = new PagerAdapter(getFragmentManager());
        viewPager.setAdapter(padapter);


    }


}