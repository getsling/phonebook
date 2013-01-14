package com.gangverk.mannvit;

import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.widget.ListView;
import android.widget.TextView;

public class SearchActivity extends PhonebookActivity {
	private ListView mListView;
	private TextView mTextView; 

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
		registerForContextMenu(mListView);
		mListView.setTextFilterEnabled(false);
		mListView.setFastScrollEnabled(false);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return false;
	}

	@Override 
	public boolean onKeyDown(int keyCode, KeyEvent event) { 
		if (keyCode == KeyEvent.KEYCODE_SEARCH) 
		{ 
			return true; 
		} 
		return super.onKeyDown(keyCode, event); 
	}

	private void showResults(String query) {
		int count = fillPhoneBook(0,query,mListView,0);
		if(count == 0) {
			// There are no results
			mTextView.setText(getString(R.string.no_results, new Object[] {query}));
		} else {
			// Display the number of results
			String countString = getResources().getQuantityString(R.plurals.search_results,	count, new Object[] {count, query});
			mTextView.setText(countString);
		}
	}
}
