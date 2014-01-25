package org.osmdroid.bonuspack.kml;

import java.io.File;
import java.io.IOException;
import java.io.Writer;

import org.osmdroid.bonuspack.utils.WebImageCache;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.BitmapDrawable;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Handling of a KML Style (PolyStyle, LineStyle, IconStyle)
 * @author M.Kergall
 */
public class Style implements Parcelable {

	public ColorStyle outlineColorStyle;
	public ColorStyle fillColorStyle;
	public ColorStyle iconColorStyle;
	public float outlineWidth = 0.0f;
	public String mIconHref;
	public float mIconScale;
	public Bitmap mIcon;
	
	private static WebImageCache mIconCache;
	static {
		//one common cache for all icons:
		mIconCache = new WebImageCache(300);
	}
	
	/** default constructor */
	Style(){
		mIconScale = 1.0f;
	}
	
	/** 
	 * @return a Paint corresponding to the style (for a line or a polygon outline)
	 */
	public Paint getOutlinePaint(){
		Paint outlinePaint = new Paint();
		if (outlineColorStyle != null)
			outlinePaint.setColor(outlineColorStyle.getFinalColor());
		outlinePaint.setStrokeWidth(outlineWidth);
		outlinePaint.setStyle(Paint.Style.STROKE);
		return outlinePaint;
	}
	
	/** set the IconStyle icon */
	public void setIcon(String iconHref){
		mIconHref = iconHref;
		if (mIconHref.startsWith("http://")){
			mIcon = mIconCache.get(mIconHref);
		} else {
			//local file loading:
			File imgFile = new  File(iconHref);
			mIcon = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
		}
	}
	
	/** @return the icon, scaled and blended with the icon color, as specified in the IconStyle. 
	 * Assumes the icon is already loaded. */
	public BitmapDrawable getFinalIcon(Context context){
		int sizeX = Math.round(mIcon.getWidth() * mIconScale);
		int sizeY = Math.round(mIcon.getHeight() * mIconScale);
		Bitmap scaledBitmap = Bitmap.createScaledBitmap(mIcon, sizeX, sizeY, true);
		BitmapDrawable marker = new BitmapDrawable(context.getResources(), scaledBitmap);
		if (iconColorStyle != null){
			int color = iconColorStyle.getFinalColor();
			if (color != 0) //there is a real color to blend with:
				marker.setColorFilter(color, Mode.MULTIPLY);
		}
		return marker;
	}
	
	protected void writeOneStyle(Writer writer, String styleType, ColorStyle colorStyle){
		try {
			writer.write("<"+styleType+">\n");
			colorStyle.writeAsKML(writer);
			//write the specifics:
			if (styleType.equals("LineStyle")){
				writer.write("<width>"+outlineWidth+"</width>\n");
			} else if (styleType.equals("IconStyle")){
				if (mIconHref != null)
					writer.write("<Icon><href>"+mIconHref+"</href></Icon>\n");
				if (mIconScale != 1.0f)
					writer.write("<scale>"+mIconScale+"</scale>\n");
			}
		writer.write("</"+styleType+">\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void writeAsKML(Writer writer, String styleId){
		try {
			writer.write("<Style id=\'"+styleId+"\'>\n");
			if (outlineColorStyle != null)
				writeOneStyle(writer, "LineStyle", outlineColorStyle);
			if (fillColorStyle != null)
				writeOneStyle(writer, "PolyStyle", fillColorStyle);
			if (iconColorStyle != null)
				writeOneStyle(writer, "IconStyle", iconColorStyle);
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
		out.writeParcelable(outlineColorStyle, flags);
		out.writeParcelable(fillColorStyle, flags);
		out.writeParcelable(iconColorStyle, flags);
		out.writeFloat(outlineWidth);
		out.writeString(mIconHref);
		out.writeFloat(mIconScale);
		out.writeParcelable(mIcon, flags);
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
		outlineColorStyle = in.readParcelable(Style.class.getClassLoader());
		fillColorStyle = in.readParcelable(Style.class.getClassLoader());
		iconColorStyle = in.readParcelable(Style.class.getClassLoader());
		outlineWidth = in.readFloat();
		mIconHref = in.readString();
		mIconScale = in.readFloat();
		mIcon = in.readParcelable(Bitmap.class.getClassLoader());
	}
}
