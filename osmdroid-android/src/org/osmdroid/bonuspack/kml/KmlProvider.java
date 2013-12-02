package org.osmdroid.bonuspack.kml;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
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
import android.os.Environment;
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
	
	protected ArrayList<GeoPoint> parseCoordinates(String input){
		String[] splitted = input.split("\\s");
		ArrayList<GeoPoint> coordinates = new ArrayList<GeoPoint>(splitted.length);
		for (int i=0; i<splitted.length; i++){
			GeoPoint p = parseGeoPoint(splitted[i]);
			if (p != null)
				coordinates.add(p);
		}
		return coordinates;
	}
	
	/* nice utils for DOM parsing - to keep somewhere else
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
	*/
	
	/**
	 * Parse a KML document from a url, to build the KML structure. 
	 * @param url
	 * @return KML object which is the root of the whole structure - or null if any error. 
	 */
	public KmlObject parseUrl(String url){
		Log.d(BonusPackHelper.LOG_TAG, "KmlProvider.parseUrl:"+url);
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
		//Log.d(BonusPackHelper.LOG_TAG, "KmlProvider.parseUrl - end");
		return handler.mKmlRoot;
	}

	/**
	 * Get the default path for KML file on Android: on the external storage, in a "kml" directory. Creates the directory if necessary. 
	 * @param fileName
	 * @return full path, as a File, or null if error. 
	 */
	public File getDefaultPathForAndroid(String fileName){
		try {
			File path = new File(Environment.getExternalStorageDirectory(), "kml");
			path.mkdir();
			File file = new File(path.getAbsolutePath(), fileName);
			return file;
		} catch (NullPointerException e){
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Parse a KML document from a file, to build the KML structure. 
	 * @param fileName
	 * @return KML object which is the root of the whole structure - or null if any error. 
	 */
	public KmlObject parseFile(File file){
		Log.d(BonusPackHelper.LOG_TAG, "KmlProvider.parseFile:"+file.getAbsolutePath());
		KmlSaxHandler handler = new KmlSaxHandler();
		InputStream stream = null;
		try {
			stream = new BufferedInputStream(new FileInputStream(file));
			SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
			parser.parse(stream, handler);
			stream.close();
		} catch (Exception e){
			e.printStackTrace();
			handler.mKmlRoot = null;
		}
		//Log.d(BonusPackHelper.LOG_TAG, "KmlProvider.parseFile - end");
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
				mCurrentStyle.outlineColorStyle = new ColorStyle();
				mColorStyle = mCurrentStyle.outlineColorStyle;
			} else if (localName.equals("PolyStyle")) {
				mCurrentStyle.fillColorStyle = new ColorStyle();
				mColorStyle = mCurrentStyle.fillColorStyle;
			} else if (localName.equals("IconStyle")) {
				mCurrentStyle.iconColorStyle = new ColorStyle();
				mColorStyle = mCurrentStyle.iconColorStyle;
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
				mKmlCurrentObject.mCoordinates = parseCoordinates(mString);
				mKmlCurrentObject.mBB = BoundingBoxE6.fromGeoPoints(mKmlCurrentObject.mCoordinates);
			} else if (localName.equals("styleUrl")){
				mKmlCurrentObject.mStyle = mString.substring(1); //remove the #
			} else if (localName.equals("color")){
				if (mCurrentStyle != null)
					mColorStyle.color = mColorStyle.parseKMLColor(mString);
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

	/**
	 * save kmlRoot as a KML file on writer
	 * @param writer
	 * @param kmlRoot
	 * @return false if error
	 */
	public boolean saveAsKML(Writer writer, KmlObject kmlRoot){
		try {
			writer.write("<?xml version='1.0' encoding='UTF-8'?>\n");
			writer.write("<kml xmlns='http://www.opengis.net/kml/2.2'>\n");
			boolean result = kmlRoot.writeAsKML(writer, true, mStyles);
			writer.write("</kml>\n");
			return result;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Save kmlRoot as a KML file on file
	 * @param file
	 * @param kmlRoot
	 * @return false if error
	 */
	public boolean saveAsKML(File file, KmlObject kmlRoot){
		try {
			FileWriter fw = new FileWriter(file);
			BufferedWriter writer = new BufferedWriter(fw);
			boolean result = saveAsKML(writer, kmlRoot);
			writer.close();
			return result;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}
	
}
