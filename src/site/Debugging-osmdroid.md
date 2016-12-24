# Debugging osmdroid

This article contains a collection of tips, tricks, tools, and procedures that you can use to help debug issues with osmdroid.

# Versions 5.6 and newer

All debugging and configuration settings were migrated to the `Configuration` class. Simply get a reference to it using
`Configuration.getInstance()` then use the appropriate setter for what you're looking for.

# Versions 5.5 and older

The vast majority of the debugging settings are hidden within a collection of osmdroid classes. The following a few of the key areas that are working looking at when you have a problem.

## Debugging tile loading issues

When your app starts up, try the following. This will produce a lot of log output to help you narrow down where the issue is.
````
OpenStreetMapTileProviderConstants.DEBUG_TILE_PROVIDERS=true;
OpenStreetMapTileProviderConstants.DEBUGMODE=true;
MapTileDownloader.DEBUG = true
````

## More logging settings

````
CloudmadeUtil.DEBUGMODE=true
MapViewConstants.DEBUGMODE=true
OverlayConstants.DEBUGMODE=true
````
