package org.osmdroid.data;

import android.content.Context;
import android.support.annotation.RawRes;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * {@link DataCountry} json loader
 *
 * @author Fabrice Fontaine
 * @since 6.0.3
 */
public class DataCountryLoader extends DataLoader<DataCountry> {

    public DataCountryLoader(final Context pContext, final @RawRes int pResId)
            throws Exception {
        super(pContext, pResId);
    }

    @Override
    protected DataCountry getItem(final String pKey, final JSONObject pJsonObject) throws JSONException {
        final String name = pJsonObject.getString("name");
        final JSONObject capital = pJsonObject.getJSONObject("capital");
        final String capitalName = capital.getString("name");
        final double latitude = capital.getDouble("latitude");
        final double longitude = capital.getDouble("longitude");
        return new DataCountry(pKey, name, capitalName, latitude, longitude);
    }
}
