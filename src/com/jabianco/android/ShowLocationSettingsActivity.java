package com.jabianco.android;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;
//import java.io.FileInputStream;
//import java.io.FileWriter;
//import org.apache.http.client.ClientProtocolException;
//import org.apache.http.client.HttpClient;
//import org.apache.http.client.methods.HttpPost;
//import org.apache.http.entity.InputStreamEntity;
//import org.apache.http.impl.client.DefaultHttpClient;

public class ShowLocationSettingsActivity extends Activity {

	private static final int DELTA_MINUTES = 1000 * 60 * 5;
	public static final String PREFS_NAME = "LocationTrackerPreferences";

	private static final String TAG = "ShowLocationSettingsActivity";
	private LocationDbAdapter dbAdapter;
	private LocationManager locationManager;
	private Location currentLocation = null;
	private Location storedLocation = null;
	private Context context = null;
	private String provider;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.location_settings);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		this.locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		this.context = this;

		dbAdapter = new LocationDbAdapter(this);

		setCurrentLocation();
		restorePreferences();
		setProvider("gps");

	}

	@Override
	protected void onPause() {
		Log.i(TAG, "onPause");
		super.onPause();
		Log.i(TAG, "After: super.onPause()");

		EditText editText = (EditText) findViewById(R.id.sampleinterval);
		int sampleInterval = Integer.parseInt(editText.getText().toString());

		editText = (EditText) findViewById(R.id.sampledistance);
		int sampleDistance = Integer.parseInt(editText.getText().toString());

		storePreferences(storedLocation, sampleInterval, sampleDistance);

		dbAdapter.close();

		// Inform the LocationListenerService about possible changes
		Intent intent = new Intent("com.jabianco.android.PREFERENCES_CHANGED");
		sendBroadcast(intent);
	}

	@Override
	protected void onResume() {
		Log.i(TAG, "onResume");
		super.onResume();
		dbAdapter.open();
	}

	public void onClearDataBase(View view) {
		Log.i(TAG, "onClearDataBase");
		int rows = dbAdapter.clearDatabase();
		Toast.makeText(this, "Deleted " + rows + " rows.", Toast.LENGTH_SHORT)
				.show();
	}

	public void onRadioButtonClicked(View view) {
		// Is the button now checked?
		boolean checked = ((RadioButton) view).isChecked();

		// Check which radio button was clicked
		switch (view.getId()) {
		case R.id.radio_GPS:
			if (checked)
				setProvider("gps");
			Log.i(TAG, getProvider());
			break;
		case R.id.radio_Network:
			if (checked)
				setProvider("network");
			Log.i(TAG, getProvider());
			break;
		}
	}

	private void setCurrentLocation() {
		Location location = findLastLocation();
		if (null != location) {
			currentLocation = location;
		}
	}

	private void setStoredLocation(Location location) {

		storedLocation = location;
	}

	private Location findLastLocation() {
		long minTime = new Date().getTime() - DELTA_MINUTES; // The last 5
																// minutes
		Location bestResult = null;
		long bestTime = Long.MAX_VALUE;
		float bestAccuracy = Float.MAX_VALUE;

		List<String> matchingProviders = locationManager.getAllProviders();
		for (String provider : matchingProviders) {
			Location location = locationManager.getLastKnownLocation(provider);
			if (location != null) {
				float accuracy = location.getAccuracy();
				long time = location.getTime();
				Log.i(TAG, "TIME= " + time + ", minTime= " + minTime
						+ ", bestTime= " + bestTime + ", accuracy= " + accuracy
						+ ", bestAccuracy= " + bestAccuracy);
				if ((time > minTime && accuracy < bestAccuracy)) {
					bestResult = location;
					bestAccuracy = accuracy;
					bestTime = time;
				} else if (time < minTime && bestAccuracy == Float.MAX_VALUE
						&& time > bestTime) {
					bestResult = location;
					bestTime = time;
				}
			}
		}
		return bestResult;
	}

	private void restorePreferences() {
		Log.i(TAG, "restorePreferences");
		Preferences preferences = ((YanApplication) getApplicationContext())
				.getPreferences();

		setStoredLocation(preferences.getLocation());

		EditText view = (EditText) findViewById(R.id.sampleinterval);
		view.setText(Integer.toString(preferences.getSampleInterval()));

		view = (EditText) findViewById(R.id.sampledistance);
		view.setText(Integer.toString(preferences.getSampleDistance()));

	}

	private void storePreferences(Location location, int sampleInterval,
			int sampleDistance) {
		Log.i(TAG, "storePreferences");

		Preferences preferences = ((YanApplication) getApplicationContext())
				.getPreferences();
		preferences.setLocation(location);
		preferences.setSampleDistance(sampleDistance);
		preferences.setSampleInterval(sampleInterval);
		preferences.store();
	}

	public void onStoreLocation(View view) {
		Log.i(TAG, "onStoreLocation");
		storedLocation = currentLocation;
		storedLocation.setProvider("stored");

		dbAdapter.addLocation(storedLocation);

		setStoredLocation(storedLocation);
	}

	public void onEmailData(View view) {
		Log.i(TAG, "onEmailData()");
		EmailDataTask emailDataTask = new EmailDataTask();
		emailDataTask.execute();
	}

	public String getProvider() {
		return provider;
	}

	public void setProvider(String provider) {
		this.provider = provider;
	}

	private class EmailDataTask extends AsyncTask<String, Void, String> {
		@Override
		protected String doInBackground(String... urls) {
			try {
				writeDbDataToTempFile();
				emailData();
			} catch (Exception e) {
				Log.i(TAG, "Error emailing data: " + e.getMessage());

				return "Error emailing data: " + e.getMessage();
			}
			return "Data successfully emailed.";
		}

		@Override
		protected void onPostExecute(String result) {
			Log.i(TAG, "result");
		}

		private void emailData() {
			final Intent emailIntent = new Intent(
					android.content.Intent.ACTION_SEND_MULTIPLE);
			emailIntent.setType("plain/text");
			emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,
					"Location Mapper Data");
			emailIntent.putExtra(android.content.Intent.EXTRA_TEXT,
					"Location Mapper Data");

			ArrayList<Uri> uris = new ArrayList<Uri>();

			File fileIn = new File(Environment.getExternalStorageDirectory(),
					"data.csv");

			Uri u = Uri.fromFile(fileIn);
			uris.add(u);
			emailIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
			context.startActivity(Intent.createChooser(emailIntent,
					"Send mail..."));
		}

		private void writeDbDataToTempFile() throws IOException {
			Log.i(TAG, "writeDbDataToTempFile");
			String data = "";
			File dataFile = new File(Environment.getExternalStorageDirectory(),
					"data.csv");
			FileOutputStream fileOutputStream = new FileOutputStream(dataFile);
			OutputStreamWriter outputStreamWriter = new OutputStreamWriter(
					fileOutputStream);

			Cursor cursor = dbAdapter.fetchAllLocations();
			cursor.moveToFirst();

			Log.i(TAG, "Writing " + cursor.getCount() + " records.");
			while (cursor.isAfterLast() == false) {
				data = cursor.getDouble(2)
						+ ","
						+ cursor.getDouble(3)
						+ ","
						+ cursor.getFloat(5)
						+ ","
						+ cursor.getString(1)
						+ ","
						+ new SimpleDateFormat("HH:mm:ss").format(new Date(
								cursor.getLong(5))) + "," + cursor.getFloat(4)
						+ "\n";
				Log.i(TAG, data);
				outputStreamWriter.write(data);
				cursor.moveToNext();
			}
			outputStreamWriter.flush();
			outputStreamWriter.close();
			cursor.close();
			Log.i(TAG, "Done.");
		}
	}
}
