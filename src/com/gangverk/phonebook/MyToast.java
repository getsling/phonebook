package com.gangverk.phonebook;

import android.content.Context;
import android.os.CountDownTimer;
import android.widget.Toast;

public class MyToast extends Toast{

	int mDuration;
	boolean mShowing = false;
	CountDownTimer cdt;

	public MyToast(Context context) {
		super(context);
		mDuration = 2;
	}


	/**
	 * Set the time to show the toast for (in seconds) 
	 * @param seconds Seconds to display the toast
	 */
	@Override
	public void setDuration(int seconds) {
		super.setDuration(LENGTH_SHORT);
		if(seconds < 2) seconds = 2; //Minimum
		mDuration = seconds;
	}

	/**
	 * Show the toast for the given time 
	 */
	@Override
	public void show() {
		super.show();

		if(mShowing) return;

		mShowing = true;
		final Toast thisToast = this;
		cdt = new CountDownTimer((mDuration-2)*1000, 1000) {
			public void onTick(long millisUntilFinished) {thisToast.show();}
			public void onFinish() {thisToast.show(); mShowing = false;}

		}.start();  
	}

	@Override
	public void cancel() {
		super.cancel();
		if(cdt != null) {
			cdt.cancel();
			cdt = null;
			mDuration = 2;
			mShowing = false;
		}
	}
}
