# OpenStreetMapsTileServer
A simple Java server that serves up OSM Sqlite database files. This tool
will serve up map tiles stored in a osmdroid sqlite database created by [MOBAC](http://mobac.sourceforge.net/).

# Usage Scenario
This tool is not designed for a high volume, production scale systems. It's most appropriate for testing, development and disconnected (from the internet) environments where you needed to serve up map tiles quickly and don't want to generate tiles using the Open Street Map database and associated tooling. This tool is not meant to be internet facing.

# Why?
Primarily for those want to test osmdroid on a network that can't reach the internet.

# Usage

## Preparing your tile databases
Fire up MOBAC and create an [OsmDroid](https://github.com/osmdroid/osmdroid) sqlite database. Use 256x256pixel tiles.

## Build
`git clone `https://github.com/osmdroid/osmdroid.git`

`gradlew install`

## configure
`vi sources.properties`

Add in whatever OsmDroid style Sqlite database files that you have using the example. A very small example database using the USGS Topographic maps is provided.

## Start it up

`java -jar target/osmdroid-server-jdk-<version>-jar-with-dependencies.jar (optional port, default 80)`

`../gradlew run <port>`

Point browser to: http://localhost:port/,  tile sources can be changed using the drop down menu.

# Web server deployment
The tile server is also packaged as a WAR file. I haven't tested it, so I probably won't work. 

# Contributing
Pull requests are encouraged and accepted

# Road Map
- flexible plugin system to enable server tiles from virtually any sqlite based table schema
- support other database servers (via JDBC connections)


# Example map data set
The example map source was produced using [MOBAC](http://mobac.sourceforge.net/) using the [USGS Topographic](http://www.usgs.gov/faq/categories/10154/3550) map source. It's considered public domain, but they asked for the following.

"Map services and data available from U.S. Geological Survey, National Geospatial Program." Please go to http://www.usgs.gov/visual-id/credit_usgs.html for further information and details regarding the USGS Visual Identity System.

# License
This bit of code: [Apache Software Foundation 2.0](http://www.apache.org/licenses/LICENSE-2.0)

OpenLayers: [BSD 2 Clause](https://github.com/openlayers/ol3/blob/master/LICENSE.md)

Apache libraries [Apache 2.0 License](http://www.apache.org/licenses/LICENSE-2.0)

