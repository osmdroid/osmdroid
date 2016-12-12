# Map Sources

This document is all about Map Sources, Tile loading, caching, offline setups and more

# Map Projections

What map projection does osmdroid use and or support? There are tons of map projections out there. The important part to know is that osmdroid is based on Open Street Maps, which currently uses the EPSG:3857 projection. 

It is possible to support different projections with osmdroid, but you'll either need to preprocess the imagery (convert it) or use a WMS based product like http://geoserver.org/ in conjunction with MOBAC to prepare offline tile packages. It may also be possible to add WMS support to osmdroid.


Related source material from Open Street Maps
http://wiki.openstreetmap.org/wiki/Perl/Projected_version_of_an_OSM_extract

# What map tile numbering system does osmdroid support?

 * ZXY - osmdroid is based on Open Street Maps, which uses something called the "Slippy Map Format". It's the same format used by many of the common map providers. Most online tile sources use the Zoom/X/Y URL format where X and Y are integer coordinates based on dividing the entire map into quadrants. This is the natively supported format for osmdroid.

 * ZYX - Some map tile servers, such as some ESRI based products, use the Zoom/Y/X URL format. Since it's trivial to convert to ZXY, support for this format is easy to setup on osmdroid. We currently have at least one map source that uses this mechanism (see USGS maps).

 * [TMS](https://wiki.osgeo.org/wiki/Tile_Map_Service_Specification) - Tile Map Service Specification is used by a number map services online. Conversion between ZXY and TMS is also pretty straight forward. In this case, X and Y are the same and the zoom levels are inverted.

 * Everything else - many other map services serve map imagery using geographic lat/lon decimal bounds instead of a numbering system. In these cases, it's fairly easy convince these services to provide imagery that fit the OSM style of map tiles by converting what osmdroid wants  in ZXY coordinates to their bounding boxes.

Sources
http://wiki.openstreetmap.org/wiki/Slippy_map_tilenames

# Tile providers vs Tile Source

osmdroid uses two components to display map imagery, the Tile Provider and the Tile Source. The Tile Provider is used to determine how to load tiles (online, offline, assets folders, etc). The Tile Source determines what imagery set is displayed, such as Bing, Mapquest, Mapnik, etc. The default Tile Provider, searches the following for your Tile Source, Assets, Offline zip/sqlite/etc in (/sdcard/osmdroid), Downloaded tile cache (/sdcard/osmdroid/tiles) and then finally the downloader. There are other alternate providers included with osmdroid that change the way tiles are loaded offline. osmdroid BonusPack has a number of alternative provides that use other libraries like MapForge to generate tiles on the fly using OSM data while offline.

# Creating a custom tile provider chain

One of the features of osmdroid is the customizable tile provider chain. This provides the ability to mix and match various tile provider modules to create a specific tile retrieval strategy. The `MapTileProviderBasic` tile provider provides a default set of tile provider modules that includes a file cache, an archive provider, and a download provider. For most users this covers the basics, but you can build you own custom tile provider chain.

```java
final Context context = getActivity();
final Context applicationContext = context.getApplicationContext();
final IRegisterReceiver registerReceiver = new SimpleRegisterReceiver(applicationContext);

// Create a custom tile source
final ITileSource tileSource = new XYTileSource(
    "Mapnik", ResourceProxy.string.mapnik, 1, 18, 256, ".png", "http://tile.openstreetmap.org/");

// Create a file cache modular provider
final TileWriter tileWriter = new TileWriter();
final MapTileFilesystemProvider fileSystemProvider = new MapTileFilesystemProvider(
    registerReceiver, tileSource);

// Create an archive file modular tile provider
GEMFFileArchive gemfFileArchive = GEMFFileArchive.getGEMFFileArchive(mGemfArchiveFilename); // Requires try/catch
MapTileFileArchiveProvider fileArchiveProvider = new MapTileFileArchiveProvider(
    registerReceiver, tileSource, new IArchiveFile[] { gemfFileArchive });

// Create a download modular tile provider
final NetworkAvailabliltyCheck networkAvailabliltyCheck = new NetworkAvailabliltyCheck(context);
final MapTileDownloader downloaderProvider = new MapTileDownloader(
    tileSource, tileWriter, networkAvailablityCheck);

// Create a custom tile provider array with the custom tile source and the custom tile providers
final MapTileProviderArray tileProviderArray = new MapTileProviderArray(
    tileSource, registerReceiver, new MapTileModuleProviderBase[] {
        fileSystemProvider, fileArchiveProvider, downloaderProvider });

// Create the mapview with the custom tile provider array
mMapView = new MapView(context, 256, new DefaultResourceProxyImpl(context), tileProviderArray);
```




## Using a different Tile Source
osmdroid comes with a bunch of tile sources preprogrammed for sources available on the internet. Some require API keys or additional information due to usage restrictions, developers accounts, pay schemes, etc. The following example will show you how to switch tile sources at runtime.

To set to USGS satellite:
````
mMapView.setTileSource(TileSourceFactory.USGS_SAT);
````

To set to USGS Topo maps:
````
mMapView.setTileSource(TileSourceFactory.USGS_TOPO);
````

To set to a custom map server/tile source. In this case, we are using the USGS Topographic maps. This tile source is a bit different and requires some explanation. Most OSM based map sources use a URL pattern  similar to Zoom/X/Y.png. USGS, as well as many other ArcGis based sources, use Zoom/Y/X and thus require a different URL pattern.

osmdroid version <= 4.3

````
mMapView.setTileSource(new OnlineTileSourceBase("USGS Topo", ResourceProxy.string.custom, 0, 18, 256, "", 
               new String[] { "http://basemap.nationalmap.gov/ArcGIS/rest/services/USGSTopo/MapServer/tile/" }) {
               @Override
               public String getTileURLString(MapTile aTile) {
                    return getBaseUrl() + aTile.getZoomLevel() + "/" + aTile.getY() + "/" + aTile.getX()
				+ mImageFilenameEnding;
               }
          });
````

osmdroid version >= 5

````
mMapView.setTileSource(new OnlineTileSourceBase("USGS Topo", 0, 18, 256, "", 
               new String[] { "http://basemap.nationalmap.gov/ArcGIS/rest/services/USGSTopo/MapServer/tile/" }) {
               @Override
               public String getTileURLString(MapTile aTile) {
                    return getBaseUrl() + aTile.getZoomLevel() + "/" + aTile.getY() + "/" + aTile.getX()
				+ mImageFilenameEnding;
               }
          });
````



## How to adjust the tile cache (in memory)

Since many devices have support for `android:largeHeap="true"` settings, which enables your app to use more memory than "normal". This code snippet will allow you to increase the tile cache (in memory) to meet your needs. It's normally set to the exact number of tiles to fill the screen.

````
Iterator<Overlay> iterator = mMapView.getOverlays().iterator();
while(iterator.hasNext()){
	Overlay next = iterator.next();
	if (next instanceof TilesOverlay){
		TilesOverlay x = (TilesOverlay)next;
		x.setOvershootTileCache(x.getOvershootTileCache() * 2);
		Toast.makeText(getActivity(), "Tiles overlay cache set to " + x.getOvershootTileCache(), Toast.LENGTH_LONG).show();
		break;
	}
}
````

## Adjust the size of the cache on disk

The primary usage is downloaded map tiles

````
//this will set the disk cache size in MB to 1GB , 9GB trim size
OpenStreetMapTileProviderConstants.setCacheSizes(1000L, 900L);
````




# Online Map Sources (out of the box)

osmdroid comes with a few map sources preconfigured for you to use. Usage access rules vary based on the source. Make sure you read the fine print.

## Mapnik (aka Open Street Maps)

Required Java dependencies
 - osmdroid-android

Code Sample:

````
final ITileSource tileSource = TileSourceFactory.MAPNIK;
mMapView.setTileSource(tileSource);
````

## Mapquest

Note: This was updated with v5.3

Required Java dependencies
 - osmdroid-android

Code Sample:

````
final ITileSource tileSource = new MapQuestTileSource(context);
mMapView.setTileSource(tileSource);
````

Requires access key in your manifest for **MapBox**.
Manifest (optional):
Under manifest/application

````
<meta-data android:name="MAPQUEST_MAPID" android:value="YOUR KEY" />
<meta-data android:name="MAPQUEST_ACCESS_TOKEN" android:value="YOUR TOKEN" />
````



## HERE We Go Maps

Note: This was added with v5.3

Required Java dependencies
 - osmdroid-android

Code Sample:

````
final ITileSource tileSource = new HEREWeGoTileSource(context);
mMapView.setTileSource(tileSource);
````

Requires access key in your manifest.
Manifest (optional):
Under manifest/application

````
<meta-data android:name="HEREWEGO_MAPID" android:value="YOUR KEY" />
<meta-data android:name="HEREWEGO_APPID" android:value="YOUR TOKEN" />
<meta-data android:name="HEREWEGO_APPCODE" android:value="YOUR TOKEN" />
````



## Cloud made

Required Java dependencies
 - osmdroid-android

Manifest (optional):
Under manifest/application

````
<meta-data android:name="CLOUDMADE_KEY" android:value="YOUR KEY" />
````

Code Sample:

````
CloudmadeUtil.retrieveCloudmadeKey(context);
final ITileSource tileSource = TileSourceFactory.CLOUDMADESTANDARDTILES;
mMapView.setTileSource(tileSource);
````


## Map Box

Note: This was updated with v5.3

Required Java dependencies
 - osmdroid-android

Manifest (optional):
Under manifest/application

````
<meta-data android:name="MAPBOX_MAPID" android:value="YOUR KEY" />
<meta-data android:name="MAPBOX_ACCESS_TOKEN" android:value="YOUR TOKEN" />
````

Code Sample:

````
final MapBoxTileSource tileSource = new MapBoxTileSource();
//option 1, load your settings from the manifest
tileSource.retrieveAccessToken(context);
tileSource.retrieveMapBoxMapId(context);
//option 2, provide them programmatically
tileSource.setAccessToken(context);
tileSource.setMapBoxMapId(context);
mMapView.setTileSource(tileSource);
````
## USGS Topo

Required Java dependencies
 - osmdroid-android

Code Sample:

````
final ITileSource tileSource = TileSourceFactory.USGS_TOPO;
mMapView.setTileSource(tileSource);
````

## Bing Maps

Required Java dependencies
 - osmdroid-android
 - osmdroid-third-party

Manifest (optional):
Under manifest/application

````
<meta-data android:name="BING_KEY" android:value="YOUR KEY" />
````

Code Sample:

````
//load from manifest
org.osmdroid.tileprovider.tilesource.bing.BingMapTileSource.retrieveBingKey(this);
//or load programmatically
org.osmdroid.tileprovider.tilesource.bing.BingMapTileSource.setBingKey("YOUR KEY");
org.osmdroid.tileprovider.tilesource.bing.BingMapTileSource bing=new org.osmdroid.tileprovider.tilesource.bing.BingMapTileSource(null);
mapView.setTileSource(bing);
````

### Set the imagery options

#### Hybrid (Road + Aerial)

````
bing.setStyle(BingMapTileSource.IMAGERYSET_AERIALWITHLABELS);
````

#### Road

````
bing.setStyle(BingMapTileSource.IMAGERYSET_ROAD);
````

#### Aerial

````
bing.setStyle(BingMapTileSource.IMAGERYSET_AERIAL);
````

## Google Maps

Required Java dependencies
 - osmdroid-android
 - osmdroid-third-party

MinSDK 9

Code Sample:
See the example "GoogleMapsWrapper" application


## WMS Support

WMS Support on device is something that we're working on, but it's not a high priority on the moment.

You can use MOBAC to connected to a WMS map provider, then convert those tiles into an offline map source in the mean time.

## Geopackage Support

Geopackage is an open standard for defining a file format that can contain multiple imagery sets and/or vector graphics. It's possible to [convert geopackage files](https://github.com/spyhunter99/geopkg4osmdroid) into osmdroid compatible sqlite database. It's also possible to perform this task on an android device, however initial experiments showed that the performance wasn't very good.


# Offline map tiles

See this article https://github.com/osmdroid/osmdroid/wiki/Offline-Map-Tiles

# Can I use more than one tile source at a time?

The answer is yes. First, a few use cases.
 * Weather
 * Grid lines
 * Data that's too complex to draw or that's drawn on a server
 * Elevation data
 * Tile sources that use transparency 

Example (MGRS grid lines from an older ArcGIS REST endpoint. This is a US military provided service and may not be available everywhere and may go away at any point. It's just an example):
 1. First setup your base layer.
```` 
mMapView.setTileSource(TileSourceFactory.USGS_TOPO);
````
 2. Create a new Tile Provider and associated Tile Source
````
MapTileProviderBasic provider = new MapTileProviderBasic(getActivity(), new OnlineTileSourceBase("MGRS",0,15,256,"PNG", new String[0]) {
			@Override
			public String getTileURLString(MapTile aTile) {
				BoundingBox bbox=tile2boundingBox(aTile.getX(), aTile.getY(), aTile.getZoomLevel());
				String baseUrl ="http://egeoint.nrlssc.navy.mil/arcgis/rest/services/usng/USNG_93/MapServer/export?dpi=96&transparent=true&format=png24&bbox="+bbox.west+","+bbox.south+","+bbox.east+","+bbox.north+"&size=256,256&f=image";
				return baseUrl;
			}
		});
````
Note: the method `tile2boundingBox` was pulled from OSM's [Slippy Map](http://wiki.openstreetmap.org/wiki/Slippy_map_tilenames#Java) wiki entry. It converts a tile coordinate to a lat/lon bounds.
 3. Create the tile layer
````  TilesOverlay layer = new TilesOverlay(provider, getActivity());````
 4. Important, set the background and loading bits to transparent
````
		layer.setLoadingBackgroundColor(Color.TRANSPARENT);
		layer.setLoadingLineColor(Color.TRANSPARENT);
````
 5. Add it to the map view
```` mMapView.getOverlays().add(layer); ````




# Can I use an ESRI ArcGIS REST API to display tiles on osmdroid?

Yes, as long as the endpoint supports the `export` API with bounding box then the above example should get you going. Note: ESRI's APIs can vary significantly by version and configuration settings. Consult their API guide additional details (and report back here!)

# Translating Map Scale to OSM zoom levels

| Approximate Map Scale | OSM Zoom Level |
| --- | --- |
5M | 5
2M | 8
1M | 9
500k | 10
250k | 11-12
50 | 13-14
25k | 15
8k | 16

