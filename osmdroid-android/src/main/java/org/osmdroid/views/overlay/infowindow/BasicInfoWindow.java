package org.osmdroid.views.overlay.infowindow;

import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import org.osmdroid.api.IMapView;
import org.osmdroid.library.R;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.OverlayWithIW;

/**
 * {@link org.osmdroid.views.overlay.infowindow.BasicInfoWindow} is the default
 * implementation of {@link org.osmdroid.views.overlay.infowindow.InfoWindow} for an
 * {@link org.osmdroid.views.overlay.OverlayWithIW}.
 * <p>
 * It handles a title, a description and a sub-description.
 * Clicking on the bubble will close it.
 *
 * <img alt="Class diagram around Marker class" width="686" height="413" src='./doc-files/marker-infowindow-classes.png' />
 *
 * @author M.Kergall
 * @see Marker
 */
public class BasicInfoWindow extends InfoWindow {

    public BasicInfoWindow(int layoutResId, MapView mapView) {
        super(layoutResId, mapView);

        //default behavior: close it when clicking on the bubble:
        mView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent e) {
                if (e.getAction() == MotionEvent.ACTION_UP)
                    close();
                return true;
            }
        });
    }

    @Override
    public void onOpen(Object item) {
        OverlayWithIW overlay = (OverlayWithIW) item;
        String title = overlay.getTitle();
        if (title == null)
            title = "";
        if (mView == null) {
            Log.w(IMapView.LOGTAG, "Error trapped, BasicInfoWindow.open, mView is null!");
            return;
        }
        TextView temp = (mView.findViewById(R.id.bubble_title));

        if (temp != null) temp.setText(title);

        String snippet = overlay.getSnippet();
        if (snippet == null)
            snippet = "";
        Spanned snippetHtml = Html.fromHtml(snippet);
        ((TextView) mView.findViewById(R.id.bubble_description)).setText(snippetHtml);

        //handle sub-description, hidding or showing the text view:
        TextView subDescText = mView.findViewById(R.id.bubble_subdescription);
        String subDesc = overlay.getSubDescription();
        if (subDesc != null && !subDesc.isEmpty()) {
            subDescText.setText(Html.fromHtml(subDesc));
            subDescText.setVisibility(View.VISIBLE);
        } else {
            subDescText.setVisibility(View.GONE);
        }

    }

    @Override
    public void onClose() {
        //by default, do nothing
    }

}
