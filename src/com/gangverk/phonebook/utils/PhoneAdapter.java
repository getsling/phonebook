package com.gangverk.phonebook.utils;

import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.preference.PreferenceManager;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.gangverk.phonebook.R;
import com.gangverk.phonebook.database.ContactsProvider;

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
		String phone = cursor.getString(cursor.getColumnIndex(ContactsProvider.PHONE));
		TextView tv_smallText = (TextView)view.findViewById(R.id.textSmall1);

		AssetManager assetManager = context.getAssets();
		InputStream istr = null;
		ImageView iv_smallProfilePic = (ImageView)view.findViewById(R.id.smallProfilePic);
		
		try {
			istr = assetManager.open("profile/img_"+id+".jpg");
			Bitmap bitmap = BitmapFactory.decodeStream(istr);
			iv_smallProfilePic.setImageBitmap(bitmap);
		} catch (IOException e) {
			iv_smallProfilePic.setImageResource(R.drawable.icon);
		}
		
		int checkText = tv_smallText.getText().length();
		if(checkText == 0) {
			tv_smallText.setText(phone);
			Editor editor = settings.edit();
			editor.putBoolean(String.format("phone_%d",id), true);
			editor.commit();
		}
		// phone_{id} is set if the user prefers phone instead of mobile
		boolean usesPhoneNumber = settings.getBoolean(String.format("phone_%d",id),false);
		if(usesPhoneNumber)	{
			tv_smallText.setText(phone);
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