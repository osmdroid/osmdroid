package org.osmdroid.tileprovider;

import org.osmdroid.tileprovider.constants.OpenStreetMapTileProviderConstants;
import org.osmdroid.tileprovider.modules.MapTileModuleProviderBase;
import org.osmdroid.tileprovider.util.StreamUtils;
import org.osmdroid.views.overlay.TilesOverlay;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * A map tile is distributed using the observer pattern. The tile is delivered by a tile provider
 * (i.e. a descendant of {@link MapTileModuleProviderBase} or
 * {@link MapTileProviderBase} to a consumer of tiles (e.g. descendant of
 * {@link TilesOverlay}). Tiles are typically images (e.g. png or jpeg).
 */
public class MapTile {

	public static final int MAPTILE_SUCCESS_ID = 0;
	public static final int MAPTILE_FAIL_ID = MAPTILE_SUCCESS_ID + 1;

	// This class must be immutable because it's used as the key in the cache hash map
	// (ie all the fields are final).
	private final int x;
	private final int y;
	private final int zoomLevel;
	private Date expires;

	public MapTile(final int zoomLevel, final int tileX, final int tileY) {
		this.zoomLevel = zoomLevel;
		this.x = tileX;
		this.y = tileY;
	}

	public int getZoomLevel() {
		return zoomLevel;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	@Override
	public String toString() {
		return "/" + zoomLevel + "/" + x + "/" + y;
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == null)
			return false;
		if (obj == this)
			return true;
		if (!(obj instanceof MapTile))
			return false;
		final MapTile rhs = (MapTile) obj;
		return zoomLevel == rhs.zoomLevel && x == rhs.x && y == rhs.y;
	}

	@Override
	public int hashCode() {
		int code = 17;
		code *= 37 + zoomLevel;
		code *= 37 + x;
		code *= 37 + y;
		return code;
	}

	public Date getExpires() {
		return expires;
	}

	public void setExpires(Date expires) {
		this.expires = expires;
	}

	public void writeProperties(OutputStream outputStream) throws IOException {

		SimpleDateFormat dateFormat =
			new SimpleDateFormat(OpenStreetMapTileProviderConstants.HTTP_EXPIRES_HEADER_FORMAT,
				Locale.US);

		String formattedDateExpires = dateFormat.format(expires);

		//Add each property on a new line as properties are split with "\n"
		String properties = String.format(
				"%s:%s", //Expires
			OpenStreetMapTileProviderConstants.PROPERTY_EXPIRES, formattedDateExpires,
			OpenStreetMapTileProviderConstants.PROPERTY_EXPIRES, formattedDateExpires);

		outputStream.write(properties.getBytes("UTF-8"));
	}

	public void readProperties(InputStream inputStream) throws IOException {
		StringBuilder propertiesBuilder = new StringBuilder();

		char[] buffer = new char[StreamUtils.IO_BUFFER_SIZE];
		Reader reader = new InputStreamReader(inputStream);

		int read = -1;
		while((read = reader.read(buffer)) != -1) {
			propertiesBuilder.append(buffer, 0, read);
		}

		final String[] properties = propertiesBuilder.toString().split("\n");
		for(String property : properties) {
			final String[] keyValuePair = property.split(":", 2);
			if(keyValuePair.length == 2) {
				final String key = keyValuePair[0];
				final String value = keyValuePair[1];

				switch (key) {
					case OpenStreetMapTileProviderConstants.PROPERTY_EXPIRES:
						try {
							final SimpleDateFormat dateFormat =
								new SimpleDateFormat(OpenStreetMapTileProviderConstants.HTTP_EXPIRES_HEADER_FORMAT,
									Locale.US);

							setExpires(dateFormat.parse(value));
						} catch (ParseException e) { //Default if not available/parseable
							setExpires(new Date(OpenStreetMapTileProviderConstants.TILE_EXPIRY_TIME_MILLISECONDS));
						}
						break;
				}
			}
		}
	}
}
