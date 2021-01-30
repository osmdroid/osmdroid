package org.osmdroid.views.overlay.infowindow;

import android.content.Context;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import org.osmdroid.api.IMapView;
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

    /**
     * resource id value meaning "undefined resource id"
     */
    public static final int UNDEFINED_RES_ID = 0;

    static int mTitleId = UNDEFINED_RES_ID,
            mDescriptionId = UNDEFINED_RES_ID,
            mSubDescriptionId = UNDEFINED_RES_ID,
            mImageId = UNDEFINED_RES_ID; //resource ids

    private static void setResIds(Context context) {
        String packageName = context.getPackageName(); //get application package name
        mTitleId = context.getResources().getIdentifier("id/bubble_title", null, packageName);
        mDescriptionId = context.getResources().getIdentifier("id/bubble_description", null, packageName);
        mSubDescriptionId = context.getResources().getIdentifier("id/bubble_subdescription", null, packageName);
        mImageId = context.getResources().getIdentifier("id/bubble_image", null, packageName);
        if (mTitleId == UNDEFINED_RES_ID || mDescriptionId == UNDEFINED_RES_ID
                || mSubDescriptionId == UNDEFINED_RES_ID || mImageId == UNDEFINED_RES_ID) {
            Log.e(IMapView.LOGTAG, "BasicInfoWindow: unable to get res ids in " + packageName);
        }
    }

    public BasicInfoWindow(int layoutResId, MapView mapView) {
        super(layoutResId, mapView);

        if (mTitleId == UNDEFINED_RES_ID)
            setResIds(mapView.getContext());

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
        TextView temp = ((TextView) mView.findViewById(mTitleId /*R.id.title*/));

        if (temp != null) temp.setText(title);

        String snippet = overlay.getSnippet();
        if (snippet == null)
            snippet = "";
        Spanned snippetHtml = Html.fromHtml(snippet);
        ((TextView) mView.findViewById(mDescriptionId /*R.id.description*/)).setText(snippetHtml);

        //handle sub-description, hidding or showing the text view:
        TextView subDescText = (TextView) mView.findViewById(mSubDescriptionId);
        String subDesc = overlay.getSubDescription();
        if (subDesc != null && !("".equals(subDesc))) {
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
