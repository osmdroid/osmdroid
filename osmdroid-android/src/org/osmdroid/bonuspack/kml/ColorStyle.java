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
	
	public int color;
	public int colorMode;
	
	ColorStyle(){
		color = 0;
		colorMode = MODE_NORMAL;
	}
	
	ColorStyle(int color){
		super();
		this.color = color;
	}
	
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
	public String colorAsKMLString(){
		return String.format("%02X%02X%02X%02X", Color.alpha(color), Color.blue(color), Color.green(color), Color.red(color));
	}
	
	/** return color in usual Android color format, which is: #AARRGGBB, in hexa values*/
	public String colorAsAndroidString(){
		return String.format("#%08X", 0xFFFFFFFF & color);
	}
	
	/**
	 * @return the color to use on an actual object. If color mode is random, generate appropriate random color. 
	 */
	public int getFinalColor(){
		if (colorMode == MODE_NORMAL)
			return color;
		else  { //mode random:
			//generate a random color within the range of each color component:
			int alpha = Color.alpha(color);
			double randomRange = Math.random();
			int red = Color.red(color); red = (int)(red * randomRange);
			int green = Color.green(color); green = (int)(green * randomRange);
			int blue = Color.blue(color); blue = (int)(blue * randomRange);
			return Color.argb(alpha, red, green, blue);
		}
	}

	public void writeAsKML(Writer writer){
		try {
			writer.write("<color>"+colorAsKMLString()+"</color>\n");
			if (colorMode == MODE_RANDOM){
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
		out.writeInt(color);
		out.writeInt(colorMode);
	}
	
	public static final Parcelable.Creator<ColorStyle> CREATOR = new Parcelable.Creator<ColorStyle>() {
		@Override public ColorStyle createFromParcel(Parcel source) {
			return new ColorStyle(source);
		}
		@Override public ColorStyle[] newArray(int size) {
			return new ColorStyle[size];
		}
	};
	
	public ColorStyle(Parcel in){
		color = in.readInt();
		colorMode = in.readInt();
	}
}

