package com.mhy.snstest;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.mhy.snstest.sns.ISns;
import com.mhy.snstest.sns.SnsFacebook;
import com.mhy.snstest.sns.SnsTwitter;
import com.mhy.snstest.util.AlertDialogMgr;
import com.mhy.snstest.util.AlertDialogMgr.ICallback;
import com.mhy.snstest.util.ConnectionMgr;

/**
 * Main Activity for testing SnsFacebook & SnsTwitter.<br>
 * Please make sure you have Facebook SDK project on your computer.
 * @see <a href="https://developers.facebook.com/docs/android/getting-started/">https://developers.facebook.com/docs/android/getting-started/</a>
 * @author hyoyeol
 */
public class MainActivity extends Activity implements OnClickListener{
	private SnsTwitter mSnsTwitter;
	private SnsFacebook mSnsFacebook;
	private ISns sns;
	private Button btnTwitter;
	private Button btnFb;
	
	private static int SELECT_PHOTO = 300;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		mSnsTwitter = SnsTwitter.getInstance();
		mSnsFacebook = SnsFacebook.getInstance();
		
		mSnsTwitter.setActivity(this);
		mSnsFacebook.setActivity(this);
		mSnsFacebook.onCreate(savedInstanceState);
		
		btnTwitter = (Button)findViewById(R.id.btnLoginTwitter);
		btnFb = (Button)findViewById(R.id.btnLoginFb);
		btnTwitter.setOnClickListener(this);
		btnFb.setOnClickListener(this);
		
		//check the network connection
		ConnectionMgr cMgr = new ConnectionMgr(this);
		AlertDialogMgr aMgr = new AlertDialogMgr(this);
		aMgr.setOKCallback(new ICallback() {
			
			@Override
			public void run() {
				startActivity(new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS));
			}
		});
		if(!cMgr.isConnected())
			aMgr.showAlertDialog("No Connection", "Do you want to turn WiFi on?");
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		btnTwitter.setText(getResId(mSnsTwitter.isLoggedIn(), R.string.btn_Twitter_logged_in, R.string.btn_Twitter_not_logged_in));
		btnFb.setText(getResId(mSnsFacebook.isLoggedIn(), R.string.btn_Facebook_logged_in, R.string.btn_Facebook_not_logged_in));
	}

	private int getResId(boolean state, int idOn, int idOff){
		return state ? idOn : idOff	;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode == SELECT_PHOTO && resultCode == Activity.RESULT_OK){ //upload a photo selected w/ text
			
			//get photo's path
			Uri uri = data.getData();
			Cursor cursor = getContentResolver().query(uri, new String[] { android.provider.MediaStore.Images.ImageColumns.DATA }, null, null, null);
            cursor.moveToFirst();
            String imageFilePath = cursor.getString(0);
            cursor.close();
			
			Bitmap photo = BitmapFactory.decodeFile(imageFilePath);
			final Bitmap photoScaled = Bitmap.createScaledBitmap(photo, photo.getWidth() / 3, photo.getHeight() / 3, true);	//scale down the photo
			
			AlertDialogMgr aMgr = new AlertDialogMgr(this);
			aMgr.setOKCallback(new ICallback() {
				
				@Override
				public void run() {
					sns.updateStatus(photoScaled, "test posting");
				}
			});
			aMgr.showAlertDialog("Upload", "Do you want to upload now?");
			aMgr.addBitmap(photoScaled);	//display selected photo on the dialog(for test)
		}else{
			mSnsTwitter.onActivityResult(requestCode, resultCode, data);
			mSnsFacebook.onActivityResult(requestCode, resultCode, data);
		}
	}

	@Override
	public void onClick(View v) {
		sns = null;
		switch(v.getId()){
			case R.id.btnLoginFb :
				sns = mSnsFacebook;
				break;
			case R.id.btnLoginTwitter :
				sns = mSnsTwitter;
				break;
		}
		if(sns.isLoggedIn()){
			//select a photo to upload
			Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
			photoPickerIntent.setType("image/*");
			startActivityForResult(photoPickerIntent, SELECT_PHOTO);    
		}else
			sns.requestLogIn();
	}
}
