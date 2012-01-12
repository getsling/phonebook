package com.gangverk.phonebook;

import java.util.ArrayList;

import com.gangverk.phonebook.database.ContactsProvider;
import com.gangverk.phonebook.database.DownloadPhonebookAsyncTask;
import com.gangverk.phonebook.service.MannvitService;
import com.gangverk.phonebook.utils.AlphabetizedAdapter;

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

public class PhonebookActivity extends ListActivity {

	private ListView lv;
	private AlphabetizedAdapter aAdapter;
	private SharedPreferences settings; 

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		startService(new Intent(getApplicationContext(),MannvitService.class));
		settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		new DownloadPhonebookAsyncTask(getApplicationContext()).execute();
		lv = getListView();
		lv.setFastScrollEnabled(true);
		fillPhoneBook(-1);
		registerForContextMenu(lv);
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
		if(menuItemName.equals(getResources().getString(R.string.default_workphone))) {
			Editor editor = settings.edit();
			editor.putBoolean(String.format("phone_%d",info.id), true);
			editor.commit();
			fillPhoneBook(truePosition);
		}
		else if(menuItemName.equals(getResources().getString(R.string.default_mobile))) {
			Editor editor = settings.edit();
			editor.putBoolean(String.format("phone_%d",info.id), false);
			editor.commit();
			fillPhoneBook(truePosition);
		}
		else if(menuItemName.equals(getResources().getString(R.string.add_to_contacts))) {
			ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
			Uri singleContact = Uri.parse("content://com.gangverk.phonebook.Contacts/contacts/" + info.id);
			Cursor c = managedQuery(singleContact, null, null, null, null);
			c.moveToFirst();
			String workPhone = c.getString(c.getColumnIndex(ContactsProvider.PHONE));
			String mobile = c.getString(c.getColumnIndex(ContactsProvider.MOBILE));
			String name = c.getString(c.getColumnIndex(ContactsProvider.NAME));

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
		Intent i = new Intent(this, SingleEmployee.class);   
		i.putExtra(ContactsProvider._ID, trueID);
		startActivity(i); 
	}

	private void fillPhoneBook(int position) 
	{
		Uri allContacts = Uri.parse("content://com.gangverk.phonebook.Contacts/contacts");
		Cursor c = managedQuery(allContacts, null, null, null, null);
		String[] from = new String[] { ContactsProvider.NAME, ContactsProvider.MOBILE};
		int[] to = new int[] { R.id.textLarge, R.id.textSmall1 };
		aAdapter = new AlphabetizedAdapter(getApplicationContext(), R.layout.phone_item, c, from, to,0);
		aAdapter.setCallButtonListener(callButtonListener);
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
					String strNumber = null;
					boolean usesPhoneNumber = settings.getBoolean(String.format("phone_%d",id),false);
					if(usesPhoneNumber)	{
						strNumber = c.getString(c.getColumnIndex(ContactsProvider.PHONE));
					} else {
						strNumber = c.getString(c.getColumnIndexOrThrow(ContactsProvider.MOBILE));
					}
					strNumber = strNumber.replace("+", "00").replaceAll("[^0-9]","");
					@SuppressWarnings("unused") // Parse testing so technically used
					long longNum = Long.parseLong(strNumber);
					Intent callIntent = new Intent(Intent.ACTION_CALL);
					callIntent.setData(Uri.parse("tel:"+strNumber));
					startActivity(callIntent);
				} catch (ActivityNotFoundException e) {
					Log.e("Call function, onClickListener", "Call failed", e);
				} catch (NumberFormatException e) {
					Toast.makeText(getApplicationContext(), getString(R.string.invalid_number), Toast.LENGTH_LONG).show();
				}
			}
		}
	};

}


