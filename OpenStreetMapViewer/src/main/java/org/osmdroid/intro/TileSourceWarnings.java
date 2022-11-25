package org.osmdroid.intro;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.osmdroid.R;

import androidx.fragment.app.Fragment;

/**
 * created on 1/12/2017.
 *
 * @author Alex O'Ree
 */

public class TileSourceWarnings extends Fragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.intro_tilesources_osm, container, false);
        return v;
    }

}