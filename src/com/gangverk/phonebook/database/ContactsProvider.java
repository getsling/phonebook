package com.gangverk.phonebook.database;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

public class ContactsProvider extends ContentProvider {

	//public static final String TABLE = "employee AS e "
	//        + "JOIN workplace ON (employee.workplace_id = workplace._id) "
	//        + "JOIN division ON (employee.division_id = division._id)";
	public static final String TABLE = "employeeInfo";
	public static SQLiteDatabase contactsDB;
	public static final String PROVIDER_NAME = 
			"com.gangverk.phonebook.Contacts";

	public static final Uri CONTENT_URI = 
			Uri.parse("content://"+ PROVIDER_NAME + "/contacts");


	public static final String KEY_WORD = SearchManager.SUGGEST_COLUMN_TEXT_1;
	public static final String KEY_DEFINITION = SearchManager.SUGGEST_COLUMN_TEXT_2;
	public static final String _ID = "_id";
	public static final String NAME = "employee";
	public static final String TITLE = "title";
	public static final String MOBILE = "mobile";
	public static final String PHONE = "phone";
	public static final String EMAIL = "email";
	public static final String WORKPLACE = "workplace";
	public static final String DIVISION = "division";

	private static final int CONTACTS = 1;
	private static final int CONTACT_ID = 2;   
	private static final UriMatcher uriMatcher;
	static{
		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		uriMatcher.addURI(PROVIDER_NAME, "contacts", CONTACTS);
		uriMatcher.addURI(PROVIDER_NAME, "contacts/#", CONTACT_ID);
	}

	@Override
	public boolean onCreate() {
		Context context = getContext();
		DatabaseHelper myDB = new DatabaseHelper(context);
		contactsDB = myDB.getReadableDatabase();
		return (contactsDB == null)? false:true;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getType(Uri uri) {
		switch (uriMatcher.match(uri)){
		// Get all contacts
		case CONTACTS:
			return "vnd.android.cursor.dir/vnd.gangverk.phonebook.contacts ";
			// Get a particular contact
		case CONTACT_ID:                
			return "vnd.android.cursor.item/vnd.gangverk.phonebook.contacts ";
		default:
			throw new IllegalArgumentException("Unsupported URI: " + uri);
		}
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder sqlBuilder = new SQLiteQueryBuilder();
		sqlBuilder.setTables(TABLE);
		int uriMatch = uriMatcher.match(uri);
		if (uriMatch == CONTACT_ID)
			// If getting a particular contact
			sqlBuilder.appendWhere(
					_ID + " = " + uri.getPathSegments().get(1));  
		if (sortOrder==null || sortOrder=="")
			sortOrder = NAME + " COLLATE LOCALIZED ASC";

		Cursor c = sqlBuilder.query(
				contactsDB, 
				projection, 
				selection, 
				selectionArgs, 
				null, 
				null, 
				sortOrder);
		//---register to watch a content URI for changes---
		c.setNotificationUri(getContext().getContentResolver(), uri);
		return c;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}
}