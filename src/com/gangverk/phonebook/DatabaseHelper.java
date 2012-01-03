package com.gangverk.phonebook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper{

	//The Android's default system path of your application database.
	private static String DB_PATH = "/data/data/com.gangverk.phonebook/databases/";
	private static String DB_NAME = "phonebook.db";
	private SQLiteDatabase myDataBase; 
	private final Context myContext;
	public static final String DATABASE_TABLE_EMPLOYEE = "employee";
	public static final String DATABASE_TABLE_DIVISION = "division";
	public static final String DATABASE_TABLE_WORKPLACE = "workplace";
	public static final String TABLE_ID = "_id";
	public static final String EMPLOYEE_KEY_NAME = DATABASE_TABLE_EMPLOYEE + ".name";
	public static final String EMPLOYEE_KEY_TITLE = DATABASE_TABLE_EMPLOYEE + ".title";
	public static final String EMPLOYEE_KEY_PHONE = DATABASE_TABLE_EMPLOYEE + ".phone";
	public static final String EMPLOYEE_KEY_MOBILE = DATABASE_TABLE_EMPLOYEE + ".mobile";
	public static final String EMPLOYEE_KEY_EMAIL = DATABASE_TABLE_EMPLOYEE + ".email";
	public static final String EMPLOYEE_KEY_WORKPLACE_ID = DATABASE_TABLE_EMPLOYEE + ".workplace_id";
	public static final String EMPLOYEE_KEY_DIVISION_ID = DATABASE_TABLE_EMPLOYEE + ".division_id";
	public static final String DIVISION_KEY_NAME = DATABASE_TABLE_DIVISION + ".name";
	public static final String WORKPLACE_KEY_ADDRESS = DATABASE_TABLE_WORKPLACE + ".address";
	
	/**
	 * Constructor
	 * Takes and keeps a reference of the passed context in order to access to the application assets and resources.
	 * @param context
	 */
	public DatabaseHelper(Context context) {

		super(context, DB_NAME, null, 1);
		this.myContext = context;
	}	

	/**
	 * Creates a empty database on the system and rewrites it with your own database.
	 * */
	public void createDataBase() throws IOException{

		boolean dbExist = checkDataBase();

		if(dbExist){
			//do nothing - database already exist
		}else{

			//By calling this method and empty database will be created into the default system path
			//of your application so we are gonna be able to overwrite that database with our database.
			this.getReadableDatabase();

			try {

				copyDataBase();

			} catch (IOException e) {

				throw new Error("Error copying database");

			}
		}

	}

	/**
	 * Check if the database already exist to avoid re-copying the file each time you open the application.
	 * @return true if it exists, false if it doesn't
	 */
	private boolean checkDataBase(){

		SQLiteDatabase checkDB = null;

		try{
			String myPath = DB_PATH + DB_NAME;
			checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);

		}catch(SQLiteException e){

			//database does't exist yet.

		}

		if(checkDB != null){

			checkDB.close();

		}

		return checkDB != null ? true : false;
	}

	/**
	 * Copies your database from your local assets-folder to the just created empty database in the
	 * system folder, from where it can be accessed and handled.
	 * This is done by transfering bytestream.
	 * */
	private void copyDataBase() throws IOException{

		//Open your local db as the input stream
		InputStream myInput = myContext.getAssets().open(DB_NAME);

		// Path to the just created empty db
		String outFileName = DB_PATH + DB_NAME;

		//Open the empty db as the output stream
		OutputStream myOutput = new FileOutputStream(outFileName);

		//transfer bytes from the inputfile to the outputfile
		byte[] buffer = new byte[1024];
		int length;
		while ((length = myInput.read(buffer))>0){
			myOutput.write(buffer, 0, length);
		}

		//Close the streams
		myOutput.flush();
		myOutput.close();
		myInput.close();

	}

	public void openDataBase() throws SQLException{

		//Open the database
		String myPath = DB_PATH + DB_NAME;
		myDataBase = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);

	}

	@Override
	public synchronized void close() {

		if(myDataBase != null)
			myDataBase.close();

		super.close();

	}

	@Override
	public void onCreate(SQLiteDatabase db) {

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

	}

	/**
	 * Return a Cursor over the list of all notes in the database
	 * 
	 * @return Cursor over all notes
	 */
	public Cursor fetchEmployees() {
		return myDataBase.query(DATABASE_TABLE_EMPLOYEE, new String[] {TABLE_ID, EMPLOYEE_KEY_NAME, EMPLOYEE_KEY_TITLE, EMPLOYEE_KEY_PHONE, EMPLOYEE_KEY_MOBILE, EMPLOYEE_KEY_EMAIL, EMPLOYEE_KEY_WORKPLACE_ID, EMPLOYEE_KEY_DIVISION_ID}, null, null, null, null, EMPLOYEE_KEY_NAME + " ASC");
	}

	public Cursor fetchEmployees(long id) {
		String rawQuery = "SELECT " + DATABASE_TABLE_EMPLOYEE + "." + TABLE_ID + "," + EMPLOYEE_KEY_NAME + "," + EMPLOYEE_KEY_TITLE + "," + EMPLOYEE_KEY_PHONE + "," + EMPLOYEE_KEY_MOBILE + "," + EMPLOYEE_KEY_EMAIL + "," + WORKPLACE_KEY_ADDRESS + "," + DIVISION_KEY_NAME + " FROM " + DATABASE_TABLE_EMPLOYEE + "," + DATABASE_TABLE_DIVISION + "," + DATABASE_TABLE_WORKPLACE + " WHERE " + DATABASE_TABLE_EMPLOYEE + "." + TABLE_ID + "=" + id + " AND " +  EMPLOYEE_KEY_WORKPLACE_ID + "=" + DATABASE_TABLE_WORKPLACE + "." + TABLE_ID + " AND " +  EMPLOYEE_KEY_DIVISION_ID + "=" + DATABASE_TABLE_DIVISION + "." + TABLE_ID;
		Cursor mCursor = myDataBase.rawQuery(rawQuery, null);
		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;
	}

}
