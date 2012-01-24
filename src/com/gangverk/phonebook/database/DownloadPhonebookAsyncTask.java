package com.gangverk.phonebook.database;

import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.http.HttpStatus;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

import com.gangverk.phonebook.utils.SystemUtils;

public class DownloadPhonebookAsyncTask extends AsyncTask<Void, Void, Void> {
	private static final String SETTINGS_KEY_LAST_DOWNLOAD_CHECK_TIME = "lastDownloadCheckTime";
	public static final String SETTINGS_KEY_HAS_NEW_DB = "hasNewDb";
	private static final String PHONEBOOK_BASEURL = "http://192.168.80.95/~hoddih/";
	public static final String DOWNLOADED_DB_FILENAME = "mannvit_staff.sqlite";
	
	private boolean fetched_db;
	private long currentTime;
	private Context context;
	
	
	public DownloadPhonebookAsyncTask(Context context) {
		super();
		this.context = context;
	}

	@Override
	protected Void doInBackground(Void... args) {
		String db_name = "mannvit_staff.sqlite";
		File dbFile = context.getDatabasePath(db_name);
		long localDBDate = dbFile.lastModified();
		currentTime = System.currentTimeMillis() /1000;
		URL url = null;
		HttpURLConnection urlConnection = null;
		try {
			url = new URL(PHONEBOOK_BASEURL + DOWNLOADED_DB_FILENAME);
			urlConnection = (HttpURLConnection) url.openConnection();
			urlConnection.setDoInput(true);
			urlConnection.setIfModifiedSince(localDBDate);
			urlConnection.connect();
			if(urlConnection.getResponseCode() == HttpStatus.SC_OK) {
				SystemUtils.copyInputStreamToOutputStream(urlConnection.getInputStream(), context.openFileOutput(DOWNLOADED_DB_FILENAME,Context.MODE_PRIVATE));
				fetched_db = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
        return null;
	}
		
	protected void onPostExecute (Void result) {
		// Only interact with activity on main thread
        SharedPreferences.Editor settingsEditor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        settingsEditor.putLong(SETTINGS_KEY_LAST_DOWNLOAD_CHECK_TIME, currentTime);
        settingsEditor.commit();
        if(fetched_db) {
        	settingsEditor.putBoolean(SETTINGS_KEY_HAS_NEW_DB, true);
            settingsEditor.commit();
        }
	}
}
