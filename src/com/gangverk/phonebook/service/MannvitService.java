package com.gangverk.phonebook.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

public class MannvitService extends Service {

	private MyPhoneStateListener mPhoneStateListener;
	private TelephonyManager mTelephonyManager;
	@Override
	public void onCreate() {
		super.onCreate();
		mPhoneStateListener = new MyPhoneStateListener(getBaseContext());

		/*setContentView(R.layout.call_intercepter);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);


		String number = getIntent().getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
		TextView text = (TextView)findViewById(R.id.text);
		text.setText("Incoming call from " + number);*/

		mTelephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
		mTelephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		//handleCommand(intent);
		return START_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
}