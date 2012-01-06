package com.gangverk.phonebook;

import android.support.v4.widget.SimpleCursorAdapter;
import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
//import android.widget.SimpleCursorAdapter;

public class PhoneAdapter extends SimpleCursorAdapter {
	private View.OnClickListener callButtonListener = null;
	
	public PhoneAdapter(Context context, int layout, Cursor c,String[] from, int[] to, int flags) {
		super(context, layout, c, from, to, flags);
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