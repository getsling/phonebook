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
import android.widget.TextView;

public class PhonebookActivity extends ListActivity {

	private DatabaseHelper myDB = new DatabaseHelper(this);
	private Cursor c;
	ListView lv;
	AlphabetizedAdapter aAdapter;
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		lv = getListView();
		//lv.setTextFilterEnabled(true);
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
		c = managedQuery(allContacts, null, null, null, null);
		String[] from = new String[] { ContactsProvider.NAME, ContactsProvider.MOBILE};
		int[] to = new int[] { R.id.textLarge, R.id.textSmall1 };
		aAdapter = new AlphabetizedAdapter(this, R.layout.phone_item, c, from, to,0);
		aAdapter.setCallButtonListener(callButtonListener);
		lv.setAdapter(aAdapter);
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
			View parentListView = getListView().getChildAt(position);
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
		fillPhoneBook();
	}
}
/*public Loader<Cursor> onCreateLoader(int id, Bundle args) {       
		// This is called when a new Loader needs to be created.  This        
		// sample only has one Loader, so we don't care about the ID.        
		// First, pick the base URI to use depending on whether we are        
		// currently filtering.        
		Uri baseUri;        
		if (mCurFilter != null) {            
			baseUri = Uri.withAppendedPath(Contacts.CONTENT_FILTER_URI,                    
					Uri.encode(mCurFilter));        
		} else {            
			baseUri = Contacts.CONTENT_URI;        
		}        
		// Now create and return a CursorLoader that will take care of        
		// creating a Cursor for the data being displayed.        
		String select = "((" + Contacts.DISPLAY_NAME + " NOTNULL) AND ("                
				+ Contacts.HAS_PHONE_NUMBER + "=1) AND ("                
				+ Contacts.DISPLAY_NAME + " != '' ))";        
		return new CursorLoader(getActivity(), baseUri,CONTACTS_SUMMARY_PROJECTION, select, null,                
				Contacts.DISPLAY_NAME + " COLLATE LOCALIZED ASC");    
	}    
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {        
		// Swap the new cursor in.  		(The framework will take care of closing the        
		// old cursor once we return.)        mAdapter.swapCursor(data);        
		// The list should now be shown.        
		if (isResumed()) {            
			setListShown(true);        
		} else {            
			setListShownNoAnimation(true);        
		}    
	}    

	public void onLoaderReset(Loader<Cursor> loader) 
	{        
		// This is called when the last Cursor provided to onLoadFinished()        
		// above is about to be closed.  We need to make sure we are no        
		// longer using it.        
		mAdapter.swapCursor(null);    
	}
}*/


