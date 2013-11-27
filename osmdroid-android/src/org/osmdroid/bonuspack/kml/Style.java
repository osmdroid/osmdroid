package org.osmdroid.bonuspack.kml;

import java.io.IOException;
import java.io.Writer;

import android.graphics.Paint;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Handling of KML PolyStyle and LineStyle
 * @author M.Kergall
 */
public class Style implements Parcelable {

	ColorStyle outlineColorStyle;
	ColorStyle fillColorStyle;
	ColorStyle iconColorStyle;
	float outlineWidth = 0.0f;
	
	/** 
	 * @return a Paint corresponding to the style (for a line or a polygon outline)
	 */
	public Paint getOutlinePaint(){
		Paint outlinePaint = new Paint();
		outlinePaint.setColor(outlineColorStyle.getFinalColor());
		outlinePaint.setStrokeWidth(outlineWidth);
		outlinePaint.setStyle(Paint.Style.STROKE);
		return outlinePaint;
	}
	
	/** default constructor */
	Style(){
	}
	
	//Parcelable implementation ------------
	
	@Override public int describeContents() {
		return 0;
	}

	@Override public void writeToParcel(Parcel out, int flags) {
		out.writeParcelable(outlineColorStyle, flags);
		out.writeParcelable(fillColorStyle, flags);
		out.writeFloat(outlineWidth);
	}
	
	public void writeAsKML(Writer writer, String styleId){
		try {
			writer.write("<Style id=\'"+styleId+"\'>\n");
			if (outlineColorStyle != null)
				outlineColorStyle.writeAsKML(writer, "LineStyle", outlineWidth);
			if (fillColorStyle != null)
				fillColorStyle.writeAsKML(writer, "PolyStyle", 0.0f);
			if (iconColorStyle != null)
				iconColorStyle.writeAsKML(writer, "IconStyle", 0.0f);
			writer.write("</Style>\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	//Parcelable implementation ------------
	
	public static final Parcelable.Creator<Style> CREATOR = new Parcelable.Creator<Style>() {
		@Override public Style createFromParcel(Parcel source) {
			return new Style(source);
		}
		@Override public Style[] newArray(int size) {
			return new Style[size];
		}
	};
	
	public Style(Parcel in){
		outlineColorStyle = in.readParcelable(Style.class.getClassLoader());
		fillColorStyle = in.readParcelable(Style.class.getClassLoader());
		outlineWidth = in.readFloat();
	}
}
