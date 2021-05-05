package org.osmdroid.tileprovider.modules;

import android.util.Log;

import org.osmdroid.api.IMapView;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.util.GEMFFile;
import org.osmdroid.util.MapTileIndex;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

public class GEMFFileArchive implements IArchiveFile {

    private GEMFFile mFile;

    public GEMFFileArchive() {
    }

    private GEMFFileArchive(final File pFile) throws IOException {
        mFile = new GEMFFile(pFile);
    }

    public static GEMFFileArchive getGEMFFileArchive(final File pFile) throws IOException {
        return new GEMFFileArchive(pFile);
    }

    @Override
    public void init(File pFile) throws Exception {
        mFile = new GEMFFile(pFile);
    }

    @Override
    public InputStream getInputStream(final ITileSource pTileSource, final long pMapTileIndex) {
        return mFile.getInputStream(MapTileIndex.getX(pMapTileIndex), MapTileIndex.getY(pMapTileIndex), MapTileIndex.getZoom(pMapTileIndex));
    }


    public Set<String> getTileSources() {
        Set<String> ret = new HashSet<String>();
        try {
            ret.addAll(mFile.getSources().values());
        } catch (final Exception e) {
            Log.w(IMapView.LOGTAG, "Error getting tile sources: ", e);
        }
        return ret;
    }

    @Override
    public void setIgnoreTileSource(boolean pIgnoreTileSource) {

    }

    @Override
    public void close() {
        try {
            mFile.close();
        } catch (IOException e) {
        }
    }

    @Override
    public String toString() {
        return "GEMFFileArchive [mGEMFFile=" + mFile.getName() + "]";
    }

}
