# OSM Map Tile Packager

## Why would you want to use this?
Caching tiles for offline/disconnect use

## Any legal issues?
Plenty, make sure you read the tile usage agreement from whomever you cache tiles from. Many tile sources do not allow via their license agreements.

## What sources can I use with this?
Any map source (baring legal issues) that uses the Z/X/Y tile naming convention.

## Any examples?

Tons. keep reading.

## What output files can this produce?

Osmdroid style zip, sqlite and GEMFile databases.

## OK I downloaded the internet, what do I do with the output file?

Copy the output file to /sdcard/osmdroid/. Then tell Osmdroid to use the correct map source. See below for an important note...

## I deployed the zip but no tiles are displaying. What's wrong?

You probably missed the 'important note' section.

## Any known issues?

Check the issue tracker. There's also issues when using the entire planet as your download set, whereby the tile bound calculations produce strange results (negative y indexes). Try a smaller bounding box.

## How do I use this?

Open a command line/terminal/bash shell

`java -jar osmdroid-packager-<VERSION>-jar-with-dependencies.jar -u url -t <MapSourceName> -d <outputfile.zip> -zmin <MIN ZOOM> -zmax <MAX ZOOM up to 22> -n <NORTH> -s <SOUTH> -e <EAST> -w <WEST> -nthreads <optional, number of current threads, default 2> -fa <optional file extension add on, not really needed>1`

All bounds are in decimal degrees latitude/longitude.


## Important note

Here's a few usage examples. There's a few caveats that you must follow if you want these tiles to show up in OsmDroid.

1) The temporary folder to save files should be named the name of your map source.
2) In OsmDroid, set the map source's name equal to the temporary folder name.

`java -jar ....-jar -t <name of map source here> -d outputfile`

Example: Open Streem Map tiles (Mapnik)

````
java -jar ....jar -u http://b.tile.openstreetmap.org/%d/%d/%d.png -t Mapnik -d at_mapnik_13.zip -zmax 13 -n 49.03942 -s 46.40162 -e 17.14736 -w 9.44595
                                                                       ^ that's the important part!
````

## More reading

https://github.com/osmdroid/osmdroid/wiki/Offline-Map-Tiles

https://github.com/osmdroid/osmdroid/wiki/Map-Sources


## More examples

### Austria, Mapnik
	-u http://b.tile.openstreetmap.org/%d/%d/%d.png -t Mapnik -d at_mapnik_13.zip -zmax 13 -n 49.03942 -s 46.40162 -e 17.14736 -w 9.44595
	
### Sweden, Mapnik
	-u http://b.tile.openstreetmap.org/%d/%d/%d.png -t Mapnik -d se_mapnik_12.zip -zmax 12 -n 69.05567 -s 55.18336 -e 24.31045 -w 11.17080
	
### Germany, Mapnik
	-u http://b.tile.openstreetmap.org/%d/%d/%d.png -t Mapnik -d de_mapnik_12.zip -zmax 12 -n 54.9174 -s 47.281101 -e 15.03805 -w 5.869443

### Germany, Berlin(wider area), Mapnik
	-u http://b.tile.openstreetmap.org/%d/%d/%d.png -t Mapnik -d de_berlin_mapnik_15.zip -zmax 15 -n 52.80 -s 52.24 -e 13.94 -w 12.80
	
### BeNeLux, Mapnik
	-u http://b.tile.openstreetmap.org/%d/%d/%d.png -t Mapnik -d benelux_mapnik_13.zip -zmax 13 -n 53.59187 -s 49.44601 -e 7.25555 -w 2.35564
	
### Utrecht, Mapnik
	-u http://b.tile.openstreetmap.org/%d/%d/%d.png -t Mapnik -d nl_utrecht_mapnik_16.zip -zmax 16 -n 52.161593 -s 52.045820 -e 5.189602 -w 4.988071

### France
````
France, Mapnik
	-u http://b.tile.openstreetmap.org/%d/%d/%d.png -t Mapnik -d fr_mapnik_12.zip -zmax 12 -n 51.091099 -s 41.366379 -e 9.543055 -w -4.790556
	
Paris, Mapnik
	-u http://b.tile.openstreetmap.org/%d/%d/%d.png -t Mapnik -d fr_paris_mapnik_15.zip -zmax 15 -n 49.07002 -s 48.60997 -e 2.87887 -w 1.85302	
````

### Czech Republic, Mapnik
	-u http://b.tile.openstreetmap.org/%d/%d/%d.png -t Mapnik -d cz_mapnik_13.zip -zmax 13 -n 51.08776 -s 48.54 -e 18.87222 -w 12.049
	
### Ireland, Mapnik
	-u http://b.tile.openstreetmap.org/%d/%d/%d.png -t Mapnik -d ie_mapnik_13.zip -zmax 13 -n 55.37187 -s 51.40497 -e -5.35950 -w -10.67689

### UK

````
UK, Mapnik
	-u http://b.tile.openstreetmap.org/%d/%d/%d.png -t Mapnik -d uk_mapnik_12.zip -zmax 12 -n 58.62459 -s 49.87227 -e 1.73766 -w -8.23792
	
London, Mapnik
	-u http://b.tile.openstreetmap.org/%d/%d/%d.png -t Mapnik -d uk_london_mapnik_15.zip -zmax 15 -n 51.73360 -s 51.25223 -e 0.30257 -w -0.60105
	

Spain, Mapnik
	-u http://b.tile.openstreetmap.org/%d/%d/%d.png -t Mapnik -d es_mapnik_12.zip -zmax 12 -n 43.77677 -s 36.05546 -e 4.34829 -w -9.31870
	
````

### Poland, Mapnik
	-u http://b.tile.openstreetmap.org/%d/%d/%d.png -t Mapnik -d pl_mapnik_12.zip -zmax 12 -n 54.85584 -s 48.94210 -e 24.21157 -w 14.08218
	
### Italy, Mapnik
	-u http://b.tile.openstreetmap.org/%d/%d/%d.png -t Mapnik -d it_mapnik_13.zip -zmax  13 -n 47.09792 -s 36.63066 -e 18.53164 -w 6.64443

### Switzerland, Mapnik
	-u http://b.tile.openstreetmap.org/%d/%d/%d.png -t Mapnik -d ch_mapnik_13.zip -zmax  13 -n 47.82581 -s 45.80365 -e 10.55007 -w 5.59779
	
### Portugal
````
Portugal, Mapnik
	-u http://b.tile.openstreetmap.org/%d/%d/%d.png -t Mapnik -d pt_mapnik_13.zip -zmax 13 -n 42.17330 -s 36.95179 -e -6.18760 -w -9.51646
	
Lisboa, Mapnik
	-u http://b.tile.openstreetmap.org/%d/%d/%d.png -t Mapnik -d pt_lisbon_mapnik_17.zip -zmax 17 -n 38.83006 -s 38.63616 -e -9.09623 -w -9.30016
	
Porto, Mapnik
	-u http://b.tile.openstreetmap.org/%d/%d/%d.png -t Mapnik -d pt_porto_mapnik_17.zip -zmax 17 -n 41.22138 -s 41.06004 -e -8.56271 -w -8.70587
````

### USA

````
USA, Mapnik
	-u http://b.tile.openstreetmap.org/%d/%d/%d.png -t Mapnik -d us_mapnik_11.zip -zmax 11 -n 49.15093 -s 24.84373 -e -66.67832 -w -124.90586
	
USA, Manhattan
	-u http://b.tile.openstreetmap.org/%d/%d/%d.png -t Mapnik -d us_manhattan_mapnik_17.zip -zmax 17 -n 40.83197 -s 40.69545 -e -73.87409 -w -74.06223
	
USA, Washington D.C. 
	-u http://b.tile.openstreetmap.org/%d/%d/%d.png -t Mapnik -d us_washingtondc_mapnik_17.zip -zmax 17 -n 38.99648 -s 38.80143 -e -76.90797 -w -77.17370
	
USA, San Francisco(specificly), Mapnik, MaxZoom=17, for AndNav2
    -u http://b.tile.openstreetmap.org/%d/%d/%d.png -t Mapnik -d us_sanfrancisco_mapnik_17.zip -zmax 17 -n 37.811976 -s 37.699600 -e -122.345098 -w -122.521050
 ````