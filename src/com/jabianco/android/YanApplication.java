package com.jabianco.android;

import android.app.Application;
import android.util.Log;

public class YanApplication extends Application {
	public static final String PREFS_NAME = "YanPreferences";
	private static final String TAG = "YanApplication";

	private Preferences preferences = null;

	@Override
	public void onCreate() {
		Log.i(TAG, "onCreate()");
		super.onCreate();
		preferences = new Preferences(getSharedPreferences(PREFS_NAME, 0));
		preferences.load();
	}

	public Preferences getPreferences() {
		return preferences;
	}

	public void setPreferences(Preferences preferences) {
		this.preferences = preferences;
	}
	
}
