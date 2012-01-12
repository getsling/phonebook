package com.gangverk.phonebook;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

public class MyPhoneStateListener extends PhoneStateListener {
	
	private MyToast mToast;
	Context context;
	public MyPhoneStateListener(Context context) {
		super();
		this.context = context;
		mToast = new MyToast(context);
	}

	@Override
	public void onCallStateChanged(int state,String incomingNumber){
		switch(state)
		{
		case TelephonyManager.CALL_STATE_IDLE:
			Log.d("DEBUG", "IDLE");
			mToast.cancel();
			break;
		case TelephonyManager.CALL_STATE_OFFHOOK:
			Log.d("DEBUG", "OFFHOOK");
			mToast.cancel();
			break;
		case TelephonyManager.CALL_STATE_RINGING:
			Log.d("DEBUG", "RINGING");
			if(!incomingNumber.equals("")){
				handleCall(incomingNumber);
			}
			break;
		}
	}
	
	public void handleCall(String incomingNumber){
		Uri singleContact = Uri.parse("content://com.gangverk.phonebook.Contacts/contacts");
		String selection = ContactsProvider.PHONE + " LIKE ? OR "+ ContactsProvider.MOBILE + " LIKE ?";
		Cursor cursor = context.getContentResolver().query(singleContact, null, selection, new String[]{"%"+ incomingNumber,"%"+ incomingNumber}, null);
		String strName = null;
		if(cursor.moveToFirst()) {
			strName = cursor.getString(cursor.getColumnIndex(ContactsProvider.NAME));
		}
		if(strName != null) {
			LayoutInflater inflater = (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );

			View layout = inflater.inflate(R.layout.toast_layout,null);

			TextView toastText = (TextView) layout.findViewById(R.id.text);
			toastText.setText(strName);

			mToast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
			mToast.setDuration(100);
			mToast.setView(layout);
			mToast.show();
		}
	}
}
