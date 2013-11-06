package org.osmdroid.bonuspack.kml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.osmdroid.bonuspack.overlays.ExtendedOverlayItem;
import org.osmdroid.bonuspack.overlays.FolderOverlay;
import org.osmdroid.bonuspack.overlays.ItemizedOverlayWithBubble;
import org.osmdroid.bonuspack.overlays.Polygon;
import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.PathOverlay;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import android.content.Context;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;

/**
 * Helper class to read and parse KML content, and build related overlays. 
 * Supports KML Point, LineString and Polygon placemarks. 
 * Supports KML Folder hierarchy. 
 * Supports styles for LineString and Polygon (not for Point). 
 * Supports colorMode (normal, random)
 * 
 * TODO: 
 * Objects ids
 * "open" tag for Folder and Document
 * 
 * @author M.Kergall
 */
public class KmlProvider {

	protected static final int NO_SHAPE=0, POINT=1, LINE_STRING=2, POLYGON=3; //KML geometry
	protected HashMap<String, Style> mStyles;
	protected boolean mVisibilitySupport = true;
	
	/**
	 * @param uri of the KML content. Can be for instance the url of a KML layer created with Google Maps. 
	 * @return DOM Document. 
	 */
	public Document getKml(String uri){
		DocumentBuilder docBuilder;
		Document kmlDoc = null;
		try {
			docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			kmlDoc = docBuilder.parse(uri);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return kmlDoc;
	}

	/**
	 * Define if visibility tag is handled or not. Default is true. 
	 * Setting it to false means that the visibility tag will not be considered, so all components will be set to visible. 
	 * @param visibilitySupport 
	 */
	public void setVisibilitySupport(boolean visibilitySupport){
		mVisibilitySupport = visibilitySupport;
	}
	
	protected GeoPoint parseGeoPoint(String input){
		String[] coords = input.split(",");
		try {
			double lon = Double.parseDouble(coords[0]);
			double lat = Double.parseDouble(coords[1]);
			double alt = (coords.length == 3 ? Double.parseDouble(coords[2]) : 0.0);
			GeoPoint p = new GeoPoint(lat, lon, alt);
			return p;
		} catch (NumberFormatException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	protected ArrayList<GeoPoint> getCoordinates(Element node){
		String coords = getChildText(node);
		String[] splitted = coords.split("\\s");
		ArrayList<GeoPoint> list = new ArrayList<GeoPoint>(splitted.length);
		for (int i=0; i<splitted.length; i++){
			GeoPoint p = parseGeoPoint(splitted[i]);
			if (p != null)
				list.add(p);
		}
		return list;
	}

	public static String getChildText(Element element) {
		StringBuilder builder = new StringBuilder();
		NodeList list = element.getChildNodes();
		for(int i = 0; i < list.getLength(); i++) {
			Node node = list.item(i);
			short type = node.getNodeType();
			if(type == Node.TEXT_NODE || type == Node.CDATA_SECTION_NODE) {
				builder.append(node.getNodeValue());
			}
		}
		return builder.toString();
	}
	
	public static List<Element> getChildrenByTagName(Element parent, String name) {
		List<Element> nodeList = new ArrayList<Element>();
		boolean getAll = name.equals("*");
		for (Node child = parent.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getNodeType() == Node.ELEMENT_NODE && (getAll || name.equals(child.getNodeName()))) {
				nodeList.add((Element) child);
			}
		}
	    return nodeList;
	}
	
	protected void handlePlacemark(Element element, FolderOverlay folderOverlay, Context context, MapView map, Drawable marker,
			ItemizedOverlayWithBubble<ExtendedOverlayItem> kmlPointsOverlay){
		List<Element> components = getChildrenByTagName(element, "*");
		String styleUrl = "", name = "", description = "";
		int type =  NO_SHAPE;
		ArrayList<GeoPoint> list = null;
		for (Element component:components){
			String nodeName = component.getTagName();
			if ("styleUrl".equals(nodeName)){
				styleUrl = getChildText(component);
				styleUrl = styleUrl.substring(1); //remove the #
			} else if ("name".equals(nodeName)){
					name = getChildText(component);
			} else if ("description".equals(nodeName)){
					description = getChildText(component);
			} else if ("Point".equals(nodeName)){
				NodeList coordinates = component.getElementsByTagName("coordinates");
				if (coordinates.getLength()>0){
					type = POINT;
					list = getCoordinates((Element)coordinates.item(0));
				}
			} else if ("LineString".equals(nodeName)){
				NodeList coordinates = component.getElementsByTagName("coordinates");
				if (coordinates.getLength()>0){
					type = LINE_STRING;
					list = getCoordinates((Element)coordinates.item(0));
				}
			} else if ("Polygon".equals(nodeName)){
				//TODO: distinguish outerBoundaryIs and innerBoundaryIs
				NodeList coordinates = component.getElementsByTagName("coordinates");
				if (coordinates.getLength()>0){
					type = POLYGON;
					list = getCoordinates((Element)coordinates.item(0));
				}
			}
		}
		//Create the overlay with the placemark geometry:
		switch (type){
		case POINT:
			if (list.size()>0){
				ExtendedOverlayItem item = new ExtendedOverlayItem(name, description, list.get(0), context);
				item.setMarkerHotspot(OverlayItem.HotspotPlace.BOTTOM_CENTER);
				item.setMarker(marker);
				kmlPointsOverlay.addItem(item);
			}
			break;
		case LINE_STRING:{
			Paint paint = null;
			Style style = mStyles.get(styleUrl);
			if (style != null){
				paint = style.getOutlinePaint();
			}
			if (paint == null){ 
				//set default:
				paint = new Paint();
				paint.setColor(0x90101010);
				paint.setStyle(Paint.Style.STROKE);
				paint.setStrokeWidth(5);
			}
			PathOverlay lineStringOverlay = new PathOverlay(0, context);
			lineStringOverlay.setPaint(paint);
			for (GeoPoint point:list)
				lineStringOverlay.addPoint(point);
			BoundingBoxE6 bb = BoundingBoxE6.fromGeoPoints(list);
			folderOverlay.add(lineStringOverlay, bb);
			}
			break;
		case POLYGON:{
			Paint outlinePaint = null; 
			int fillColor = 0x20101010; //default
			Style style = mStyles.get(styleUrl);
			if (style != null){
				outlinePaint = style.getOutlinePaint();
				fillColor = style.fillColorStyle.getColor();
			}
			if (outlinePaint == null){ 
				//set default:
				outlinePaint = new Paint();
				outlinePaint.setColor(0x90101010);
				outlinePaint.setStrokeWidth(5);
			}
			Polygon polygonOverlay = new Polygon(context);
			polygonOverlay.setFillColor(fillColor);
			polygonOverlay.setStrokeColor(outlinePaint.getColor());
			polygonOverlay.setStrokeWidth(outlinePaint.getStrokeWidth());
			polygonOverlay.setPoints(list);
			polygonOverlay.setTitle(name);
			polygonOverlay.setSnippet(description);
			if (!name.equals("") || !description.equals("")){
				String packageName = context.getPackageName();
				int layoutResId = context.getResources().getIdentifier("layout/bonuspack_bubble", null, packageName);
				polygonOverlay.setInfoWindow(layoutResId, map);
			}
			BoundingBoxE6 bb = BoundingBoxE6.fromGeoPoints(list);
			folderOverlay.add(polygonOverlay, bb);
			}
			break;
		default:
			break;
		}
	}
	
	protected void handleFolder(Element kmlElement, FolderOverlay folderOverlay, 
			Context context, MapView map, Drawable marker){
		
		ArrayList<ExtendedOverlayItem> kmlPointsItems = null;
		ItemizedOverlayWithBubble<ExtendedOverlayItem> kmlPointsOverlay = null;
		
		List<Element> children = getChildrenByTagName(kmlElement, "*");
		for (Element child:children){
			String childName = child.getTagName();
			if ("Document".equals(childName) || "Folder".equals(childName)){
				FolderOverlay subFolderOverlay = new FolderOverlay(context);
				handleFolder(child, subFolderOverlay, context, map, marker);
				folderOverlay.add(subFolderOverlay, subFolderOverlay.getBoundingBox());
			} else if ("Placemark".equals(childName)){
				if (kmlPointsOverlay == null){
					//we have placemarks inside this folder: initialize the overlay that will contain all points placemarks
					kmlPointsItems = new ArrayList<ExtendedOverlayItem>();
					kmlPointsOverlay = new ItemizedOverlayWithBubble<ExtendedOverlayItem>(context, 
							kmlPointsItems, map);
				}
				handlePlacemark(child, folderOverlay, context, map, marker, kmlPointsOverlay);
			} else if ("name".equals(childName)){
				folderOverlay.setName(getChildText(child));
			} else if ("description".equals(childName)){
				folderOverlay.setDescription(getChildText(child));
			} else if ("visibility".equals(childName)){
				if (mVisibilitySupport){
					String sVisibility = getChildText(child);
					boolean visibility = ("1".equals(sVisibility));
					folderOverlay.setEnabled(visibility);
				}
			}
		}
		
		if (kmlPointsOverlay != null && kmlPointsOverlay.size()>0){
			BoundingBoxE6 bb = kmlPointsOverlay.getBoundingBoxE6();
			folderOverlay.add(kmlPointsOverlay, bb);
		}
	}
	
	protected void getStyles(Element kmlRoot){
		NodeList styles = kmlRoot.getElementsByTagName("Style");
		mStyles = new HashMap<String, Style>(styles.getLength());
		for (int i=0; i<styles.getLength(); i++){
			Element eStyle = (Element)styles.item(i);
			String id = eStyle.getAttribute("id");
			Style sStyle = new Style(eStyle);
			mStyles.put(id, sStyle);
		}
	}
	
	/**
	 * From a KML document, fill overlay with placemarks
	 * @param kmlDoc
	 * @param placemarksOverlay
	 * @param context
	 * @param marker
	 */
	public void buildOverlays(Element kmlRoot, FolderOverlay folderOverlay, Context context, MapView map, Drawable marker){
		getStyles(kmlRoot);
		//Recursively handle the element and all its sub-folders:
		handleFolder(kmlRoot, folderOverlay, context, map, marker);
	}
}
