package org.osmdroid.bonuspack.cachemanager;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;;
import org.osmdroid.api.IMapView;
import org.osmdroid.bonuspack.utils.BonusPackHelper;
import org.osmdroid.tileprovider.MapTile;
import org.osmdroid.tileprovider.MapTileProviderBase;
import org.osmdroid.tileprovider.constants.OpenStreetMapTileProviderConstants;
import org.osmdroid.tileprovider.modules.TileWriter;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;
import org.osmdroid.tileprovider.util.StreamUtils;
import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.util.MyMath;
import org.osmdroid.views.MapView;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.graphics.Point;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

/**
 * Provides various methods for managing the local filesystem cache of osmdroid tiles: <br>
 * - Dowloading of tiles inside a specified area, <br>
 * - Cleaning of tiles inside a specified area,<br>
 * - Information about cache capacity and current cache usage. <br>
 * 
 * Important note 1: <br>
 * These methods only make sense for a MapView using an OnlineTileSourceBase: 
 * bitmap tiles downloaded from urls. <br>
 * 
 * Important note 2 - about Bulk Downloading:<br>
 * When using OSM Mapnik tile server as the tile source, take care about OSM Tile usage policy 
 * (http://wiki.openstreetmap.org/wiki/Tile_usage_policy). 
 * Do not let to end-users the ability to download significant areas of tiles. <br>
 * 
 * @author M.Kergall
 *
 */
public class CacheManager {
	
	protected final MapTileProviderBase mTileProvider;
	protected final TileWriter mTileWriter;
	protected final MapView mMapView;
	
	public CacheManager(final MapView mapView){
		mTileProvider = mapView.getTileProvider();
		mTileWriter = new TileWriter();
		mMapView = mapView;
	}
	
	public Point getMapTileFromCoordinates(final double aLat, final double aLon, final int zoom){
		final int y = (int) Math.floor((1 - Math.log(Math.tan(aLat * Math.PI / 180) + 1 / Math.cos(aLat * Math.PI / 180)) / Math.PI) / 2 * (1 << zoom));
		final int x = (int) Math.floor((aLon + 180) / 360 * (1 << zoom));
		return new Point(x, y);
	}

	public File getFileName(ITileSource tileSource, MapTile tile){
		final File file = new File(OpenStreetMapTileProviderConstants.TILE_PATH_BASE,
				tileSource.getTileRelativeFilenameString(tile) + OpenStreetMapTileProviderConstants.TILE_PATH_EXTENSION);
		return file;
	}
	
	/** 
	 * @return true if success, false if error 
	 */
	public boolean loadTile(OnlineTileSourceBase tileSource, MapTile tile){
		//check if file is already downloaded:
		File file = getFileName(tileSource, tile);
		if (file.exists()){
			return true;
		}
		
		InputStream in = null;
		OutputStream out = null;
		HttpURLConnection urlConnection=null;
		try {
			final String tileURLString = tileSource.getTileURLString(tile);

			 urlConnection = (HttpURLConnection) new URL(tileURLString).openConnection();

			// Check to see if we got success
			if (urlConnection.getResponseCode() != 200) {
				Log.w(BonusPackHelper.LOG_TAG, "Problem downloading MapTile: " + tile + " HTTP response: " + urlConnection.getResponseMessage());
				return false;
			}

			in = urlConnection.getInputStream();
	
			final ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
			out = new BufferedOutputStream(dataStream, StreamUtils.IO_BUFFER_SIZE);
			StreamUtils.copy(in, out);
			out.flush();
			final byte[] data = dataStream.toByteArray();
			final ByteArrayInputStream byteStream = new ByteArrayInputStream(data);
	
			// Save the data to the filesystem cache
			mTileWriter.saveFile(tileSource, tile, byteStream);
			byteStream.reset();
	
			//final Drawable result = tileSource.getDrawable(byteStream);
			return true;
		} catch (final UnknownHostException e) {
			// no network connection 
			Log.w(BonusPackHelper.LOG_TAG, "UnknownHostException downloading MapTile: " + tile + " : " + e);
		} catch (final FileNotFoundException e) {
			Log.w(BonusPackHelper.LOG_TAG, "Tile not found: " + tile + " : " + e);
		} catch (final IOException e) {
			Log.w(BonusPackHelper.LOG_TAG, "IOException downloading MapTile: " + tile + " : " + e);
		} catch (final Throwable e) {
			Log.e(BonusPackHelper.LOG_TAG, "Error downloading MapTile: " + tile, e);
		} finally {
			StreamUtils.closeStream(in);
			StreamUtils.closeStream(out);
			if (urlConnection!=null)
				urlConnection.disconnect();
			urlConnection=null;
		}

		return false;
	}
	
	/** @return the theoretical number of tiles in the specified area */
	public int possibleTilesInArea(BoundingBoxE6 bb, final int zoomMin, final int zoomMax){
		int total = 0;
		for (int zoomLevel=zoomMin; zoomLevel<=zoomMax; zoomLevel++){
			Point mLowerRight = getMapTileFromCoordinates(bb.getLatSouthE6()*1E-6, bb.getLonEastE6()*1E-6, zoomLevel);
			Point mUpperLeft = getMapTileFromCoordinates(bb.getLatNorthE6()*1E-6, bb.getLonWestE6()*1E-6, zoomLevel);
			int y = mLowerRight.y - mUpperLeft.y + 1;
			int x = mLowerRight.x - mUpperLeft.x +1;
			int nbTilesForZoomLevel = x * y;
			total += nbTilesForZoomLevel;
		}
		return total;
	}
	
	protected String zoomMessage(int zoomLevel, int zoomMin, int zoomMax){
		return "Handling zoom level: "+zoomLevel+" (from "+zoomMin+" to "+zoomMax+")";
	}
	
	/**
	 * Download in background all tiles of the specified area in osmdroid cache. 
	 * @param ctx
	 * @param bb
	 * @param zoomMin
	 * @param zoomMax
	 */
	public void downloadAreaAsync(Context ctx, BoundingBoxE6 bb, final int zoomMin, final int zoomMax){
		new DownloadingTask(ctx, bb, zoomMin, zoomMax,null).execute();
	}

	/**
	 * Download in background all tiles of the specified area in osmdroid cache.
	 * @param ctx
	 * @param bb
	 * @param zoomMin
	 * @param zoomMax
	 */
	public void downloadAreaAsync(Context ctx, BoundingBoxE6 bb, final int zoomMin, final int zoomMax, final CacheManagerCallback callback){
		new DownloadingTask(ctx, bb, zoomMin, zoomMax, callback).execute();
	}

	public interface CacheManagerCallback{
		public void onTaskComplete();
	}
	
	/** generic class for common code related to AsyncTask management */
	protected abstract class CacheManagerTask extends AsyncTask<Object, Integer, Integer>{
		ProgressDialog mProgressDialog;
		int mZoomMin, mZoomMax;
		BoundingBoxE6 mBB;
		Context mCtx;
		CacheManagerCallback callback=null;

		public CacheManagerTask(Context pCtx, BoundingBoxE6 pBB, final int pZoomMin, final int pZoomMax, final CacheManagerCallback callback) {
			this(pCtx,pBB,pZoomMin,pZoomMax);
			this.callback=callback;
		}

		public CacheManagerTask(Context pCtx, BoundingBoxE6 pBB, final int pZoomMin, final int pZoomMax) {
			mCtx = pCtx;
			mProgressDialog = createProgressDialog(pCtx);
			mBB = pBB;
	        mZoomMin = Math.max(pZoomMin, mMapView.getMinZoomLevel());
	        mZoomMax = Math.min(pZoomMax, mMapView.getMaxZoomLevel());
	    }
		
		protected ProgressDialog createProgressDialog(Context pCtx){
			ProgressDialog pd = new ProgressDialog(pCtx);
			pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			pd.setCancelable(true);
			pd.setOnCancelListener(new OnCancelListener() {
				@Override public void onCancel(DialogInterface dialog) {
					cancel(true);
				}
	        });
			return pd;
		}
		
		@Override protected void onProgressUpdate(final Integer... count){
			//count[0] = tile counter, count[1] = current zoom level
			mProgressDialog.setProgress(count[0]);
			mProgressDialog.setMessage(zoomMessage(count[1], mZoomMin, mZoomMax));
		}
		
	}
	
	protected class DownloadingTask extends CacheManagerTask {
		
		public DownloadingTask(Context pCtx, BoundingBoxE6 pBB, final int pZoomMin, final int pZoomMax, final CacheManagerCallback callback) {
			super(pCtx, pBB, pZoomMin, pZoomMax, callback);
	    }
		
		@Override protected void onPreExecute(){
			mProgressDialog.setTitle("Downloading tiles");
			mProgressDialog.setMessage(zoomMessage(mZoomMin, mZoomMin, mZoomMax));
			int total = possibleTilesInArea(mBB, mZoomMin, mZoomMax);
			mProgressDialog.setMax(total);
			mProgressDialog.show();
		}
		
		@Override protected void onPostExecute(final Integer errors) {
			if (errors != 0)
				Toast.makeText(mCtx, "Loading completed with " + errors + " errors.", Toast.LENGTH_SHORT).show();
			if (mProgressDialog.isShowing()) {
				mProgressDialog.dismiss();
			}
			if (callback!=null){
				try {
					callback.onTaskComplete();
				}catch (Exception ex){
					Log.w(IMapView.LOGTAG, "Error caught processing cachemanager callback, your implementation is faulty", ex);
				}
			}
		}
		
		@Override protected Integer doInBackground(Object... params) {
			int errors = downloadArea();
			return errors;
		}
		
		/** Do the job. No attempt to 
		 * @return the number of loading errors. 
		 */
		protected int downloadArea(){
			OnlineTileSourceBase tileSource;
			if (mTileProvider.getTileSource() instanceof OnlineTileSourceBase)
				tileSource = (OnlineTileSourceBase)mTileProvider.getTileSource();
			else {
				Log.e(BonusPackHelper.LOG_TAG, "TileSource is not an online tile source");
				return 0;
			}
			
			int tileCounter = 0;
			int errors = 0;
			for (int zoomLevel=mZoomMin; zoomLevel<=mZoomMax; zoomLevel++){
				Point mLowerRight = getMapTileFromCoordinates(mBB.getLatSouthE6()*1E-6, mBB.getLonEastE6()*1E-6, zoomLevel);
				Point mUpperLeft = getMapTileFromCoordinates(mBB.getLatNorthE6()*1E-6, mBB.getLonWestE6()*1E-6, zoomLevel);
				final int mapTileUpperBound = 1 << zoomLevel;
				//Get all the MapTiles from the upper left to the lower right:
				for (int y = mUpperLeft.y; y <= mLowerRight.y; y++) {
					for (int x = mUpperLeft.x; x <= mLowerRight.x; x++) {
						final int tileY = MyMath.mod(y, mapTileUpperBound);
						final int tileX = MyMath.mod(x, mapTileUpperBound);
						final MapTile tile = new MapTile(zoomLevel, tileX, tileY);
						//Drawable currentMapTile = mTileProvider.getMapTile(tile);
						boolean ok = loadTile(tileSource, tile);
						if (!ok)
							errors++;
						tileCounter++;
						if (tileCounter % 20 == 0){
							if (isCancelled())
								return errors;
							publishProgress(tileCounter, zoomLevel);
						}
					}
				}
			}
			return errors;
		}
		
	} //DownloadingTask
	
	/**
	 * Remove all cached tiles in the specified area. 
	 * @param ctx
	 * @param bb
	 * @param zoomMin
	 * @param zoomMax
	 */
	public void cleanAreaAsync(Context ctx, BoundingBoxE6 bb, int zoomMin, int zoomMax){
		new CleaningTask(ctx, bb, zoomMin, zoomMax).execute();
	}
	
	protected class CleaningTask extends CacheManagerTask {
		
		public CleaningTask(Context pCtx, BoundingBoxE6 pBB, final int pZoomMin, final int pZoomMax) {
			super(pCtx, pBB, pZoomMin, pZoomMax);
	    }
		
		@Override protected void onPreExecute(){
			mProgressDialog.setTitle("Cleaning tiles");
			mProgressDialog.setMessage(zoomMessage(mZoomMin, mZoomMin, mZoomMax));
			int total = possibleTilesInArea(mBB, mZoomMin, mZoomMax);
			mProgressDialog.setMax(total);
			mProgressDialog.show();
		}
		
		@Override protected void onPostExecute(final Integer deleted) {
			Toast.makeText(mCtx, "Cleaning completed, " + deleted + " tiles deleted.", Toast.LENGTH_SHORT).show();
			if (mProgressDialog.isShowing()) {
				mProgressDialog.dismiss();
			}
		}
		
		@Override protected Integer doInBackground(Object... params) {
			int errors = cleanArea();
			return errors;
		}
		
		/** Do the job. 
		 * @return the number of tiles deleted. 
		 */
		protected int cleanArea(){
			ITileSource tileSource = mTileProvider.getTileSource();
			int deleted = 0;
			int tileCounter = 0;
			for (int zoomLevel=mZoomMin; zoomLevel<=mZoomMax; zoomLevel++){
				Point mLowerRight = getMapTileFromCoordinates(mBB.getLatSouthE6()*1E-6, mBB.getLonEastE6()*1E-6, zoomLevel);
				Point mUpperLeft = getMapTileFromCoordinates(mBB.getLatNorthE6()*1E-6, mBB.getLonWestE6()*1E-6, zoomLevel);
				
				final int mapTileUpperBound = 1 << zoomLevel;
				//Get all the MapTiles from the upper left to the lower right:
				for (int y = mUpperLeft.y; y <= mLowerRight.y; y++) {
					for (int x = mUpperLeft.x; x <= mLowerRight.x; x++) {
						final int tileY = MyMath.mod(y, mapTileUpperBound);
						final int tileX = MyMath.mod(x, mapTileUpperBound);
						final MapTile tile = new MapTile(zoomLevel, tileX, tileY);
						File file = getFileName(tileSource, tile);
						if (file.exists()){
							file.delete();
							deleted++;
						}
						tileCounter++;
						if (tileCounter % 1000 == 0){
							if (isCancelled())
								return deleted;
							publishProgress(tileCounter, zoomLevel);
						}
					}
				}
			}
			return deleted;
		}
	} //CleaningTask
	
	/** @return volume currently use in the osmdroid local filesystem cache, in bytes. 
	 * Note that this method currently takes a while. 
	 * */
	public long currentCacheUsage(){
		//return TileWriter.getUsedCacheSpace(); //returned value is not stable! Increase and decrease, for unknown reasons. 
		return directorySize(OpenStreetMapTileProviderConstants.TILE_PATH_BASE);
	}

	/** @return the capacity of the osmdroid local filesystem cache, in bytes. 
	 * This capacity is currently a hard-coded constant inside osmdroid. */
	public long cacheCapacity(){
		return OpenStreetMapTileProviderConstants.TILE_MAX_CACHE_SIZE_BYTES;
	}

	/** @return the total size of a directory and of its whole content, recursively */
	public long directorySize(final File pDirectory) {
		long usedCacheSpace = 0;
		final File[] z = pDirectory.listFiles();
		if (z != null) {
			for (final File file : z) {
				if (file.isFile()) {
					usedCacheSpace += file.length();
				} else if (file.isDirectory()) {
					usedCacheSpace += directorySize(file);
				}
			}
		}
		return usedCacheSpace;
	}

}
