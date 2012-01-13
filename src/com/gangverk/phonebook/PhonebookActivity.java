package com.gangverk.phonebook;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.ListActivity;
import android.content.ActivityNotFoundException;
import android.content.ContentProviderOperation;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.Contacts.Data;
import android.provider.ContactsContract.RawContacts;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.gangverk.phonebook.database.ContactsProvider;
import com.gangverk.phonebook.database.DownloadPhonebookAsyncTask;
import com.gangverk.phonebook.service.MannvitService;
import com.gangverk.phonebook.utils.AlphabetizedAdapter;
import com.gangverk.phonebook.utils.SystemUtils;

public class PhonebookActivity extends ListActivity {

	private ListView lv;
	private AlphabetizedAdapter aAdapter;
	private SharedPreferences settings; 
	private Map<Integer, Integer> mapNumberPrefs = null;
	private static final String SETTINGS_KEY_LAST_DOWNLOAD_CHECK_TIME = "lastDownloadCheckTime";
	private static final int CHECK_DB_INTERVAL_HOURS = 144; // One week
	public static final int NUMBER_PREFERENCE_PHONE = 0;
	public static final int NUMBER_PREFERENCE_MOBILE = 1;
	public static final String SETTINGS_JSON_NUMBER_PREFERENCE = "settingsJSONNumberPreference";


	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		startService(new Intent(getApplicationContext(),MannvitService.class));
		settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		long lastGuideDownloadCheckTime = settings.getLong(SETTINGS_KEY_LAST_DOWNLOAD_CHECK_TIME, 0);
		long currentTime = System.currentTimeMillis() / 1000;
		if(currentTime - lastGuideDownloadCheckTime > CHECK_DB_INTERVAL_HOURS * 60 * 60) {
			new DownloadPhonebookAsyncTask(getApplicationContext()).execute();
		}
		lv = getListView();
		lv.setFastScrollEnabled(true);
		fillPhoneBook(-1);
		registerForContextMenu(lv);
		Log.d("onCreate","OC started");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.options_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.search:
			onSearchRequested();
			return true;
		default:
			return false;
		}
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
		Uri singleContact = Uri.parse("content://com.gangverk.phonebook.Contacts/contacts/" + info.id);
		Cursor c = managedQuery(singleContact, null, null, null, null);
		c.moveToFirst();
		menu.setHeaderTitle(c.getString(c.getColumnIndex(ContactsProvider.NAME)));
		String[] menuItems = getResources().getStringArray(R.array.phonebook_context_menu);
		for(int i = 0; i<menuItems.length;i++) {
			menu.add(Menu.NONE,i,i,menuItems[i]);
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
		ListView lvContext = getListView();
		int truePosition = lvContext.getFirstVisiblePosition();
		int menuItemIndex = item.getItemId();
		String[] menuItems = getResources().getStringArray(R.array.phonebook_context_menu);
		String menuItemName = menuItems[menuItemIndex];
		Uri singleContact = Uri.parse("content://com.gangverk.phonebook.Contacts/contacts/" + info.id);
		Cursor c = managedQuery(singleContact, null, null, null, null);
		c.moveToFirst();
		String workPhone = c.getString(c.getColumnIndex(ContactsProvider.PHONE));
		String mobile = c.getString(c.getColumnIndex(ContactsProvider.MOBILE));
		String name = c.getString(c.getColumnIndex(ContactsProvider.NAME));

		if(menuItemName.equals(getResources().getString(R.string.default_workphone))) {
			if(workPhone.length() != 0) {
				JSONObject numberPreferences = null;
				try {
					numberPreferences = new JSONObject(settings.getString(SETTINGS_JSON_NUMBER_PREFERENCE, "{}"));
					numberPreferences.put(String.valueOf(info.id), NUMBER_PREFERENCE_PHONE);
					Editor editor = settings.edit();
					editor.putString(SETTINGS_JSON_NUMBER_PREFERENCE, numberPreferences.toString());
					editor.commit();
					fillPhoneBook(truePosition);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			else {
				Toast.makeText(getApplicationContext(), getResources().getString(R.string.number_not_existing), Toast.LENGTH_SHORT).show();
			}
		}
		else if(menuItemName.equals(getResources().getString(R.string.default_mobile))) {
			if(mobile.length() != 0) {
				JSONObject numberPreferences = null;
				try {
					numberPreferences = new JSONObject(settings.getString(SETTINGS_JSON_NUMBER_PREFERENCE, "{}"));
					numberPreferences.put(String.valueOf(info.id), NUMBER_PREFERENCE_MOBILE);
					Editor editor = settings.edit();
					editor.putString(SETTINGS_JSON_NUMBER_PREFERENCE, numberPreferences.toString());
					editor.commit();
					fillPhoneBook(truePosition);
				}  catch (JSONException e) {
					e.printStackTrace();
				}
			}else {
				Toast.makeText(getApplicationContext(), getResources().getString(R.string.number_not_existing), Toast.LENGTH_SHORT).show();
			}
		}
		else if(menuItemName.equals(getResources().getString(R.string.add_to_contacts))) {
			ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
			ops.add(ContentProviderOperation.newInsert(RawContacts.CONTENT_URI)
					.withValue(RawContacts.ACCOUNT_TYPE, null)
					.withValue(RawContacts.ACCOUNT_NAME,null )
					.build());
			ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
					.withValueBackReference(Data.RAW_CONTACT_ID, 0)
					.withValue(Data.MIMETYPE,Phone.CONTENT_ITEM_TYPE)
					.withValue(Phone.NUMBER, workPhone)
					.withValue(Phone.TYPE, Phone.TYPE_WORK)
					.build());
			ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
					.withValueBackReference(Data.RAW_CONTACT_ID, 0)
					.withValue(Data.MIMETYPE,Phone.CONTENT_ITEM_TYPE)
					.withValue(Phone.NUMBER, mobile)
					.withValue(Phone.TYPE, Phone.TYPE_MOBILE)
					.build());
			ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
					.withValueBackReference(Data.RAW_CONTACT_ID, 0)
					.withValue(Data.MIMETYPE,StructuredName.CONTENT_ITEM_TYPE)
					.withValue(StructuredName.DISPLAY_NAME, name)
					.build());  
			try {
				getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
				Toast.makeText(getApplicationContext(), getResources().getString(R.string.add_success), Toast.LENGTH_SHORT).show();
			} catch (RemoteException e) {
				e.printStackTrace();
			} catch (OperationApplicationException e) {
				e.printStackTrace();
			}
		}
		else if(menuItemName.equals(getResources().getString(R.string.txtCallContact))) {

			String strNumber = null;
			boolean usesPhoneNumber = false;

			if(mobile.length() == 0 || checkPhone((int)info.id,mapNumberPrefs,NUMBER_PREFERENCE_PHONE)) {
				usesPhoneNumber = true;
			}
			if(usesPhoneNumber)	{
				strNumber = workPhone;
			} else {
				strNumber = mobile;
			}
			strNumber = SystemUtils.fixPhoneNumber(strNumber);
			if(strNumber.length() != 0) {
				Intent callIntent = new Intent(Intent.ACTION_CALL);
				callIntent.setData(Uri.parse("tel:"+strNumber));
				startActivity(callIntent);
			} else {
				Toast.makeText(getApplicationContext(), getString(R.string.invalid_number), Toast.LENGTH_LONG).show();
			}
		} else if(menuItemName.equals(getResources().getString(R.string.send_sms))) {
			mobile = SystemUtils.fixPhoneNumber(mobile);
			if(mobile.length() != 0) {
				Intent sendIntent = new Intent(Intent.ACTION_VIEW);         
				sendIntent.setData(Uri.parse("sms:" + mobile));
				startActivity(sendIntent);
			} else { 
				Toast.makeText(getApplicationContext(), getString(R.string.mobile_missing), Toast.LENGTH_LONG).show();
			}
		}
		else {
			return false;
		}
		return true;
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		Cursor cursor = (Cursor)l.getItemAtPosition(position);
		int intID = cursor.getInt(cursor.getColumnIndexOrThrow(ContactsProvider._ID));
		long trueID = Long.valueOf(intID);
		Intent i = new Intent(this, SingleEmployeeActivity.class);   
		i.putExtra(ContactsProvider._ID, trueID);
		startActivity(i); 
	}

	private void fillPhoneBook(int position) 
	{
		try {
			JSONObject preferredNumbers = new JSONObject(settings.getString(SETTINGS_JSON_NUMBER_PREFERENCE, "{}"));
			Iterator<?> iter = preferredNumbers.keys();
			mapNumberPrefs = new HashMap<Integer, Integer>();
			while(iter.hasNext()){
				String key = (String)iter.next();
				mapNumberPrefs.put(Integer.parseInt(key), preferredNumbers.getInt(key));
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		Uri allContacts = Uri.parse("content://com.gangverk.phonebook.Contacts/contacts");
		Cursor c = managedQuery(allContacts, null, null, null, null);
		String[] from = new String[] { ContactsProvider.NAME, ContactsProvider.MOBILE};
		int[] to = new int[] { R.id.textLarge, R.id.textSmall1 };
		aAdapter = new AlphabetizedAdapter(getApplicationContext(), R.layout.phone_item, c, from, to,0);
		aAdapter.setCallButtonListener(callButtonListener);
		aAdapter.updateNumberPreferences(mapNumberPrefs);
		lv.setAdapter(aAdapter);
		if(position != -1)
			lv.setSelection(position);
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	protected void onResume() {
		super.onResume();
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
					String mobile = c.getString(c.getColumnIndex(ContactsProvider.MOBILE));
					String workPhone = c.getString(c.getColumnIndex(ContactsProvider.PHONE));
					String strNumber = null;
					boolean usesPhoneNumber = false;
					if(mobile.length() == 0 || checkPhone((int)id,mapNumberPrefs,NUMBER_PREFERENCE_PHONE)) {
						usesPhoneNumber = true;
					}
					if(usesPhoneNumber)	{
						strNumber = workPhone;
					} else {
						strNumber = mobile;
					}
					strNumber = SystemUtils.fixPhoneNumber(strNumber);
					if(strNumber.length() != 0) {
						Intent callIntent = new Intent(Intent.ACTION_CALL);
						callIntent.setData(Uri.parse("tel:"+strNumber));
						startActivity(callIntent);
					} else {
						Toast.makeText(getApplicationContext(), getString(R.string.invalid_number), Toast.LENGTH_LONG).show();
					}
				} catch (ActivityNotFoundException e) {
					Log.e("Call function, onClickListener", "Call failed", e);
				}
			}
		}
	};

	public static boolean checkPhone(int id, Map<Integer,Integer> mapNumberPrefs, int compareToConst) {
		boolean prefersPhone = false;
		if(mapNumberPrefs != null) {
			Integer keyValue = mapNumberPrefs.get(id);
			if(keyValue!=null && keyValue.intValue() == compareToConst) {
				prefersPhone = true;
			}
		}
		return prefersPhone;
	}
}


