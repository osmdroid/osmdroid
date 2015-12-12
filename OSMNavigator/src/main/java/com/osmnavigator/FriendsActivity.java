package com.osmnavigator;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Activity showing Friends as a list.
 *
 * @author M.Kergall
 */

public class FriendsActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.items_list);

        TextView title = (TextView) findViewById(R.id.title);

        ListView list = (ListView) findViewById(R.id.items);

        Intent myIntent = getIntent();
        //STATIC - final ArrayList<Friend> friends = myIntent.getParcelableArrayListExtra("FRIENDS");
        ArrayList<Friend> friends = MapActivity.mFriends;
        if (friends != null && friends.size() > 0) {
            title.setText(R.string.FriendsActivity_Title);
        } else {
            title.setText(R.string.FriendsActivity_EmptyList);
        }
        final int currentFriendId = myIntent.getIntExtra("ID", -1);
        FriendAdapter adapter = new FriendAdapter(this, friends);

        list.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View view, int position, long index) {
                Intent intent = new Intent();
                intent.putExtra("ID", position);
                setResult(RESULT_OK, intent);
                finish();
            }
        });

        list.setAdapter(adapter);
        list.setSelection(currentFriendId);
    }
}

class FriendAdapter extends BaseAdapter implements OnClickListener {
    private Context mContext;
    private ArrayList<Friend> mFriends;

    public FriendAdapter(Context context, ArrayList<Friend> friends) {
        mContext = context;
        mFriends = friends;
    }

    @Override
    public int getCount() {
        if (mFriends == null)
            return 0;
        else
            return mFriends.size();
    }

    @Override
    public Object getItem(int position) {
        if (mFriends == null)
            return null;
        else
            return mFriends.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        Friend entry = (Friend) getItem(position);
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.item_layout, null);
        }
        TextView tvTitle = (TextView) convertView.findViewById(R.id.title);
        tvTitle.setText(entry.mNickName);
        TextView tvDetails = (TextView) convertView.findViewById(R.id.details);
        tvDetails.setText(entry.mMessage);

        ImageView ivPhoto = (ImageView) convertView.findViewById(R.id.thumbnail);
        ivPhoto.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_menu_sharing/*entry.mThumbnail*/));
        //entry.fetchThumbnailOnThread(ivPhoto);

        return convertView;
    }

    @Override
    public void onClick(View arg0) {
        //nothing to do.
    }

}
