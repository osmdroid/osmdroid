package com.osmnavigator;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import org.osmdroid.bonuspack.kml.Style;
import org.osmdroid.bonuspack.kml.StyleSelector;

import java.util.ArrayList;
import java.util.HashMap;

public class KmlStylesActivity extends Activity {

	/* request codes */
	public static final int KML_STYLES_REQUEST = 100;
	public static final int KML_STYLE_REQUEST = 101;
	
	KmlStyleListAdapter mListAdapter;
	ListView mListView;
	
	HashMap<String, StyleSelector> mStyles; //the genuine Styles of the KmlDocument
	static ArrayList<String> mStyleList = new ArrayList<String>(); //the list of style names, as an array
	static int mPosition;
	
	@Override protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.kml_styles);
		mListView = (ListView) findViewById(R.id.listviewKml);
		registerForContextMenu(mListView);
		
		mStyles = MapActivity.mKmlDocument.getStyles();
		buildStyleList();
		
		mListAdapter = new KmlStyleListAdapter(this, mStyleList);
		mListView.setAdapter(mListAdapter);
		mListView.setOnItemClickListener(new OnItemClickListener() {
			@Override public void onItemClick(AdapterView<?> arg0, View view, int position, long index) {
				mPosition = position;
				Intent myIntent = new Intent(view.getContext(), KmlStyleActivity.class);
				myIntent.putExtra("STYLE_ID", mStyleList.get(position));
				startActivityForResult(myIntent, KML_STYLE_REQUEST);
			}
		});
		
	}

	public static Style getCurrentStyle(){
		String styleId = mStyleList.get(mPosition);
		return MapActivity.mKmlDocument.getStyle(styleId);
	}
	
	protected void buildStyleList(){
		mStyleList.clear();
		for (HashMap.Entry<String, StyleSelector> entry : mStyles.entrySet()) {
			mStyleList.add(entry.getKey());
		}
	}
	
	@Override protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		switch (requestCode) {
		case KML_STYLE_REQUEST:
			buildStyleList();
			mListAdapter.notifyDataSetChanged();
			break;
		default:
			break;
		}
    }
	
	//------------ Context Menu implementation
	@Override public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.style_item_menu, menu);
	}

	@Override public boolean onContextItemSelected(MenuItem item) {
	    AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
	    switch (item.getItemId()) {
	        case R.id.style_item_menu_cut:
	        	String style = mStyleList.get(info.position);
	        	mStyles.remove(style);
	        	buildStyleList();
				mListAdapter.notifyDataSetChanged();
	            return true;
	        default:
	            return super.onContextItemSelected(item);
	    }
	}
	
	//------------ Option Menu implementation
	
	@Override public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.style_option_menu, menu);	
		return true;
	}
	
	@Override public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.style_option_menu_new: 
				MapActivity.mKmlDocument.addStyle(new Style());
	        	buildStyleList();
				mListAdapter.notifyDataSetChanged();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}
}
