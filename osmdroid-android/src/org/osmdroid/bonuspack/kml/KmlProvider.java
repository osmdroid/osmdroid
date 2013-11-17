package org.osmdroid.bonuspack.kml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.util.GeoPoint;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;

/**
 * Helper class to read and parse KML content, and build KML structure. 
 * Supports KML Point, LineString and Polygon placemarks. 
 * Supports KML Folder hierarchy. 
 * Supports styles for LineString and Polygon (not for Point). 
 * Supports colorMode (normal, random)
 * 
 * TODO: 
 * Objects ids
 * 
 * @author M.Kergall
 */
public class KmlProvider {

	protected HashMap<String, Style> mStyles;
	
	public KmlProvider(){
		mStyles = new HashMap<String, Style>();
	}
	
	public Style getStyle(String styleUrl){
		return mStyles.get(styleUrl);
	}
	
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
	
	protected KmlObject parsePlacemark(Element element){
		KmlObject kmlObject = new KmlObject();
		List<Element> components = getChildrenByTagName(element, "*");
		kmlObject.mObjectType =  KmlObject.NO_SHAPE;
		for (Element component:components){
			String nodeName = component.getTagName();
			if ("styleUrl".equals(nodeName)){
				String styleUrl = getChildText(component);
				kmlObject.mStyle = styleUrl.substring(1); //remove the #
			} else if ("name".equals(nodeName)){
					kmlObject.mName = getChildText(component);
			} else if ("description".equals(nodeName)){
				kmlObject.mDescription = getChildText(component);
			} else if ("Point".equals(nodeName)){
				NodeList coordinates = component.getElementsByTagName("coordinates");
				if (coordinates.getLength()>0){
					kmlObject.mObjectType = KmlObject.POINT;
					kmlObject.mCoordinates = getCoordinates((Element)coordinates.item(0));
					kmlObject.mBB = BoundingBoxE6.fromGeoPoints(kmlObject.mCoordinates);
				}
			} else if ("LineString".equals(nodeName)){
				NodeList coordinates = component.getElementsByTagName("coordinates");
				if (coordinates.getLength()>0){
					kmlObject.mObjectType = KmlObject.LINE_STRING;
					kmlObject.mCoordinates = getCoordinates((Element)coordinates.item(0));
					kmlObject.mBB = BoundingBoxE6.fromGeoPoints(kmlObject.mCoordinates);
				}
			} else if ("Polygon".equals(nodeName)){
				//TODO: distinguish outerBoundaryIs and innerBoundaryIs
				NodeList coordinates = component.getElementsByTagName("coordinates");
				if (coordinates.getLength()>0){
					kmlObject.mObjectType = KmlObject.POLYGON;
					kmlObject.mCoordinates = getCoordinates((Element)coordinates.item(0));
					kmlObject.mBB = BoundingBoxE6.fromGeoPoints(kmlObject.mCoordinates);
				}
			}
		}
		return kmlObject;
	}
	
	protected KmlObject parseFolder(Element kmlElement){
		KmlObject kmlObject = new KmlObject();
		kmlObject.mObjectType = KmlObject.FOLDER;
		
		List<Element> children = getChildrenByTagName(kmlElement, "*");
		for (Element child:children){
			String childName = child.getTagName();
			if ("Document".equals(childName) || "Folder".equals(childName)){
				KmlObject subFolder = parseFolder(child);
				kmlObject.add(subFolder);
			} else if ("Placemark".equals(childName)){
				KmlObject placemark = parsePlacemark(child);
				kmlObject.add(placemark);
			} else if ("name".equals(childName)){
				kmlObject.mName = getChildText(child);
			} else if ("description".equals(childName)){
				kmlObject.mDescription = getChildText(child);
			} else if ("visibility".equals(childName)){
				String sVisibility = getChildText(child);
				kmlObject.mVisibility = ("1".equals(sVisibility));
			} else if ("open".equals(childName)){
				String sOpen = getChildText(child);
				kmlObject.mOpen = ("1".equals(sOpen));
			}
		}
		return kmlObject;
	}
	
	protected void parseStyles(Element kmlRoot){
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
	 * Parse a KML document to build the KML structure
	 * @param kmlDomRoot root
	 * return KML object which is the root of the whole structure
	 */
	public KmlObject parseRoot(Element kmlDomRoot){
		parseStyles(kmlDomRoot);
		//Recursively handle the element and all its sub-folders:
		KmlObject result = parseFolder(kmlDomRoot);
		return result;
	}
}
