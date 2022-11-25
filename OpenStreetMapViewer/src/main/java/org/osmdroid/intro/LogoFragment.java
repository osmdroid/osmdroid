package org.osmdroid.intro;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.osmdroid.R;

import androidx.fragment.app.Fragment;

/**
 * created on 1/5/2017.
 *
 * @author Alex O'Ree
 */

public class LogoFragment extends Fragment {


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.intro_main, container, false);

        return v;
    }
}
