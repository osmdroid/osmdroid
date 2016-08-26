package org.osmdroid.tileprovider.util;

import org.osmdroid.tileprovider.IRegisterReceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

public class SimpleRegisterReceiver implements IRegisterReceiver {

	private Context mContext;

	public SimpleRegisterReceiver(final Context pContext) {
		super();
		mContext = pContext;
	}

	@Override
	public Intent registerReceiver(final BroadcastReceiver aReceiver, final IntentFilter aFilter) {
		return mContext.registerReceiver(aReceiver, aFilter);
	}

	@Override
	public void unregisterReceiver(final BroadcastReceiver aReceiver) {
		mContext.unregisterReceiver(aReceiver);
	}

	@Override
	public void destroy() {
		mContext=null;
	}
}
