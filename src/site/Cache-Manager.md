Originally written by user MKer as part of osmbonuspack (migrated somewhere around version 5.3), the Cache Manager provides a number of super cool kick ass functions. Here's a few quick highlights

- Download and cache tiles for a given geographic area (bounds and min/max zoom)
- Download and cache tiles for a given set of way points (set of points and min/max zoom)
- Download and produce a persistent tile archive (MOBAC like function) (via bounds or waypoints) 
- Clear the cache for a given area or way points

The following section details the WPCacheManager which provides the functions for way points tile fetching produced by user 2ndGAB. WPCacheManager was merged with CacheManager in osmdroid-android.


OSMDroid CacheManager enhanced to download map tile covered by a waypoints list.

OSMDroid CacheManager available from 5.3, allows you to download map tiles based on a bounding box.
That's of course quite useful as we know there are lot of places where mobile connection is not good enough to use online map.

There is a problem anyway when you develop an application providing pedestrian or cycling courses, for example, 
where the surface of the bounding box could contain lot of tiles for high zoom level whereas the tiles 
covered by the courses are finally not so many.  
The consequence is to wait a long time to download unuseful parts of the map.  
(Note: WPCacheManager is based on CacheManager [feature#315](https://github.com/osmdroid/osmdroid/issues/315).  

So this WPCacheManager, which keep the original features, add the possibility to give a list of waypoints.  
WPCacheManager considers that you follow the path between 2 successives points in the given list.  
And as there is a possibility for your waypoint or the path to be close to the border of a tile, I decided to
download the 8 tiles around the given one at each zoom level, because I think it's not very cool to not see what's going 
few meters beside.  
Don't be afraid that's not so much, and in fact, only the first tile downloads 8 tiles more, because when you continue your trip,
some of the next 8 new tiles have already be downloaded.  

For example, consider that you define the path drawn ![on this image](http://i.imgur.com/Vxf9Z06.jpg):  
To explain what the algorithm do, I only take 3 characteristics points 1, 2 and 3.  
The red point 1 belongs to the the red tinted tile. It will download the 1+8 tiles in the big red square.  
The green point 2 belongs to the the green tinted tile. It will download the 1+8 tiles in the big green square.  
The yellow point 3 belongs to the the yellow titend tile. It will download the 1+8 tiles in the big yellow square.  
And so on...

As you can see, for point 1, you will effectively downloads 9 tiles.  
For point 2, you will only download the 3 more tiles on the right of the big red square.  
For point 3, you will only download the 3 more tiles above the big green square.  
...

#So here is the big advantage#
So of course, if you only need an offline map for this zoom level, because `WPCacheManager` downloads adjacent tiles, the result is not better because it will download more tiles than the original algorithm.
Now, consider that you need to download this map from level 13 up to level 18. You will qucikly understand the advantage taking a look below where you can see all tiles downloaded to cover the area at level 18 ![this image](http://i.imgur.com/O2hu8ur.jpg).
  
# New methods:
So the new methods have been added to OSMDroid CacheManager. All added methods take care of the extra tiles downloaded 
around the GePoints list. So the method `extendedBoundsFromGeoPoints(geoPoints)` returns a larger area than the original `BoundingBoxE6.fromGeoPoints(geoPoints)`.

    public int possibleTilesCovered(ArrayList<GeoPoint> geoPoints, final int zoomMin, final int zoomMax);
    
    public void downloadAreaAsync(Context ctx, ArrayList<GeoPoint> geoPoints, final int zoomMin, final int zoomMax);
    
    public void downloadAreaAsync(Context ctx, ArrayList<GeoPoint> geoPoints, final int zoomMin, final int zoomMax, final CacheManagerCallback callback);
    
    public void downloadAreaAsyncNoUI(Context ctx, ArrayList<GeoPoint> geoPoints, final int zoomMin, final int zoomMax, final CacheManagerCallback callback);

    public void cleanAreaAsync(Context ctx, ArrayList<GeoPoint> geoPoints, int zoomMin, int zoomMax);
    
    public BoundingBoxE6 extendedBoundsFromGeoPoints(ArrayList<GeoPoint> geoPoints);
    