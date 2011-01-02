package org.osmdroid.tileprovider.util;

import org.andnav.osm.tileprovider.OpenStreetMapTile;

import android.os.Handler;
import android.os.Message;
import android.view.View;

public class SimpleInvalidationHandler extends Handler {
	private View mView;

	public SimpleInvalidationHandler(View pView) {
		super();
		mView = pView;
	}

	@Override
	public void handleMessage(final Message msg) {
		switch (msg.what) {
		case OpenStreetMapTile.MAPTILE_SUCCESS_ID:
			mView.invalidate();
			break;
		}
	}
}
