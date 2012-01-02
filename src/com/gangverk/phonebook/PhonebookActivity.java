package com.gangverk.phonebook;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

public class PhonebookActivity extends Activity {
    private final static String LOG_TAG = "PhonebookActivity"; 
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        int a = 2;
        Log.d(LOG_TAG,String.format("the nusmber was %d", a));
    }
}