package org.osmdroid.bonuspack.mapsforge;

import java.io.File;
import org.mapsforge.map.reader.MapDatabase;
import org.mapsforge.android.maps.DebugSettings;
import org.mapsforge.android.maps.mapgenerator.JobParameters;
import org.mapsforge.android.maps.mapgenerator.MapGeneratorJob;
import org.mapsforge.android.maps.mapgenerator.databaserenderer.DatabaseRenderer;
import org.mapsforge.map.reader.header.FileOpenResult;
import org.osmdroid.ResourceProxy;
import org.osmdroid.ResourceProxy.string;
import org.osmdroid.tileprovider.MapTile;
import org.osmdroid.tileprovider.tilesource.BitmapTileSourceBase;
import org.mapsforge.map.rendertheme.InternalRenderTheme;
import org.mapsforge.map.rendertheme.XmlRenderTheme;
import org.mapsforge.core.model.Tile;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;

/**
 * http://www.salidasoftware.com/how-to-render-mapsforge-tiles-in-osmdroid/
 * @author Salida Software
 * Adapted from code found here : http://www.sieswerda.net/2012/08/15/upping-the-developer-friendliness/
 */
public class MapsForgeTileSource extends BitmapTileSourceBase {

	protected File mapFile;

	// Reasonable defaults ..
	public static final int MIN_ZOOM = 8;
	public static final int MAX_ZOOM = 20;
	public static final int TILE_SIZE_PIXELS = 256;

	private DatabaseRenderer renderer;
	private MapDatabase mapDatabase;
	private XmlRenderTheme jobTheme;
	private JobParameters jobParameters;
	private DebugSettings debugSettings;


	/**
	 * The reason this constructor is protected is because all parameters,
	 * except file should be determined from the archive file. Therefore a
	 * factory method is necessary.
	 * 
	 * @param minZoom
	 * @param maxZoom
	 * @param tileSizePixels
	 * @param file
	 */
	protected MapsForgeTileSource(int minZoom, int maxZoom, int tileSizePixels, File file) {
		super("MapsForgeTiles", minZoom, maxZoom, tileSizePixels, ".png");

		mapDatabase = new MapDatabase();

		//Make sure the database can open the file
		FileOpenResult fileOpenResult = this.mapDatabase.openFile(file);
		if (fileOpenResult.isSuccess()) {
			mapFile = file;
		}
		else{
			mapFile = null;
		}

		renderer = new DatabaseRenderer(mapDatabase);
		minZoom = renderer.getStartZoomLevel();
		maxZoom = renderer.getZoomLevelMax();
		tileSizePixels = mapDatabase.getMapFileInfo().tilePixelSize;
		Log.d("MAPSFORGE", "min="+minZoom+" max="+maxZoom+" tilesize="+tileSizePixels);

		//  For this to work I had to edit org.mapsforge.map.rendertheme.InternalRenderTheme.getRenderThemeAsStream()  to:
		//  return this.getClass().getResourceAsStream(this.absolutePath + this.file);
		jobTheme = InternalRenderTheme.OSMARENDER;    		
		jobParameters = new JobParameters(jobTheme, 1);
		debugSettings = new DebugSettings(false, false, false);

	}

	/**
	 * Creates a new MapsForgeTileSource from file.
	 * 
	 * Parameters minZoom and maxZoom are obtained from the
	 * database. If they cannot be obtained from the DB, the default values as
	 * defined by this class are used.
	 * 
	 * @param file
	 * @return the tile source
	 */
	public static MapsForgeTileSource createFromFile(File file) {
		//TODO - set these based on .map file info
		int minZoomLevel = MIN_ZOOM;
		int maxZoomLevel = MAX_ZOOM;
		int tileSizePixels = TILE_SIZE_PIXELS;

		return new MapsForgeTileSource(minZoomLevel, maxZoomLevel, tileSizePixels, file);
	}

	//The synchronized here is VERY important.  If missing, the mapDatabase read gets corrupted by multiple threads reading the file at once.
	public synchronized Drawable renderTile(MapTile pTile) {

		Tile tile = new Tile((long)pTile.getX(), (long)pTile.getY(), (byte)pTile.getZoomLevel());

		//Create a bitmap to draw on
		Bitmap bitmap = Bitmap.createBitmap(TILE_SIZE_PIXELS, TILE_SIZE_PIXELS, Bitmap.Config.RGB_565);

		//You could try something like this to load a custom theme
		//try{
		//	jobTheme = new ExternalRenderTheme(themeFile);
		//}
		//catch(Exception e){
		//	jobTheme = InternalRenderTheme.OSMARENDER;
		//}

		try{
			//Draw the tile
			MapGeneratorJob mapGeneratorJob = new MapGeneratorJob(tile, mapFile, jobParameters, debugSettings);
			renderer.executeJob(mapGeneratorJob, bitmap);
		}
		catch(Exception ex){
			//Make the bad tile easy to spot
			bitmap.eraseColor(Color.YELLOW);
		}

		Drawable d = new BitmapDrawable(bitmap);
		return d;
	}

}
