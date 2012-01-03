package com.gangverk.phonebook;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.TextView;

public class SingleEmployee extends Activity {
	private DatabaseHelper myDB = new DatabaseHelper(this);
	private Cursor c;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.single_employee);
		Bundle extras = getIntent().getExtras();
		long userID = extras.getLong(DatabaseHelper.TABLE_ID); 
		myDB.openDataBase();
		c = myDB.fetchEmployees(userID);
		startManagingCursor(c);
		TextView tv_name = (TextView)findViewById(R.id.name);
		TextView tv_title = (TextView)findViewById(R.id.title);
		TextView tv_email = (TextView)findViewById(R.id.email);
		TextView tv_divisionWorkplace = (TextView)findViewById(R.id.divisionWorkplace);
		tv_name.setText(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.EMPLOYEE_KEY_NAME.split("\\.")[1])));
		tv_title.setText(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.EMPLOYEE_KEY_TITLE.split("\\.")[1])));
		tv_email.setText(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.EMPLOYEE_KEY_EMAIL.split("\\.")[1])));
		CharSequence divisionWorkplace = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.DIVISION_KEY_NAME.split("\\.")[1])) + " / " + c.getString(c.getColumnIndexOrThrow(DatabaseHelper.WORKPLACE_KEY_ADDRESS.split("\\.")[1]));
		tv_divisionWorkplace.setText(divisionWorkplace);
		myDB.close();


		//Log.d("ID notanda",String.valueOf(extras.getLong(DatabaseHelper.TABLE_ID)));
	}
}