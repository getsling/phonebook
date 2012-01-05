package com.gangverk.phonebook;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;

public class SingleEmployee extends Activity {
	private Cursor c;

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
		TextView tv_email = (TextView)findViewById(R.id.singleEmail);
		TextView tv_divisionWorkplace = (TextView)findViewById(R.id.singleDivisionWorkplace);
		
		tv_name.setText(c.getString(c.getColumnIndexOrThrow(ContactsProvider.NAME)));
		tv_title.setText(c.getString(c.getColumnIndexOrThrow(ContactsProvider.TITLE)));
		tv_email.setText(c.getString(c.getColumnIndexOrThrow(ContactsProvider.EMAIL)));
		CharSequence divisionWorkplace = c.getString(c.getColumnIndexOrThrow(ContactsProvider.DIVISION)) + " / " + c.getString(c.getColumnIndexOrThrow(ContactsProvider.WORKPLACE));
		tv_divisionWorkplace.setText(divisionWorkplace);
	}
}