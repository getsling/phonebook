package com.gangverk.mannvit.database;

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
	public static SQLiteDatabase contactsDB;
	public static final String PROVIDER_NAME = "com.gangverk.mannvit.Contacts";

	public static final Uri CONTENT_URI = Uri.parse("content://"+ PROVIDER_NAME);

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
	public static final String IMAGE_URL = "image_url";

	private static final int CONTACTS = 1;
	private static final int CONTACTS_SINGLE = 2;   
	private static final int DIVISIONS = 3;   
	private static final int WORKPLACES = 4;   
	private static final UriMatcher uriMatcher;
	static{
		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		uriMatcher.addURI(PROVIDER_NAME, "contacts", CONTACTS);
		uriMatcher.addURI(PROVIDER_NAME, "contacts/#", CONTACTS_SINGLE);
		uriMatcher.addURI(PROVIDER_NAME, "divisions", DIVISIONS);
		uriMatcher.addURI(PROVIDER_NAME, "workplaces", WORKPLACES);
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
		return 0;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		return null;
	}

	@Override
	public String getType(Uri uri) {
		switch (uriMatcher.match(uri)){
		// Get all contacts
		case CONTACTS:
			return "vnd.android.cursor.dir/vnd.gangverk.mannvit.contacts";
			// Get a particular contact
		case CONTACTS_SINGLE:                
			return "vnd.android.cursor.item/vnd.gangverk.mannvit.contacts";
		default:
			throw new IllegalArgumentException("Unsupported URI: " + uri);
		}
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder sqlBuilder = new SQLiteQueryBuilder();
		
		int uriType = uriMatcher.match(uri);
		switch(uriType) {
		case CONTACTS:
			sortOrder = String.format("%s COLLATE LOCALIZED ASC",NAME);
			sqlBuilder.setTables("employeeInfo");
			break;
		case CONTACTS_SINGLE:
			sqlBuilder.setTables("employeeInfo");
			selection = "_id=?";
			selectionArgs = new String[]{uri.getLastPathSegment()};
			break;
		case DIVISIONS:
			sqlBuilder.setTables("division");
			break;
		case WORKPLACES:
			sqlBuilder.setTables("workplace");
			break;
		default:
	        throw new IllegalArgumentException(String.format("Unknown URI: %s",uri.toString()));
		}
		
		Cursor c = sqlBuilder.query(contactsDB, projection, selection, selectionArgs, null, null, sortOrder);

		c.setNotificationUri(getContext().getContentResolver(), uri);
		return c;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}
}