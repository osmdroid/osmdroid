package org.osmdroid.bonuspack.kml;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.zip.ZipFile;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Handling of a KML Style, which may contain one PolyStyle, one LineStyle, and one IconStyle. 
 * @author M.Kergall
 */
public class Style extends StyleSelector implements Parcelable {

	public ColorStyle mPolyStyle;
	public LineStyle mLineStyle;
	public IconStyle mIconStyle;
	
	/** default constructor */
	public Style(){
		super();
	}
	
	/** simple constructor */
	public Style(Bitmap icon, int lineColor, float lineWidth, int fillColor){
		this();
		mIconStyle = new IconStyle();
		mIconStyle.mIcon = icon;
		mLineStyle = new LineStyle();
		mLineStyle.mColor = lineColor;
		mLineStyle.mWidth = lineWidth;
		mPolyStyle = new ColorStyle();
		mPolyStyle.mColor = fillColor;
	}
	
	public void setIcon(String iconHref, File containerFile, ZipFile kmzContainer){
		if (mIconStyle == null)
			mIconStyle = new IconStyle();
		mIconStyle.setIcon(iconHref, containerFile, kmzContainer);
	}
	
	public BitmapDrawable getFinalIcon(Context context){
		if (mIconStyle != null)
			return mIconStyle.getFinalIcon(context);
		else 
			return null;
	}
	
	public Paint getOutlinePaint(){
		if (mLineStyle != null)
			return mLineStyle.getOutlinePaint();
		else {
			Paint outlinePaint = new Paint();
			outlinePaint.setStyle(Paint.Style.STROKE);
			return outlinePaint;
		}
	}
	
	protected void writePolyStyle(Writer writer, ColorStyle colorStyle){
		try {
			writer.write("<PolyStyle>\n");
			colorStyle.writeAsKML(writer);
			writer.write("</PolyStyle>\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override public void writeAsKML(Writer writer, String styleId){
		try {
			writer.write("<Style id=\'"+styleId+"\'>\n");
			if (mLineStyle != null)
				mLineStyle.writeAsKML(writer);
			if (mPolyStyle != null)
				writePolyStyle(writer, mPolyStyle);
			if (mIconStyle != null)
				mIconStyle.writeAsKML(writer);
			writer.write("</Style>\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	//Parcelable implementation ------------
	
	@Override public int describeContents() {
		return 0;
	}

	@Override public void writeToParcel(Parcel out, int flags) {
		out.writeParcelable(mLineStyle, flags);
		out.writeParcelable(mPolyStyle, flags);
		out.writeParcelable(mIconStyle, flags);
	}
	
	public static final Parcelable.Creator<Style> CREATOR = new Parcelable.Creator<Style>() {
		@Override public Style createFromParcel(Parcel source) {
			return new Style(source);
		}
		@Override public Style[] newArray(int size) {
			return new Style[size];
		}
	};
	
	public Style(Parcel in){
		mLineStyle = in.readParcelable(LineStyle.class.getClassLoader());
		mPolyStyle = in.readParcelable(ColorStyle.class.getClassLoader());
		mIconStyle = in.readParcelable(IconStyle.class.getClassLoader());
	}
}
