# Tile Cache Settings

** NOTICE ** This article is not complete, work in progress!


This article is meant to describe how osmdroid caches tiles and how to tweak the settings if needed. The defaults work for most applications. Use with cautio

## In memory LRU Cache

````
//initial setting
org.osmdroid.tileprovider.constants.OpenStreetMapTileProviderConstants#CACHE_MAPTILECOUNT_DEFAULT
//programmatic construction
org.osmdroid.tileprovider.MapTileCache(int size)
````

## In memory TileOverlay overshoot cache

````
TilesOverlay x=this.mMapView.getOverlayManager().getTilesOverlay();
x.setOvershootTileCache(x.getOvershootTileCache() * 2);
````


## On disk cache (sqlite and file system storage)
````
OpenStreetMapTileProviderConstants.setCacheSizes(max, trim);
````
