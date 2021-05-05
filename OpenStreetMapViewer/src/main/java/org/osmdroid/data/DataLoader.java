package org.osmdroid.data;

import android.content.Context;
import android.support.annotation.RawRes;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Iterator;
import java.util.LinkedHashMap;

/**
 * {@link DataRegion} json loader
 *
 * @author Fabrice Fontaine
 * @since 6.0.2
 */
abstract public class DataLoader<T> {

    private final LinkedHashMap<String, T> mList = new LinkedHashMap<>();

    public DataLoader(final Context pContext, final @RawRes int pResId)
            throws Exception {
        load(getJsonString(pContext, pResId));
    }

    abstract protected T getItem(final String pKey, final JSONObject pJsonObject) throws JSONException;

    public LinkedHashMap<String, T> getList() {
        return mList;
    }

    private void load(final String pJson) throws Exception {
        final JSONObject root = new JSONObject(pJson);
        final Iterator<String> keys = root.keys();
        while (keys.hasNext()) {
            final String key = keys.next();
            final JSONObject region = root.getJSONObject(key);
            mList.put(key, getItem(key, region));
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
