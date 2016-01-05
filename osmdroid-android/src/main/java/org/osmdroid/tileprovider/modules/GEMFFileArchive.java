package org.osmdroid.tileprovider.modules;

import android.database.Cursor;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import org.osmdroid.api.IMapView;
import org.osmdroid.tileprovider.MapTile;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.util.GEMFFile;

public class GEMFFileArchive implements IArchiveFile {

	private GEMFFile mFile;

	public GEMFFileArchive(){}

	private GEMFFileArchive(final File pFile) throws FileNotFoundException, IOException {
		mFile = new GEMFFile(pFile);
	}

	public static GEMFFileArchive getGEMFFileArchive(final File pFile) throws FileNotFoundException, IOException {
		return new GEMFFileArchive(pFile);
	}

	@Override
	public void init(File pFile) throws Exception {
		mFile = new GEMFFile(pFile);
	}

	@Override
	public InputStream getInputStream(final ITileSource pTileSource, final MapTile pTile) {
		return mFile.getInputStream(pTile.getX(), pTile.getY(), pTile.getZoomLevel());
	}


	public Set<String> getTileSources(){
		Set<String> ret = new HashSet<String>();
		try {
			ret.addAll(mFile.getSources().values());
		} catch (final Exception e) {
			Log.w(IMapView.LOGTAG, "Error getting tile sources: ", e);
		}
		return ret;
	}

	@Override
	public void close() {
		try {
			mFile.close();
		} catch (IOException e) { }
	}

	@Override
	public String toString() {
		return "GEMFFileArchive [mGEMFFile=" + mFile.getName() + "]";
	}

}
