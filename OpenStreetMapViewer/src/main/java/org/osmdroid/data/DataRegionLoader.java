package org.osmdroid.data;

import android.content.Context;
import android.support.annotation.RawRes;

import org.json.JSONObject;
import org.osmdroid.util.BoundingBox;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * {@link DataRegion} json loader
 * @since 6.0.2
 * @author Fabrice Fontaine
 */
public class DataRegionLoader {

    private final List<DataRegion> mList = new ArrayList<>();

    public DataRegionLoader(final Context pContext, final @RawRes int pResId)
            throws Exception{
        load(getJsonString(pContext, pResId));
    }

    public List<DataRegion> getList() {
        return mList;
    }

    private void load(final String pJson)
            throws Exception {
        final JSONObject root = new JSONObject(pJson);
        final Iterator<String> keys = root.keys();
        while(keys.hasNext()) {
            final String key = keys.next();
            final JSONObject region = root.getJSONObject(key);
            final String name = region.getString("name");
            final double north = region.getDouble("N");
            final double east = region.getDouble("E");
            final double south = region.getDouble("S");
            final double west = region.getDouble("W");
            final DataRegion item =
                    new DataRegion(key, name, new BoundingBox(north, east, south, west));
            mList.add(item);
        }
    }

    private String getJsonString(final Context pContext, final @RawRes int pResource)
            throws Exception {
        final InputStream is = pContext.getResources().openRawResource(pResource);
        final BufferedInputStream bis = new BufferedInputStream(is);
        final int bufferSize = 1024 * 64;
        final char[] buffer = new char[bufferSize];
        final StringBuilder out = new StringBuilder();
        final Reader in = new InputStreamReader(bis, "UTF-8");
        int read;
        while ((read = in.read(buffer, 0, buffer.length)) > 0) {
            out.append(buffer, 0, read);
        }
        is.close();
        return out.toString();
    }
}
