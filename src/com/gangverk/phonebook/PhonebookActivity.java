package com.gangverk.phonebook;

import java.io.IOException;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

public class PhonebookActivity extends ListActivity {

	private DatabaseHelper myDB = new DatabaseHelper(this);
	private Cursor c;
	private SimpleCursorAdapter employee;

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
		Intent i = new Intent(this, SingleEmployee.class);   
		i.putExtra(DatabaseHelper.TABLE_ID, id);
		startActivity(i); 
		/*try {
			String item = ((TextView)v.findViewById(R.id.text1)).getText().toString();
			Toast.makeText(this, item + " selected", Toast.LENGTH_LONG).show();
		} catch(Exception e) {
			Log.e("Listitemclick",e.getMessage()); e.printStackTrace(); }*/
	}

	private void fillPhoneBook() 
	{
		// Get all of the notes from the database and create the item list
		c = myDB.fetchEmployees();
		startManagingCursor(c);
		String[] from = new String[] { DatabaseHelper.EMPLOYEE_KEY_NAME.split("\\.")[1], DatabaseHelper.EMPLOYEE_KEY_MOBILE.split("\\.")[1]};
		int[] to = new int[] { R.id.textLarge, R.id.textSmall1 };
		employee = new PhoneAdapter(this, R.layout.phone_item, c, from, to);
		setListAdapter(employee);
	}

	private class PhoneAdapter extends SimpleCursorAdapter {
		
		private Context context;
		private Cursor cur;
		private int layout;
		public PhoneAdapter(Context context, int layout, Cursor c,String[] from, int[] to) {
			super(context, layout, c, from, to);
			this.context = context;
			this.cur = c;
			this.layout = layout;
			PhonebookActivity.this.c = null;
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			View view = super.newView(context, cursor, parent);
			ImageButton btnCall = (ImageButton)view.findViewById(R.id.btnCall);
			btnCall.setOnClickListener(callButtonListener);
			return view;
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			super.bindView(view, context, cursor);
		}

	}

	private OnClickListener callButtonListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			final int position = getListView().getPositionForView(v);
			if (position != ListView.INVALID_POSITION) {
				Toast.makeText(getApplicationContext(), "hæhæ", Toast.LENGTH_SHORT).show();
				//showMessage(getString(R.string.you_want_to_buy_format, CHEESES[position]));
			}
		}
	};

	@Override
	protected void onStop() {
		try {
			super.onStop();

			if (this.employee !=null){
				this.employee.getCursor().close();
				this.employee= null;
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

	@Override
	protected void onResume() {
		super.onResume();
		myDB.openDataBase();
		fillPhoneBook();
		myDB.close();
	}
}

