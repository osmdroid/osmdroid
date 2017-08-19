package org.osmdroid.gpkg.features;

/**
 * Shape markers interface for handling marker changes on shapes that have
 * children
 * 
 * @author osbornb
 */
public interface ShapeWithChildrenMarkers extends ShapeMarkers {

	/**
	 * Create a child shape
	 * 
	 * @return
	 */
	public ShapeMarkers createChild();

}
