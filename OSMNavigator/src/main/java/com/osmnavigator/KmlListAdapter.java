package com.osmnavigator;

import org.osmdroid.bonuspack.kml.KmlFeature;
import org.osmdroid.bonuspack.kml.KmlFolder;
import org.osmdroid.bonuspack.kml.KmlGeometry;
import org.osmdroid.bonuspack.kml.KmlGroundOverlay;
import org.osmdroid.bonuspack.kml.KmlLineString;
import org.osmdroid.bonuspack.kml.KmlMultiGeometry;
import org.osmdroid.bonuspack.kml.KmlPlacemark;
import org.osmdroid.bonuspack.kml.KmlPoint;
import org.osmdroid.bonuspack.kml.KmlPolygon;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class KmlListAdapter extends BaseAdapter {

    protected KmlFolder mRoot;
    
    public KmlListAdapter(Context context, KmlFolder root) {
        mRoot = root;
    }

    @Override public Object getItem(int itemId) {
    	return mRoot.mItems.get(itemId);
    }

    @Override public int getCount() {
    	if (mRoot.mItems == null)
    		return 0;
    	else
    		return mRoot.mItems.size();
    }

    @Override public long getItemId(int itemId) {
        return itemId;
    }

    @Override public View getView(int position, View convertView, ViewGroup viewGroup) {
    	KmlFeature item = (KmlFeature)getItem(position);
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) viewGroup.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.kml_list_item, null);
        }
        TextView itemText = (TextView) convertView.findViewById(R.id.listItemTxt);
        itemText.setText(item.mName);
        
        //Handle checkbox:
        /*
        CheckBox checkBoxIsVisible = (CheckBox)convertView.findViewById(R.id.listItemCheckbox);
        checkBoxIsVisible.setChecked(mRoot.mItems.get(position).mVisibility);
        if (checkBoxIsVisible != null) {
	        checkBoxIsVisible.setOnClickListener(new OnClickListener(){
				@Override public void onClick(View view) {
					int position = (Integer)view.getTag();
					KmlFeature item = mRoot.mItems.get(position);
					item.mVisibility = ((CheckBox)view).isChecked();
				}
	        });
	        checkBoxIsVisible.setTag(position);
        }
        */
        
		ImageView img = (ImageView)convertView.findViewById(R.id.listItemImg);
		if (item instanceof KmlFolder) {
			img.setImageResource(R.drawable.moreinfo_arrow);
		} else if (item instanceof KmlPlacemark){
			KmlGeometry geometry = ((KmlPlacemark)item).mGeometry;
			if (geometry instanceof KmlPoint)
				img.setImageResource(R.drawable.marker_kml_point);
			else if (geometry instanceof KmlLineString)
				img.setImageResource(R.drawable.kml_icon_linestring);
			else if (geometry instanceof KmlPolygon)
				img.setImageResource(R.drawable.kml_icon_polygon);
			else if (geometry instanceof KmlMultiGeometry)
				img.setImageResource(R.drawable.kml_icon_multigeometry);
			else
				img.setImageDrawable(null);
		} else if (item instanceof KmlGroundOverlay){
			img.setImageResource(R.drawable.kml_icon_groundoverlay);
		} else
			img.setImageDrawable(null);
		
        return convertView;
    }

    @Override public boolean hasStableIds() {
        return false;
    }

}
