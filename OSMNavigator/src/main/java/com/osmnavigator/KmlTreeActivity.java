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
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;

import org.osmdroid.bonuspack.kml.KmlFeature;
import org.osmdroid.bonuspack.kml.KmlFolder;

public class KmlTreeActivity extends Activity {

	/* request codes */
	public static final int KML_TREE_REQUEST = 200;
	
	KmlListAdapter mListAdapter;
	ListView mListView;
	KmlFeature mCurrentKmlFeature; //feature currently edited. 
	KmlFolder mKmlClipboard; //link to the global KML clipboard. 
	int mItemPosition; //last item opened
	EditText eHeader, eDescription;
	Spinner sStyleSpinner;

	@Override protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.kml_main);
		mListView = (ListView) findViewById(R.id.listviewKml);
		registerForContextMenu(mListView);
		
		mCurrentKmlFeature = MapActivity.mKmlStack.peek();
		mKmlClipboard = MapActivity.mKmlClipboard;
		
		eHeader = (EditText)findViewById(R.id.name);
		eHeader.setText(mCurrentKmlFeature.mName);
		
		eDescription = (EditText)findViewById(R.id.description);
		eDescription.setText(mCurrentKmlFeature.mDescription);
		
		sStyleSpinner = (Spinner) findViewById(R.id.styleSpinner);
		ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, stylesWithEmpty());
		spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		sStyleSpinner.setAdapter(spinnerAdapter);
		if (mCurrentKmlFeature.mStyle != null){
			int spinnerPosition = spinnerAdapter.getPosition(mCurrentKmlFeature.mStyle);
			sStyleSpinner.setSelection(spinnerPosition);
		}
		
		CheckBox cVisible = (CheckBox)findViewById(R.id.checkbox_visible);
		cVisible.setChecked(mCurrentKmlFeature.mVisibility);
		
		if (mCurrentKmlFeature instanceof KmlFolder){
			mListAdapter = new KmlListAdapter(this, (KmlFolder)mCurrentKmlFeature);
			// setting list adapter
			mListView.setAdapter(mListAdapter);
			// Listview on child click listener
			mListView.setOnItemClickListener(new OnItemClickListener() {
				@Override public void onItemClick(AdapterView<?> arg0, View view, int position, long index) {
					mItemPosition = position;
					KmlFeature item = ((KmlFolder)mCurrentKmlFeature).mItems.get(position);
					Intent myIntent = new Intent(view.getContext(), KmlTreeActivity.class);
					//myIntent.putExtra("KML", item);
					MapActivity.mKmlStack.push(item);
					startActivityForResult(myIntent, KML_TREE_REQUEST);
				}
			});
		}
		
	}
	
	/** @return an array with all shared styles ids, plus one empty entry at the beginning */
	String[] stylesWithEmpty(){
		String[] styles = MapActivity.mKmlDocument.getStylesList();
		String[] stylesPlusEmpty = new String[styles.length+1];
		stylesPlusEmpty[0] = "";
		System.arraycopy(styles, 0, stylesPlusEmpty, 1, styles.length);
		return stylesPlusEmpty;
	}

	@Override protected void onStop(){
		saveCurrentFeature();
		//setResult(RESULT_OK);
		//finish();
		super.onStop();
	}
	
	protected void saveCurrentFeature(){
		mCurrentKmlFeature.mName = eHeader.getText().toString();
		mCurrentKmlFeature.mDescription = eDescription.getText().toString();
		Object item = sStyleSpinner.getSelectedItem();
		if (item != null && !"".equals(item.toString()))
			mCurrentKmlFeature.mStyle = item.toString();
		else 
			mCurrentKmlFeature.mStyle = null;
	}
	
	/* method assigned to the checkbox in the layout */
	public void onCheckboxClicked(View view) {
		boolean checked = ((CheckBox)view).isChecked();
		switch(view.getId()) {
		case R.id.checkbox_visible:
			mCurrentKmlFeature.mVisibility = checked;
			break;
		}
	}

	@Override protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		switch (requestCode) {
		case KML_TREE_REQUEST:
			MapActivity.mKmlStack.pop();
			mListAdapter.notifyDataSetChanged();
			if (intent != null && intent.getParcelableExtra("KML_FEATURE") != null){
				saveCurrentFeature();
				setResult(RESULT_OK, intent);
				finish();
			}
			break;
		default:
			break;
		}
    }
	
	//------------ Context Menu implementation
	@Override public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.kml_item_menu, menu);
	}

	@Override public boolean onContextItemSelected(MenuItem item) {
	    AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
	    KmlFolder currentKmlFolder = (KmlFolder)mCurrentKmlFeature;
	    switch (item.getItemId()) {
	        case R.id.kml_item_menu_cut: //=move to the emptied clipboard
	        	mKmlClipboard.mItems.clear();
	        	mKmlClipboard.add(currentKmlFolder.mItems.get(info.position));
	        	currentKmlFolder.removeItem(info.position);
				mListAdapter.notifyDataSetChanged();
	            return true;
	        case R.id.kml_item_menu_copy:
	        	KmlFeature copy = currentKmlFolder.mItems.get(info.position).clone();
	        	mKmlClipboard.mItems.clear();
	        	mKmlClipboard.mItems.add(copy);
	            return true;
	        case R.id.kml_item_menu_behind:
	        	if (info.position > 0){
	        		KmlFeature kmlItem = currentKmlFolder.removeItem(info.position);
	        		currentKmlFolder.mItems.add(info.position-1, kmlItem);
	        		mListAdapter.notifyDataSetChanged();
	        	}
	        	return true;
	        case R.id.kml_item_menu_front:
	        	if (info.position < currentKmlFolder.mItems.size()-1){
	        		KmlFeature kmlItem = currentKmlFolder.removeItem(info.position);
	        		currentKmlFolder.mItems.add(info.position+1, kmlItem);
					mListAdapter.notifyDataSetChanged();
	        	}
	        	return true;
	        case R.id.kml_item_menu_show_on_map:
	        	Intent intent = new Intent();
	        	//TODO: is it the right way to pass a handle to an object?
	        	intent.putExtra("KML_FEATURE", currentKmlFolder.mItems.get(info.position));
				saveCurrentFeature();
				setResult(RESULT_OK, intent);
				finish();
	        	return true;
	        default:
	            return super.onContextItemSelected(item);
	    }
	}
	
	//------------ Option Menu implementation
	
	@Override public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.kml_option_menu, menu);	
		return true;
	}
	
	@Override public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.kml_option_menu_paste: 
				if (mCurrentKmlFeature instanceof KmlFolder){
					KmlFolder currentKmlFolder = (KmlFolder)mCurrentKmlFeature;
					for (KmlFeature kmlItem:mKmlClipboard.mItems){
						currentKmlFolder.add(kmlItem.clone());
					}
					mListAdapter.notifyDataSetChanged();
				}
				return true;
			case R.id.kml_option_menu_new: 
				if (mCurrentKmlFeature instanceof KmlFolder){
					KmlFolder currentKmlFolder = (KmlFolder)mCurrentKmlFeature;
					currentKmlFolder.add(new KmlFolder());
					mListAdapter.notifyDataSetChanged();
				}
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}
}
