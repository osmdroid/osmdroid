package org.osmdroid.tileprovider.modules;

import java.io.File;
import org.osmdroid.tileprovider.IMapTileProviderCallback;
import org.osmdroid.tileprovider.IRegisterReceiver;
import org.osmdroid.tileprovider.MapTileProviderArray;
import org.osmdroid.tileprovider.MapTileProviderBasic;
import org.osmdroid.tileprovider.tilesource.FileBasedTileSource;

/**
 * Causes Osmdroid to load from tiles from only the referenced file source and
 * no where else. online sources are not even attempted.
 *
 * @since 4.4 Created by alex on 6/14/2015.
 */
public class OfflineTileProvider extends MapTileProviderArray implements IMapTileProviderCallback {

	/**
	 * Creates a {@link MapTileProviderBasic}.
	 * throws with the source[] is null or empty
	 */
	public OfflineTileProvider(final IRegisterReceiver pRegisterReceiver, File[] source
	)
		throws Exception {
		super(FileBasedTileSource.getSource(source[0].getName()), pRegisterReceiver);
		IArchiveFile[] f = new IArchiveFile[source.length];
		for (int i=0; i < source.length; i++)
			f[i]=ArchiveFileFactory.getArchiveFile(source[i]);
		
		mTileProviderList.add(new MapTileFileArchiveProvider(pRegisterReceiver, getTileSource(), f));

	}
}
