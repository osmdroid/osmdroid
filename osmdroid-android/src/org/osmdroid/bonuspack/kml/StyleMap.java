package org.osmdroid.bonuspack.kml;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Handling of a KML StyleMap. 
 * @author M.Kergall
 */
public class StyleMap extends StyleSelector implements Parcelable {
	
	protected HashMap<String, String> mPairs;
	
	/** default constructor */
	public StyleMap(){
		super();
		mPairs = new HashMap<String, String>();
	}
	
	public void setPair(String key, String styleUrl){
		mPairs.put(key, styleUrl);
	}
	
	public String getStyleUrl(String key){
		return mPairs.get(key);
	}

	/** @return the "normal" Style referenced, if any - null if none */
	public Style getNormalStyle(KmlDocument doc){
		return doc.getStyle(getStyleUrl("normal"));
	}
	
	@Override public void writeAsKML(Writer writer, String styleId){
		try {
			writer.write("<StyleMap id=\'"+styleId+"\'>\n");
			for (HashMap.Entry<String, String> entry : mPairs.entrySet()) {
				String key = entry.getKey();
				String styleUrl = entry.getValue();
				writer.write("<Pair><key>"+key+"</key><styleUrl>"+styleUrl+"</styleUrl></Pair>\n");
			}
			writer.write("</StyleMap>\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	//Parcelable implementation ------------
	
	@Override public int describeContents() {
		return 0;
	}

	@Override public void writeToParcel(Parcel out, int flags) {
		//out.writeMap(mPairs); - not recommended in the Google JavaDoc, for mysterious reasons, so: 
		out.writeInt(mPairs.size());
		for(String key : mPairs.keySet()){
			out.writeString(key);
			out.writeString(mPairs.get(key));
		}
	}
	
	public static final Parcelable.Creator<StyleMap> CREATOR = new Parcelable.Creator<StyleMap>() {
		@Override public StyleMap createFromParcel(Parcel source) {
			return new StyleMap(source);
		}
		@Override public StyleMap[] newArray(int size) {
			return new StyleMap[size];
		}
	};
	
	public StyleMap(Parcel in){
		int size = in.readInt();
		mPairs = new HashMap<String, String>(size);
		for(int i=0; i<size; i++){
			String key = in.readString();
			String value = in.readString();
			mPairs.put(key,value);
		}
	}
}
