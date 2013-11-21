package org.osmdroid.bonuspack.kml;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.osmdroid.bonuspack.utils.BonusPackHelper;
import org.osmdroid.bonuspack.utils.HttpConnection;
import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.util.GeoPoint;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import android.util.Log;

/**
 * Helper class to read and parse KML content, and build KML structure. 
 * Supports KML Point, LineString and Polygon placemarks. 
 * Supports KML Folder hierarchy. 
 * Supports styles for LineString and Polygon (but not for Point). 
 * Supports colorMode (normal, random)
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
	/*
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
	*/
	
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
	/*
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
	*/
	/*
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
	*/
	/*
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
	*/
	
	/**
	 * Parse a KML document to build the KML structure
	 * @param kmlDomRoot root
	 * return KML object which is the root of the whole structure
	 */
	/*
	public KmlObject parseRoot(Element kmlDomRoot){
		Log.d(BonusPackHelper.LOG_TAG, "KmlProvider.parse - start");
		parseStyles(kmlDomRoot);
		//Recursively handle the element and all its sub-folders:
		KmlObject result = parseFolder(kmlDomRoot);
		Log.d(BonusPackHelper.LOG_TAG, "KmlProvider.parse - end");
		return result;
	}
	*/
	
	/**
	 * Parse a KML document to build the KML structure - SAX implementation
	 * @param url
	 * @return KML object which is the root of the whole structure - or null if any error. 
	 */
	public KmlObject parse(String url){
		Log.d(BonusPackHelper.LOG_TAG, "KmlProvider.parse:"+url);
		HttpConnection connection = new HttpConnection();
		connection.doGet(url);
		InputStream stream = connection.getStream();
		KmlSaxHandler handler = new KmlSaxHandler();
		if (stream == null){
			handler.mKmlRoot = null;
		} else {
			try {
				SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
				parser.parse(stream, handler);
			} catch (Exception e) {
				e.printStackTrace();
				handler.mKmlRoot = null;
			}
		}
		connection.close();
		Log.d(BonusPackHelper.LOG_TAG, "KmlProvider.parse - end");
		return handler.mKmlRoot;
	}
	
	// KmlSaxHandler -------------
	
	class KmlSaxHandler extends DefaultHandler {
		
		private String mString;
		private KmlObject mKmlCurrentObject;
		private ArrayList<KmlObject> mKmlStack;
		KmlObject mKmlRoot;
		Style mCurrentStyle;
		String mCurrentStyleId;
		ColorStyle mColorStyle;
		
		public KmlSaxHandler(){
			mKmlRoot = new KmlObject();
			mKmlRoot.mObjectType = KmlObject.FOLDER;
			mKmlStack = new ArrayList<KmlObject>();
			mKmlStack.add(mKmlRoot);
		}
		
		public void startElement(String uri, String localName, String name,
				Attributes attributes) throws SAXException {
			if (localName.equals("Document")){
				mKmlCurrentObject = mKmlRoot; //If there is a Document, it will be the root. 
				mKmlCurrentObject.mId = attributes.getValue("id");
			} else if (localName.equals("Folder")){
				mKmlCurrentObject = new KmlObject();
				mKmlCurrentObject.mObjectType = KmlObject.FOLDER;
				mKmlCurrentObject.mId = attributes.getValue("id");
				mKmlStack.add(mKmlCurrentObject); //push on stack
			} else if (localName.equals("Placemark")) {
				mKmlCurrentObject = new KmlObject();
				mKmlCurrentObject.mObjectType = KmlObject.NO_SHAPE;
				mKmlCurrentObject.mId = attributes.getValue("id");
				mKmlStack.add(mKmlCurrentObject); //push on stack
			} else if (localName.equals("Style")) {
				mCurrentStyle = new Style();
				mCurrentStyleId = attributes.getValue("id");
			} else if (localName.equals("LineStyle")) {
				mColorStyle = mCurrentStyle.outlineColorStyle;
			} else if (localName.equals("PolyStyle")) {
				mColorStyle = mCurrentStyle.fillColorStyle;
			}
			mString = new String();
		}
		
		public @Override void characters(char[] ch, int start, int length)
				throws SAXException {
			String chars = new String(ch, start, length);
			mString = mString.concat(chars);
		}
		
		public void endElement(String uri, String localName, String name)
				throws SAXException {
			if (localName.equals("Document")){
				//Document is the root, nothing to do. 
			} else if (localName.equals("Folder") || localName.equals("Placemark")) {
				KmlObject parent = mKmlStack.get(mKmlStack.size()-2); //get parent
				parent.add(mKmlCurrentObject); //add current in its parent
				mKmlStack.remove(mKmlStack.size()-1); //pop current from stack
				mKmlCurrentObject = mKmlStack.get(mKmlStack.size()-1); //set current to top of stack
			} else if (localName.equals("Point")){
				mKmlCurrentObject.mObjectType = KmlObject.POINT;
			} else if (localName.equals("LineString")){
				mKmlCurrentObject.mObjectType = KmlObject.LINE_STRING;
			} else if (localName.equals("Polygon")){
				mKmlCurrentObject.mObjectType = KmlObject.POLYGON;
			} else if (localName.equals("name")){
				mKmlCurrentObject.mName = mString;
			} else if (localName.equals("description")){
				mKmlCurrentObject.mDescription = mString;
			} else if (localName.equals("visibility")){
				mKmlCurrentObject.mVisibility = ("1".equals(mString));
			} else if (localName.equals("open")){
				mKmlCurrentObject.mOpen = ("1".equals(mString));
			} else if (localName.equals("coordinates")){
				String[] splitted = mString.split("\\s");
				mKmlCurrentObject.mCoordinates = new ArrayList<GeoPoint>(splitted.length);
				for (int i=0; i<splitted.length; i++){
					GeoPoint p = parseGeoPoint(splitted[i]);
					if (p != null)
						mKmlCurrentObject.mCoordinates.add(p);
				}
				mKmlCurrentObject.mBB = BoundingBoxE6.fromGeoPoints(mKmlCurrentObject.mCoordinates);
			} else if (localName.equals("styleUrl")){
				mKmlCurrentObject.mStyle = mString.substring(1); //remove the #
			} else if (localName.equals("color")){
				if (mCurrentStyle != null)
					mColorStyle.color = mColorStyle.parseColor(mString);
			} else if (localName.equals("colorMode")){
				if (mCurrentStyle != null)
					mColorStyle.colorMode = (mString.equals("random")?ColorStyle.MODE_RANDOM:ColorStyle.MODE_NORMAL);
			} else if (localName.equals("width")){
				if (mCurrentStyle != null)
					mCurrentStyle.outlineWidth = Float.parseFloat(mString);
			} else if (localName.equals("Style")){
				mStyles.put(mCurrentStyleId, mCurrentStyle);
				mCurrentStyle = null;
			}
		}
		
	}
	
}
