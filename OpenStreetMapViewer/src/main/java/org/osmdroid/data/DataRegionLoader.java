package org.osmdroid.data;

import android.content.Context;
import androidx.annotation.RawRes;

import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.util.BoundingBox;

/**
 * {@link DataRegion} json loader
 * @since 6.0.2
 * @author Fabrice Fontaine
 */
public class DataRegionLoader extends DataLoader<DataRegion>{

    public DataRegionLoader(final Context pContext, final @RawRes int pResId)
            throws Exception{
        super(pContext, pResId);
    }

    @Override
    protected DataRegion getItem(final String pKey, final JSONObject pJsonObject) throws JSONException{
        final String name = pJsonObject.getString("name");
        final double north = pJsonObject.getDouble("N");
        final double east = pJsonObject.getDouble("E");
        final double south = pJsonObject.getDouble("S");
        final double west = pJsonObject.getDouble("W");
        return new DataRegion(pKey, name, new BoundingBox(north, east, south, west));
    }
}
