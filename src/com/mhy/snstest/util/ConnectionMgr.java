package com.mhy.snstest.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * ConnectionMgr help check current network connection
 * @author hyoyeol
 */
public class ConnectionMgr {
	private Context mContext;
	
	public ConnectionMgr(Context context){
		mContext = context;
	}
	
	public boolean isConnected(){
		ConnectivityManager connMgr = (ConnectivityManager)mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
		
		if(connMgr == null)
			return false;
		else{	
			NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
			if (networkInfo != null && networkInfo.isConnected()) {
				return true;
			} else {
				return false;
			}
		}
	}
	
	
}
