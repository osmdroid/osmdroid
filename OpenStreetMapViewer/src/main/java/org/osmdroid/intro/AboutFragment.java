package org.osmdroid.intro;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.osmdroid.R;

/**
 * created on 1/5/2017.
 *
 * @author Alex O'Ree
 */
public class AboutFragment extends Fragment implements View.OnClickListener {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.intro_about, container, false);
        v.findViewById(R.id.introbuttonsite).setOnClickListener(this);
        return v;
    }

    @Override
    public void onClick(View v) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/osmdroid/osmdroid/"));
        startActivity(browserIntent);
    }
}
