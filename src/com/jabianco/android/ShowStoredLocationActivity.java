package com.jabianco.android;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Random;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.location.Location;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class ShowStoredLocationActivity extends ListActivity implements
		OnClickListener {
	private static final String TAG = "LocationTrackerActivity";

	private LocationDbAdapter dbAdapter;
	private SimpleCursorAdapter cursorAdapter;
	private Button mapButton;
	private Button serviceButton;
	private Cursor locations;
	private int lastEstimatedRow;

	public SimpleCursorAdapter getCursorAdapter() {
		return cursorAdapter;
	}

	public void setCursorAdapter(SimpleCursorAdapter cursorAdapter) {
		this.cursorAdapter = cursorAdapter;
	}

	private boolean isMyServiceRunning() {
		ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		for (RunningServiceInfo service : manager
				.getRunningServices(Integer.MAX_VALUE)) {
			if (LocationListenerService.class.getName().equals(
					service.service.getClassName())) {
				return true;
			}
		}
		return false;
	}

	private void setLocations() {
		Log.i(TAG, "setLocations");
		locations = dbAdapter.fetchUnproccessedLocations();
	}

	private boolean nextLocation() {
		// Log.i(TAG, "nextLocation");
		return locations.moveToNext();
	}

	private String getLocationProvider() {
		// Log.i(TAG, "getLocationProvider");
		return locations.getString(1);
	}

	private double getLocationLong() {
		// Log.i(TAG, "getLocationLong");
		return locations.getDouble(3);
	}

	private double getLocationLat() {
		// Log.i(TAG, "getLocationLat");
		return locations.getDouble(2);
	}

	private float getLocationAccuracy() {
		// Log.i(TAG, "getLocationLat");
		return locations.getFloat(5);
	}

	private Date getLocationTimeStamp() {
		// Log.i(TAG, "getLocationLat");
		return new Date(locations.getLong(6));
	}

	private void applyKalman() {
		KalmanGPSSmoother kalman = new KalmanGPSSmoother(1);
		double measuredLatitude = 0;
		double measuredLongitude = 0;
		float measuredAccuracy = 0;
		long measuredTimeStamp = 0;
		setLocations();

		while (nextLocation()) {
			if (getLocationProvider().equals("gps")) {
				measuredLatitude = getLocationLat();
				measuredLongitude = getLocationLong();
				measuredAccuracy = getLocationAccuracy();
				measuredTimeStamp = getLocationTimeStamp().getTime();

				kalman.Process(measuredLatitude, measuredLongitude,
						measuredAccuracy, measuredTimeStamp);

				double estimatedLatitude = kalman.get_lat();
				double estimatedLongitude = kalman.get_lng();
				float estimatedAccuracy = kalman.get_accuracy();
				long estimatedTimeStamp = kalman.get_TimeStamp();

				dbAdapter.addEstimatedLocation("kalman", estimatedLatitude,
						estimatedLongitude, estimatedAccuracy,
						estimatedTimeStamp, 1);
			}

		}
		dbAdapter.setProcessed();
		Log.i(TAG, "After Kalman execution");
	}

	private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
		@SuppressWarnings("deprecation")
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.i(TAG, "Received UPDATE_UI message");
			getCursorAdapter().getCursor().requery();
			getCursorAdapter().notifyDataSetChanged();
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.stored_locations);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		dbAdapter = new LocationDbAdapter(this);

		mapButton = (Button) findViewById(R.id.map_button);
		serviceButton = (Button) findViewById(R.id.service_button);

		mapButton.setOnClickListener(this);
		serviceButton.setOnClickListener(this);

	}

	@SuppressWarnings("deprecation")
	@Override
	public void onResume() {
		Log.i(TAG, "onResume()");
		super.onResume();
		dbAdapter.open();
		String[] from = { LocationDbAdapter.KEY_NAME,
				LocationDbAdapter.KEY_LATITUDE,
				LocationDbAdapter.KEY_LONGITUDE, LocationDbAdapter.KEY_SPEED,
				LocationDbAdapter.KEY_ACCURACY, LocationDbAdapter.KEY_TIME };
		int[] to = { R.id.locationname, R.id.locationlatitude,
				R.id.locationlongitude, R.id.locationspd,
				R.id.locationaccuracy, R.id.locationtime };
		cursorAdapter = new LocationCursorAdapter(this,
				R.layout.stored_locations_row_layout,
				dbAdapter.fetchAllLocations(), from, to);
		this.setListAdapter(cursorAdapter);
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction("com.jabianco.android.UPDATE_UI");
		this.registerReceiver(this.broadcastReceiver, intentFilter);
		getCursorAdapter().getCursor().requery();
		getCursorAdapter().notifyDataSetChanged();
	}

	@Override
	public void onPause() {
		Log.i(TAG, "onPause()");
		super.onPause();
		// applyKalman();
		dbAdapter.close();
		this.unregisterReceiver(this.broadcastReceiver);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.options_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.settings:
			startActivity(new Intent(this, ShowLocationSettingsActivity.class));
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onClick(View v) {
		ComponentName locationListenerServiceName = new ComponentName(
				getPackageName(), LocationListenerService.class.getName());
		Intent i = new Intent().setComponent(locationListenerServiceName);
		if (v.getId() == R.id.service_button) {
			/*
			 * NOTE: Changing behavior to be able to debug the tool without
			 * looking for new data
			 */

			TextView buttonText = (TextView) findViewById(R.id.service_button);
			if (this.isMyServiceRunning()) {
				stopService(i); 
				applyKalman();
				buttonText.setText(getResources().getString(
						R.string.serviceStart));
				Log.i(TAG, "location service stopped...");
			} else {
				startService(i);
				buttonText.setText(getResources().getString(
						R.string.serviceStop));
				Log.i(TAG, "location service started...");
			}
		} else {
			startActivity(new Intent(this, DisplayLocationActivity.class));
		}
	}
}
