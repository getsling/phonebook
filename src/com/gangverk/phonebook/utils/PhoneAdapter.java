package com.gangverk.phonebook.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.gangverk.phonebook.PhonebookActivity;
import com.gangverk.phonebook.R;
import com.gangverk.phonebook.database.ContactsProvider;

public class PhoneAdapter extends SimpleCursorAdapter {
	private View.OnClickListener callButtonListener = null;
	private Map<Integer, Integer> mapNumberPrefs = null; 
	public PhoneAdapter(Context context, int layout, Cursor c,String[] from, int[] to, int flags) {
		super(context, layout, c, from, to, flags);
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
		boolean prefersPhone = false;
		prefersPhone = PhonebookActivity.checkPhone(id,mapNumberPrefs,PhonebookActivity.NUMBER_PREFERENCE_PHONE);
		
		if(checkText == 0 || prefersPhone) {
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

	public void updateNumberPreferences(Map<Integer,Integer> mapNumberPrefs) {
		this.mapNumberPrefs  = mapNumberPrefs;

	}
}