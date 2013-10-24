package org.osmdroid.bonuspack.kml;

import java.util.List;
import org.w3c.dom.Element;
import android.graphics.Color;
import android.graphics.Paint;

public class Style {

	Paint outlinePaint = null;
	int fillColor = 0;

	int parseColor(String sColor){
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
	
	/**
	 * Construct a KML style from its associated XML Element <Style>...</Style>
	 * @param eStyle Element
	 */
	Style(Element eStyle){
		List<Element> lineStyles = KmlProvider.getChildrenByTagName(eStyle, "LineStyle");
		if (lineStyles.size()>0){
			Element l = lineStyles.get(0);
			List<Element> colors = KmlProvider.getChildrenByTagName(l, "color");
			String sColor = KmlProvider.getChildText(colors.get(0));
			int iColor = parseColor(sColor);
			List<Element> widths = KmlProvider.getChildrenByTagName(l, "width");
			String sWidth = KmlProvider.getChildText(widths.get(0));
			float fWidth = Float.parseFloat(sWidth);
			outlinePaint = new Paint();
			outlinePaint.setColor(iColor);
			outlinePaint.setStrokeWidth(fWidth);
			outlinePaint.setStyle(Paint.Style.STROKE);
		}
		List<Element> polyStyles = KmlProvider.getChildrenByTagName(eStyle, "PolyStyle");
		if (polyStyles.size()>0){
			Element p = (Element)polyStyles.get(0);
			List<Element> colors = KmlProvider.getChildrenByTagName(p, "color");
			String sColor = KmlProvider.getChildText(colors.get(0));
			fillColor = parseColor(sColor);
			//TODO: read <fill> and <outline> components
		}
		//TODO: IconStyle
	}
}
