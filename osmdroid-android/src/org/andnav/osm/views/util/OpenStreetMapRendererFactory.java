package org.andnav.osm.views.util;

import java.util.Collection;
import java.util.HashMap;

import org.andnav.osm.ResourceProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.util.AttributeSet;

public class OpenStreetMapRendererFactory {

	private static final Logger logger = LoggerFactory.getLogger(OpenStreetMapRendererFactory.class);

	private static HashMap<String, IOpenStreetMapRendererInfo> mRenderers = new HashMap<String, IOpenStreetMapRendererInfo>();

	/**
	 * Get the renderer with the specified name.
	 *
	 * @param aRendererId
	 * @return the renderer
	 * @throws IllegalArgumentException if renderer not found
	 */
	public static IOpenStreetMapRendererInfo getRenderer(String aName) throws IllegalArgumentException {
		final IOpenStreetMapRendererInfo renderer = mRenderers.get(aName);
		if (renderer == null) {
			throw new IllegalArgumentException("No such renderer: " + aName);
		}
		return renderer;
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
			return DEFAULT_RENDERER;
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

	/**
	 * Get the renderer based on the attributes.
	 * TODO document the attribute parameters - renderer and cloudmadeStyle
	 * @param aRenderer the renderer to use, or null if it's specified in attributes
	 * @param aAttributeSet
	 * @return the renderer, or default renderer if not found
	 */
	public static IOpenStreetMapRendererInfo getRenderer(
			final IOpenStreetMapRendererInfo aRenderer,
			final AttributeSet aAttributeSet) {

		IOpenStreetMapRendererInfo renderer = DEFAULT_RENDERER;

		if (aRenderer != null) {
			logger.info("Using renderer specified in parameter: " + aRenderer);
			renderer = aRenderer;
		} else {
			if (aAttributeSet != null) {
				final String rendererAttr = aAttributeSet.getAttributeValue(null, "renderer");
				if (rendererAttr != null) {
					try {
						final IOpenStreetMapRendererInfo r = OpenStreetMapRendererFactory.getRenderer(rendererAttr);
						logger.info("Using renderer specified in layout attributes: " + r);
						renderer = r;
					} catch (final IllegalArgumentException e) {
						logger.warn("Invalid renderer specified in layout attributes: " + renderer);
					}
				}
			}
		}

		if (aAttributeSet != null && renderer instanceof CloudmadeRenderer) {
			final String style = aAttributeSet.getAttributeValue(null, "cloudmadeStyle");
			if (style != null) {
				try {
					final int s = Integer.valueOf(style);
					logger.info("Using Cloudmade style specified in layout attributes: " + s);
					((CloudmadeRenderer)renderer).cloudmadeStyle = s;
				} catch (final NumberFormatException e) {
					logger.warn("Invalid Cloudmade style specified in layout attributes: " + style);
				}
			}
			logger.info("Using default Cloudmade style : 1");
		}

		logger.info("Using renderer : " + DEFAULT_RENDERER);
		return renderer;
	}

	/**
	 * Get all renders in no particular order.
	 * TODO perhaps it should be sorted by the order they were added (ordinal).
	 * Or maybe we should leave that to the GUI because it may want them sorted
	 * in a different order, eg alphabetical according to the locale.
	 */
	public static Collection<IOpenStreetMapRendererInfo> getRenderers() {
		return mRenderers.values();
	}

	public static final IOpenStreetMapRendererInfo OSMARENDER =
		new XYRenderer("OSMARENDER", ResourceProxy.string.osmarender, 0, 17, 8, ".png",
				"http://tah.openstreetmap.org/Tiles/tile/");

	public static final IOpenStreetMapRendererInfo MAPNIK =
		new XYRenderer("MAPNIK", ResourceProxy.string.mapnik, 0, 18, 8, ".png",
				"http://tile.openstreetmap.org/");

	public static final IOpenStreetMapRendererInfo CYCLEMAP =
		new XYRenderer("CYCLEMAP", ResourceProxy.string.cyclemap, 0, 17, 8, ".png",
				"http://a.andy.sandbox.cloudmade.com/tiles/cycle/",
				"http://b.andy.sandbox.cloudmade.com/tiles/cycle/",
				"http://c.andy.sandbox.cloudmade.com/tiles/cycle/");

	public static final IOpenStreetMapRendererInfo OPENARIELMAP =
		new XYRenderer("OPENARIELMAP", ResourceProxy.string.openareal_sat, 0, 13, 8, ".jpg",
				"http://tile.openaerialmap.org/tiles/1.0.0/openaerialmap-900913/");

	public static final IOpenStreetMapRendererInfo BASE =
		new XYRenderer("BASE", ResourceProxy.string.base, 4, 17, 8, ".png",
				"http://topo.openstreetmap.de/base/");

	public static final IOpenStreetMapRendererInfo TOPO =
		new XYRenderer("TOPO", ResourceProxy.string.topo, 4, 17, 8, ".png",
				"http://topo.openstreetmap.de/topo/");

	public static final IOpenStreetMapRendererInfo HILLS =
		new XYRenderer("HILLS", ResourceProxy.string.hills, 8, 17, 8, ".png",
				"http://topo.geofabrik.de/hills/");

	public static final IOpenStreetMapRendererInfo CLOUDMADESTANDARDTILES =
		new CloudmadeRenderer("CLOUDMADESTANDARDTILES", ResourceProxy.string.cloudmade_standard, 0, 18, 8, ".png",
				"http://a.tile.cloudmade.com/%s/%d/256/%d/%d/%d%s?token=%s",
				"http://b.tile.cloudmade.com/%s/%d/256/%d/%d/%d%s?token=%s",
				"http://c.tile.cloudmade.com/%s/%d/256/%d/%d/%d%s?token=%s");

	public static final IOpenStreetMapRendererInfo CLOUDMADESMALLTILES =
		new CloudmadeRenderer("CLOUDMADESMALLTILES", ResourceProxy.string.cloudmade_small, 0, 13, 6, ".png",
				"http://a.tile.cloudmade.com/%s/%d/64/%d/%d/%d%s?token=%s",
				"http://b.tile.cloudmade.com/%s/%d/64/%d/%d/%d%s?token=%s",
				"http://c.tile.cloudmade.com/%s/%d/64/%d/%d/%d%s?token=%s");

	public static final IOpenStreetMapRendererInfo DEFAULT_RENDERER = MAPNIK;

	// FIXME the whole point of this implementation is that the list of renderers should be extensible,
	//       so that means making it possible to have a bigger or smaller list of renderers
	//   - there's a number of ways of doing that

	// static initialisation
	static {
		mRenderers.put(OSMARENDER.name(), OSMARENDER);
		mRenderers.put(MAPNIK.name(), MAPNIK);
		mRenderers.put(CYCLEMAP.name(), CYCLEMAP);
		mRenderers.put(OPENARIELMAP.name(), OPENARIELMAP);
		mRenderers.put(BASE.name(), BASE);
		mRenderers.put(TOPO.name(), TOPO);
		mRenderers.put(HILLS.name(), HILLS);
		mRenderers.put(CLOUDMADESTANDARDTILES.name(), CLOUDMADESTANDARDTILES);
		mRenderers.put(CLOUDMADESMALLTILES.name(), CLOUDMADESMALLTILES);
	}
}
