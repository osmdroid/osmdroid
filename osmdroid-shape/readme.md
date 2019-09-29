Originally sourced from https://sourceforge.net/projects/javashapefilere/ by Author: olenus
License: ASF 2.0

For sample Shapefiles, try these
http://thematicmapping.org/downloads/TM_WORLD_BORDERS_SIMPL-0.3.zip
https://catalog.data.gov/dataset/military-installations-ranges-and-training-areas


See also https://github.com/osmdroid/osmdroid/issues/906

## usage

gradle build file

    compile project('org.osmdroid:osmdroid-shape:VERSION')

java

    List<Overlay>  folder = ShapeConverter.convert(mMapView, new File(myshape));
    mMapView.getOverlayManager().addAll(folder);
    mMapView.invalidate();

Where `myshape` is a .shp file somewhere on the drive.
If other metadata files are found, they will be used and injected into the converted shapes.