package com.osmbonuspackdemo;

import org.osmdroid.bonuspack.kml.KmlObject;
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
	KmlObject kmlObject;
	protected static final int KML_TREE_REQUEST = 3;
	int mItemPosition; //last item opened
	EditText eHeader, eDescription, eOutlineColor, eFillColor;
	ColorStyle mOutlineColorStyle, mFillColorStyle; //direct pointers on the ColorStyle, or null. 
	
	@Override protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.kml_main);
		listView = (ListView) findViewById(R.id.listviewKml);
		registerForContextMenu(listView);
		
		Intent myIntent = getIntent();
		kmlObject = myIntent.getParcelableExtra("KML");
		
		eHeader = (EditText)findViewById(R.id.name);
		eHeader.setText(kmlObject.mName);
		
		eDescription = (EditText)findViewById(R.id.description);
		eDescription.setText(kmlObject.mDescription);
		
		Style style = null;
		if (kmlObject.mStyle != null)
			style = MapActivity.mKmlDocument.getStyle(kmlObject.mStyle);
		
		eOutlineColor = (EditText)findViewById(R.id.outlineColor);
		if ((kmlObject.mObjectType == KmlObject.LINE_STRING || kmlObject.mObjectType == KmlObject.POLYGON) && style!=null){
			mOutlineColorStyle = style.outlineColorStyle;
			eOutlineColor.setText(mOutlineColorStyle.colorAsAndroidString());
		} else {
			LinearLayout lOutlineColorLayout = (LinearLayout)findViewById(R.id.outlineColorLayout);
			lOutlineColorLayout.setVisibility(View.GONE);
		}
		
		eFillColor = (EditText)findViewById(R.id.fillColor);
		if (kmlObject.mObjectType == KmlObject.POLYGON && style!=null){
			mFillColorStyle = style.fillColorStyle;
			eFillColor.setText(mFillColorStyle.colorAsAndroidString());
		} else {
			LinearLayout lFillColorLayout = (LinearLayout)findViewById(R.id.fillColorLayout);
			lFillColorLayout.setVisibility(View.GONE);
		}
		
		listAdapter = new KmlListAdapter(this, kmlObject);
		
		// setting list adapter
		listView.setAdapter(listAdapter);
		
		// Listview on child click listener
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override public void onItemClick(AdapterView<?> arg0, View view, int position, long index) {
				mItemPosition = position;
				KmlObject item = kmlObject.mItems.get(position);
				Intent myIntent = new Intent(view.getContext(), KmlTreeActivity.class);
				myIntent.putExtra("KML", item);
				startActivityForResult(myIntent, KML_TREE_REQUEST);
			}
		});
		
		Button btnOk = (Button) findViewById(R.id.btnOK);
		btnOk.setOnClickListener( new View.OnClickListener() {
		    public void onClick(View view) {
		    	kmlObject.mName = eHeader.getText().toString();
		    	kmlObject.mDescription = eDescription.getText().toString();
		    	if (mOutlineColorStyle != null){
			    	String sColor = eOutlineColor.getText().toString();
			    	try  { 
			    		mOutlineColorStyle.color = Color.parseColor(sColor);
			    	} catch (IllegalArgumentException e) {
			    		Toast.makeText(view.getContext(), "Invalid outline color", Toast.LENGTH_SHORT).show();
			    	}
		    	}
		    	if (mFillColorStyle != null){
			    	String sColor = eFillColor.getText().toString();
			    	try  { 
			    		mFillColorStyle.color = Color.parseColor(sColor);
			    	} catch (IllegalArgumentException e) {
			    		Toast.makeText(view.getContext(), "Invalid fill color", Toast.LENGTH_SHORT).show();
			    	}
		    	}
		        Intent intent = new Intent();
		        intent.putExtra("KML", kmlObject);
		        setResult(RESULT_OK, intent);
		        finish();
		    }
		});
    }

	@Override protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		switch (requestCode) {
		case KML_TREE_REQUEST:
			if (resultCode == RESULT_OK) {
				KmlObject item = intent.getParcelableExtra("KML");
				kmlObject.mItems.set(mItemPosition, item);
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
	        case R.id.menu_delete:
				kmlObject.removeItem(info.position);
				listAdapter.notifyDataSetChanged();
	            return true;
	        case R.id.menu_behind:
	        	if (info.position > 0){
	        		KmlObject kmlItem = kmlObject.removeItem(info.position);
	        		kmlObject.mItems.add(info.position-1, kmlItem);
	        		listAdapter.notifyDataSetChanged();
	        	}
	        	return true;
	        case R.id.menu_front:
	        	if (info.position < kmlObject.mItems.size()-1){
	        		KmlObject kmlItem = kmlObject.removeItem(info.position);
	        		kmlObject.mItems.add(info.position+1, kmlItem);
					listAdapter.notifyDataSetChanged();
	        	}
	        	return true;
	        default:
	            return super.onContextItemSelected(item);
	    }
	}
}
