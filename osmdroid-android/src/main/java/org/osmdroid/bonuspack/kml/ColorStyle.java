package org.osmdroid.bonuspack.kml;

import java.io.IOException;
import java.io.Writer;

import android.graphics.Color;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Handling of KML ColorStyle
 * @author M.Kergall
 */
public class ColorStyle implements Parcelable {
	/** color modes */
	static final int MODE_NORMAL=0, MODE_RANDOM=1;
	
	public int mColor;
	public int mColorMode;
	
	public ColorStyle(){
		this(0);
	}
	
	ColorStyle(int color){
		this.mColor = color;
		mColorMode = MODE_NORMAL;
	}
	
	/** return color in Android int color format */
	public static int parseKMLColor(String sColor){
		sColor = sColor.trim();
		while (sColor.length()<8)
			sColor = "0"+sColor;
		String aa = sColor.substring(0, 2);
		String bb = sColor.substring(2, 4);
		String gg = sColor.substring(4, 6);
		String rr = sColor.substring(6, 8);
		sColor = "#"+aa+rr+gg+bb;
		int iColor = 0xFF000000; //default
		try {
			iColor = Color.parseColor(sColor);
		} catch (IllegalArgumentException e){
			e.printStackTrace();
		}
		return iColor;
	}

	/** return color in KML color format, which is: AABBGGRR, in hexa values*/
	public static String colorAsKMLString(int aColor){
		return String.format("%02X%02X%02X%02X", Color.alpha(aColor), Color.blue(aColor), Color.green(aColor), Color.red(aColor));
	}
	
	/** return color in usual Android color format, which is: #AARRGGBB, in hexa values*/
	public static String colorAsAndroidString(int aColor){
		return String.format("#%08X", 0xFFFFFFFF & aColor);
	}
	
	public String colorAsAndroidString(){
		return colorAsAndroidString(mColor);
	}
	
	/**
	 * @return the color to use on an actual object. If color mode is random, generate appropriate random color. 
	 */
	public int getFinalColor(){
		if (mColorMode == MODE_NORMAL)
			return mColor;
		else  { //mode random:
			//generate a random color within the range of each color component:
			int alpha = Color.alpha(mColor);
			double randomRange = Math.random();
			int red = Color.red(mColor); red = (int)(red * randomRange);
			int green = Color.green(mColor); green = (int)(green * randomRange);
			int blue = Color.blue(mColor); blue = (int)(blue * randomRange);
			return Color.argb(alpha, red, green, blue);
		}
	}

	public void writeAsKML(Writer writer){
		try {
			writer.write("<color>"+colorAsKMLString(mColor)+"</color>\n");
			if (mColorMode == MODE_RANDOM){
				writer.write("<colorMode>random</colorMode>\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	//Parcelable implementation ------------

	@Override public int describeContents() {
		return 0;
	}

	@Override public void writeToParcel(Parcel out, int flags) {
		out.writeInt(mColor);
		out.writeInt(mColorMode);
	}
	
	public static final Creator<ColorStyle> CREATOR = new Creator<ColorStyle>() {
		@Override public ColorStyle createFromParcel(Parcel source) {
			return new ColorStyle(source);
		}
		@Override public ColorStyle[] newArray(int size) {
			return new ColorStyle[size];
		}
	};
	
	public ColorStyle(Parcel in){
		mColor = in.readInt();
		mColorMode = in.readInt();
	}
}

