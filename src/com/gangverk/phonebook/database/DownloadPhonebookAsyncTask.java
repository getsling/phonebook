package com.gangverk.phonebook.database;

import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.http.HttpStatus;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import com.gangverk.phonebook.R;
import com.gangverk.phonebook.utils.SystemUtils;

public class DownloadPhonebookAsyncTask extends AsyncTask<Void, Void, Void> {
	private static final String SETTINGS_KEY_LAST_DOWNLOAD_CHECK_TIME = "lastDownloadCheckTime";
	public static final String SETTINGS_KEY_HAS_NEW_DB = "hasNewDb";
	public static final String SETTINGS_KEY_LAST_MODIFIED = "lastModifiedDatabaseFile";
	private long lastModified;
	private boolean fetched_db;
	private long currentTime;
	private Context context;
	
	
	public DownloadPhonebookAsyncTask(Context context) {
		super();
		this.context = context;
	}

	@Override
	protected Void doInBackground(Void... args) {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);;
		long localDBDate = settings.getLong(SETTINGS_KEY_LAST_MODIFIED, 0);
		currentTime = System.currentTimeMillis() /1000;
		URL url = null;
		HttpURLConnection urlConnection = null;
		try {
			url = new URL(context.getString(R.string.db_url));
			urlConnection = (HttpURLConnection) url.openConnection();
			urlConnection.setDoInput(true);
			urlConnection.setIfModifiedSince(localDBDate);
			urlConnection.connect();
			if(urlConnection.getResponseCode() == HttpStatus.SC_OK) {
				lastModified = urlConnection.getLastModified();
				SystemUtils.copyInputStreamToOutputStream(urlConnection.getInputStream(), context.openFileOutput(DatabaseHelper.DB_NAME,Context.MODE_PRIVATE));
				fetched_db = true;
				Log.d("DownloadPhonebookAsync","downloaded DB");
			} else {
				Log.d("DownloadPhonebookAsync","has updated db");
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
        	settingsEditor.putLong(SETTINGS_KEY_LAST_MODIFIED, lastModified);
        	settingsEditor.putBoolean(SETTINGS_KEY_HAS_NEW_DB, true);
            settingsEditor.commit();
        }
	}
}
