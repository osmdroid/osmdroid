package org.osmdroid.bonuspack.kml;

import java.io.IOException;
import java.io.Writer;
import android.graphics.Paint;
import android.os.Parcel;
import android.os.Parcelable;

/** Handling of KML LineStyle */
public class LineStyle extends ColorStyle implements Parcelable {
	public float mWidth;

	public LineStyle(){
		this(0, 1.0f);
	}
	
	public LineStyle(int color, float width){
		super(color);
		mWidth = width;
	}
	
	/** 
	 * @return a Paint corresponding to the LineStyle (for a line or a polygon outline)
	 */
	public Paint getOutlinePaint(){
		Paint outlinePaint = new Paint();
		outlinePaint.setColor(getFinalColor());
		outlinePaint.setStrokeWidth(mWidth);
		outlinePaint.setStyle(Paint.Style.STROKE);
		return outlinePaint;
	}
	
	public void writeAsKML(Writer writer){
		try {
			writer.write("<LineStyle>\n");
			super.writeAsKML(writer);
			//write the specifics:
			writer.write("<width>"+mWidth+"</width>\n");
			writer.write("</LineStyle>\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	//Parcelable implementation ------------
	
	@Override public int describeContents() {
		return 0;
	}

	@Override public void writeToParcel(Parcel out, int flags) {
		super.writeToParcel(out, flags);
		out.writeFloat(mWidth);
	}
	
	public static final Parcelable.Creator<LineStyle> CREATOR = new Parcelable.Creator<LineStyle>() {
		@Override public LineStyle createFromParcel(Parcel source) {
			return new LineStyle(source);
		}
		@Override public LineStyle[] newArray(int size) {
			return new LineStyle[size];
		}
	};
	
	public LineStyle(Parcel in){
		super(in);
		mWidth = in.readFloat();
	}
	
}
