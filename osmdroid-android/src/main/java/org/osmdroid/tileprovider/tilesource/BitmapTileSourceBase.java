package org.osmdroid.tileprovider.tilesource;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

import org.osmdroid.tileprovider.BitmapPool;
import org.osmdroid.tileprovider.MapTile;
import org.osmdroid.tileprovider.ReusableBitmapDrawable;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.util.Log;
import org.osmdroid.api.IMapView;
import org.osmdroid.tileprovider.constants.OpenStreetMapTileProviderConstants;
import org.osmdroid.tileprovider.util.StreamUtils;

public abstract class BitmapTileSourceBase implements ITileSource {

	private static int globalOrdinal = 0;

	private final int mMinimumZoomLevel;
	private final int mMaximumZoomLevel;

	private final int mOrdinal;
	protected final String mName;
	protected final String mImageFilenameEnding;
	protected final Random random = new Random();

	private final int mTileSizePixels;

	//private final string mResourceId;

	/**
	 * Constructor
	 * @param aName a human-friendly name for this tile source. this name is also used on the file system, to keep the characters linux file system friendly
	 * @param aZoomMinLevel the minimum zoom level this tile source can provide
	 * @param aZoomMaxLevel the maximum zoom level this tile source can provide
	 * @param aTileSizePixels the tile size in pixels this tile source provides
	 * @param aImageFilenameEnding the file name extension used when constructing the filename
	 */
	public BitmapTileSourceBase(final String aName, 
			final int aZoomMinLevel, final int aZoomMaxLevel, final int aTileSizePixels,
			final String aImageFilenameEnding) {
		mOrdinal = globalOrdinal++;
		mName = aName;
		mMinimumZoomLevel = aZoomMinLevel;
		mMaximumZoomLevel = aZoomMaxLevel;
		mTileSizePixels = aTileSizePixels;
		mImageFilenameEnding = aImageFilenameEnding;
	}

	@Override
	public int ordinal() {
		return mOrdinal;
	}

	@Override
	public String name() {
		return mName;
	}

	public String pathBase() {
		return mName;
	}

	public String imageFilenameEnding() {
		return mImageFilenameEnding;
	}

	@Override
	public int getMinimumZoomLevel() {
		return mMinimumZoomLevel;
	}

	@Override
	public int getMaximumZoomLevel() {
		return mMaximumZoomLevel;
	}

	@Override
	public int getTileSizePixels() {
		return mTileSizePixels;
	}


	@Override
	public Drawable getDrawable(final MapTile aTile, final String aFilePath) {
		BufferedInputStream inputStream = null;
		try {
			// default implementation will load the file as a bitmap and create
			// a BitmapDrawable from it
			BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
			BitmapPool.getInstance().applyReusableOptions(bitmapOptions);



			inputStream = new BufferedInputStream(new FileInputStream(aFilePath),
				StreamUtils.IO_BUFFER_SIZE);

			readExpiresHeader(aTile, inputStream);

			final Bitmap bitmap = BitmapFactory.decodeStream(inputStream, null, bitmapOptions); //BitmapFactory.decodeFile(aFilePath, bitmapOptions);
			if (bitmap != null) {
				StreamUtils.closeStream(inputStream);
				return new ReusableBitmapDrawable(bitmap);
			} else {
				// if we couldn't load it then it's invalid - delete it
				try {
					new File(aFilePath).delete();
				} catch (final Throwable e) {
					Log.e(IMapView.LOGTAG,"Error deleting invalid file: " + aFilePath, e);
				}
			}
		} catch (final OutOfMemoryError e) {
			Log.e(IMapView.LOGTAG,"OutOfMemoryError loading bitmap: " + aFilePath);
			System.gc();
		} catch (final FileNotFoundException e) {
			Log.e(IMapView.LOGTAG,"FileNotFoundException loading bitmap: " + aFilePath);
		} catch (final IOException e) {
			Log.e(IMapView.LOGTAG,"IOException loading bitmap: " + aFilePath);
		} finally {
			StreamUtils.closeStream(inputStream);
		}
		return null;
	}

	@Override
	public String getTileRelativeFilenameString(final MapTile tile) {
		final StringBuilder sb = new StringBuilder();
		sb.append(pathBase());
		sb.append('/');
		sb.append(tile.getZoomLevel());
		sb.append('/');
		sb.append(tile.getX());
		sb.append('/');
		sb.append(tile.getY());
		sb.append(imageFilenameEnding());
		return sb.toString();
	}

	@Override
	public Drawable getDrawable(MapTile aTile, final InputStream aFileInputStream) throws LowMemoryException {

		try {
			// default implementation will load the file as a bitmap and create
			// a BitmapDrawable from it
			BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
			BitmapPool.getInstance().applyReusableOptions(bitmapOptions);

			readExpiresHeader(aTile, aFileInputStream);

			final Bitmap bitmap = BitmapFactory.decodeStream(aFileInputStream, null, bitmapOptions); //BitmapFactory.decodeFile(aFilePath, bitmapOptions);
			if (bitmap != null) {
				return new ReusableBitmapDrawable(bitmap);
			}
		} catch (final OutOfMemoryError e) {
			Log.e(IMapView.LOGTAG,"OutOfMemoryError loading bitmap");
			System.gc();
			throw new LowMemoryException(e);
		} catch (final FileNotFoundException e) {
			Log.e(IMapView.LOGTAG,"FileNotFoundException loading bitmap");
		} catch (final IOException e) {
			Log.e(IMapView.LOGTAG,"IOException loading bitmap");
		}
		return null;
	}

	private boolean readExpiresHeader(MapTile aTile, InputStream inputStream) throws IOException {
		try {
			String expires = StreamUtils.readString(inputStream);
			SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");
			Date dateExpires = dateFormat.parse(expires);
			aTile.setExpires(dateExpires);
			return true;
		} catch (ParseException e) {
			return false;
		}
	}

	public final class LowMemoryException extends Exception {
		private static final long serialVersionUID = 146526524087765134L;

		public LowMemoryException(final String pDetailMessage) {
			super(pDetailMessage);
		}

		public LowMemoryException(final Throwable pThrowable) {
			super(pThrowable);
		}
	}
}
