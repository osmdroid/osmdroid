# About OSMBonusPack
osmdroid is a library to interact with OpenStreetMap data inside an Android application. It offers an almost full/free replacement to Android map objects: MapView, MapController, Overlays, etc.

This "OSMBonusPack" library complements osmdroid with (very) useful classes:

*  [Markers](https://github.com/MKergall/osmbonuspack/wiki/Tutorial_0) with nice and flexible "cartoon-bubbles",
*  [Routes and Directions](https://github.com/MKergall/osmbonuspack/wiki/Tutorial_1),
*  [Points of Interests](https://github.com/MKergall/osmbonuspack/wiki/Tutorial_2) (directory services),
*  [Marker Clustering](https://github.com/MKergall/osmbonuspack/wiki/Tutorial_3),
*  Polyline, [Polygon](https://github.com/MKergall/osmbonuspack/wiki/Tutorial_5) and [GroundOverlay](https://github.com/MKergall/osmbonuspack/wiki/Tutorial_5), similar to their Google Maps equivalents,
*  Support for [KML and GeoJSON](https://github.com/MKergall/osmbonuspack/wiki/Tutorial_4) content,
*  Geocoding and Reverse Geocoding,
*  Integrated Cache Management tools for off-line maps
*  and more...

Have a look to the examples below, and to the [features](https://github.com/MKergall/osmbonuspack/wiki/features). 

The [OSMNavigator](https://github.com/MKergall/osmbonuspack/wiki/OSMNavigator) application demonstrates the use of these classes. This is a generic-purpose Map/Navigation tool, including a KML viewer and editor.

# Examples
Geocoding, route display, bubble on the destination with the address and an image

![Geocoding, route display and bubble](https://github.com/MKergall/osmbonuspack/wiki/images/osmnavigator_1_1.png)

Turn-by-turn instructions shown in bubbles (with instructions in the default language of the phone):

![Turn-by-turn instructions shown in bubbles](https://github.com/MKergall/osmbonuspack/wiki/images/osmbonuspackdemo_2_1.png)

The same turn-by-turn instructions shown in list view:

![Turn-by-turn instructions shown in list view](https://github.com/MKergall/osmbonuspack/wiki/images/osmbonuspackdemo_3_1.png)

Searching for fuel stations along the route:

![Searching for fuel stations along the route](https://github.com/MKergall/osmbonuspack/wiki/images/osmbonuspackdemo_4_1.png)

Searching fo cinemas inside an area, with clustered markers:

![Clustering](https://github.com/MKergall/osmbonuspack/wiki/images/osmbonuspackdemo_4_2.png)

Showing Wikipedia POIs related to the current map view. In the bubble, the "more info" button will open the full Wikipedia page: 

![Wikipedia POIs](https://github.com/MKergall/osmbonuspack/wiki/images/osmbonuspackdemo_5_3.png)

Showing geolocalized Flickr photos related to the current map view:

![Geolocalized Flickr photos](https://github.com/MKergall/osmbonuspack/wiki/images/osmbonuspackdemo_6_1.png)

Showing geolocalized Picasa photos related to the current map view: 

on the map | as a list view
------------- | -------------
<img src="https://github.com/MKergall/osmbonuspack/wiki/images/osmnavigator_7_1.png" width="368" /> | <img src="https://github.com/MKergall/osmbonuspack/wiki/images/osmnavigator_8_1.png" width="368" />

When searching a place by name, shows its enclosing polygon

![Enclosing polygon](https://github.com/MKergall/osmbonuspack/wiki/images/osmbonuspackdemo_9_2.png)

Support for KML content.

Google Maps "My Places"  | rendered with OSMBonusPack
------------- | -------------
<img src="https://github.com/MKergall/osmbonuspack/wiki/images/osmbonuspackdemo_12.png" width="368" /> | <img src="https://github.com/MKergall/osmbonuspack/wiki/images/osmbonuspackdemo_11.png" width="368" />

# How to use it
Start with the [installation guide](https://github.com/MKergall/osmbonuspack/wiki/HowToInclude), then follow the [Tutorials](https://github.com/MKergall/osmbonuspack/wiki/Tutorial_0).

In the [releases](https://github.com/MKergall/osmbonuspack/releases), you will find the library (aar file) and the javadoc. 

# How to get help
If you need help to use osmdroid or OSMBonusPack, go to [StackOverflow](http://stackoverflow.com/questions/tagged/osmdroid) with "osmdroid" tag. 

If you think there is an issue with OSMBonusPack, or want to raise a request related to OSMBonusPack, post an issue in [OSMBonusPack Issues](https://github.com/MKergall/osmbonuspack/issues). 

If you think there is an issue with osmdroid, or want to raise a request related to osmdroid, read [osmdroid](https://github.com/osmdroid/osmdroid) advices. 

# Licence
The components inside this GitHub project are under LGPL licence, with an important simplification: 
The constraints described in Section 5.d and 5.e of the [LGPL LICENCE](https://github.com/MKergall/osmbonuspack/blob/master/LICENSE.md) are DISCARDED. 

This means that you are allowed to convey a Combined Work without providing the user any way to recombine or relink the application, and without providing any shared library mechanism. 

In other terms, you are allowed to include the OSMBonusPack library in your Android application, without making your application open source. 
