package com.osmbonuspackdemo;

import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadNode;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
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

public class RouteActivity extends Activity {
	
	@Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.route);

        ListView list = (ListView) findViewById(R.id.items);

        Intent myIntent = getIntent();
        final Road road = myIntent.getParcelableExtra("ROAD");
        final int currentNodeId = myIntent.getIntExtra("NODE_ID", -1);
        RoadNodesAdapter adapter = new RoadNodesAdapter(this, road);

        list.setOnItemClickListener(new OnItemClickListener() {
            @Override public void onItemClick(AdapterView<?> arg0, View view, int position, long index) {
				Intent intent = new Intent();
	    		intent.putExtra("NODE_ID", position);
				setResult(RESULT_OK, intent);
				finish();
            }
        });

        list.setAdapter(adapter);
        list.setSelection(currentNodeId);
    }
}

class RoadNodesAdapter extends BaseAdapter implements OnClickListener {
    private Context mContext;
    private Road mRoad;
    TypedArray iconIds;
    
    public RoadNodesAdapter(Context context, Road road) {
        mContext = context;
        mRoad = road;
		iconIds = mContext.getResources().obtainTypedArray(R.array.direction_icons);
    }

    @Override public int getCount() {
    	if (mRoad == null || mRoad.mNodes == null)
    		return 0;
    	else
    		return mRoad.mNodes.size();
    }

    @Override public Object getItem(int position) {
    	if (mRoad == null || mRoad.mNodes == null)
    		return null;
    	else
    		return mRoad.mNodes.get(position);
    }

    @Override public long getItemId(int position) {
        return position;
    }

    @Override public View getView(int position, View convertView, ViewGroup viewGroup) {
        RoadNode entry = (RoadNode)getItem(position);
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.route_item, null);
        }
        TextView tvInstructions = (TextView)convertView.findViewById(R.id.node_instructions);
        String instructions = (entry.mInstructions==null ? "" : entry.mInstructions);
        tvInstructions.setText("" + (position+1) + ". " + instructions);
        TextView tvNextStep = (TextView)convertView.findViewById(R.id.next_step);
        tvNextStep.setText(mRoad.getLengthDurationText(entry.mLength, entry.mDuration));
		int iconId = iconIds.getResourceId(entry.mManeuverType, R.drawable.ic_empty);
   		Drawable icon = mContext.getResources().getDrawable(iconId);
		ImageView ivManeuver = (ImageView)convertView.findViewById(R.id.maneuver_icon);
   		ivManeuver.setImageDrawable(icon);
        return convertView;
    }

	@Override public void onClick(View arg0) {
		//nothing to do.
	}
    
}
