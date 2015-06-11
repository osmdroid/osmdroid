package com.osmnavigator;

import java.util.ArrayList;
import org.osmdroid.bonuspack.location.POI;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;

/**
 * Activity showing POIs as a list. 
 * @author M.Kergall
 */

public class POIActivity extends Activity {
	
	@Override public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.items_list);
		
		TextView title = (TextView)findViewById(R.id.title);
		title.setText("Features");
		
		ListView list = (ListView)findViewById(R.id.items);
		
		Intent myIntent = getIntent();
		//STATIC - final ArrayList<POI> pois = myIntent.getParcelableArrayListExtra("POI");
		ArrayList<POI> pois = MapActivity.mPOIs;
		final int currentNodeId = myIntent.getIntExtra("ID", -1);
		POIAdapter adapter = new POIAdapter(this, pois);
		
		list.setOnItemClickListener(new OnItemClickListener() {
			@Override public void onItemClick(AdapterView<?> arg0, View view, int position, long index) {
				Intent intent = new Intent();
				intent.putExtra("ID", position);
				setResult(RESULT_OK, intent);
				finish();
			}
		});

		list.setAdapter(adapter);
		list.setSelection(currentNodeId);
	}
}

class POIAdapter extends BaseAdapter implements OnClickListener {
	private Context mContext;
	private ArrayList<POI> mPois;
	
	public POIAdapter(Context context, ArrayList<POI> pois) {
		mContext = context;
		mPois = pois;
	}

    @Override public int getCount() {
    	if (mPois == null)
    		return 0;
    	else
    		return mPois.size();
    }

    @Override public Object getItem(int position) {
    	if (mPois == null)
    		return null;
    	else
    		return mPois.get(position);
    }

    @Override public long getItemId(int position) {
        return position;
    }

    @Override public View getView(int position, View convertView, ViewGroup viewGroup) {
        POI entry = (POI)getItem(position);
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.item_layout, null);
        }
        TextView tvTitle = (TextView)convertView.findViewById(R.id.title);
        tvTitle.setText(entry.mType);
        TextView tvDetails = (TextView)convertView.findViewById(R.id.details);
        tvDetails.setText(entry.mDescription);
        
		ImageView ivManeuver = (ImageView)convertView.findViewById(R.id.thumbnail);
   		//ivManeuver.setImageBitmap(entry.mThumbnail);
   		entry.fetchThumbnailOnThread(ivManeuver);

   		return convertView;
    }

	@Override public void onClick(View arg0) {
		//nothing to do.
	}
    
}
