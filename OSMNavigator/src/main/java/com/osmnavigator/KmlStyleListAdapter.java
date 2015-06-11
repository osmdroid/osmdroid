package com.osmnavigator;

import java.util.ArrayList;
import java.util.HashMap;

import org.osmdroid.bonuspack.kml.Style;
import org.osmdroid.bonuspack.kml.StyleSelector;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class KmlStyleListAdapter extends BaseAdapter {

	protected ArrayList<String> mStyleList;
	
	public KmlStyleListAdapter(Context context, ArrayList<String> styleList) {
	    mStyleList = styleList;
	}
	
	@Override public Object getItem(int itemId) {
		return mStyleList.get(itemId);
	}
	
	@Override public int getCount() {
		return mStyleList.size();
	}
	
	@Override public long getItemId(int itemId) {
	    return itemId;
	}
	
	@Override public View getView(int position, View convertView, ViewGroup viewGroup) {
		String item = (String)getItem(position);
	    if (convertView == null) {
	        LayoutInflater inflater = (LayoutInflater) viewGroup.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	        convertView = inflater.inflate(R.layout.kml_list_item, null);
	    }
	    TextView itemText = (TextView) convertView.findViewById(R.id.listItemTxt);
	    itemText.setText(item);
	    
		ImageView img = (ImageView)convertView.findViewById(R.id.listItemImg);
		Style style = MapActivity.mKmlDocument.getStyle(item);
		if (style != null && style.mIconStyle != null && style.mIconStyle.mIcon != null)
			img.setImageDrawable(style.mIconStyle.getFinalIcon(convertView.getContext()));
		else 
			img.setImageDrawable(null);
		
	    return convertView;
	}
	
	@Override public boolean hasStableIds() {
	    return false;
	}
	
}
