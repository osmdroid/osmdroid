package org.osmdroid.intro;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.ViewGroup;

/**
 * Created by alex on 10/22/16.
 */

public class IntroSliderAdapter extends FragmentStatePagerAdapter {


    public IntroSliderAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new LogoFragment();
            case 1:
                return new AboutFragment();
            case 2:
                return new PermissionsFragment();
            case 3:
                return new StoragePreferenceFragment();
            case 4:
                return new DataUseWarning();
            case 5:
                return new TileSourceWarnings();
        }
        return null;
    }

    @Override
    public int getCount() {
        return 6;
    }

    @Override
    public void setPrimaryItem(ViewGroup container, int position, Object object) {
        super.setPrimaryItem(container, position, object);
        System.out.println("New pager is " + position);

    }
}
