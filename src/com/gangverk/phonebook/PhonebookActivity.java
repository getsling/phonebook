package com.gangverk.phonebook;

import java.io.IOException;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.os.Bundle;
import android.provider.Contacts.People;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

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
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			return super.newView(context, cursor, parent);
			/*Cursor c = getCursor();
			final LayoutInflater inflater = LayoutInflater.from(context);
			View v = inflater.inflate(layout, parent, false);
			int nameCol = c.getColumnIndex(People.NAME);
			String name = c.getString(nameCol);
			/**
			 * Next set the name of the entry.
			 */    
			/*TextView name_text = (TextView) v.findViewById(R.id.name_entry);
			if (name_text != null) {
				name_text.setText(name);
			}
			return v;*/
		}

		@Override
		public void bindView(View v, Context context, Cursor c) {
			super.bindView(v, context, c);
			/*int nameCol = c.getColumnIndex(People.NAME);
			String name = c.getString(nameCol);
			/**
			 * Next set the name of the entry.
			 */    
			/*TextView name_text = (TextView) v.findViewById(R.id.name_entry);
			if (name_text != null) {
				name_text.setText(name);
			}*/
		}

	}

	private OnClickListener PhoneButtonListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			final int position = getListView().getPositionForView(v);
			if (position != ListView.INVALID_POSITION) {
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

