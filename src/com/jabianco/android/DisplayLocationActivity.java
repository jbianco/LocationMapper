package com.jabianco.android;

import java.text.SimpleDateFormat;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;
import android.util.Log;
import java.util.Date;

public class DisplayLocationActivity extends Activity {
	private WebView mWebview;
	private static final String TAG = "DisplayLocationActivity";
	private LocationDbAdapter dbAdapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		mWebview = new WebView(this);
		mWebview.getSettings().setJavaScriptEnabled(true); // enable javascript
		final Activity activity = this;
		dbAdapter = new LocationDbAdapter(this);
		mWebview.setWebViewClient(new WebViewClient() {

			@Override
			public void onPageFinished(WebView view, String url) {

				// //////////////////////////////////////////////
				Log.i(TAG, "writeDbDataToTempFile");
				String data = "";
				String separator = "";
				dbAdapter.open();
				Cursor cursor = dbAdapter.fetchAllLocations();
				cursor.moveToFirst();

				Log.i(TAG, "Writing " + cursor.getCount() + " records.");
				while (cursor.isAfterLast() == false) {
					data += separator
							+ cursor.getDouble(2) + ","
							+ cursor.getDouble(3) + ","
							+ cursor.getFloat(5) + ","
							+ cursor.getString(1) + ","
							+ new SimpleDateFormat("HH:mm:ss").format(new Date(
									cursor.getLong(5)));
					Log.i(TAG, data);
					separator = ";";
					cursor.moveToNext();
				}
				dbAdapter.close();

				mWebview.loadUrl("javascript:visualiseDataWithParams('" + data
						+ "')");				 
			}

			public void onReceivedError(WebView view, int errorCode,
					String description, String failingUrl) {
				Toast.makeText(activity, description, Toast.LENGTH_SHORT)
						.show();
			}
		});
		mWebview.loadUrl("file:///android_asset/RenderLocation.html");
		setContentView(mWebview);

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
}
