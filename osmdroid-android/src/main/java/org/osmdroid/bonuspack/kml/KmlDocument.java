package org.osmdroid.bonuspack.kml;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.osmdroid.bonuspack.utils.BonusPackHelper;
import org.osmdroid.bonuspack.utils.HttpConnection;
import org.osmdroid.util.GeoPoint;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonWriter;
import android.os.Environment;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

/**
 * Object handling a whole KML document. 
 * This is the entry point to read, handle and save KML content. <br>
 * Features are stored in the kmlRoot attribute, which is a KmlFolder. <br>
 * Also contains the Shared Styles, referenced in Features using a styleId. <br>
 * 
 * Supports the following KML Geometry: Point, LineString, Polygon and MultiGeometry. <br>
 * Supports KML Document and Folder hierarchy. <br>
 * Supports NetworkLink. <br>
 * Supports GroundOverlay. <br>
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
	public KmlFolder mKmlRoot;
	/** Shared Styles in this document. String key is the styleId. */
	protected HashMap<String, StyleSelector> mStyles;
	protected int mMaxStyleId;
	
	/** Local File that has been loaded. null if this is not a local file. */
	protected File mLocalFile;

	/** default constructor, with the kmlRoot as an empty Folder */
	public KmlDocument(){
		mStyles = new HashMap<String, StyleSelector>();
		mMaxStyleId = 0;
		mKmlRoot = new KmlFolder();
		mLocalFile = null;
	}

	/** @return the Shared Styles */
	public HashMap<String, StyleSelector> getStyles(){
		return mStyles;
	}

	/** @return the list of all Shared Styles ids */
	public String[] getStylesList(){
		Set<String> set = mStyles.keySet();
		String[] array = new String[0];
		return set.toArray(array);
	}
	
	/** @return the Shared Style associated to the styleId, or null if none.
	 *  If this is a StyleMap, returns its "normal" Style (if any). */
	public Style getStyle(String styleId){
		StyleSelector s = mStyles.get(styleId);
		if (s == null)
			return null;
		else if (s instanceof StyleMap)
			return ((StyleMap)s).getNormalStyle(this);
		else //if (s instanceof Style)
			return (Style)s;
	}
	
	/** put the StyleSelector (Style or StyleMap) in the list of Shared Styles, associated to its styleId */
	public void putStyle(String styleId, StyleSelector styleSelector){
		//Check if maxStyleId needs an update:
		try {
			int id = Integer.parseInt(styleId);
			mMaxStyleId = Math.max(mMaxStyleId, id);
		} catch (NumberFormatException e){
			//styleId was not a number: nothing to do
		}
		mStyles.put(styleId, styleSelector);
	}
	
	/**
	 * Add the StyleSelector in the Shared Styles
	 * @param styleSelector to add
	 * @return the unique styleId assigned for this style
	 */
	public String addStyle(StyleSelector styleSelector){
		mMaxStyleId++;
		String newId = ""+mMaxStyleId;
		putStyle(newId, styleSelector);
		return newId;
	}
	
	/** @return the local File that has been opened (KML, KMZ or GeoJSON), or null if this is not a local file */
	public File getLocalFile(){
		return mLocalFile;
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
		return coordinates;
	}
	
	/**
	 * Parse a KML document from a url, and build the KML structure in kmlRoot. 
	 * If the KML file has a "Document" node, kmlRoot will be a Folder "mapping" to this Document. 
	 * In all other cases, kmlRoot will be a Folder, containing the features of the KML file. 
	 * @param url
	 * @return true if OK, false if any error. 
	 */
	public boolean parseKMLUrl(String url){
		Log.d(BonusPackHelper.LOG_TAG, "KmlProvider.parseUrl:"+url);
		HttpConnection connection = new HttpConnection();
		connection.doGet(url);
		InputStream stream = connection.getStream();
		boolean ok;
		if (stream == null){
			ok = false;
		} else {
			ok = parseKMLStream(stream, null);
		}
		connection.close();
		//Log.d(BonusPackHelper.LOG_TAG, "KmlProvider.parseUrl - end");
		return ok;
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
	 * @see #parseUrl
	 */
	public boolean parseKMLFile(File file){
		mLocalFile = file;
		Log.d(BonusPackHelper.LOG_TAG, "KmlProvider.parseKMLFile:"+mLocalFile.getAbsolutePath());
		InputStream stream = null;
		boolean ok;
		try {
			stream = new BufferedInputStream(new FileInputStream(mLocalFile));
			ok = parseKMLStream(stream, null);
			stream.close();
		} catch (Exception e){
			e.printStackTrace();
			ok = false;
		}
		//Log.d(BonusPackHelper.LOG_TAG, "KmlProvider.parseFile - end");
		return ok;
	}
	
	/** 
	 * Parse a local KMZ document. 
	 * @param file full file path
	 * @return true if OK. 
	 */
	public boolean parseKMZFile(File file){
		mLocalFile = file;
		Log.d(BonusPackHelper.LOG_TAG, "KmlProvider.parseKMZFile:"+mLocalFile.getAbsolutePath());
		try {
			ZipFile kmzFile = new ZipFile(mLocalFile);
			String rootFileName = null;
			//Iterate in the KMZ to find the first ".kml" file:
			Enumeration<? extends ZipEntry> list = kmzFile.entries();
			while (list.hasMoreElements() && rootFileName == null){
				ZipEntry ze = list.nextElement();
				String name = ze.getName();
				if (name.endsWith(".kml") && !name.contains("/"))
					rootFileName = name;
			}
			boolean result;
			if (rootFileName != null){
				ZipEntry rootEntry = kmzFile.getEntry(rootFileName);
				InputStream stream = kmzFile.getInputStream(rootEntry);
				Log.d(BonusPackHelper.LOG_TAG, "KML root:"+rootFileName);
				result = parseKMLStream(stream, kmzFile);
			} else {
				Log.d(BonusPackHelper.LOG_TAG, "No .kml entry found.");
				result = false;
			}
			kmzFile.close();
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * Parse a KML content from an InputStream. 
	 * @param stream the InputStream
	 * @param kmzContainer KMZ file containing this KML file - or null if not applicable. 
	 * @return true if OK, false if any error. 
	 */
	public boolean parseKMLStream(InputStream stream, ZipFile kmzContainer){
		KmlSaxHandler handler = new KmlSaxHandler(mLocalFile, kmzContainer);
		boolean ok;
		try {
			SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
			parser.parse(stream, handler);
			mKmlRoot = handler.mKmlRoot;
			ok = true;
		} catch (Exception e) {
			e.printStackTrace();
			ok = false;
		}
		return ok;
	}
	
	// KmlSaxHandler -------------
	
	protected class KmlSaxHandler extends DefaultHandler {
		
		private StringBuilder mStringBuilder = new StringBuilder(1024);
		private KmlFeature mKmlCurrentFeature;
		private KmlGroundOverlay mKmlCurrentGroundOverlay; //if GroundOverlay, pointer to mKmlCurrentFeature
		private ArrayList<KmlFeature> mKmlFeatureStack;
		private KmlGeometry mKmlCurrentGeometry;
		private ArrayList<KmlGeometry> mKmlGeometryStack;
		public KmlFolder mKmlRoot;
		Style mCurrentStyle;
		String mCurrentStyleId;
		StyleMap mCurrentStyleMap; //for StyleSelector: "normal" or "highlight"
		String mCurrentStyleKey;
		ColorStyle mColorStyle;
		String mDataName;
		boolean mIsNetworkLink;
		boolean mIsInnerBoundary;
		File mFile; //to get the path of relative sub-files
		ZipFile mKMZFile;
		double mNorth, mEast, mSouth, mWest;
		
		public KmlSaxHandler(File file, ZipFile kmzContainer){
			mFile = file;
			mKMZFile = kmzContainer;
			mKmlRoot = new KmlFolder();
			mKmlFeatureStack = new ArrayList<KmlFeature>();
			mKmlFeatureStack.add(mKmlRoot);
			mKmlGeometryStack = new ArrayList<KmlGeometry>();
			mIsNetworkLink = false;
			mIsInnerBoundary = false;
		}
		
		protected void loadNetworkLink(String href, ZipFile kmzContainer){
			KmlDocument subDocument = new KmlDocument();
			boolean ok;
			if (href.startsWith("http://") || href.startsWith("https://") )
				ok = subDocument.parseKMLUrl(href);
			else if (kmzContainer == null){
				File subFile = new File(mFile.getParent()+'/'+href);
				ok = subDocument.parseKMLFile(subFile);
			} else {
				try {
					final ZipEntry fileEntry = kmzContainer.getEntry(href);
					InputStream stream = kmzContainer.getInputStream(fileEntry);
					Log.d(BonusPackHelper.LOG_TAG, "Load NetworkLink:"+href);
					ok = subDocument.parseKMLStream(stream, kmzContainer);
				} catch (Exception e) {
					ok = false;
				}
			}
			if (ok){
				//add subDoc root to the current feature, which is -normally- the NetworkLink:
				((KmlFolder)mKmlCurrentFeature).add(subDocument.mKmlRoot);
				//add all subDocument styles to mStyles:
				mStyles.putAll(subDocument.mStyles);
			} else {
				Log.e(BonusPackHelper.LOG_TAG, "Error reading NetworkLink:"+href);
			}
		}
		
		public void startElement(String uri, String localName, String name,
				Attributes attributes) throws SAXException {
			if (localName.equals("Document")){
				mKmlCurrentFeature = mKmlRoot; //If there is a Document, it will be the root. 
				mKmlCurrentFeature.mId = attributes.getValue("id");
			} else if (localName.equals("Folder")){
				mKmlCurrentFeature = new KmlFolder();
				mKmlCurrentFeature.mId = attributes.getValue("id");
				mKmlFeatureStack.add(mKmlCurrentFeature); //push on stack
			} else if (localName.equals("NetworkLink")){
				mKmlCurrentFeature = new KmlFolder();
				mKmlCurrentFeature.mId = attributes.getValue("id");
				mKmlFeatureStack.add(mKmlCurrentFeature); //push on stack
				mIsNetworkLink = true;
			} else if (localName.equals("GroundOverlay")){
				mKmlCurrentGroundOverlay = new KmlGroundOverlay();
				mKmlCurrentFeature = mKmlCurrentGroundOverlay;
				mKmlCurrentFeature.mId = attributes.getValue("id");
				mKmlFeatureStack.add(mKmlCurrentFeature); //push on stack
			} else if (localName.equals("Placemark")) {
				mKmlCurrentFeature = new KmlPlacemark();
				mKmlCurrentFeature.mId = attributes.getValue("id");
				mKmlFeatureStack.add(mKmlCurrentFeature); //push on Feature stack
			} else if (localName.equals("Point")){
				mKmlCurrentGeometry = new KmlPoint();
				mKmlGeometryStack.add(mKmlCurrentGeometry); //push on Geometry stack
			} else if (localName.equals("LineString")){
				mKmlCurrentGeometry = new KmlLineString();
				mKmlGeometryStack.add(mKmlCurrentGeometry);
			} else if (localName.equals("Polygon")){
				mKmlCurrentGeometry = new KmlPolygon();
				mKmlGeometryStack.add(mKmlCurrentGeometry);
			} else if (localName.equals("innerBoundaryIs")) {
				mIsInnerBoundary = true;
			} else if (localName.equals("MultiGeometry")){
				mKmlCurrentGeometry = new KmlMultiGeometry();
				mKmlGeometryStack.add(mKmlCurrentGeometry);
			} else if (localName.equals("Style")) {
				mCurrentStyle = new Style();
				mCurrentStyleId = attributes.getValue("id");
			} else if (localName.equals("StyleMap")) {
				mCurrentStyleMap = new StyleMap();
				mCurrentStyleId = attributes.getValue("id");
			} else if (localName.equals("LineStyle")) {
				mCurrentStyle.mLineStyle = new LineStyle();
				mColorStyle = mCurrentStyle.mLineStyle;
			} else if (localName.equals("PolyStyle")) {
				mCurrentStyle.mPolyStyle = new ColorStyle();
				mColorStyle = mCurrentStyle.mPolyStyle;
			} else if (localName.equals("IconStyle")) {
				mCurrentStyle.mIconStyle = new IconStyle();
				mColorStyle = mCurrentStyle.mIconStyle;
			} else if (localName.equals("hotSpot")){
				if (mCurrentStyle != null && mColorStyle != null && mColorStyle instanceof IconStyle){
					mCurrentStyle.mIconStyle.mHotSpot = new HotSpot(
							Float.parseFloat(attributes.getValue("x")),
							Float.parseFloat(attributes.getValue("y")),
							attributes.getValue("xunits"),
							attributes.getValue("yunits")
					);
					/*
					if ("fraction".equals(attributes.getValue("xunits")))
						mCurrentStyle.mIconStyle.mHotSpotX = Float.parseFloat(attributes.getValue("x"));
					if ("fraction".equals(attributes.getValue("yunits")))
						mCurrentStyle.mIconStyle.mHotSpotY = Float.parseFloat(attributes.getValue("y"));
					*/
				}
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
			} else if (localName.equals("Folder") || localName.equals("Placemark") 
					|| localName.equals("NetworkLink") || localName.equals("GroundOverlay")) {
				//this was a Feature:
				KmlFolder parent = (KmlFolder)mKmlFeatureStack.get(mKmlFeatureStack.size()-2); //get parent
				parent.add(mKmlCurrentFeature); //add current in its parent
				mKmlFeatureStack.remove(mKmlFeatureStack.size()-1); //pop current from stack
				mKmlCurrentFeature = mKmlFeatureStack.get(mKmlFeatureStack.size()-1); //set current to top of stack
				if (localName.equals("NetworkLink"))
					mIsNetworkLink = false;
				else if (localName.equals("GroundOverlay"))
					mKmlCurrentGroundOverlay = null;
			} else if (localName.equals("innerBoundaryIs")){
				mIsInnerBoundary = false;
			} else if (localName.equals("Point") || localName.equals("LineString") || localName.equals("Polygon")
					|| localName.equals("MultiGeometry") ){
				//this was a Geometry:
				if (mKmlGeometryStack.size() == 1){
					//no MultiGeometry parent: add this Geometry in the current Feature:
					((KmlPlacemark)mKmlCurrentFeature).mGeometry = mKmlCurrentGeometry;
					mKmlGeometryStack.remove(mKmlGeometryStack.size()-1); //pop current from stack
					mKmlCurrentGeometry = null;
				} else {
					KmlMultiGeometry parent = (KmlMultiGeometry)mKmlGeometryStack.get(mKmlGeometryStack.size()-2); //get parent
					parent.addItem(mKmlCurrentGeometry); //add current in its parent
					mKmlGeometryStack.remove(mKmlGeometryStack.size()-1); //pop current from stack
					mKmlCurrentGeometry = mKmlGeometryStack.get(mKmlGeometryStack.size()-1); //set current to top of stack
				}
			} else if (localName.equals("name")){
				mKmlCurrentFeature.mName = mStringBuilder.toString();
			} else if (localName.equals("description")){
				mKmlCurrentFeature.mDescription = mStringBuilder.toString();
			} else if (localName.equals("visibility")){
				mKmlCurrentFeature.mVisibility = ("1".equals(mStringBuilder.toString()));
			} else if (localName.equals("open")){
				mKmlCurrentFeature.mOpen = ("1".equals(mStringBuilder.toString()));
			} else if (localName.equals("coordinates")){
				if (mKmlCurrentFeature instanceof KmlPlacemark){
					if (!mIsInnerBoundary){
						mKmlCurrentGeometry.mCoordinates = parseKmlCoordinates(mStringBuilder.toString());
						//mKmlCurrentFeature.mBB = BoundingBoxE6.fromGeoPoints(mKmlCurrentGeometry.mCoordinates);
					} else { //inside a Polygon innerBoundaryIs element: new hole
						KmlPolygon polygon = (KmlPolygon)mKmlCurrentGeometry;
						if (polygon.mHoles == null)
							polygon.mHoles = new ArrayList<ArrayList<GeoPoint>>();
						ArrayList<GeoPoint> hole = parseKmlCoordinates(mStringBuilder.toString());
						polygon.mHoles.add(hole);
					}
				}
			} else if (localName.equals("styleUrl")){
				String styleUrl;
				if (mStringBuilder.charAt(0) == '#')
					styleUrl = mStringBuilder.substring(1); //remove the #
				else //external url: keep as is:
					styleUrl = mStringBuilder.toString();
				
				if (mCurrentStyleMap != null){
					mCurrentStyleMap.setPair(mCurrentStyleKey, styleUrl);
				} else if (mKmlCurrentFeature != null){
					mKmlCurrentFeature.mStyle = styleUrl;
				}
			} else if (localName.equals("key")){
				mCurrentStyleKey = mStringBuilder.toString();
			} else if (localName.equals("color")){
				if (mCurrentStyle != null) {
					if (mColorStyle != null)
						mColorStyle.mColor = ColorStyle.parseKMLColor(mStringBuilder.toString());
				} else if (mKmlCurrentGroundOverlay != null){
					mKmlCurrentGroundOverlay.mColor = ColorStyle.parseKMLColor(mStringBuilder.toString());
				}
			} else if (localName.equals("colorMode")){
				if (mCurrentStyle != null && mColorStyle != null)
					mColorStyle.mColorMode = (mStringBuilder.toString().equals("random")?ColorStyle.MODE_RANDOM:ColorStyle.MODE_NORMAL);
			} else if (localName.equals("width")){
				if (mCurrentStyle != null && mColorStyle != null && mColorStyle instanceof LineStyle)
					mCurrentStyle.mLineStyle.mWidth = Float.parseFloat(mStringBuilder.toString());
			} else if (localName.equals("scale")){
				if (mCurrentStyle != null && mColorStyle != null && mColorStyle instanceof IconStyle){
					mCurrentStyle.mIconStyle.mScale = Float.parseFloat(mStringBuilder.toString());
				}
			} else if (localName.equals("heading")){
				if (mCurrentStyle != null && mColorStyle != null && mColorStyle instanceof IconStyle){
					mCurrentStyle.mIconStyle.mHeading = Float.parseFloat(mStringBuilder.toString());
				}
			} else if (localName.equals("href")) {
				if (mCurrentStyle != null && mColorStyle != null && mColorStyle instanceof IconStyle) {
					//href of an Icon in an IconStyle:
					String href = mStringBuilder.toString();
					mCurrentStyle.setIcon(href, mFile, mKMZFile);
				} else if (mIsNetworkLink) {
					//href of a NetworkLink:
					String href = mStringBuilder.toString();
					loadNetworkLink(href, mKMZFile);
				} else if (mKmlCurrentGroundOverlay != null) {
					//href of a GroundOverlay Icon:
					mKmlCurrentGroundOverlay.setIcon(mStringBuilder.toString(), mFile, mKMZFile);
				}
			} else if (localName.equals("LineStyle")){
				mColorStyle = null;
			} else if (localName.equals("PolyStyle")){
				mColorStyle = null;
			} else if (localName.equals("IconStyle")){
				mColorStyle = null;
			} else if (localName.equals("Style")){
				if (mCurrentStyleId != null)
					putStyle(mCurrentStyleId, mCurrentStyle);
				else {
					mCurrentStyleId = addStyle(mCurrentStyle);
				}
				if (mKmlCurrentFeature != null && mKmlCurrentFeature != mKmlRoot){
					//this is an inline style. Set its style id to the KmlObject container:
					mKmlCurrentFeature.mStyle = mCurrentStyleId;
				}
				mCurrentStyle = null;
				mCurrentStyleId = null;
			} else if (localName.equals("StyleMap")){
				if (mCurrentStyleId != null)
					putStyle(mCurrentStyleId, mCurrentStyleMap);
				//TODO: inline StyleMap ???
				mCurrentStyleMap = null;
				mCurrentStyleId = null;
				mCurrentStyleKey = null;
			} else if (localName.equals("north")){
				mNorth = Double.parseDouble(mStringBuilder.toString());
			} else if (localName.equals("south")){
				mSouth = Double.parseDouble(mStringBuilder.toString());
			} else if (localName.equals("east")){
				mEast = Double.parseDouble(mStringBuilder.toString());
			} else if (localName.equals("west")){
				mWest = Double.parseDouble(mStringBuilder.toString());
			} else if (localName.equals("rotation")){
				mKmlCurrentGroundOverlay.mRotation = Float.parseFloat(mStringBuilder.toString());
			} else if (localName.equals("LatLonBox")){
				if (mKmlCurrentGroundOverlay != null){
					mKmlCurrentGroundOverlay.setLatLonBox(mNorth, mSouth, mEast, mWest);
				}
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
			if (mKmlRoot != null)
				result = mKmlRoot.writeAsKML(writer, true, this);
			writer.write("</kml>\n");
			return result;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	public void writeKMLStyles(Writer writer){
		for (Map.Entry<String, StyleSelector> entry : mStyles.entrySet()) {
			String styleId = entry.getKey();
			StyleSelector styleSelector = entry.getValue();
			styleSelector.writeAsKML(writer, styleId);
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
			//FileWriter fw = new FileWriter(file);
			OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");
			BufferedWriter writer = new BufferedWriter(out, 8192);
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
		JsonObject json = mKmlRoot.asGeoJSON(true);
		if (json == null)
			return false;
		try {
			Gson gson = new GsonBuilder().create();
			JsonWriter jsonWriter = new JsonWriter(writer);
			gson.toJson(json, jsonWriter);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * Save the document as a GeoJSON file
	 * @param file full path of the destination file
	 * @return false if error
	 * @see <a href="http://geojson.org">GeoJSON</a>
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
	
	/** Parse a GeoJSON object. */
	public boolean parseGeoJSON(JsonObject json){
		KmlFeature feature = KmlFeature.parseGeoJSON(json);
		if (feature instanceof KmlFolder)
			mKmlRoot = (KmlFolder)feature;
		else {
			mKmlRoot = new KmlFolder();
			mKmlRoot.add(feature);
		}
		return true;
	}
	
	/** Parse a GeoJSON String */
	public boolean parseGeoJSON(String jsonString){
		try {
			JsonParser parser = new JsonParser();
			JsonElement json = parser.parse(jsonString);
			return parseGeoJSON(json.getAsJsonObject());
		} catch (JsonSyntaxException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/** Parse a GeoJSON File */
	public boolean parseGeoJSON(File file){
		mLocalFile = file;
		try {
			FileInputStream input = new FileInputStream(mLocalFile);
			JsonParser parser = new JsonParser();
			JsonElement json = parser.parse(new InputStreamReader(input));
			input.close();
			return parseGeoJSON(json.getAsJsonObject());
		} catch (Exception e) {
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
		out.writeParcelable(mKmlRoot, flags);
		//write styles map:
		//out.writeMap(mStyles); - not recommended in the Google JavaDoc, for mysterious reasons, so: 
		out.writeInt(mStyles.size());
		for(String key : mStyles.keySet()){
			out.writeString(key);
			out.writeParcelable(mStyles.get(key), flags);
		}
		out.writeInt(mMaxStyleId);
		if (mLocalFile != null)
			out.writeString(mLocalFile.getAbsolutePath());
		else
			out.writeString("");
	}
	
	public static final Creator<KmlDocument> CREATOR = new Creator<KmlDocument>() {
		@Override public KmlDocument createFromParcel(Parcel source) {
			return new KmlDocument(source);
		}
		@Override public KmlDocument[] newArray(int size) {
			return new KmlDocument[size];
		}
	};
	
	public KmlDocument(Parcel in){
		mKmlRoot = in.readParcelable(KmlFeature.class.getClassLoader());
		//mStyles = in.readHashMap(Style.class.getClassLoader());
		int size = in.readInt();
		mStyles = new HashMap<String, StyleSelector>(size);
		for(int i=0; i<size; i++){
			String key = in.readString();
			Style value = in.readParcelable(Style.class.getClassLoader());
			mStyles.put(key,value);
		}
		mMaxStyleId = in.readInt();
		String filePath = in.readString();
		if (filePath.equals(""))
			mLocalFile = null;
		else 
			mLocalFile = new File(filePath);
	}

}
