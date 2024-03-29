package com.gangverk.mannvit.database;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.preference.PreferenceManager;
import android.util.Log;

import com.gangverk.mannvit.R;
import com.gangverk.mannvit.utils.SystemUtils;

public class DatabaseHelper extends SQLiteOpenHelper{

	private static final String LOG_TAG = "DatabaseHelper";
	//The Android's default system path of your application database.
	public final static String DB_NAME = "employee_database.sqlite";
	private static final int DB_VERSION = 3;
	private static final boolean FORCE_RECOPY = false;
	private Context mContext;
	/**
	 * Constructor
	 * Takes and keeps a reference of the passed context in order to access to the application assets and resources.
	 * @param context
	 */
	public DatabaseHelper(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
		mContext = context;
		File dbFile = context.getDatabasePath(DB_NAME);
		String[] tables = new String[]{"employee","division","workplace"};
		String[] views = new String[]{"employeeInfo"};
		String[] indexes = new String[]{};
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
		boolean hasDownloadedDb = settings.getBoolean(DownloadPhonebookAsyncTask.SETTINGS_KEY_HAS_NEW_DB, false);
        if(!FORCE_RECOPY && hasDownloadedDb) {
        	Log.d(LOG_TAG,"Starting processing downloaded db file");
    		File srcFile = new File(String.format("%s/%s", context.getFilesDir(),DB_NAME));
    		if(checkDBFileValidity(srcFile, tables, views, indexes))
    		{
    			try {
    				SystemUtils.copy(srcFile, dbFile);
    			} catch (IOException e) {
    				e.printStackTrace();
    			}
    		}
        	
        	SharedPreferences.Editor settingsEditor = settings.edit();
        	settingsEditor.putBoolean(DownloadPhonebookAsyncTask.SETTINGS_KEY_HAS_NEW_DB, false);
        	settingsEditor.commit();
			if(srcFile.isFile()) {
				context.deleteFile(DB_NAME);
			}
        	Log.d(LOG_TAG,"Ended processing downloaded db file");
        } else if(FORCE_RECOPY || !dbFile.exists() || !checkDBFileValidity(dbFile,tables,views,indexes)) {
			copyDbToDbFolder();
		}
	}	

	@Override
	public void onCreate(SQLiteDatabase db) {
	}

	private void copyDbToDbFolder() {
		File dbFile = mContext.getDatabasePath(DB_NAME);
		dbFile.getParentFile().mkdirs();
		InputStream inputRaw = null;
		OutputStream outputDbFile = null;
				
        try {
        	// copy file from raw resource to a file in app root
        	inputRaw = mContext.getResources().openRawResource(R.raw.shipped_db);
        	outputDbFile = new FileOutputStream(dbFile);
        	SystemUtils.copyInputStreamToOutputStream(inputRaw, outputDbFile);
        } catch (IOException ioe) {
        	Log.e(LOG_TAG,"Could not find or read sqlite db");
        	ioe.printStackTrace();
        } finally {
        	if(inputRaw != null) {try{inputRaw.close();}catch(IOException ioe){}}
        	if(outputDbFile != null) {try{outputDbFile.close();}catch(IOException ioe){}}
        }
	}
	
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// Delete old database, put in new
		File dbFile = mContext.getDatabasePath(DB_NAME);
		dbFile.delete();
		copyDbToDbFolder();
	}
	
	public static boolean checkDBFileValidity(File dbFile, String[] tables, String[] views, String[] indexes) {
		if(!dbFile.isFile()) {
			return false;
		}
		
		SQLiteDatabase newDatabase = null;
		Cursor tableCursor = null;
		
		try {
			newDatabase = SQLiteDatabase.openDatabase(dbFile.getAbsolutePath(), null, SQLiteDatabase.OPEN_READONLY);
			// Check if we have all tables and indexes in incoming db
			tableCursor = newDatabase.query("sqlite_master", new String[]{"type","name","tbl_name",}, null, null, null, null, null);
			int columnType = tableCursor.getColumnIndex("type");
			int columnName = tableCursor.getColumnIndex("name");
			int columnTblName = tableCursor.getColumnIndex("tbl_name");
			Set<String> tableSet = new HashSet<String>();
			Set<String> viewSet = new HashSet<String>();
			Set<String> indexSet = new HashSet<String>();
			
			// collect all table/index name
			while(tableCursor.moveToNext()) {
				String type = tableCursor.getString(columnType);
				if(type.equals("table")) {
					tableSet.add(tableCursor.getString(columnName));
				} else if(type.equals("index")) {
					indexSet.add(String.format("%s.%s", tableCursor.getString(columnTblName), tableCursor.getString(columnName)));
				} else if(type.equals("view")) {
					viewSet.add(tableCursor.getString(columnName));
				}
			}
	
			for(String tblName : tables) {
				if(!tableSet.contains(tblName)) {
					Log.e(LOG_TAG,String.format("Invalid database, missing table %s",tblName));
					return false;
				}
			}
			for(String viewName : views) {
				if(!viewSet.contains(viewName)) {
					Log.e(LOG_TAG,String.format("Invalid database, missing view %s",viewName));
					return false;
				}
			}
			for(String indexName : indexes) {
				if(!indexSet.contains(indexName)) {
					Log.e(LOG_TAG,String.format("Invalid database, missing table %s",indexName));
					return false;
				}
			}
		} catch (SQLiteException e) {
			Log.e(LOG_TAG,String.format("Could not read new database, error: %s",e.toString()));
			return false;
		} finally {
			if(tableCursor != null) { tableCursor.close(); }
			if(newDatabase != null) { newDatabase.close(); }
			
		}
		return true;
	}
}
