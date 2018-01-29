package org.osmdroid.tileprovider.util;

import org.osmdroid.tileprovider.MapTileProviderBase;

import android.os.Handler;
import android.os.Message;
import android.view.View;

public class SimpleInvalidationHandler extends Handler {
	private View mView;

	public SimpleInvalidationHandler(final View pView) {
		super();
		mView = pView;
	}

	@Override
	public void handleMessage(final Message msg) {
		switch (msg.what) {
		case MapTileProviderBase.MAPTILE_SUCCESS_ID:
			if (mView!=null)
				mView.invalidate();
			break;
		}
	}

	/**
	 * See <a href="https://github.com/osmdroid/osmdroid/issues/390">https://github.com/osmdroid/osmdroid/issues/390</a>
	 *
	 */
	public void destroy(){
		mView=null;
	}
}
