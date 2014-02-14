package com.osmbonuspackdemo;

import org.osmdroid.bonuspack.kml.KmlFeature;
import org.osmdroid.bonuspack.kml.LineStyle;
import org.osmdroid.bonuspack.kml.Style;
import org.osmdroid.bonuspack.kml.ColorStyle;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

public class KmlTreeActivity extends Activity {

	KmlListAdapter listAdapter;
	ListView listView;
	KmlFeature currentKmlFeature; //feature currently edited. 
	KmlFeature kmlClipboard; //link to the global KML clipboard. 
	protected static final int KML_TREE_REQUEST = 3;
	int mItemPosition; //last item opened
	EditText eHeader, eDescription, eOutlineColor, eFillColor;
	LineStyle mLineStyle;  //direct pointer to the LineStyle, or null. 
	ColorStyle mPolyStyle; //direct pointer to the PolyStyle, or null. 
	
	@Override protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.kml_main);
		listView = (ListView) findViewById(R.id.listviewKml);
		registerForContextMenu(listView);
		
		currentKmlFeature = MapActivity.mKmlStack.peek();
		kmlClipboard = MapActivity.mKmlClipboard;
		
		eHeader = (EditText)findViewById(R.id.name);
		eHeader.setText(currentKmlFeature.mName);
		
		eDescription = (EditText)findViewById(R.id.description);
		eDescription.setText(currentKmlFeature.mDescription);
		
		Style style = null;
		if (currentKmlFeature.mStyle != null)
			style = MapActivity.mKmlDocument.getStyle(currentKmlFeature.mStyle);
		
		eOutlineColor = (EditText)findViewById(R.id.outlineColor);
		if ((currentKmlFeature.mObjectType == KmlFeature.LINE_STRING || currentKmlFeature.mObjectType == KmlFeature.POLYGON) && style!=null){
			mLineStyle = style.mLineStyle;
			eOutlineColor.setText(mLineStyle.colorAsAndroidString());
		} else {
			LinearLayout lOutlineColorLayout = (LinearLayout)findViewById(R.id.outlineColorLayout);
			lOutlineColorLayout.setVisibility(View.GONE);
		}
		
		eFillColor = (EditText)findViewById(R.id.fillColor);
		if (currentKmlFeature.mObjectType == KmlFeature.POLYGON && style!=null){
			mPolyStyle = style.mPolyStyle;
			eFillColor.setText(mPolyStyle.colorAsAndroidString());
		} else {
			LinearLayout lFillColorLayout = (LinearLayout)findViewById(R.id.fillColorLayout);
			lFillColorLayout.setVisibility(View.GONE);
		}
		
		listAdapter = new KmlListAdapter(this, currentKmlFeature);
		
		// setting list adapter
		listView.setAdapter(listAdapter);
		
		// Listview on child click listener
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override public void onItemClick(AdapterView<?> arg0, View view, int position, long index) {
				mItemPosition = position;
				KmlFeature item = currentKmlFeature.mItems.get(position);
				Intent myIntent = new Intent(view.getContext(), KmlTreeActivity.class);
				//myIntent.putExtra("KML", item);
				MapActivity.mKmlStack.push(item.clone());
				startActivityForResult(myIntent, KML_TREE_REQUEST);
			}
		});
		
		Button btnOk = (Button) findViewById(R.id.btnOK);
		btnOk.setOnClickListener( new View.OnClickListener() {
		    public void onClick(View view) {
		    	currentKmlFeature.mName = eHeader.getText().toString();
		    	currentKmlFeature.mDescription = eDescription.getText().toString();
		    	if (mLineStyle != null){
			    	String sColor = eOutlineColor.getText().toString();
			    	try  { 
			    		mLineStyle.mColor = Color.parseColor(sColor);
			    	} catch (IllegalArgumentException e) {
			    		Toast.makeText(view.getContext(), "Invalid outline color", Toast.LENGTH_SHORT).show();
			    	}
		    	}
		    	if (mPolyStyle != null){
			    	String sColor = eFillColor.getText().toString();
			    	try  { 
			    		mPolyStyle.mColor = Color.parseColor(sColor);
			    	} catch (IllegalArgumentException e) {
			    		Toast.makeText(view.getContext(), "Invalid fill color", Toast.LENGTH_SHORT).show();
			    	}
		    	}
		        setResult(RESULT_OK);
		        finish();
		    }
		});
    }

	@Override protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		switch (requestCode) {
		case KML_TREE_REQUEST:
			KmlFeature result = MapActivity.mKmlStack.pop();
			if (resultCode == RESULT_OK) {
				currentKmlFeature.mItems.set(mItemPosition, result);
				listAdapter.notifyDataSetChanged();
			}
			break;
		default:
			break;
		}
    }
	
	@Override public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.kml_item_menu, menu);
	}

	@Override public boolean onContextItemSelected(MenuItem item) {
	    AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
	    switch (item.getItemId()) {
	        case R.id.menu_cut: //=move to the emptied clipboard
	        	kmlClipboard.mItems.clear();
	        	kmlClipboard.add(currentKmlFeature.mItems.get(info.position));
				currentKmlFeature.removeItem(info.position);
				listAdapter.notifyDataSetChanged();
	            return true;
	        case R.id.menu_copy:
	        	KmlFeature copy = currentKmlFeature.mItems.get(info.position).clone();
	        	kmlClipboard.mItems.clear();
	        	kmlClipboard.mItems.add(copy);
	            return true;
	        case R.id.menu_paste: 
	        	for (KmlFeature kmlItem:kmlClipboard.mItems){
		        	currentKmlFeature.add(kmlItem.clone());
	        	}
	        	listAdapter.notifyDataSetChanged();
	            return true;
	        case R.id.menu_behind:
	        	if (info.position > 0){
	        		KmlFeature kmlItem = currentKmlFeature.removeItem(info.position);
	        		currentKmlFeature.mItems.add(info.position-1, kmlItem);
	        		listAdapter.notifyDataSetChanged();
	        	}
	        	return true;
	        case R.id.menu_front:
	        	if (info.position < currentKmlFeature.mItems.size()-1){
	        		KmlFeature kmlItem = currentKmlFeature.removeItem(info.position);
	        		currentKmlFeature.mItems.add(info.position+1, kmlItem);
					listAdapter.notifyDataSetChanged();
	        	}
	        	return true;
	        default:
	            return super.onContextItemSelected(item);
	    }
	}
}
