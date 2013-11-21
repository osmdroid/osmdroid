package org.osmdroid.bonuspack.kml;

import android.graphics.Color;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Handling of KML ColorStyle
 * @author M.Kergall
 */
class ColorStyle implements Parcelable {
	/** color modes */
	static final int MODE_NORMAL=0, MODE_RANDOM=1;
	
	public int color = 0;
	public int colorMode = MODE_NORMAL;
	
	ColorStyle(){
	}
	
	protected int parseColor(String sColor){
		sColor = sColor.trim();
		while (sColor.length()<8)
			sColor = "0"+sColor;
		String aa = sColor.substring(0, 2);
		String bb = sColor.substring(2, 4);
		String gg = sColor.substring(4, 6);
		String rr = sColor.substring(6, 8);
		sColor = "#"+aa+rr+gg+bb;
		int iColor = 0xFF000000;
		try {
			iColor = Color.parseColor(sColor);
		} catch (IllegalArgumentException e){
			e.printStackTrace();
		}
		return iColor;
	}

	/*
	public ColorStyle(Element eColorStyle){
		color = 0;
		colorMode = MODE_NORMAL;
		List<Element> colors = KmlProvider.getChildrenByTagName(eColorStyle, "color");
		if (colors.size()>0){
			String sColor = KmlProvider.getChildText(colors.get(0));
			color = parseColor(sColor);
		}
		List<Element> colorModes = KmlProvider.getChildrenByTagName(eColorStyle, "colorMode");
		if (colorModes.size()>0){
			String sColorMode = KmlProvider.getChildText(colorModes.get(0));
			if ("random".equals(sColorMode)){
				colorMode = MODE_RANDOM;
			}
		}
	}
	*/
	
	public int getColor(){
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

