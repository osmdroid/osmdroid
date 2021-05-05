package org.osmdroid.intro;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import org.osmdroid.MainActivity;
import org.osmdroid.R;


/**
 * Intro activity, this is a simple intro to osmdroid, some legal stuff, tile storage preference, etc
 * <p>
 * created on 1/5/2017.
 *
 * @author Alex O'Ree
 */

public class IntroActivity extends FragmentActivity implements View.OnClickListener, ViewPager.OnPageChangeListener {
    ViewPager introviewpager;
    ProgressBar introProgressBar;
    IntroSliderAdapter adapter;
    Button next;
    Button prev;
    int viewpagerCurrentPosition = 0;

    @Override
    public void onCreate(Bundle savedInstanced) {
        super.onCreate(savedInstanced);

        //skip this nonsense
        if (PreferenceManager.getDefaultSharedPreferences(this).contains("osmdroid_first_ran")) {
            Intent i = new Intent(this, MainActivity.class);
            startActivity(i);
            finish();
        }


        setContentView(R.layout.intro_frame);
        introviewpager = findViewById(R.id.introviewpager);
        adapter = new IntroSliderAdapter(getSupportFragmentManager());
        introviewpager.setAdapter(adapter);
        introviewpager.addOnPageChangeListener(this);
        introProgressBar = findViewById(R.id.introProgressBar);
        introProgressBar.setMax(adapter.getCount() - 1);
        introProgressBar.setProgress(0);

        next = findViewById(R.id.introNext);
        prev = findViewById(R.id.introPrev);
        next.setOnClickListener(this);
        prev.setOnClickListener(this);

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.introNext:
                if (viewpagerCurrentPosition + 1 < adapter.getCount())
                    introviewpager.setCurrentItem(viewpagerCurrentPosition + 1, true);
                else {
                    SharedPreferences.Editor edit = PreferenceManager.getDefaultSharedPreferences(this).edit();
                    edit.putString("osmdroid_first_ran", "yes");
                    edit.commit();
                    //next to MainActivity
                    Intent i = new Intent(this, MainActivity.class);
                    startActivity(i);
                    finish();   //prevent the back button from returning to this activity
                }
                break;
            case R.id.introPrev:
                if (viewpagerCurrentPosition - 1 >= 0)
                    introviewpager.setCurrentItem(viewpagerCurrentPosition - 1, true);


                break;
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        viewpagerCurrentPosition = position;
        introProgressBar.setProgress(position);
        if (position == 0) {
            prev.setVisibility(View.INVISIBLE);
        } else {
            prev.setVisibility(View.VISIBLE);
        }

        if (position == adapter.getCount() - 1) {
            next.setText(R.string.done);
        } else {
            next.setText(R.string.next);
        }
        if (position == 3) {
            //storage preference fragment, force the update since now permissions may have been granted
            StoragePreferenceFragment item = (StoragePreferenceFragment) adapter.getItem(position);
            item.updateStorage(this);
        }
    }

    @Override
    public void onPageSelected(int position) {

    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }
}
