package com.gangverk.phonebook;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;
//import android.widget.SimpleCursorAdapter;

public class PhoneAdapter extends SimpleCursorAdapter {
	private View.OnClickListener callButtonListener = null;
	private SharedPreferences settings; 
	public PhoneAdapter(Context context, int layout, Cursor c,String[] from, int[] to, int flags) {
		super(context, layout, c, from, to, flags);
		settings = PreferenceManager.getDefaultSharedPreferences(context);
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		super.bindView(view, context, cursor);
		int id = cursor.getInt(cursor.getColumnIndex(ContactsProvider._ID));
		// phone_{id} is set if the user prefers phone instead of mobile
		boolean usesPhoneNumber = settings.getBoolean(String.format("phone_%d",id),false);
		if(usesPhoneNumber)
		{
			TextView tv_smallText = (TextView)view.findViewById(R.id.textSmall1);
			tv_smallText.setText(cursor.getString(cursor.getColumnIndex(ContactsProvider.PHONE)));
		}
	}
	
	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		View view = super.newView(context, cursor, parent);		
		DontPressWithParentImgButton btnCall = (DontPressWithParentImgButton)view.findViewById(R.id.btnCall);
		btnCall.setOnClickListener(callButtonListener);
		return view;
	}

	public void setCallButtonListener(OnClickListener callButtonListener) {
		this.callButtonListener = callButtonListener;
	}
}