package org.andnav.osm.views.util;

import java.util.Collection;
import java.util.HashMap;

import org.andnav.osm.ResourceProxy;
import org.andnav.osm.tileprovider.CloudmadeException;
import org.andnav.osm.tileprovider.IOpenStreetMapTileProviderCallback;
import org.andnav.osm.tileprovider.IOpenStreetMapTileProviderCloudmadeTokenCallback;
import org.andnav.osm.tileprovider.OpenStreetMapTile;
import org.andnav.osm.views.util.OpenStreetMapRendererFactory.CodeScheme;

public class OpenStreetMapRendererFactory {

	// TODO remove legacy code
	public static final IOpenStreetMapRendererInfo OSMARENDER = new LegacyRenderer(OpenStreetMapRendererInfo.OSMARENDER);
	public static final IOpenStreetMapRendererInfo MAPNIK = new LegacyRenderer(OpenStreetMapRendererInfo.MAPNIK);
	public static final IOpenStreetMapRendererInfo CYCLEMAP = new LegacyRenderer(OpenStreetMapRendererInfo.CYCLEMAP);
	public static final IOpenStreetMapRendererInfo OPENARIELMAP = new LegacyRenderer(OpenStreetMapRendererInfo.OPENARIELMAP);
	public static final IOpenStreetMapRendererInfo BASE = new LegacyRenderer(OpenStreetMapRendererInfo.BASE);
	public static final IOpenStreetMapRendererInfo TOPO = new LegacyRenderer(OpenStreetMapRendererInfo.TOPO);
	public static final IOpenStreetMapRendererInfo HILLS = new LegacyRenderer(OpenStreetMapRendererInfo.HILLS);
	public static final IOpenStreetMapRendererInfo CLOUDMADESMALLTILES = new LegacyRenderer(OpenStreetMapRendererInfo.CLOUDMADESMALLTILES);
	public static final IOpenStreetMapRendererInfo CLOUDMADESTANDARDTILES = new LegacyRenderer(OpenStreetMapRendererInfo.CLOUDMADESTANDARDTILES);

	private static HashMap<String, IOpenStreetMapRendererInfo> mRenderers = new HashMap<String, IOpenStreetMapRendererInfo>();
	private static IOpenStreetMapRendererInfo mDefaultRenderer = MAPNIK;

	/**
	 * Get the renderer with the specified name.
	 *
	 * @param aRendererId
	 * @return the renderer, or null if not found
	 */
	// TODO maybe I can delete this method
	public static IOpenStreetMapRendererInfo getRenderer(String aName) {
		return getRenderer(aName, false);
	}

	/**
	 * Get the renderer with the specified name.
	 *
	 * @param aRendererId
	 * @param aDefaultIfNotFound
	 * @return the renderer. If not found and aDefaultIfNotFound is true then return the default renderer,
	 *  otherwise return null.
	 */
	public static IOpenStreetMapRendererInfo getRenderer(final String aName, final boolean aDefaultIfNotFound) {
		final IOpenStreetMapRendererInfo renderer = mRenderers.get(aName);
		if (renderer == null && aDefaultIfNotFound) {
			return mDefaultRenderer;
		} else {
			return renderer;
		}
	}

	/**
	 * Get the renderer at the specified position.
	 *
	 * @param aOrdinal
	 * @return the renderer, or null if not found
	 */
	public static IOpenStreetMapRendererInfo getRenderer(int aOrdinal) {
		for (IOpenStreetMapRendererInfo renderer : mRenderers.values()) {
			if (renderer.ordinal() == aOrdinal) {
				return renderer;
			}
		}
		return null;
	}

	public static IOpenStreetMapRendererInfo getDefaultRenderer() {
		return mDefaultRenderer;
	}

	public static Collection<IOpenStreetMapRendererInfo> getRenderers() {
		return mRenderers.values();
	}

	// TODO the whole point of this implementation is that the list of renderers should be extensible,
	//      so that means making it possible to have a bigger or smaller list of renderers

	// static initialisation
	static {
		mRenderers.put(OSMARENDER.name(), OSMARENDER);
		mRenderers.put(MAPNIK.name(), MAPNIK);
		mRenderers.put(CYCLEMAP.name(), CYCLEMAP);
		mRenderers.put(OPENARIELMAP.name(), OPENARIELMAP);
		mRenderers.put(BASE.name(), BASE);
		mRenderers.put(TOPO.name(), TOPO);
		mRenderers.put(HILLS.name(), HILLS);
		mRenderers.put(CLOUDMADESMALLTILES.name(), CLOUDMADESMALLTILES);
		mRenderers.put(CLOUDMADESTANDARDTILES.name(), CLOUDMADESTANDARDTILES);
	}

	// TODO remove legacy code
	public enum CodeScheme { X_Y, CLOUDMADE, QUAD_TREE };

}

// TODO remove legacy code
class LegacyRenderer implements IOpenStreetMapRendererInfo {

	private static int globalOrdinal = 0;

	private final OpenStreetMapRendererInfo mInfo;
	private final int mOrdinal;

	public LegacyRenderer(OpenStreetMapRendererInfo aInfo) {
		mInfo = aInfo;
		mOrdinal = globalOrdinal++;
	}

	@Override
	public String name() {
		return mInfo.name();
	}

	@Override
	public ResourceProxy.string resourceId() {
		return mInfo.NAME;
	}

	@Override
	public int ordinal() {
		return mOrdinal;
	}

	@Override
	public CodeScheme codeScheme() {
		return mInfo.CODE_SCHEME;
	}

	@Override
	public int maptileSizePx() {
		return mInfo.MAPTILE_SIZEPX;
	}

	@Override
	public int maptileZoom() {
		return mInfo.MAPTILE_ZOOM;
	}

	@Override
	public int zoomMinLevel() {
		return mInfo.ZOOM_MINLEVEL;
	}

	@Override
	public int zoomMaxLevel() {
		return mInfo.ZOOM_MAXLEVEL;
	}

	@Override
	public String imageFilenameEnding() {
		return mInfo.IMAGE_FILENAMEENDING;
	}


	@Override
	public void setCloudmadeStyle(int aStyle) {
		mInfo.setCloudmadeStyle(aStyle);
	}

	@Override
	public String getTileURLString(OpenStreetMapTile aTile, IOpenStreetMapTileProviderCallback aCallback, IOpenStreetMapTileProviderCloudmadeTokenCallback aCloudmadeTokenCallback) throws CloudmadeException {
		return mInfo.getTileURLString(aTile, aCallback, aCloudmadeTokenCallback);
	}
}
