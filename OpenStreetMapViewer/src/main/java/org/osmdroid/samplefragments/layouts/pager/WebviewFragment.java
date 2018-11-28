package org.osmdroid.samplefragments.layouts.pager;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import org.osmdroid.R;

/**
 * Created by alex on 10/22/16.
 */

public class WebviewFragment extends Fragment {
    //webview1

    WebView webview;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.map_viewpager_webview, null);
        webview = v.findViewById(R.id.webview1);
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        webview.loadUrl("https://github.com/osmdroid/osmdroid");
    }
}
