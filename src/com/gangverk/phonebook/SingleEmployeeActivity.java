package com.gangverk.phonebook;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.gangverk.phonebook.database.ContactsProvider;
import com.gangverk.phonebook.utils.RemoteImageView;
import com.gangverk.phonebook.utils.SingleEmployeeAdapter;
import com.gangverk.phonebook.utils.SystemUtils;

public class SingleEmployeeActivity extends Activity {
	ListView lv;
	SingleEmployeeAdapter dAdapter;
	long userID;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.single_employee);
		Bundle extras = getIntent().getExtras();
		userID = extras.getLong(ContactsProvider._ID); 
		AssetManager assetManager = getAssets();
		RemoteImageView IV_profilePic = (RemoteImageView)findViewById(R.id.profilePic);
		InputStream istr = null;
		try {
			istr = assetManager.open("profile/img_"+userID+".jpg");
			Bitmap bitmap = BitmapFactory.decodeStream(istr);
			IV_profilePic.setImageBitmap(bitmap);
		} catch (IOException e) {
			IV_profilePic.setImageResource(R.drawable.icon);
			e.printStackTrace();
		}

		Cursor c = managedQuery(Uri.withAppendedPath(ContactsProvider.CONTENT_URI, "/contacts/" + userID), null, null, null, null);
		c.moveToFirst();
		TextView tv_name = (TextView)findViewById(R.id.singleName);
		TextView tv_title = (TextView)findViewById(R.id.singleTitle);

		String dbName = c.getString(c.getColumnIndexOrThrow(ContactsProvider.NAME));
		String dbTitle = c.getString(c.getColumnIndexOrThrow(ContactsProvider.TITLE));
		String dbEmail = c.getString(c.getColumnIndexOrThrow(ContactsProvider.EMAIL));
		String dbDivision = c.getString(c.getColumnIndexOrThrow(ContactsProvider.DIVISION));
		String dbWorkplace = c.getString(c.getColumnIndexOrThrow(ContactsProvider.WORKPLACE));
		String dbPhone = c.getString(c.getColumnIndexOrThrow(ContactsProvider.PHONE));
		String dbGsm = c.getString(c.getColumnIndexOrThrow(ContactsProvider.MOBILE));
		String dbImgUrl = c.getString(c.getColumnIndexOrThrow(ContactsProvider.IMAGE_URL));
		if(dbImgUrl.length() > 0) {
			IV_profilePic.setImageFromUrl(dbImgUrl);
		}

		List<HashMap<String, String>> valueList = new ArrayList<HashMap<String, String>>();
		String descriptiveDescription[] = {getString(R.string.email),getString(R.string.division),getString(R.string.workplace),getString(R.string.phone),getString(R.string.mobile)};
		String descriptiveData[] = {dbEmail,dbDivision,dbWorkplace,dbPhone,dbGsm};
		int i=0;
		for(String data : descriptiveData)
		{
			if(TextUtils.isEmpty(data)) {
				i++;
				continue;
			}
			HashMap<String, String> map = new HashMap<String, String>();
			map.put("description",descriptiveDescription[i]);
			if(descriptiveDescription[i] == getString(R.string.email))
			{
				data = data.replace("mailto:", "");
			}
			map.put("value",data);
			valueList.add(map);	
			i++;
		}

		String[] from = new String[] {"description","value"};
		int[] to = new int[] { R.id.singleListDescription, R.id.singleListValue  };
		dAdapter = new SingleEmployeeAdapter(this,valueList,R.layout.single_employee_listview_item,from,to );
		lv = (ListView) findViewById(R.id.singleDescriptionListview);
		lv.setAdapter(dAdapter);
		dAdapter.setCallButtonListener(callButtonListener);
		lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {

				Object o = lv.getItemAtPosition(position);
				@SuppressWarnings("unchecked")
				HashMap<String,String> map = (HashMap<String,String>) o;
				String clickedDescription = (String)map.get("description");
				String clickedValue = (String)map.get("value");
				if(clickedDescription == getString(R.string.email))
				{
					Intent i = new Intent(Intent.ACTION_VIEW);
					Uri mailData = Uri.parse("mailto:"+ (String)map.get("value"));
					i.setData(mailData);
					startActivity(Intent.createChooser(i, "Send email"));
				}
				if(clickedDescription == getString(R.string.phone) || clickedDescription == getString(R.string.mobile))
				{
					try {
						String strNumber = SystemUtils.fixPhoneNumber(clickedValue);
						Intent callIntent = new Intent(Intent.ACTION_CALL);
						callIntent.setData(Uri.parse("tel:"+strNumber));
						startActivity(callIntent);
					} catch (ActivityNotFoundException e) {
						Log.e("Call function, onClickListener", "Call failed", e);
					}
				} else if(clickedDescription == getString(R.string.workplace)) {
					clickedValue = clickedValue.replaceAll("[0-9]", "");
					String nearString = "&near=iceland";
					if(clickedValue.contains("Grens‡s")) {
						nearString = "&near=reykjavik,iceland"; 
					}
					Intent mapIntent = new Intent(Intent.ACTION_VIEW);
					mapIntent.setData(Uri.parse("geo:0,0?q="+clickedValue+nearString));
					startActivity(mapIntent);
				}
			}
		});

		tv_name.setText(dbName);
		tv_title.setText(dbTitle);
	}
	
	protected OnClickListener callButtonListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			final int position = lv.getPositionForView(v);
			Object o = lv.getItemAtPosition(position);
			@SuppressWarnings("unchecked")
			HashMap<String,String> map = (HashMap<String,String>) o;
			String clickedDescription = (String)map.get("description");
			String clickedValue = (String)map.get("value");
			if(clickedDescription == getString(R.string.phone) || clickedDescription == getString(R.string.mobile))
			{
				try {
					String strNumber = SystemUtils.fixPhoneNumber(clickedValue);
					Intent callIntent = new Intent(Intent.ACTION_CALL);
					callIntent.setData(Uri.parse("tel:"+strNumber));
					startActivity(callIntent);
				} catch (ActivityNotFoundException e) {
					Log.e("Call function, onClickListener", "Call failed", e);
				}
			}
		}
	};
}