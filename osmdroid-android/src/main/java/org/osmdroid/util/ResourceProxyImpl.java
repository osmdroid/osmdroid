package org.osmdroid.util;

import java.lang.reflect.Field;

import org.osmdroid.DefaultResourceProxyImpl;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;

/**
 * This is an extension of {@link org.osmdroid.DefaultResourceProxyImpl}
 * that first tries to get from resources by using reflection to get the id of the resource
 * from the package that the context is in.
 */
public class ResourceProxyImpl extends DefaultResourceProxyImpl {

	private final Resources mResources;
	private final String mResourceNameBase;

	public ResourceProxyImpl(final Context aContext) {
		super(aContext);
		mResources = aContext.getResources();
		mResourceNameBase = aContext.getPackageName() + ".R$";
	}

	@Override
	public String getString(final string pResId) {
		final int id = getId("string", pResId.name());
		return id != 0 ? mResources.getString(id) : super.getString(pResId);
	}

	@Override
	public String getString(final string pResId, final Object... formatArgs) {
		final int id = getId("string", pResId.name());
		return id != 0 ? mResources.getString(id, formatArgs) : super.getString(pResId, formatArgs);
	}

	@Override
	public Bitmap getBitmap(final bitmap pResId) {
		final int id = getId("drawable", pResId.name());
		return id != 0 ? BitmapFactory.decodeResource(mResources, id) : super.getBitmap(pResId);
	}

	@Override
	public Drawable getDrawable(final bitmap pResId) {
		final int id = getId("drawable", pResId.name());
		return id != 0 ? mResources.getDrawable(id) : super.getDrawable(pResId);
	}

	private int getId(final String aType, final String aName) {
		try {
			final Class<?> cls = Class.forName(mResourceNameBase + aType);
			final Field field = cls.getDeclaredField(aName);
			return field.getInt(null);
		} catch (final Exception e) {
			return 0;
		}
	}
}
