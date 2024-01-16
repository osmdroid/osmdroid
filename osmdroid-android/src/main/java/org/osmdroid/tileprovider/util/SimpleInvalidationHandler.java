package org.osmdroid.tileprovider.util;

import android.os.Handler;
import android.os.Message;
import android.view.View;

import androidx.annotation.CallSuper;
import androidx.annotation.MainThread;
import androidx.annotation.UiThread;

import org.osmdroid.tileprovider.MapTileProviderBase;

public class SimpleInvalidationHandler extends Handler {
    private View mView;

    public SimpleInvalidationHandler(final View pView) {
        super();
        mView = pView;
    }

    @CallSuper
    @UiThread @MainThread
    @Override
    public void handleMessage(final Message msg) {
        @MapTileProviderBase.MAPTYPERESULT
        final int cMapTypeResult = MapTileProviderBase.unmaskMapTypeResult(msg.what);
        switch (cMapTypeResult) {
            case MapTileProviderBase.MAPTYPERESULT_SUCCESS:
                if (mView != null)
                    mView.invalidate();
                break;
        }
    }

    /**
     * See <a href="https://github.com/osmdroid/osmdroid/issues/390">https://github.com/osmdroid/osmdroid/issues/390</a>
     */
    public void destroy() {
        mView = null;
    }
}
