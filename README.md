# OSMBonusPack
osmdroid is a library to interact with OpenStreetMap data inside an Android application. It offers an almost full/free replacement to Android map objects: MapView, MapController, Overlays, etc.

This "OSMBonusPack" library complements osmdroid with (very) useful classes:

*  [Markers](Tutorial_0) with nice and flexible "cartoon-bubbles",
*  [Routes and Directions](Tutorial_1),
*  [Points of Interests](Tutorial_2) (directory services),
*  [Marker Clustering](Tutorial_3),
*  Polyline, [Polygon](Tutorial_5) and [GroundOverlay](Tutorial_5), similar to their Google Maps equivalents,
*  Support for [KML and GeoJSON](Tutorial_4) content,
*  Geocoding and Reverse Geocoding,
*  Integrated Cache Management tools for off-line maps
*  and more...

Have a look to the [Features](https://github.com/MKergall/osmbonuspack/wiki/features).

The [OSMNavigator](OSMNavigator) application demonstrates the use of these classes. This is a generic-purpose Map/Navigation tool, including a KML viewer and editor.

# Examples

Geocoding, route display, bubble on the destination with the address and an image
[[images/osmnavigator_1_1.png]]

Turn-by-turn instructions shown in bubbles (with instructions in the default language of the phone):

[[images/osmbonuspackdemo_2_1.png]]

The same turn-by-turn instructions shown in list view:<br/>
[[images/osmbonuspackdemo_3_1.png]]

Searching for fuel stations along the route:<br/>
[[images/osmbonuspackdemo_4_1.png]]

Searching fo cinemas inside an area, with clustered markers:<br/>
[[images/osmbonuspackdemo_4_2.png]]

Showing Wikipedia POIs related to the current map view. In the bubble, the "more info" button will open the full Wikipedia page: 
[[images/osmbonuspackdemo_5_3.png]]

Showing geolocalized Flickr photos related to the current map view:<br/>
[[images/osmbonuspackdemo_6_1.png]]

Showing geolocalized Picasa photos related to the current map view: 1) on the map, and 2) as a list view<br/>
[[images/osmnavigator_7_1.png]]
[[images/osmnavigator_8_1.png]]

When searching a place by name, shows its enclosing polygon<br/>
![9](http://osmbonuspack.googlecode.com/svn/BonusPackDownloads/img/osmbonuspackdemo_9_2.png)

MapBox Satellite maps in OSMNavigator:<br/>
![10](http://osmbonuspack.googlecode.com/svn/BonusPackDownloads/img/osmbonuspackdemo_10_1.png)

Support for KML content. Example: on the left, a Google Maps "My Places", rendered with OSMBonusPack on the right:<br/> 
![12](http://osmbonuspack.googlecode.com/svn/BonusPackDownloads/img/osmbonuspackdemo_12.png)
![11](http://osmbonuspack.googlecode.com/svn/BonusPackDownloads/img/osmbonuspackdemo_11.png)

# How to use it
Start with the [installation guide](HowToInclude), then follow the [Tutorials](Tutorial_0). 

In the [Downloads](http://code.google.com/p/osmbonuspack/source/browse/#svn%2FBonusPackDownloads), you will find the library (jar file), the javadoc, and the [OSMNavigator] application. 
