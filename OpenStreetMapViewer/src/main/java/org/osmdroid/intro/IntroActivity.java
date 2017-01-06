package org.osmdroid.intro;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;

import com.github.paolorotolo.appintro.AppIntro;
import com.github.paolorotolo.appintro.AppIntroFragment;

import org.osmdroid.MainActivity;
import org.osmdroid.R;

/**
 * created on 1/5/2017.
 *
 * @author Alex O'Ree
 */

public class IntroActivity extends AppIntro {

    @Override
    public void onCreate(Bundle savedInstanced){
        super.onCreate(savedInstanced);

        addSlide(AppIntroFragment.newInstance("Open Map", "osmdroid sample app", R.drawable.icon, getResources().getColor(R.color.primary)));
        // Add your slide fragments here.
        // AppIntro will automatically generate the dots indicator and buttons.
        //addSlide(new LogoFragment());
        addSlide(new AboutFragment());
        addSlide(new PermissionsFragment());
        addSlide(new StoragePreferenceFragment());


        showSkipButton(true);
        setProgressButtonEnabled(true);
        setVibrate(false);

    }

    @Override
    public void onDonePressed(Fragment currentFragment) {
        super.onDonePressed(currentFragment);
        // Do something when users tap on Done button.
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
        this.finish();
    }


    @Override
    public void onSlideChanged(@Nullable Fragment oldFragment, @Nullable Fragment newFragment) {
        super.onSlideChanged(oldFragment, newFragment);

    }
}
