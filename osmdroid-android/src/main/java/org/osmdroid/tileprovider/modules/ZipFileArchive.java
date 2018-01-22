package org.osmdroid.tileprovider.modules;

import android.util.Log;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import org.osmdroid.api.IMapView;

import org.osmdroid.tileprovider.MapTile;
import org.osmdroid.tileprovider.tilesource.ITileSource;

public class ZipFileArchive implements IArchiveFile {

	protected ZipFile mZipFile;	
	private boolean mIgnoreTileSource = false;

	public ZipFileArchive(){}

	private ZipFileArchive(final ZipFile pZipFile) {
		mZipFile = pZipFile;
	}

	public static ZipFileArchive getZipFileArchive(final File pFile) throws ZipException, IOException {
		return new ZipFileArchive(new ZipFile(pFile));
	}
	
	/**
	 * @since 6.0
	 * If set to true, tiles from this archive will be loaded regardless of their associated tile source name
	 */
	public void setIgnoreTileSource(boolean pIgnoreTileSource) {
		mIgnoreTileSource = pIgnoreTileSource;
	}

	@Override
	public void init(File pFile) throws Exception {
		mZipFile=new ZipFile(pFile);
	}

	@Override
	public InputStream getInputStream(final ITileSource pTileSource, final MapTile pTile) {
		try {
			if(!mIgnoreTileSource) {
				final String path = pTileSource.getTileRelativeFilenameString(pTile);
				final ZipEntry entry = mZipFile.getEntry(path);
				if (entry != null) {
					return mZipFile.getInputStream(entry);
				}
			} else {
				// Search all sources in ZIP internal order
				Enumeration<? extends ZipEntry> entries = mZipFile.entries();
				while (entries.hasMoreElements()) {
					ZipEntry nextElement = entries.nextElement();
					String str=nextElement.getName();
					if (str.contains("/")) {
						String path = getTileRelativeFilenameString(pTile, str.split("/")[0]);
						ZipEntry entry = mZipFile.getEntry(path);
						if (entry != null) {
							return mZipFile.getInputStream(entry);
						}
					}
				}
			}
		} catch (final IOException e) {
			Log.w(IMapView.LOGTAG,"Error getting zip stream: " + pTile, e);
		}
		return null;
	}
	
	/**
	 * @since 6.0
	 * Creating paths for ZIP scanning
	 */
    	private String getTileRelativeFilenameString(final MapTile tile, final String pathBase) {
		final StringBuilder sb = new StringBuilder();
		sb.append(pathBase);
		sb.append('/');
		sb.append(tile.getZoomLevel());
		sb.append('/');
		sb.append(tile.getX());
		sb.append('/');
		sb.append(tile.getY());
		sb.append(".png");
		return sb.toString();
    	}

	public Set<String> getTileSources(){
		Set<String> ret = new HashSet<String>();
		try {
			Enumeration<? extends ZipEntry> entries = mZipFile.entries();
			while (entries.hasMoreElements()) {
				ZipEntry nextElement = entries.nextElement();
				String str=nextElement.getName();
				if (str.contains("/"))
					ret.add(str.split("/")[0]);
			}
		} catch (final Exception e) {
			Log.w(IMapView.LOGTAG,"Error getting tile sources: ", e);
		}
		return ret;
	}

	@Override
	public void close() {
		try {
			mZipFile.close();
		} catch (IOException e) { }
	}

	@Override
	public String toString() {
		return "ZipFileArchive [mZipFile=" + mZipFile.getName() + "]";
	}

}
