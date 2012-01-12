package com.gangverk.phonebook;

import com.gangverk.phonebook.database.ContactsProvider;
import com.gangverk.phonebook.utils.AlphabetizedAdapter;

import android.app.Activity;
import android.app.SearchManager;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

public class SearchActivity extends Activity {
	private ListView mListView;
	private TextView mTextView; 
	private AlphabetizedAdapter results;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.search);
		mTextView = (TextView)findViewById(R.id.noOfResults);
		mListView = (ListView)findViewById(R.id.searchListview);
		// Get the intent, verify the action and get the query
		Intent intent = getIntent();
		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			String query = intent.getStringExtra(SearchManager.QUERY);
			
			showResults(query);
		}
	}
	
	private void showResults(String query) {
		Uri searchContact = Uri.parse("content://com.gangverk.phonebook.Contacts/contacts");
		String querySearch = query + "%";
		Cursor cursor = managedQuery(searchContact, null, ContactsProvider.NAME + " LIKE ?", new String[]{querySearch}, null);
		if(!cursor.moveToFirst()) {
			// There are no results
			mTextView.setText(getString(R.string.no_results, new Object[] {query}));
		} else {
			// Display the number of results
			int count = cursor.getCount();
			String countString = getResources().getQuantityString(R.plurals.search_results,	count, new Object[] {count, query});
			mTextView.setText(countString);

			// Specify the columns we want to display in the result
			String[] from = new String[] { ContactsProvider.NAME,ContactsProvider.MOBILE };

			// Specify the corresponding layout elements where we want the columns to go
			int[] to = new int[] { R.id.textLarge,R.id.textSmall1 };

			// Create a simple cursor adapter for the definitions and apply them to the ListView
			results = new AlphabetizedAdapter(getApplicationContext(),R.layout.phone_item, cursor, from, to, 0);
			results.setCallButtonListener(callButtonListener);
			mListView.setAdapter(results);
			mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id){
					Cursor cursor = (Cursor)mListView.getItemAtPosition(position);
					int intID = cursor.getInt(cursor.getColumnIndexOrThrow(ContactsProvider._ID));
					long trueID = Long.valueOf(intID);
					Intent i = new Intent(getApplicationContext(), SingleEmployee.class);   
					i.putExtra(ContactsProvider._ID, trueID);
					startActivity(i); 
					//Do stuff
				}
			});
		}
	}



	private OnClickListener callButtonListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			final int position = mListView.getPositionForView(v);
			long id = results.getItemId(position);
			if (position != ListView.INVALID_POSITION) {
				try {
					Uri singleContact = Uri.parse("content://com.gangverk.phonebook.Contacts/contacts/"+id);
					Cursor c = managedQuery(singleContact, null, null, null, null);
					c.moveToFirst();
					String strNumber = c.getString(c.getColumnIndexOrThrow(ContactsProvider.MOBILE));
					if(strNumber == "")
					{
						strNumber = c.getString(c.getColumnIndexOrThrow(ContactsProvider.PHONE));
					}
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
