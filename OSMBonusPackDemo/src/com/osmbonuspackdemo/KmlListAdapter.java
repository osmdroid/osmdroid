package com.osmbonuspackdemo;

import org.osmdroid.bonuspack.kml.KmlObject;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

public class KmlListAdapter extends BaseAdapter {

    protected KmlObject mRoot;
    
    public KmlListAdapter(Context context, KmlObject root) {
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
    	KmlObject item = (KmlObject)getItem(position);
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) viewGroup.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.kml_list_item, null);
        }
        TextView itemText = (TextView) convertView.findViewById(R.id.listItemTxt);
        itemText.setText(item.mName);
        
        //Handle checkbox:
        CheckBox checkBoxIsVisible = (CheckBox)convertView.findViewById(R.id.listItemCheckbox);
        checkBoxIsVisible.setChecked(mRoot.mItems.get(position).mVisibility);
        if (checkBoxIsVisible != null) {
	        checkBoxIsVisible.setOnClickListener(new OnClickListener(){
				@Override public void onClick(View view) {
					int position = (Integer)view.getTag();
					KmlObject item = mRoot.mItems.get(position);
					item.mVisibility = ((CheckBox)view).isChecked();
				}
	        });
	        checkBoxIsVisible.setTag(position);
        }
        
		ImageView img = (ImageView)convertView.findViewById(R.id.listItemImg);
		switch (item.mObjectType){
		case KmlObject.FOLDER:
			img.setImageResource(R.drawable.moreinfo_arrow);
			break;
		case KmlObject.POINT:
			img.setImageResource(R.drawable.marker_kml_point);
			break;
		case KmlObject.LINE_STRING:
			img.setImageResource(R.drawable.kml_icon_linestring);
			break;
		case KmlObject.POLYGON:
			img.setImageResource(R.drawable.kml_icon_polygon);
			break;
		default:
			break;
		}
		
        return convertView;
    }

    @Override public boolean hasStableIds() {
        return false;
    }

}
