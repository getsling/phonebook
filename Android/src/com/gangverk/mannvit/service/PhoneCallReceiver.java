package com.gangverk.mannvit.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class PhoneCallReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Intent serviceIntent = new Intent();
		serviceIntent.setAction("com.gangverk.phonebook.service.MannvitService");
		context.startService(serviceIntent);

	}
}