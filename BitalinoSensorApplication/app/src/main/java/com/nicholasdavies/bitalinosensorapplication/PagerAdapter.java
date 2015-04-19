package com.nicholasdavies.bitalinosensorapplication;

import android.support.v13.app.FragmentPagerAdapter;

import com.nicholasdavies.bitalinosensorapplication.FragmentA;
import com.nicholasdavies.bitalinosensorapplication.FragmentB;
import com.nicholasdavies.bitalinosensorapplication.FragmentC;

/**
 * This Controls what fragment is returned when the user Swipes left or right
 *
 * @author Nick Davies
 */
public class PagerAdapter extends FragmentPagerAdapter {

    public PagerAdapter(android.app.FragmentManager fm) {
        super(fm);
    }

    @Override
    public int getCount() {
        return 4;
    }

    @Override
    public android.app.Fragment getItem(int arg0) {
        switch (arg0) {
            case 1:
                return new FragmentB();
            case 0:
                return new FragmentC();
            case 2:
                return new FragmentD();
            case 3:
                return new FragmentA();


            default:
                break;
        }
        return null;
    }


}
