package com.gangverk.phonebook;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.Menu;
import android.widget.ListView;

public class FilteredActivity extends PhonebookActivity {

	private ListView lv;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	protected void initializeActivity() {
		settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		lv = (ListView)findViewById(R.id.mainListView);
		lv.setTextFilterEnabled(true);
		lv.setFastScrollEnabled(true);
		Bundle extras = getIntent().getExtras();
		if(extras != null) {
			String query = extras.getString("com.gangverk.phonebook.query");
			int flags = extras.getInt("com.gangverk.phonebook.filter_type");
			fillPhoneBook(-1,query,lv,flags);
		}
		registerForContextMenu(lv);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
	}
	
	@Override 
	public boolean onKeyDown(int keyCode, KeyEvent event) { 
		if (keyCode == KeyEvent.KEYCODE_SEARCH) 
		{ 
			return true; 
		} 
		return super.onKeyDown(keyCode, event); 
	}
}
