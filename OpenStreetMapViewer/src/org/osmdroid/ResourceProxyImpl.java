package org.osmdroid;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;

public class ResourceProxyImpl extends DefaultResourceProxyImpl {

	private final Context mContext;

	public ResourceProxyImpl(final Context pContext) {
		super(pContext);
		mContext = pContext;
	}

	@Override
	public String getString(final string pResId) {
		try {
			final int res = R.string.class.getDeclaredField(pResId.name()).getInt(null);
			return mContext.getString(res);
		} catch (final Exception e) {
			return super.getString(pResId);
		}
	}

	@Override
	public Bitmap getBitmap(final bitmap pResId) {
		try {
			final int res = R.drawable.class.getDeclaredField(pResId.name()).getInt(null);
			return BitmapFactory.decodeResource(mContext.getResources(), res);
		} catch (final Exception e) {
			return super.getBitmap(pResId);
		}
	}

	@Override
	public Drawable getDrawable(final bitmap pResId) {
		try {
			final int res = R.drawable.class.getDeclaredField(pResId.name()).getInt(null);
			return mContext.getResources().getDrawable(res);
		} catch (final Exception e) {
			return super.getDrawable(pResId);
		}
	}
}
