package com.gangverk.phonebook;

import java.io.IOException;

import android.app.ListActivity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;
import android.widget.TextView;

public class PhonebookActivity extends ListActivity {

	private DatabaseHelper myDB = new DatabaseHelper(this);
	private Cursor c;
	//PhoneAdapter aAdapter;
	AlphabetizedAdapter aAdapter;
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//setContentView(R.layout.main);
		try {
			myDB.createDataBase();
		} catch (IOException ioe) {
			throw new Error("Unable to create database");
		}
		try {
			myDB.openDataBase();
		} catch(SQLException sqle){
			throw sqle;
		}

		fillPhoneBook();
		myDB.close();
		ListView lv = getListView();
		lv.setTextFilterEnabled(true);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		Cursor cursor = (Cursor)l.getItemAtPosition(position);
		int intID = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.TABLE_ID));
		long trueID = Long.valueOf(intID);
		Intent i = new Intent(this, SingleEmployee.class);   
		i.putExtra(DatabaseHelper.TABLE_ID, trueID);
		startActivity(i); 
	}

	private void fillPhoneBook() 
	{
		// Get all of the notes from the database and create the item list
		c = myDB.fetchEmployees();
		startManagingCursor(c);
		String[] from = new String[] { DatabaseHelper.EMPLOYEE_KEY_NAME.split("\\.")[1], DatabaseHelper.EMPLOYEE_KEY_MOBILE.split("\\.")[1]};
		int[] to = new int[] { R.id.textLarge, R.id.textSmall1 };
		aAdapter = new AlphabetizedAdapter(this, R.layout.phone_item, c, from, to,0);
		//aAdapter = new PhoneAdapter(this, R.layout.phone_item, c, from, to);
		aAdapter.setCallButtonListener(callButtonListener);
		setListAdapter(aAdapter);
	}

	@Override
	protected void onStop() {
		try {
			super.onStop();

			if (this.aAdapter !=null){
				this.aAdapter.getCursor().close();
				this.aAdapter= null;
			}

			if (this.c != null) {
				this.c.close();
			}

			if (this.myDB != null) {
				this.myDB .close();
			}
		} catch (Exception error) {
			Log.e("OnStop error", error.getMessage());
		}
	}

	private OnClickListener callButtonListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			final int position = getListView().getPositionForView(v);
			View parentListView = getListView();
			if (position != ListView.INVALID_POSITION) {
				try {
					TextView number = (TextView) parentListView.findViewById(R.id.textSmall1);
					int intNum = Integer.parseInt(number.getText().toString());
					Intent callIntent = new Intent(Intent.ACTION_CALL);
					callIntent.setData(Uri.parse("tel:"+intNum));
					startActivity(callIntent);
				} catch (ActivityNotFoundException e) {
					Log.e("Call function, onClickListener", "Call failed", e);
				}
			}
		}
	};
	@Override
	protected void onResume() {
		super.onResume();
		myDB.openDataBase();
		fillPhoneBook();
		myDB.close();
	}
}



