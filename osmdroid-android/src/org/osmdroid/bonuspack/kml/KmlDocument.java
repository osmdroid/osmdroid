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
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

/**
 * Object handling a whole KML document. 
 * Features are stored in the kmlRoot attribute. In most cases, kmlRoot will be a Folder. 
 * Shared components (like styles) are stored in specific attributes. 
 * This is the entry point to read and save KML content. 
 * 
 * Supports KML Point, LineString and Polygon placemarks. 
 * Supports KML Document and Folder hierarchy. 
 * Supports LineStyle, PolyStyle, and partially IconStyle. 
 * Supports colorMode (normal, random)
 * 
 * @see KmlObject
 * 
 * @author M.Kergall
 */
public class KmlDocument implements Parcelable {

	/** root for KML features contained in this document */
	public KmlObject kmlRoot;
	/** list of shared Styles in this document */
	protected HashMap<String, Style> mStyles;
	protected int mMaxStyleId;
	
	/** default constructor, with the kmlRoot as an empty Folder */
	public KmlDocument(){
		mStyles = new HashMap<String, Style>();
		mMaxStyleId = 0;
		kmlRoot = new KmlObject();
		kmlRoot.createAsFolder();
	}
	
	/** @return the shared Style associated to the styleId, or null if none */
	public Style getStyle(String styleId){
		return mStyles.get(styleId);
	}
	
	/** put the style in the list of shared Styles, associated to its styleId */
	public void putStyle(String styleId, Style style){
		//Check if maxStyleId needs an update:
		try {
			int id = Integer.parseInt(styleId);
			mMaxStyleId = Math.max(mMaxStyleId, id);
		} catch (NumberFormatException e){
			//styleId was not a number: nothing to do
		}
		mStyles.put(styleId, style);
	}
	
	/**
	 * Add the Style in the shared Styles
	 * @param style to add
	 * @return the unique styleId assigned for this style
	 */
	public String addStyle(Style style){
		mMaxStyleId++;
		String newId = ""+mMaxStyleId;
		putStyle(newId, style);
		return newId;
	}
	
	protected static GeoPoint parseKmlCoord(String input){
		/* TODO try to find a more efficient way, reducing object creation... 
		Scanner s = new Scanner(input).useDelimiter("\\,");
		try {
			double lon = s.nextDouble();
			double lat = s.nextDouble();
			double alt = 0.0; //s.nextDouble();
			GeoPoint p = new GeoPoint(lat, lon, alt);
			return p;
		} catch (Exception e) {
			return null;
		}
		*/
		String[] coords = input.split(",");
		try {
			double lon = Double.parseDouble(coords[0]);
			double lat = Double.parseDouble(coords[1]);
			double alt = (coords.length == 3 ? Double.parseDouble(coords[2]) : 0.0);
			GeoPoint p = new GeoPoint(lat, lon, alt);
			return p;
		} catch (NumberFormatException e) {
			return null;
		}
	}
	
	/** KML coordinates are: lon,lat{,alt} tuples separated by spaces. */
	protected static ArrayList<GeoPoint> parseKmlCoordinates(String input){
		String[] splitted = input.split("\\s");
		ArrayList<GeoPoint> coordinates = new ArrayList<GeoPoint>(splitted.length);
		for (int i=0; i<splitted.length; i++){
			GeoPoint p = parseKmlCoord(splitted[i]);
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
		kmlRoot = handler.mKmlRoot;
		return handler.mKmlRoot;
	}

	/**
	 * Get the default path for KML file on Android: on the external storage, in a "kml" directory. 
	 * Creates the directory if necessary. 
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
		kmlRoot = handler.mKmlRoot;
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
			mKmlRoot.createAsFolder();
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
				mKmlCurrentObject.createAsFolder();
				mKmlCurrentObject.mId = attributes.getValue("id");
				mKmlStack.add(mKmlCurrentObject); //push on stack
			} else if (localName.equals("Placemark")) {
				mKmlCurrentObject = new KmlObject();
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
				mKmlCurrentObject.mCoordinates = parseKmlCoordinates(mString);
				mKmlCurrentObject.mBB = BoundingBoxE6.fromGeoPoints(mKmlCurrentObject.mCoordinates);
			} else if (localName.equals("styleUrl")){
				if (mString.charAt(0) == '#')
					mKmlCurrentObject.mStyle = mString.substring(1); //remove the #
				else //external url: keep as is:
					mKmlCurrentObject.mStyle = mString;
			} else if (localName.equals("color")){
				if (mCurrentStyle != null)
					mColorStyle.color = ColorStyle.parseKMLColor(mString);
			} else if (localName.equals("colorMode")){
				if (mCurrentStyle != null)
					mColorStyle.colorMode = (mString.equals("random")?ColorStyle.MODE_RANDOM:ColorStyle.MODE_NORMAL);
			} else if (localName.equals("width")){
				if (mCurrentStyle != null)
					mCurrentStyle.outlineWidth = Float.parseFloat(mString);
			} else if (localName.equals("href")){
				if (mCurrentStyle != null && mCurrentStyle.iconColorStyle != null)
					mCurrentStyle.iconHref = mString;
			} else if (localName.equals("Style")){
				putStyle(mCurrentStyleId, mCurrentStyle);
				mCurrentStyle = null;
			}
		}
		
	}

	/**
	 * save the document as a KML file on writer
	 * @param writer
	 * @return false if error
	 */
	public boolean saveAsKML(Writer writer){
		try {
			writer.write("<?xml version='1.0' encoding='UTF-8'?>\n");
			writer.write("<kml xmlns='http://www.opengis.net/kml/2.2'>\n");
			boolean result = true;
			if (kmlRoot != null)
				result = kmlRoot.writeAsKML(writer, true, this);
			writer.write("</kml>\n");
			return result;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	public void writeStyles(Writer writer){
		for (HashMap.Entry<String, Style> entry : mStyles.entrySet()) {
			String styleId = entry.getKey();
			Style style = entry.getValue();
			style.writeAsKML(writer, styleId);
		}
	}
	
	/**
	 * Save the document as a KML file
	 * @param file full path of the destination file
	 * @return false if error
	 */
	public boolean saveAsKML(File file){
		try {
			FileWriter fw = new FileWriter(file);
			BufferedWriter writer = new BufferedWriter(fw);
			boolean result = saveAsKML(writer);
			writer.close();
			return result;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	//Parcelable implementation ------------

	@Override public int describeContents() {
		return 0;
	}

	@Override public void writeToParcel(Parcel out, int flags) {
		out.writeParcelable(kmlRoot, flags);
		//write styles map:
		//out.writeMap(mStyles); - not recommended in the Google JavaDoc, for mysterious reasons, so: 
		out.writeInt(mStyles.size());
		for(String key : mStyles.keySet()){
			out.writeString(key);
			out.writeParcelable(mStyles.get(key), flags);
		}
		out.writeInt(mMaxStyleId);
	}
	
	public static final Parcelable.Creator<KmlDocument> CREATOR = new Parcelable.Creator<KmlDocument>() {
		@Override public KmlDocument createFromParcel(Parcel source) {
			return new KmlDocument(source);
		}
		@Override public KmlDocument[] newArray(int size) {
			return new KmlDocument[size];
		}
	};
	
	public KmlDocument(Parcel in){
		kmlRoot = in.readParcelable(KmlObject.class.getClassLoader());
		//mStyles = in.readHashMap(Style.class.getClassLoader());
		int size = in.readInt();
		mStyles = new HashMap<String, Style>(size);
		for(int i=0; i<size; i++){
			String key = in.readString();
			Style value = in.readParcelable(Style.class.getClassLoader());
			mStyles.put(key,value);
		}
		mMaxStyleId = in.readInt();
	}

}
