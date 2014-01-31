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
import java.util.LinkedList;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.osmdroid.bonuspack.utils.BonusPackHelper;
import org.osmdroid.bonuspack.utils.HttpConnection;
import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.util.GeoPoint;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

/**
 * Object handling a whole KML document. 
 * Features are stored in the kmlRoot attribute. In most cases, kmlRoot will be a Folder. 
 * Shared components (like styles) are stored in specific attributes. 
 * This is the entry point to read, handle and save KML content. <br>
 * 
 * Supports the following KML Geometry: Point, LineString, Polygon, GroundOverlay. <br>
 * Supports KML Document and Folder hierarchy. <br>
 * Supports NetworkLink. <br>
 * Supports LineStyle, PolyStyle and IconStyle - shared and inline. <br>
 * Supports colorMode: normal, random<br>
 * Supports ExtendedData inside Features, with support for Data elements and SimpleData elements. 
 * In all cases, values are stored as Java String, there is no handling of Schema definition. <br>
 * 
 * @see KmlFeature
 * @see Style
 * 
 * @author M.Kergall
 */
public class KmlDocument implements Parcelable {

	/** the root of KML features contained in this document */
	public KmlFeature kmlRoot;
	/** list of shared Styles in this document */
	protected HashMap<String, Style> mStyles;
	protected int mMaxStyleId;
	
	/** default constructor, with the kmlRoot as an empty Folder */
	public KmlDocument(){
		mStyles = new HashMap<String, Style>();
		mMaxStyleId = 0;
		kmlRoot = new KmlFeature();
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
	
	/** similar to GeoPoint.fromInvertedDoubleString, with exceptions handling */
	protected static GeoPoint parseKmlCoord(String input){
		int end1 = input.indexOf(',');
		int end2 = input.indexOf(',', end1+1);
		try {
			if (end2 == -1){
				double lon = Double.parseDouble(input.substring(0, end1));
				double lat = Double.parseDouble(input.substring(end1+1, input.length()));
				return new GeoPoint(lat, lon);
			} else {
				double lon = Double.parseDouble(input.substring(0, end1));
				double lat = Double.parseDouble(input.substring(end1+1, end2));
				double alt = Double.parseDouble(input.substring(end2+1, input.length()));
				return new GeoPoint(lat, lon, alt);
			}
		} catch (NumberFormatException e) {
			return null;
		} catch (IndexOutOfBoundsException e) {
			return null;
		}
	}
	
	protected static boolean parseKmlCoord2(String input, int tuple[]){
		int end1 = input.indexOf(',');
		int end2 = input.indexOf(',', end1+1);
		try {
			if (end2 == -1){
				tuple[1] = (int)(Double.parseDouble(input.substring(0, end1))*1E6); //lon
				tuple[0] = (int)(Double.parseDouble(input.substring(end1+1, input.length()))*1E6); //lat
			} else {
				tuple[1] = (int)(Double.parseDouble(input.substring(0, end1))*1E6);
				tuple[0] = (int)(Double.parseDouble(input.substring(end1+1, end2))*1E6);
				tuple[2] = (int)(Double.parseDouble(input.substring(end2+1, input.length()))*1E6);
			}
			return true;
		} catch (NumberFormatException e) {
			return false;
		} catch (IndexOutOfBoundsException e) {
			return false;
		}
	}
	
	/** KML coordinates are: lon,lat{,alt} tuples separated by separators (space, tab, cr). */
	protected static ArrayList<GeoPoint> parseKmlCoordinates(String input){
		LinkedList<GeoPoint> tmpCoords = new LinkedList<GeoPoint>();
		int i = 0;
		int tupleStart = 0;
		int length = input.length();
		boolean startReadingTuple = false;
		while (i<length){
			char c = input.charAt(i);
			if (c==' '|| c=='\n' || c=='\t'){
				if (startReadingTuple){ //just ending coords portion:
					String tuple = input.substring(tupleStart, i);
					GeoPoint p = parseKmlCoord(tuple);
					if (p != null)
						tmpCoords.add(p);
					startReadingTuple = false;
				}
			} else { //data
				if (!startReadingTuple){ //just ending space portion
					startReadingTuple = true;
					tupleStart = i;
				}
				if (i == length-1){ //at the end => handle last tuple:
					String tuple = input.substring(tupleStart, i+1);
					GeoPoint p = parseKmlCoord(tuple);
					if (p != null)
						tmpCoords.add(p);
				}
			}
			i++;
		}
		ArrayList<GeoPoint> coordinates = new ArrayList<GeoPoint>(tmpCoords.size());
		coordinates.addAll(tmpCoords);
		//Various attempts to optimize - without significant result
		/*
		String[] splitted = input.split("\\s+");
		ArrayList<GeoPoint> coordinates = new ArrayList<GeoPoint>(splitted.length);
		for (int i=0; i<splitted.length; i++){
			GeoPoint p = parseKmlCoord(splitted[i]);
			if (p != null)
				coordinates.add(p);
		}
		*/
		/*
		String[] splitted = input.split("\\s+");
		int[][] coords = new int[splitted.length][3];
		int end = 0;
		for (int i=0; i<splitted.length; i++){
			boolean ok = parseKmlCoord2(splitted[i], coords[end]);
			if (ok)
				end++;
		}
		ArrayList<GeoPoint> coordinates = new ArrayList<GeoPoint>(10);
		if (end > 0)
			coordinates.add(new GeoPoint(coords[0][0], coords[0][1], coords[0][2]));
		*/
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
	 * Parse a KML document from a url, and build the KML structure in kmlRoot. 
	 * If the KML file has a "Document" node, kmlRoot will be a Folder "mapping" to this Document. 
	 * In all other cases, kmlRoot will be a Folder, containing the features of the KML file. 
	 * (In all cases, kmlRoot will be a Folder object)
	 * @param url
	 * @return true if OK, false if any error. 
	 */
	public boolean parseUrl(String url){
		Log.d(BonusPackHelper.LOG_TAG, "KmlProvider.parseUrl:"+url);
		HttpConnection connection = new HttpConnection();
		connection.doGet(url);
		InputStream stream = connection.getStream();
		if (stream == null){
			kmlRoot = null;
		} else {
			parseStream(stream, url);
		}
		connection.close();
		//Log.d(BonusPackHelper.LOG_TAG, "KmlProvider.parseUrl - end");
		return (kmlRoot != null);
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
	 * @param file full file path
	 * @return true if OK, false if any error. 
	 * @see parseUrl
	 */
	public boolean parseFile(File file){
		Log.d(BonusPackHelper.LOG_TAG, "KmlProvider.parseFile:"+file.getAbsolutePath());
		InputStream stream = null;
		try {
			stream = new BufferedInputStream(new FileInputStream(file));
			parseStream(stream, file.getAbsolutePath());
			stream.close();
		} catch (Exception e){
			e.printStackTrace();
			kmlRoot = null;
		}
		//Log.d(BonusPackHelper.LOG_TAG, "KmlProvider.parseFile - end");
		return (kmlRoot != null);
	}
	
	/**
	 * Parse a KML content from an InputStream. 
	 * @param stream the InputStream
	 * @param fullFilePath of the content, which is used inside the parser to handle "relative" files, to determine their full file path. 
	 * Note that relative files are supported only for regular files. 
	 * @return true if OK, false if any error. 
	 */
	public boolean parseStream(InputStream stream, String fullFilePath){
		KmlSaxHandler handler = new KmlSaxHandler(fullFilePath);
		try {
			SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
			parser.parse(stream, handler);
			kmlRoot = handler.mKmlRoot;
		} catch (Exception e) {
			e.printStackTrace();
			kmlRoot = null;
		}
		return (kmlRoot != null);
	}
	
	// KmlSaxHandler -------------
	
	class KmlSaxHandler extends DefaultHandler {
		
		private StringBuilder mStringBuilder = new StringBuilder(1024);
		private KmlFeature mKmlCurrentFeature;
		private ArrayList<KmlFeature> mKmlStack;
		KmlFeature mKmlRoot;
		Style mCurrentStyle;
		String mCurrentStyleId;
		ColorStyle mColorStyle;
		String mDataName;
		boolean mIsNetworkLink;
		boolean mIsInnerBoundary;
		String mFullPath; //to get the path of relative sub-files
		double mNorth, mEast, mSouth, mWest;
		
		public KmlSaxHandler(String fullPath){
			mFullPath = fullPath;
			mKmlRoot = new KmlFeature();
			mKmlRoot.createAsFolder();
			mKmlStack = new ArrayList<KmlFeature>();
			mKmlStack.add(mKmlRoot);
			mIsNetworkLink = false;
			mIsInnerBoundary = false;
		}
		
		public void startElement(String uri, String localName, String name,
				Attributes attributes) throws SAXException {
			if (localName.equals("Document")){
				mKmlCurrentFeature = mKmlRoot; //If there is a Document, it will be the root. 
				mKmlCurrentFeature.mId = attributes.getValue("id");
			} else if (localName.equals("Folder")){
				mKmlCurrentFeature = new KmlFeature();
				mKmlCurrentFeature.createAsFolder();
				mKmlCurrentFeature.mId = attributes.getValue("id");
				mKmlStack.add(mKmlCurrentFeature); //push on stack
			} else if (localName.equals("NetworkLink")){
				mKmlCurrentFeature = new KmlFeature();
				mKmlCurrentFeature.createAsFolder();
				mKmlCurrentFeature.mId = attributes.getValue("id");
				mKmlStack.add(mKmlCurrentFeature); //push on stack
				mIsNetworkLink = true;
			} else if (localName.equals("Placemark")) {
				mKmlCurrentFeature = new KmlFeature();
				mKmlCurrentFeature.mId = attributes.getValue("id");
				mKmlStack.add(mKmlCurrentFeature); //push on stack
			} else if (localName.equals("Point")){
				mKmlCurrentFeature.mObjectType = KmlFeature.POINT;
			} else if (localName.equals("LineString")){
				mKmlCurrentFeature.mObjectType = KmlFeature.LINE_STRING;
			} else if (localName.equals("Polygon")){
				mKmlCurrentFeature.mObjectType = KmlFeature.POLYGON;
			} else if (localName.equals("GroundOverlay")){
				mKmlCurrentFeature.mObjectType = KmlFeature.GROUND_OVERLAY;
			} else if (localName.equals("innerBoundaryIs")) {
				mIsInnerBoundary = true;
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
			} else if (localName.equals("Data") || localName.equals("SimpleData")) {
				mDataName = attributes.getValue("name");
			}
			mStringBuilder.setLength(0);
		}
		
		public @Override void characters(char[] ch, int start, int length)
				throws SAXException {
			mStringBuilder.append(ch, start, length);
		}
		
		public void endElement(String uri, String localName, String name)
				throws SAXException {
			if (localName.equals("Document")){
				//Document is the root, nothing to do. 
			} else if (localName.equals("Folder") || localName.equals("Placemark") || localName.equals("NetworkLink")) {
				KmlFeature parent = mKmlStack.get(mKmlStack.size()-2); //get parent
				parent.add(mKmlCurrentFeature); //add current in its parent
				mKmlStack.remove(mKmlStack.size()-1); //pop current from stack
				mKmlCurrentFeature = mKmlStack.get(mKmlStack.size()-1); //set current to top of stack
				if (localName.equals("NetworkLink"))
					mIsNetworkLink = false;
			} else if (localName.equals("innerBoundaryIs")){
				mIsInnerBoundary = false;
			} else if (localName.equals("name")){
				mKmlCurrentFeature.mName = mStringBuilder.toString();
			} else if (localName.equals("description")){
				mKmlCurrentFeature.mDescription = mStringBuilder.toString();
			} else if (localName.equals("visibility")){
				mKmlCurrentFeature.mVisibility = ("1".equals(mStringBuilder.toString()));
			} else if (localName.equals("open")){
				mKmlCurrentFeature.mOpen = ("1".equals(mStringBuilder.toString()));
			} else if (localName.equals("coordinates")){
				if (!mIsInnerBoundary){
					mKmlCurrentFeature.mCoordinates = parseKmlCoordinates(mStringBuilder.toString());
					mKmlCurrentFeature.mBB = BoundingBoxE6.fromGeoPoints(mKmlCurrentFeature.mCoordinates);
				} else { //inside a Polygon innerBoundaryIs element: new hole
					if (mKmlCurrentFeature.mHoles == null)
						mKmlCurrentFeature.mHoles = new ArrayList<ArrayList<GeoPoint>>();
					ArrayList<GeoPoint> hole = parseKmlCoordinates(mStringBuilder.toString());
					mKmlCurrentFeature.mHoles.add(hole);
					//no need for bounding box update, as holes must be inside the polygon outer boundary. 
				}
			} else if (localName.equals("styleUrl")){
				if (mStringBuilder.charAt(0) == '#')
					mKmlCurrentFeature.mStyle = mStringBuilder.substring(1); //remove the #
				else //external url: keep as is:
					mKmlCurrentFeature.mStyle = mStringBuilder.toString();
			} else if (localName.equals("color")){
				if (mCurrentStyle != null) {
					mColorStyle.color = ColorStyle.parseKMLColor(mStringBuilder.toString());
				} else if (mKmlCurrentFeature.mObjectType == KmlFeature.GROUND_OVERLAY){
					mKmlCurrentFeature.mColor = ColorStyle.parseKMLColor(mStringBuilder.toString());
				}
			} else if (localName.equals("colorMode")){
				if (mCurrentStyle != null)
					mColorStyle.colorMode = (mStringBuilder.toString().equals("random")?ColorStyle.MODE_RANDOM:ColorStyle.MODE_NORMAL);
			} else if (localName.equals("width")){
				if (mCurrentStyle != null)
					mCurrentStyle.outlineWidth = Float.parseFloat(mStringBuilder.toString());
			} else if (localName.equals("scale")){
				if (mCurrentStyle != null){
					mCurrentStyle.mIconScale = Float.parseFloat(mStringBuilder.toString());
				}
			} else if (localName.equals("href")){
				if (mCurrentStyle != null && mCurrentStyle.iconColorStyle != null){
					//href of an Icon:
					String href = mStringBuilder.toString();
					if (!href.startsWith("http://")){
						File file = new File(mFullPath);
						href = file.getParent()+'/'+href;
					}
					mCurrentStyle.setIcon(href);
				} else if (mIsNetworkLink){
					//href of a NetworkLink:
					String href = mStringBuilder.toString();
					KmlDocument subDocument = new KmlDocument();
					if (href.startsWith("http://"))
						subDocument.parseUrl(href);
					else {
						File file = new File(mFullPath);
						File subFile = new File(file.getParent()+'/'+href);
						subDocument.parseFile(subFile);
					}
					if (subDocument.kmlRoot != null){
						//add subDoc root to the current feature, which is -normally- the NetworkLink:
						mKmlCurrentFeature.add(subDocument.kmlRoot);
						//add subDoc styles to mStyles:
						mStyles.putAll(subDocument.mStyles);
					} else {
						Log.e(BonusPackHelper.LOG_TAG, "Error reading NetworkLink:"+href);
					}
				} else if (mKmlCurrentFeature.mObjectType == KmlFeature.GROUND_OVERLAY){
					//href of a GroundOverlay Icon:
					String href = mStringBuilder.toString();
					if (!href.startsWith("http://")){
						File file = new File(mFullPath);
						href = file.getParent()+'/'+href;
						mKmlCurrentFeature.mIcon = BitmapFactory.decodeFile(href);
					} else {
						mKmlCurrentFeature.mIcon = BonusPackHelper.loadBitmap(href);
					}
				}
			} else if (localName.equals("north")){
				mNorth = Double.parseDouble(mStringBuilder.toString());
			} else if (localName.equals("south")){
				mSouth = Double.parseDouble(mStringBuilder.toString());
			} else if (localName.equals("east")){
				mEast = Double.parseDouble(mStringBuilder.toString());
			} else if (localName.equals("west")){
				mWest = Double.parseDouble(mStringBuilder.toString());
			} else if (localName.equals("rotation")){
				mKmlCurrentFeature.mRotation = Float.parseFloat(mStringBuilder.toString());
			} else if (localName.equals("LatLonBox")){
				if (mKmlCurrentFeature.mObjectType == KmlFeature.GROUND_OVERLAY){
					mKmlCurrentFeature.mCoordinates = new ArrayList<GeoPoint>(2);
					mKmlCurrentFeature.mCoordinates.add(new GeoPoint(mNorth, mWest));
					mKmlCurrentFeature.mCoordinates.add(new GeoPoint(mSouth, mEast));
					mKmlCurrentFeature.mBB = BoundingBoxE6.fromGeoPoints(mKmlCurrentFeature.mCoordinates);
				}
			} else if (localName.equals("Style")){
				if (mCurrentStyleId != null)
					putStyle(mCurrentStyleId, mCurrentStyle);
				else {
					mCurrentStyleId = addStyle(mCurrentStyle);
					if (mKmlCurrentFeature != null){
						//this is an inline style. Set its style id to the KmlObject container:
						mKmlCurrentFeature.mStyle = mCurrentStyleId;
					}
				}
				mCurrentStyle = null;
			} else if (localName.equals("SimpleData")){
				//We don't check the schema from SchemaData. We just pick the name and the value from SimpleData:
				mKmlCurrentFeature.setExtendedData(mDataName, mStringBuilder.toString());
				mDataName = null;
			} else if (localName.equals("value")){
				mKmlCurrentFeature.setExtendedData(mDataName, mStringBuilder.toString());
				mDataName = null;
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

	public void writeKMLStyles(Writer writer){
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
			Log.d(BonusPackHelper.LOG_TAG, "Saving "+file.getAbsolutePath());
			FileWriter fw = new FileWriter(file);
			BufferedWriter writer = new BufferedWriter(fw, 8192);
			boolean result = saveAsKML(writer);
			writer.close();
			Log.d(BonusPackHelper.LOG_TAG, "Saved.");
			return result;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	public boolean saveAsGeoJSON(Writer writer){
		return kmlRoot.writeAsGeoJSON(writer, true);
	}
	
	/**
	 * Save the document as a GeoJSON file
	 * @param file full path of the destination file
	 * @return false if error
	 * @see http://geojson.org
	 */
	public boolean saveAsGeoJSON(File file){
		try {
			FileWriter fw = new FileWriter(file);
			BufferedWriter writer = new BufferedWriter(fw, 8192);
			boolean result = saveAsGeoJSON(writer);
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

	/** WARNING - Parcel mechanism doesn't work with very large objects. Refer to Android doc, and use carefully. */
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
		kmlRoot = in.readParcelable(KmlFeature.class.getClassLoader());
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
