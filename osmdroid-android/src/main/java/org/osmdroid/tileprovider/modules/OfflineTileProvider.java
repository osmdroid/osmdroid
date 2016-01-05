package org.osmdroid.tileprovider.modules;

import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.osmdroid.api.IMapView;
import org.osmdroid.tileprovider.IMapTileProviderCallback;
import org.osmdroid.tileprovider.IRegisterReceiver;
import org.osmdroid.tileprovider.MapTileProviderArray;
import org.osmdroid.tileprovider.MapTileProviderBasic;
import org.osmdroid.tileprovider.tilesource.FileBasedTileSource;

/**
 * Causes Osmdroid to load from tiles from only the referenced file sources and
 * no where else. online sources are not even attempted.
 *
 * @since 5.0 Created by alex on 6/14/2015.
 */
public class OfflineTileProvider extends MapTileProviderArray implements IMapTileProviderCallback {

	IArchiveFile[] archives;
	/**
	 * Creates a {@link MapTileProviderBasic}.
	 * throws with the source[] is null or empty
	 */
	public OfflineTileProvider(final IRegisterReceiver pRegisterReceiver, File[] source
	)
		throws Exception {
		super(FileBasedTileSource.getSource(source[0].getName()), pRegisterReceiver);
		List<IArchiveFile> files = new ArrayList<IArchiveFile>();

		for (int i=0; i < source.length; i++){
			IArchiveFile temp=ArchiveFileFactory.getArchiveFile(source[i]);
			if (temp!=null)
				files.add(temp);
			else{
				Log.w(IMapView.LOGTAG, "Skipping " + source[i] + ", no tile provider is registered to handle the file extension");
			}
		}
		archives = new IArchiveFile[files.size()];
		archives=files.toArray(archives);
		
		mTileProviderList.add(new MapTileFileArchiveProvider(pRegisterReceiver, getTileSource(), archives));

	}
	public IArchiveFile[] getArchives(){
		return archives;
	}
}
