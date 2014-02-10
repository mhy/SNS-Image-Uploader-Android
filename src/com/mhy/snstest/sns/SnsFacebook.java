package com.mhy.snstest.sns;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import com.facebook.Session;
import com.facebook.Request.Callback;
import com.facebook.Session.StatusCallback;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.mhy.snstest.R;

/**
 * Please make sure you have your own App ID in string.xml
 * Also you have to register your key hash on developer.facebook.com
 * @see <a href="https://developers.facebook.com/docs/android/getting-started/">https://developers.facebook.com/docs/android/getting-started/</a>
 * @author hyoyeol
 */
public class SnsFacebook implements ISns {
	private static SnsFacebook mSnsFacebook;
	private Activity mActivity;
	private UiLifecycleHelper mUiLifecycleHelper;
	
	private SnsFacebook(){ }
	
	private Session.StatusCallback mCallback = new StatusCallback(){
		@Override
		public void call(Session session, SessionState state, Exception exception) {
			if(state.isOpened()){
				Button btn = (Button)mActivity.findViewById(R.id.btnLoginFb);
				btn.setText(R.string.btn_Facebook_logged_in);
				
				Toast.makeText(mActivity,
						"Test - Facebook : Logged In",
						Toast.LENGTH_SHORT).show();
			}
			
		}
	};
	
	/**
	 * Please follow these 2 step initialization<br><br>
	 * 1. {@link #setActivity(Activity activity)} must be called before using SnsFacebook in an Activity or Fragment.<br><br>
	 * 2. {@link #onCreate(Bundle savedInstanceState)} and {@link #onActivityResult(int requestCode, int resultCode, Intent data)} 
	 * must be called in an Activity or Fragment's same methods.
	 * @return instance of SnsFacebook
	 */
	public static SnsFacebook getInstance(){
		if(mSnsFacebook == null)
			mSnsFacebook = new SnsFacebook();
		return mSnsFacebook;
	}
	
	/**
	 * Set the activity that will use Facebook's feature.
	 * @param activity the Activity associated with SnsFacebook. If calling from a Fragment,
     *                 use {@link android.support.v4.app.Fragment#getActivity()} 
	 */
	public void setActivity(Activity activity){
		mActivity = activity;
		mUiLifecycleHelper = new UiLifecycleHelper(activity, mCallback);	
	}
	
	/**
     * To be called from an Activity or Fragment's onCreate method.
     *
     * @param savedInstanceState the previously saved state
     */
	public void onCreate(Bundle savedInstanceState){
		mUiLifecycleHelper.onCreate(savedInstanceState);
	}
	
	/**
     * To be called from an Activity or Fragment's onActivityResult method.
     *
     * @param requestCode the request code
     * @param resultCode the result code
     * @param data the result data
     */
	public void onActivityResult(int requestCode, int resultCode, Intent data){
		mUiLifecycleHelper.onActivityResult(requestCode, resultCode, data);
	}
	
	@Override
	public boolean isLoggedIn() {
		Session session = Session.getActiveSession();
		if(session != null && session.isOpened())
			return true;
		else
			return false;
	}
	
	@Override
	public void requestLogIn() {
		Session session = Session.getActiveSession();
		if(session.getState() == SessionState.CREATED_TOKEN_LOADED){
			session.closeAndClearTokenInformation();
		}

		Session.openActiveSession(mActivity, true, mCallback);	//ask for login on the pop-up dialog if user's not logged in.
	}

	@Override
	public void updateStatus(Bitmap photo, String text) {
		if(photo == null){
			Toast.makeText(mActivity.getApplicationContext(),
					"Test : No photo, only text will be posted",
					Toast.LENGTH_SHORT).show();
		}
		
		Session session = Session.getActiveSession();
	    Request reqPost = Request.newUploadPhotoRequest(session, photo, null);
		
	    //Parameters-info can be found on https://developers.facebook.com/docs/reference/api/post/
	    Bundle postParams = reqPost.getParameters();
	    postParams.putString("name", text);
	    
	    reqPost.setParameters(postParams);
	    reqPost.setCallback(new Callback() {
			@Override
			public void onCompleted(Response response) {
				Toast.makeText(mActivity,
						"TEST : updateStatus on Facebook has been finished",
						Toast.LENGTH_SHORT).show();
			}
		});
		reqPost.executeAsync();
	}
}
