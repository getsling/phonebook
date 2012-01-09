package com.gangverk.phonebook;

import android.app.ListActivity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;

public class PhonebookActivity extends ListActivity {

	private static final String LOG_TAG = "PhonebookActivity";
	ListView lv;
	AlphabetizedAdapter aAdapter;
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		new DownloadPhonebookAsyncTask(getApplicationContext()).execute();
		lv = getListView();
		lv.setTextFilterEnabled(true);
		lv.setFastScrollEnabled(true);
		fillPhoneBook();
	}


	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		Cursor cursor = (Cursor)l.getItemAtPosition(position);
		int intID = cursor.getInt(cursor.getColumnIndexOrThrow(ContactsProvider._ID));
		long trueID = Long.valueOf(intID);
		Intent i = new Intent(this, SingleEmployee.class);   
		i.putExtra(ContactsProvider._ID, trueID);
		startActivity(i); 
	}

	private void fillPhoneBook() 
	{
		Uri allContacts = Uri.parse("content://com.gangverk.phonebook.Contacts/contacts");
		Cursor c = managedQuery(allContacts, null, null, null, null);
		String[] from = new String[] { ContactsProvider.NAME, ContactsProvider.MOBILE};
		int[] to = new int[] { R.id.textLarge, R.id.textSmall1 };
		aAdapter = new AlphabetizedAdapter(getApplicationContext(), R.layout.phone_item, c, from, to,0);
		aAdapter.setCallButtonListener(callButtonListener);
		lv.setAdapter(aAdapter);
	}

	@Override
	protected void onStart() {
		super.onStart();
		Log.d(LOG_TAG,"onStart");
	}

	@Override
	protected void onStop() {
		super.onStop();
		Log.d(LOG_TAG,"onStop");
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		//fillPhoneBook();
		Log.d(LOG_TAG,"onResume");
	}
	
	private OnClickListener callButtonListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			final int position = getListView().getPositionForView(v);
			long id = aAdapter.getItemId(position);
			if (position != ListView.INVALID_POSITION) {
				try {
					Uri singleContact = Uri.parse("content://com.gangverk.phonebook.Contacts/contacts/"+id);
					Cursor c = managedQuery(singleContact, null, null, null, null);
					c.moveToFirst();
					String strNumber = c.getString(c.getColumnIndexOrThrow(ContactsProvider.MOBILE));
					strNumber = strNumber.replace("+", "00").replaceAll("[^0-9]","");
					long longNum = Long.parseLong(strNumber);
					Intent callIntent = new Intent(Intent.ACTION_CALL);
					callIntent.setData(Uri.parse("tel:"+longNum));
					startActivity(callIntent);
				} catch (ActivityNotFoundException e) {
					Log.e("Call function, onClickListener", "Call failed", e);
				}
			}
		}
	};


}


