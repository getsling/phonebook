package com.gangverk.phonebook;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

public class DownloadPhonebookAsyncTask extends AsyncTask<Void, Void, Void> {
	private static final String LOG_TAG = "DownloadPhonebookAsyncTask";
	private static final int CHECK_DB_INTERVAL_HOURS = 1;
	private static final String SETTINGS_KEY_LAST_DOWNLOAD_CHECK_TIME = "lastDownloadCheckTime";
	private static final String SETTINGS_KEY_DB_TIME = "lastDbTime";
	public static final String SETTINGS_KEY_HAS_NEW_DB = "hasNewDb";
	private static final String PHONEBOOK_BASEURL = "http://stash.gangverk.is";
	public static final String DOWNLOADED_DB_FILENAME = "mannvit_staff.sqlite";
	
	private boolean fetched_db;
	private long newestDbTime, currentTime;
	private Context context;
	
	
	public DownloadPhonebookAsyncTask(Context context) {
		super();
		this.context = context;
	}

	@Override
	protected Void doInBackground(Void... args) {
        // get last db download date from preferences
    	SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        long lastGuideDownloadCheckTime = settings.getLong(SETTINGS_KEY_LAST_DOWNLOAD_CHECK_TIME, 0);
        int lastDbTime = settings.getInt(SETTINGS_KEY_DB_TIME, 0);
    	boolean hasDownloadedDatabase = settings.getBoolean(SETTINGS_KEY_HAS_NEW_DB, false);
    	String dbNameSchema = "phonebook_db";
		fetched_db = false;
    	newestDbTime = 0;
        currentTime = System.currentTimeMillis() / 1000;
        // The database helper has to have cleaned up any previous downloads before we go again
        if(!hasDownloadedDatabase && currentTime - lastGuideDownloadCheckTime > CHECK_DB_INTERVAL_HOURS * 60 * 60) {
        	// on-device db might be stale, check if online version is more recent
        	HttpClient httpClient = new DefaultHttpClient();
        	String dateCheckUrl = String.format("%s/%s.last_update.json", PHONEBOOK_BASEURL, dbNameSchema);
        	HttpGet httpGet = new HttpGet(dateCheckUrl);
        	try {
        		HttpResponse response = httpClient.execute(httpGet);
        		StatusLine statusLine = response.getStatusLine();
        		int statusCode = statusLine.getStatusCode();
        		if(statusCode == 200) {
        			HttpEntity entity = response.getEntity();
        			JSONObject jsonObject = new JSONObject(SystemUtils.stringFromStream(entity.getContent()));
            		newestDbTime = jsonObject.getLong("last_update");
        		} else {
        			Log.e(LOG_TAG,String.format("Failed to download file (http code: %s, url: %s)",statusCode,dateCheckUrl));
        		}
        	} catch(ClientProtocolException e) {
        		e.printStackTrace();
        	} catch (IOException e) {
        		e.printStackTrace();
        	} catch (JSONException e) {
				e.printStackTrace();
			}
        	
        	// Check if db has been updated since we last checked it
            if(newestDbTime > lastDbTime) {
            	// Remote database was updated, need to download new version
            	try {
					Log.d(LOG_TAG,"Starting database file download");
					URL dbUrl = new URL(String.format("%s/%s",PHONEBOOK_BASEURL, String.format("%s.sqlite.gz",dbNameSchema)));
					SystemUtils.copyInputStreamToOutputStream(dbUrl.openConnection().getInputStream(), context.openFileOutput(DOWNLOADED_DB_FILENAME,Context.MODE_PRIVATE));
					Log.d(LOG_TAG,"Downloaded new database file");
	            	fetched_db = true;
				} catch (MalformedURLException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
            }
        }
        return null;
	}
		
	protected void onPostExecute (Void result) {
		// Only interact with activity on main thread
        SharedPreferences.Editor settingsEditor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        settingsEditor.putLong(SETTINGS_KEY_LAST_DOWNLOAD_CHECK_TIME, currentTime);
        settingsEditor.commit();
        if(fetched_db) {
        	settingsEditor.putLong(SETTINGS_KEY_DB_TIME,newestDbTime);
        	settingsEditor.putBoolean(SETTINGS_KEY_HAS_NEW_DB, true);
            settingsEditor.commit();
        }
	}
}
