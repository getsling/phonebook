package com.gangverk.phonebook;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

public class ContactsProvider extends ContentProvider {

	@Override
	public boolean onCreate() {
		return false;
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
		// TODO Auto-generated method stub
		return null;
	}



	@Override
	public Cursor query(Uri uri, String[] projection, String selection,String[] selectionArgs, String sortOrder) {
		// TODO Auto-generated method stub
		return null;
	}



	@Override
	public int update(Uri uri, ContentValues values, String selection,String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}
}