package com.gangverk.phonebook;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class SingleEmployee extends Activity {
	private Cursor c;
	//ListView lv;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.single_employee);
		Bundle extras = getIntent().getExtras();
		long userID = extras.getLong(ContactsProvider._ID); 

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
		String descriptiveDescription[] = {"Netfang","Deild","Vinnustaður","Símanúmer","Gsm"};
		String descriptiveData[] = {dbEmail,dbDivision,dbWorkplace,dbPhone,dbGsm};
		int i=0;
		for(String data : descriptiveData)
		{
			HashMap<String, String> map = new HashMap<String, String>();
			map.put("description",descriptiveDescription[i]);
			map.put("value",data);
			valueList.add(map);	
			i++;
		}

		String[] from = new String[] {"description","value"};
		int[] to = new int[] { R.id.singleListDescription, R.id.singleListValue  };
		SimpleAdapter dAdapter = new SimpleAdapter(this,valueList,R.layout.single_employee_listview_item,from,to );
		ListView lv = (ListView) findViewById(R.id.singleDescriptionListview);
		lv.setAdapter(dAdapter);
		
		tv_name.setText(dbName);
		tv_title.setText(dbTitle);
	}
}