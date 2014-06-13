package org.osmdroid.bonuspack.kml;

import java.io.Writer;
import android.os.Parcelable;

/**
 * Handling of a KML StyleSelector (abstract class). 
 * @author M.Kergall
 */
public abstract class StyleSelector implements Parcelable {

	/** default constructor */
	public StyleSelector(){
	}
	
	abstract public void writeAsKML(Writer writer, String styleId);
	
	//TODO: need to implement Parcelable?
}
