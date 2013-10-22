package org.osmdroid.bonuspack.kml;

import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.osmdroid.bonuspack.overlays.ExtendedOverlayItem;
import org.osmdroid.bonuspack.overlays.FolderOverlay;
import org.osmdroid.bonuspack.overlays.ItemizedOverlayWithBubble;
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
 * TODO: 
 * Handle styles
 * Handle polygons
 * Handle folder name
 * Build bounding box
 * 
 * @author M.Kergall
 */
public class KmlProvider {

	/**
	 * @param uri of the KML content. Can be for instance Google Map the url of a KML layer created with Google Maps. 
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

	protected static final int NO_SHAPE=0, POINT=1, LINE_STRING=2;

	public String getChildText(Element element) {
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
	
	public List<Element> getChildrenByTagName(Element parent, String name) {
		List<Element> nodeList = new ArrayList<Element>();
		for (Node child = parent.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getNodeType() == Node.ELEMENT_NODE && name.equals(child.getNodeName())) {
				nodeList.add((Element) child);
			}
		}
	    return nodeList;
	}

	void handleSubFolders(List<Element> subFolders, FolderOverlay folderOverlay, Context context, MapView map, Drawable marker){
		for (Element subFolder:subFolders){
			FolderOverlay subFolderOverlay = new FolderOverlay(context, map);
			buildOverlays(subFolder, subFolderOverlay, context, map, marker);
			folderOverlay.add(subFolderOverlay, subFolderOverlay.getBoundingBox());
		}
	}
	
	/**
	 * From a KML document, fill overlay with placemarks
	 * @param kmlDoc
	 * @param placemarksOverlay
	 * @param context
	 * @param marker
	 */
	public void buildOverlays(Element kmlRoot, FolderOverlay folderOverlay, 
			Context context, MapView map, Drawable marker){
		
		final ArrayList<ExtendedOverlayItem> kmlPointsItems = new ArrayList<ExtendedOverlayItem>();
		ItemizedOverlayWithBubble<ExtendedOverlayItem> kmlPointsOverlay = new ItemizedOverlayWithBubble<ExtendedOverlayItem>(context, 
				kmlPointsItems, map);
		
		//Recursively handle all sub-folders:
		List<Element> subDocs = getChildrenByTagName(kmlRoot, "Document");
		handleSubFolders(subDocs, folderOverlay, context, map, marker);
		
		List<Element> subFolders = getChildrenByTagName(kmlRoot, "Folder");
		handleSubFolders(subFolders, folderOverlay, context, map, marker);
		
		//Handle all placemarks:
		List<Element> placemarks = getChildrenByTagName(kmlRoot, "Placemark");
		for (Element placemark:placemarks){
			NodeList components = placemark.getElementsByTagName("*");
			String name = "", description = "";
			int type =  NO_SHAPE;
			ArrayList<GeoPoint> list = null;
			for (int c=0; c<components.getLength(); c++){
				Element component = (Element)components.item(c);
				String nodeName = component.getTagName();
				if ("name".equals(nodeName)){
					name = getChildText(component);
				} else if ("description".equals(nodeName)){
						description = getChildText(component);
				} else if ("Point".equals(nodeName)){
					NodeList coordinates = placemark.getElementsByTagName("coordinates");
					if (coordinates.getLength()>0){
						type = POINT;
						list = getCoordinates((Element)coordinates.item(0));
					}
				} else if ("LineString".equals(nodeName)){
					NodeList coordinates = placemark.getElementsByTagName("coordinates");
					if (coordinates.getLength()>0){
						type = LINE_STRING;
						list = getCoordinates((Element)coordinates.item(0));
					}
				} else if ("Polygon".equals(nodeName)){
					//TODO...
				}
			} //for placemark components
			
			//Fill the overlay with the placemark geometry:
			switch (type){
			case POINT:
				if (list.size()>0){
					ExtendedOverlayItem item = new ExtendedOverlayItem(name, description, list.get(0), context);
					item.setMarkerHotspot(OverlayItem.HotspotPlace.BOTTOM_CENTER);
					item.setMarker(marker);
					kmlPointsOverlay.addItem(item);
				}
				break;
			case LINE_STRING:
				Paint paint = new Paint();
				paint.setColor(0x90101010);
				paint.setStyle(Paint.Style.STROKE);
				paint.setStrokeWidth(5);
				PathOverlay lineStringOverlay = new PathOverlay(0, context);
				lineStringOverlay.setPaint(paint);
				for (GeoPoint point:list)
					lineStringOverlay.addPoint(point);
				BoundingBoxE6 bb = BoundingBoxE6.fromGeoPoints(list);
				folderOverlay.add(lineStringOverlay, bb);
				break;
			default:
				break;
			}
		} //for all placemarks

		if (kmlPointsOverlay.size()>0){
			folderOverlay.add(kmlPointsOverlay, kmlPointsOverlay.getBoundingBoxE6());
		}
	}
}
