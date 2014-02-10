package com.mhy.snstest.sns;

import android.graphics.Bitmap;

public interface ISns {
	/**
	 * Ask user to log in
	 */
	public void requestLogIn();
	
	/**
	 * Post a new img/text on sns
	 */
	public void updateStatus(Bitmap photo, String text);
	
	/**
	 * Check the log-in status.
	 * @return true : if user's logged in
	 */
	public boolean isLoggedIn();

}
