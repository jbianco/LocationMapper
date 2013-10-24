package com.jabianco.android;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
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
	private Button stopButton;
	private Button startButton;

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

		stopButton = (Button) findViewById(R.id.map_button);
		startButton = (Button) findViewById(R.id.service_button);

		stopButton.setOnClickListener(this);
		startButton.setOnClickListener(this);

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
			TextView buttonText = (TextView) findViewById(R.id.service_button);
			if (this.isMyServiceRunning()) {
				stopService(i);
				buttonText.setText(getResources().getString(R.string.serviceStart));
				Log.i(TAG, "location service stopped...");
			} else {
				startService(i);
				buttonText.setText(getResources().getString(R.string.serviceStop));
				Log.i(TAG, "location service started...");
			}
		}
	}
}
