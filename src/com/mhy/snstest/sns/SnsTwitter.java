package com.mhy.snstest.sns;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import twitter4j.Status;
import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.Toast;
import com.mhy.snstest.R;

/**
 * SnsTwitter is implemented w/ twitter4j.<br>
 * Please make sure you have your own Consumer key/secret on <a href="https://apps.twitter.com/">https://apps.twitter.com/</a>
 * @author hyoyeol
 */
public class SnsTwitter implements ISns {
	private final String CONSUMER_KEY = "AAAAAAaaaaaAAAAAAAaaaa"; // "your consumer key here";
	private final String CONSUMER_SECRET = "BBBBBBBBBbbbbbbbbbBBBBBBBBBBBBBbbbbbbBBBB"; // "your consumer secret here";
	
	private final int REQ_LOGIN = 100;
	public final static int RES_OK = 1;
	public final static int RES_FAIL = 3;
	
	public final static String CALLBACK_URL = "oauth://activity_main";  //Callback URL that tells the WebView in TwitterLogInActivity whether user's successfully logged in or not.

	private Activity mActivity;
	private Twitter mTwitter;
	private RequestToken mReqToken;
	private AccessToken mAccessToken;
	
	private static SnsTwitter mSnsTwitter;
	
	private SnsTwitter() { }
	
	/**
	 * Please follow these 2 step initialization<br><br>
	 * 1. {@link #setActivity(Activity activity)} must be called before using SnsFacebook in an Activity or Fragment.<br><br>
	 * 2. {@link #onActivityResult(int requestCode, int resultCode, Intent data)} 
	 * must be called in an Activity or Fragment's same methods.
	 * @return instance of SnsFacebook
	 */
	public static SnsTwitter getInstance(){
		if(mSnsTwitter == null)
			mSnsTwitter = new SnsTwitter();
		return mSnsTwitter;
	}

	private void setAccessToken(String uriString){
		if(uriString != null){
			Uri uri = Uri.parse(uriString);
			final String verifier = uri.getQueryParameter("oauth_verifier");
			
			new Thread(new Runnable() {
				
				@Override
				public void run() {
					try {
						/**
						 * After you acquired the AccessToken for the user,
						 * the RequestToken is not required anymore. You can persist the AccessToken to any kind of persistent store
						*/
						mAccessToken = mTwitter.getOAuthAccessToken(mReqToken, verifier);
						mTwitter.setOAuthAccessToken(mAccessToken);
						runCallback(new TwitCallback() {
							
							@Override
							public void run() {
								Button btn = (Button)mActivity.findViewById(R.id.btnLoginTwitter);
								btn.setText(R.string.btn_Twitter_logged_in);
								
								Toast.makeText(mActivity,
										"Test - Twitter : Logged In",
										Toast.LENGTH_SHORT).show();
							}
						});
					} catch (TwitterException e) {
						e.printStackTrace();
					}
				}
			}).start();
		}
	}
	
	private interface TwitCallback{
		void run();
	}
	
	private void runCallback(final TwitCallback callback){
		mActivity.runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				callback.run();
			}
		});
	}
	
	/**
	 * Set the activity that will use Twitter's feature.
	 * @param activity the Activity associated with SnsTwitter. If calling from a Fragment,
     *                 use {@link android.support.v4.app.Fragment#getActivity()} 
	 */
	public void setActivity(Activity activity){
		mActivity = activity;
	}
	
	/**
     * To be called from an Activity or Fragment's onActivityResult method.
     *
     * @param requestCode the request code
     * @param resultCode the result code
     * @param data the result data
     */
	public void onActivityResult(int requestCode, int resultCode, Intent data){
		if(requestCode == REQ_LOGIN){
			switch(resultCode){
				case RES_OK :
					mSnsTwitter.setAccessToken(data.getStringExtra("url"));
					break;
				case RES_FAIL :
					mSnsTwitter = null;
					break;
			}
		}
	}
	
	@Override
	public boolean isLoggedIn() {
		return (mTwitter != null && mAccessToken != null) ? true : false;
	}
	
	@Override
	public void requestLogIn() {
		new Thread(new Runnable() {
			
			@Override
			public void run() {

				mTwitter = new TwitterFactory().getInstance();
				mTwitter.setOAuthConsumer(CONSUMER_KEY, CONSUMER_SECRET);
				
				try {
					mReqToken = mTwitter.getOAuthRequestToken(CALLBACK_URL);
					String authUrl = mReqToken.getAuthenticationURL();
					
					Intent intent = new Intent();
					intent.setClass(mActivity.getApplicationContext(), TwitterLogInActivity.class);
					intent.putExtra("url", authUrl);
					mActivity.startActivityForResult(intent, REQ_LOGIN);
				} catch (TwitterException e) {
					e.printStackTrace();
				}
			}
		}).start();
	}

	@Override
	public void updateStatus(Bitmap photo, final String text) {
		if(photo == null){
			Toast.makeText(mActivity.getApplicationContext(),
					"Test : No photo, only text will be posted",
					Toast.LENGTH_SHORT).show();
		}
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		photo.compress(Bitmap.CompressFormat.PNG, 100, baos);
		byte[] imgBytes = baos.toByteArray();
		Base64.encodeToString(imgBytes, Base64.DEFAULT);
		final ByteArrayInputStream bais = new ByteArrayInputStream(imgBytes);
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					StatusUpdate toUpdate = new StatusUpdate(text);
					toUpdate.setMedia("img", bais);
					
					Status status = mTwitter.updateStatus(toUpdate);
					
					if(status != null){	//when successfully posted on twitter
						runCallback(new TwitCallback() {
							
							@Override
							public void run() {
								Toast.makeText(mActivity,
										"TEST : updateStatus on Twitter has been finished",
										Toast.LENGTH_SHORT).show();
							}
						});
					}
				} catch (TwitterException e) {
					e.printStackTrace();
				}	
			}
		}).start();
	}
	
	
	public static class TwitterLogInActivity extends Activity {

		@Override
		protected void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			setContentView(R.layout.activity_webview);
			String url = getIntent().getStringExtra("url");
			
			WebView webView = (WebView)findViewById(R.id.webView);
			webView.setWebViewClient(new WebViewClient(){

				@Override
				public boolean shouldOverrideUrlLoading(WebView view, String url) {
					if(url.startsWith(SnsTwitter.CALLBACK_URL)){
						
						Intent i = new Intent();
						i.putExtra("url", url);
						
						if(url.contains("denied"))
							setResult(SnsTwitter.RES_FAIL, i);
						else
							setResult(SnsTwitter.RES_OK, i);
						
						finish();
					}else
						startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
					return true;
				}
				
			});
			
			webView.loadUrl(url);
		}
	}
}
