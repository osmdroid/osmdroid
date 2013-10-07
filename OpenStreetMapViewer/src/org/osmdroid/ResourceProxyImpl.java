package org.osmdroid;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;

/**
 * This is an extension of {@link org.osmdroid.DefaultResourceProxyImpl}
 * that first tries to get from the resources that this class is defined in.
 */
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
	public String getString(final string pResId, final Object... formatArgs) {
		try {
			final int res = R.string.class.getDeclaredField(pResId.name()).getInt(null);
			return mContext.getString(res, formatArgs);
		} catch (final Exception e) {
			return super.getString(pResId, formatArgs);
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
