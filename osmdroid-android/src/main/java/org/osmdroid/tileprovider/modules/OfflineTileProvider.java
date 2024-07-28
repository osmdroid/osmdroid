package org.osmdroid.tileprovider.modules;

import android.content.Context;
import android.util.Log;

import org.osmdroid.api.IMapView;
import org.osmdroid.tileprovider.IMapTileProviderCallback;
import org.osmdroid.tileprovider.IRegisterReceiver;
import org.osmdroid.tileprovider.MapTileProviderArray;
import org.osmdroid.tileprovider.MapTileProviderBasic;
import org.osmdroid.tileprovider.tilesource.FileBasedTileSource;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Causes Osmdroid to load from tiles from only the referenced file sources and
 * no where else. online sources are not even attempted.
 *
 * @since 5.0 Created by alex on 6/14/2015.
 */
public class OfflineTileProvider extends MapTileProviderArray implements IMapTileProviderCallback {

    private IArchiveFile[] archives;

    /**
     * Creates a {@link MapTileProviderBasic}.
     * throws with the source[] is null or empty
     */
    public OfflineTileProvider(@NonNull final Context context, final IRegisterReceiver pRegisterReceiver, File[] source) {
        super(context, FileBasedTileSource.getSource(source[0].getName()), pRegisterReceiver);
        List<IArchiveFile> files = new ArrayList<>();

        for (final File file : source) {
            final IArchiveFile temp = ArchiveFileFactory.getArchiveFile(file);
            if (temp != null)
                files.add(temp);
            else {
                Log.w(IMapView.LOGTAG, "Skipping " + file + ", no tile provider is registered to handle the file extension");
            }
        }
        archives = new IArchiveFile[files.size()];
        archives = files.toArray(archives);
        final MapTileFileArchiveProvider mapTileFileArchiveProvider = new MapTileFileArchiveProvider(context, pRegisterReceiver, getTileSource(), archives);
        mTileProviderList.add(mapTileFileArchiveProvider);

        final MapTileApproximater approximationProvider = new MapTileApproximater();
        mTileProviderList.add(approximationProvider);
        approximationProvider.addProvider(mapTileFileArchiveProvider);

    }

    public IArchiveFile[] getArchives() {
        return archives;
    }

    @Override
    public void onDetach(@Nullable final Context context) {
        if (archives != null) {
            for (final IArchiveFile file : archives) {
                file.close();
            }
        }
        super.onDetach(context);
    }

    @Override
    protected boolean isDowngradedMode(final long pMapTileIndex) {
        return true;
    }
}
