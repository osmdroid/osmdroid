package org.andnav.osm.views.util;

import org.andnav.osm.ResourceProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.util.AttributeSet;

public class OpenStreetMapRendererFactory {

	private static final Logger logger = LoggerFactory.getLogger(OpenStreetMapRendererFactory.class);

	/**
	 * Get the renderer with the specified name.
	 *
	 * @param aRendererId
	 * @return the renderer
	 * @throws IllegalArgumentException if renderer not found
	 */
	public static IOpenStreetMapRendererInfo getRenderer(String aName) throws IllegalArgumentException {
		for (IOpenStreetMapRendererInfo renderer : mRenderers) {
			// TODO perhaps we should ignore case and white space
			if (renderer.name().equals(aName)) {
				return renderer;
			}
		}
		throw new IllegalArgumentException("No such renderer: " + aName);
	}

	/**
	 * Get the renderer at the specified position.
	 *
	 * @param aOrdinal
	 * @return the renderer
	 * @throws IllegalArgumentException if renderer not found
	 */
	public static IOpenStreetMapRendererInfo getRenderer(int aOrdinal) throws IllegalArgumentException {
		for (IOpenStreetMapRendererInfo renderer : mRenderers) {
			if (renderer.ordinal() == aOrdinal) {
				return renderer;
			}
		}
		throw new IllegalArgumentException("No renderer at position: " + aOrdinal);
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
			if (style == null) {
				logger.info("Using default Cloudmade style: 1");
			} else {
				try {
					final int s = Integer.valueOf(style);
					logger.info("Using Cloudmade style specified in layout attributes: " + s);
					((CloudmadeRenderer)renderer).cloudmadeStyle = s;
				} catch (final NumberFormatException e) {
					logger.warn("Invalid Cloudmade style specified in layout attributes: " + style);
				}
			}
		}

		logger.info("Using renderer : " + DEFAULT_RENDERER);
		return renderer;
	}

	public static IOpenStreetMapRendererInfo[] getRenderers() {
		return mRenderers;
	}

	public static final IOpenStreetMapRendererInfo OSMARENDER =
		new XYRenderer("Osmarender", ResourceProxy.string.osmarender, 0, 17, 8, ".png",
				"http://tah.openstreetmap.org/Tiles/tile/");

	public static final IOpenStreetMapRendererInfo MAPNIK =
		new XYRenderer("Mapnik", ResourceProxy.string.mapnik, 0, 18, 8, ".png",
				"http://tile.openstreetmap.org/");

	public static final IOpenStreetMapRendererInfo CYCLEMAP =
		new XYRenderer("CycleMap", ResourceProxy.string.cyclemap, 0, 17, 8, ".png",
				"http://a.andy.sandbox.cloudmade.com/tiles/cycle/",
				"http://b.andy.sandbox.cloudmade.com/tiles/cycle/",
				"http://c.andy.sandbox.cloudmade.com/tiles/cycle/");

	public static final IOpenStreetMapRendererInfo PUBLIC_TRANSPORT =
		new XYRenderer("OSMPublicTransport", ResourceProxy.string.public_transport, 0, 17, 8, ".png",
				"http://tile.xn--pnvkarte-m4a.de/tilegen/");

	public static final IOpenStreetMapRendererInfo BASE =
		new XYRenderer("Base", ResourceProxy.string.base, 4, 17, 8, ".png",
				"http://topo.openstreetmap.de/base/");

	public static final IOpenStreetMapRendererInfo TOPO =
		new XYRenderer("Topo", ResourceProxy.string.topo, 4, 17, 8, ".png",
				"http://topo.openstreetmap.de/topo/");

	public static final IOpenStreetMapRendererInfo HILLS =
		new XYRenderer("Hills", ResourceProxy.string.hills, 8, 17, 8, ".png",
				"http://topo.geofabrik.de/hills/");

	public static final IOpenStreetMapRendererInfo CLOUDMADESTANDARDTILES =
		new CloudmadeRenderer("CloudMadeStandardTiles", ResourceProxy.string.cloudmade_standard, 0, 18, 8, ".png",
				"http://a.tile.cloudmade.com/%s/%d/%d/%d/%d/%d%s?token=%s",
				"http://b.tile.cloudmade.com/%s/%d/%d/%d/%d/%d%s?token=%s",
				"http://c.tile.cloudmade.com/%s/%d/%d/%d/%d/%d%s?token=%s");

	public static final IOpenStreetMapRendererInfo CLOUDMADESMALLTILES =
		new CloudmadeRenderer("CloudMadeSmallTiles", ResourceProxy.string.cloudmade_small, 0, 21, 6, ".png",
				"http://a.tile.cloudmade.com/%s/%d/%d/%d/%d/%d%s?token=%s",
				"http://b.tile.cloudmade.com/%s/%d/%d/%d/%d/%d%s?token=%s",
				"http://c.tile.cloudmade.com/%s/%d/%d/%d/%d/%d%s?token=%s");

	public static final IOpenStreetMapRendererInfo DEFAULT_RENDERER = MAPNIK;

	// The following renderers are overlays, not standalone map views.
	// They are therefore not in mRenderers.

	public static final IOpenStreetMapRendererInfo FIETS_OVERLAY_NL =
		new XYRenderer("Fiets", ResourceProxy.string.fiets_nl, 3, 16, 8, ".png",
				"http://overlay.openstreetmap.nl/openfietskaart-overlay/");

	public static final IOpenStreetMapRendererInfo BASE_OVERLAY_NL =
		new XYRenderer("BaseNL", ResourceProxy.string.base_nl, 0, 18, 8, ".png",
				"http://overlay.openstreetmap.nl/basemap/");

	public static final IOpenStreetMapRendererInfo ROADS_OVERLAY_NL =
		new XYRenderer("RoadsNL", ResourceProxy.string.roads_nl, 0, 18, 8, ".png",
				"http://overlay.openstreetmap.nl/roads/");

	// FIXME the whole point of this implementation is that the list of renderers should be extensible,
	//       so that means making it possible to have a bigger or smaller list of renderers
	//   - there's a number of ways of doing that
	private static IOpenStreetMapRendererInfo[] mRenderers = new IOpenStreetMapRendererInfo[] {
		OSMARENDER,
		MAPNIK,
		CYCLEMAP,
		PUBLIC_TRANSPORT,
		BASE,
		TOPO,
		HILLS,
		CLOUDMADESTANDARDTILES,
		CLOUDMADESMALLTILES
	};
}
