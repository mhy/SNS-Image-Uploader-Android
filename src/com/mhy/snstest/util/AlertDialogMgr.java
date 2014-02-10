package com.mhy.snstest.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.widget.ImageView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;

/**
 * AlertDialogMgr help create AlertDialog
 * @author hyoyeol
 */
public class AlertDialogMgr implements DialogInterface.OnClickListener{
	private ICallback mCallback;
	private AlertDialog alertDialog;
	private Activity mActivity;

	public AlertDialogMgr(Activity activity){
		mActivity = activity;
		alertDialog = new AlertDialog.Builder(activity).create();
	}
	
	public void showAlertDialog(String title, String msg){
		alertDialog.setTitle(title);
		alertDialog.setMessage(msg);
		alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", this);
		alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel", this);
		alertDialog.show();
	}
	
	/**
	 * add a Bitmap object on the dialog. this is for test
	 * @param bitmap Bitmap object to add
	 */
	public void addBitmap(Bitmap bitmap){
		CustomView cv = new CustomView(mActivity);
		cv.setBitmap(bitmap);
		alertDialog.addContentView(cv, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		switch(which){
			case AlertDialog.BUTTON_POSITIVE :
				mCallback.run();
				break;
			case AlertDialog.BUTTON_NEGATIVE :
				//do nothing
				break;
		}
	}

	/**
	 * @param callback to be called when OK button is clicked.
	 */
	public void setOKCallback(ICallback callback){
		mCallback = callback;
	}
	
	public interface ICallback{
		public void run();
	}
	
	private class CustomView extends RelativeLayout{
		private ImageView iv;
		public CustomView(Context context) {
			super(context);
			iv = new ImageView(context);
			LayoutParams lp = new LayoutParams(100, 100);
			lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
			lp.rightMargin = 30;
			lp.topMargin = 30;
			lp.addRule(RelativeLayout.ALIGN_PARENT_TOP);
			addView(iv, lp);
		}
		
		public void setBitmap(Bitmap bm){
			iv.setImageBitmap(bm);
		}
	}
}
