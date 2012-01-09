package com.gangverk.phonebook;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class SingleEmployee extends Activity {
	private Cursor c;
	ListView lv;
	SimpleAdapter dAdapter;
	long userID;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.single_employee);
		Bundle extras = getIntent().getExtras();
		userID = extras.getLong(ContactsProvider._ID); 

		AssetManager assetManager = getAssets();

		InputStream istr = null;
		try {
			istr = assetManager.open("profile/img_"+userID+".jpg");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Bitmap bitmap = BitmapFactory.decodeStream(istr);

		ImageView IV_profilePic = (ImageView)findViewById(R.id.profilePic);
		IV_profilePic.setImageBitmap(bitmap);

		Uri allContacts = Uri.parse("content://com.gangverk.phonebook.Contacts/contacts/" + userID);
		c = managedQuery(allContacts, null, null, null, null);
		c.moveToFirst();
		TextView tv_name = (TextView)findViewById(R.id.singleName);
		TextView tv_title = (TextView)findViewById(R.id.singleTitle);
		//TextView tv_email = (TextView)findViewById(R.id.singleEmail);
		//TextView tv_divisionWorkplace = (TextView)findViewById(R.id.singleDivisionWorkplace);

		String dbName = c.getString(c.getColumnIndexOrThrow(ContactsProvider.NAME));
		String dbTitle = c.getString(c.getColumnIndexOrThrow(ContactsProvider.TITLE));
		String dbEmail = c.getString(c.getColumnIndexOrThrow(ContactsProvider.EMAIL));
		String dbDivision = c.getString(c.getColumnIndexOrThrow(ContactsProvider.DIVISION));
		String dbWorkplace = c.getString(c.getColumnIndexOrThrow(ContactsProvider.WORKPLACE));
		String dbPhone = c.getString(c.getColumnIndexOrThrow(ContactsProvider.PHONE));
		String dbGsm = c.getString(c.getColumnIndexOrThrow(ContactsProvider.MOBILE));

		List<HashMap<String, String>> valueList = new ArrayList<HashMap<String, String>>();
		String descriptiveDescription[] = {getString(R.string.email),"Deild","Vinnustaður","Símanúmer","Gsm"};
		String descriptiveData[] = {dbEmail,dbDivision,dbWorkplace,dbPhone,dbGsm};
		int i=0;
		for(String data : descriptiveData)
		{
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
		dAdapter = new SimpleAdapter(this,valueList,R.layout.single_employee_listview_item,from,to );
		lv = (ListView) findViewById(R.id.singleDescriptionListview);
		lv.setAdapter(dAdapter);
		lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
				
				Object o = lv.getItemAtPosition(position);
				@SuppressWarnings("unchecked")
				HashMap<String,String> map = (HashMap<String,String>) o;
				if((String)map.get("description") == getString(R.string.email))
				{
					Intent i = new Intent(Intent.ACTION_VIEW);
					Uri mailData = Uri.parse("mailto:"+ (String)map.get("value"));
					i.setData(mailData);
					startActivity(Intent.createChooser(i, "Send email"));
				}
				if((String)map.get("description") == "Símanúmer")
				{
					SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
					Editor editor = settings.edit();
					editor.putBoolean(String.format("phone_%d",userID), true);
					boolean a = settings.getBoolean(String.format("phone_%d",userID), false);
					editor.commit();
				}
			}
		});

		tv_name.setText(dbName);
		tv_title.setText(dbTitle);
	}
}