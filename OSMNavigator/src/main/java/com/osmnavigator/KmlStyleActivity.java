package com.osmnavigator;

import java.util.HashMap;
import org.osmdroid.bonuspack.kml.IconStyle;
import org.osmdroid.bonuspack.kml.LineStyle;
import org.osmdroid.bonuspack.kml.Style;
import org.osmdroid.bonuspack.kml.ColorStyle;
import org.osmdroid.bonuspack.kml.StyleSelector;
import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

public class KmlStyleActivity extends Activity {

	Style mStyle; //direct pointer to the KmlDocument Style currently edited
	EditText eStyleId, eIconHref, eOutlineColor, eFillColor;
	TextView tOutlineWidthValue;
	String mInitialStyleId;
	SeekBar sbOutlineWidth;
	
	@Override protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.kml_style);

		mStyle = KmlStylesActivity.getCurrentStyle();
		if (mStyle.mIconStyle == null){
			mStyle.mIconStyle = new IconStyle();
		}
		if (mStyle.mLineStyle == null){
			mStyle.mLineStyle = new LineStyle();
		}
		if (mStyle.mPolyStyle == null){
			mStyle.mPolyStyle = new ColorStyle();
		}
		
		eStyleId = (EditText)findViewById(R.id.style_id);
		mInitialStyleId = getIntent().getExtras().getString("STYLE_ID"); 
		eStyleId.setText(mInitialStyleId);
		
		eIconHref = (EditText)findViewById(R.id.icon);
		if (mStyle.mIconStyle.mHref != null)
			eIconHref.setText(mStyle.mIconStyle.mHref);
		
		eOutlineColor = (EditText)findViewById(R.id.outlineColor);
		eOutlineColor.setText(mStyle.mLineStyle.colorAsAndroidString());
		
		tOutlineWidthValue = (TextView)findViewById(R.id.outlineWidthValue);
		tOutlineWidthValue.setText(""+(int)mStyle.mLineStyle.mWidth);
		
		sbOutlineWidth = (SeekBar)findViewById(R.id.outlineWidth);
		sbOutlineWidth.setProgress((int)mStyle.mLineStyle.mWidth);
		sbOutlineWidth.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				tOutlineWidthValue.setText(""+progress);
			}
			@Override public void onStartTrackingTouch(SeekBar seekBar) {}
			@Override public void onStopTrackingTouch(SeekBar seekBar) {}
		});
		
		eFillColor = (EditText)findViewById(R.id.fillColor);
		eFillColor.setText(mStyle.mPolyStyle.colorAsAndroidString());
		
		Button btnOk = (Button) findViewById(R.id.btnOK);
		btnOk.setOnClickListener( new View.OnClickListener() {
			public void onClick(View view) {
				save();
				setResult(RESULT_OK);
				finish();
			}
		});
	}

	protected void save(){
		String newStyleId = eStyleId.getText().toString();
		//update the styleId, by removing the old one and putting the new one:
		HashMap<String, StyleSelector> styles = MapActivity.mKmlDocument.getStyles();
		styles.remove(mInitialStyleId);
		styles.put(newStyleId, mStyle);
		
		String iconHref = eIconHref.getText().toString();
		mStyle.mIconStyle.setIcon(iconHref, MapActivity.mKmlDocument.getLocalFile(), null);
		
		String sColor = eOutlineColor.getText().toString();
		try  { 
			mStyle.mLineStyle.mColor = Color.parseColor(sColor);
		} catch (IllegalArgumentException e) {
			Toast.makeText(this, "Invalid line color", Toast.LENGTH_SHORT).show();
		}
		
		mStyle.mLineStyle.mWidth = sbOutlineWidth.getProgress();
		
		sColor = eFillColor.getText().toString();
		try  { 
			mStyle.mPolyStyle.mColor = Color.parseColor(sColor);
		} catch (IllegalArgumentException e) {
			Toast.makeText(this, "Invalid fill color", Toast.LENGTH_SHORT).show();
		}
	}
	
}
