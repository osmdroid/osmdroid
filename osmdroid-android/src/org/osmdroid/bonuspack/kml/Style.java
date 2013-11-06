package org.osmdroid.bonuspack.kml;

import java.util.List;
import org.w3c.dom.Element;
import android.graphics.Color;
import android.graphics.Paint;

/**
 * Handling of KML PolyStyle and LineStyle
 * @author M.Kergall
 */
public class Style {

	ColorStyle outlineColorStyle;
	ColorStyle fillColorStyle;
	float outlineWidth = 0.0f;

	class ColorStyle {
		/** color modes */
		static final int MODE_NORMAL=0, MODE_RANDOM=1;
		
		protected int color = 0;
		protected int colorMode = MODE_NORMAL;
		
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
	}
	
	void parseLineStyle(Element eLineStyle){
		outlineColorStyle = new ColorStyle(eLineStyle);
		List<Element> widths = KmlProvider.getChildrenByTagName(eLineStyle, "width");
		String sWidth = KmlProvider.getChildText(widths.get(0));
		outlineWidth = Float.parseFloat(sWidth);
	}

	void parsePolyStyle(Element ePolyStyle){
		fillColorStyle = new ColorStyle(ePolyStyle);
		//TODO: read <fill> and <outline> components
	}
	
	/** 
	 * @return a Paint corresponding to the style (for a line or a polygon outline)
	 */
	public Paint getOutlinePaint(){
		Paint outlinePaint = new Paint();
		outlinePaint.setColor(outlineColorStyle.getColor());
		outlinePaint.setStrokeWidth(outlineWidth);
		outlinePaint.setStyle(Paint.Style.STROKE);
		return outlinePaint;
	}
	
	/**
	 * Construct a KML style from its associated XML Element <Style>...</Style>
	 * @param eStyle Element
	 */
	Style(Element eStyle){
		outlineColorStyle = new ColorStyle();
		fillColorStyle = new ColorStyle();
		List<Element> lineStyles = KmlProvider.getChildrenByTagName(eStyle, "LineStyle");
		if (lineStyles.size()>0){
			Element l = lineStyles.get(0);
			parseLineStyle(l);
		}
		List<Element> polyStyles = KmlProvider.getChildrenByTagName(eStyle, "PolyStyle");
		if (polyStyles.size()>0){
			Element p = (Element)polyStyles.get(0);
			parsePolyStyle(p);
		}
		//TODO: IconStyle
	}
}
