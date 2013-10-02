package org.osmdroid.google.wrapper.v2;

import java.lang.reflect.Field;

import org.osmdroid.ResourceProxy;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

class ResourceProxyImpl implements ResourceProxy {

	private final Resources mResources;
	private final String mResourceNameBase;
	private final float mDisplayMetricsDensity;

	public ResourceProxyImpl(final Context aContext) {
		mResources = aContext.getResources();
		mResourceNameBase = aContext.getPackageName() + ".R$";
		mDisplayMetricsDensity = mResources.getDisplayMetrics().density;
	}

	@Override
	public String getString(final string pResId) {
		final int id = getId("string", pResId.name());
		return mResources.getString(id);
	}

	@Override
	public String getString(final string pResId, final Object... formatArgs) {
		final int id = getId("string", pResId.name());
		return mResources.getString(id, formatArgs);
	}

	@Override
	public Bitmap getBitmap(final bitmap pResId) {
		return null; // TODO implementation
	}

	@Override
	public Drawable getDrawable(final bitmap pResId) {
		final int id = getId("drawable", pResId.name());
		return mResources.getDrawable(id);
	}

	@Override
	public float getDisplayMetricsDensity() {
		return mDisplayMetricsDensity;
	}

	private int getId(final String aType, final String aName) {
		try {
			final Class<?> cls = Class.forName(mResourceNameBase + aType);
			final Field field = cls.getDeclaredField(aName);
			return field.getInt(null);
		} catch (final ClassNotFoundException e) {
			// TODO logging
			throw new IllegalArgumentException("Resource not found: " + aName);
		} catch (final NoSuchFieldException e) {
			// TODO logging
			throw new IllegalArgumentException("Resource not found: " + aName);
		} catch (final IllegalAccessException e) {
			// TODO logging
			throw new IllegalArgumentException("Resource not found: " + aName);
		}
	}
}
