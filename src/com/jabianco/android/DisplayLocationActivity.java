package com.jabianco.android;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class DisplayLocationActivity extends Activity {
	private WebView mWebview;
//	private static final String TAG = "DisplayLocationActivity";

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		mWebview = new WebView(this);
		mWebview.getSettings().setJavaScriptEnabled(true); // enable javascript
		final Activity activity = this;
		mWebview.setWebViewClient(new WebViewClient() {

			@Override
			public void onPageFinished(WebView view, String url) {

				
////////////////////////////////////////////////
/*					Log.i(TAG, "writeDbDataToTempFile");
					String data = "";
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
						cursor.moveToNext();*/

///////////////////////////////////////////////

				String data = "-37.81599490000001,144.96441501428572,96,stored,18:29:46;-37.815929014285715,144.9642501857143,91,prx_enter,18:30:46;-37.8163697,144.9636684,60,prx_exit,19:10:51;-37.81354171428571,144.96557667142855,60,network,19:17:06;-37.81354171428571,144.96557667142855,60,network,19:17:06;-37.8134822845459,144.96479272842407,8,gps,19:17:09;-37.81366467475891,144.9651199579239,12,gps,19:17:14;-37.81367540359497,144.965318441391,12,gps,19:17:17;-37.81335890293121,144.9655383825302,12,gps,19:18:01;-37.81326770782471,144.96558666229248,8,gps,19:18:04;-37.81265988333333,144.96521451666666,60,network,19:18:44;-37.81265988333333,144.96521451666666,60,network,19:18:44;-37.813085317611694,144.96561348438263,16,gps,19:18:47;-37.81265988333333,144.96521451666666,60,network,19:18:55;-37.813085317611694,144.96561348438263,16,gps,19:18:57;-37.8129243850708,144.9653559923172,8,gps,19:19:32;-37.81232015,144.9650399875,75,network,19:19:38;-37.812854647636414,144.96531307697296,12,gps,19:19:41;-37.81245231628418,144.96527552604675,16,gps,19:20:03;-37.81236112117767,144.96529698371887,24,gps,19:20:05;-37.812066078186035,144.96495366096497,32,gps,19:20:26;-37.81195342540741,144.96497511863708,32,gps,19:20:34;-37.81169593334198,144.96462643146515,12,gps,19:20:58;-37.81162083148956,144.96458888053894,8,gps,19:21:06;-37.811381688888886,144.9646280222222,54,network,19:21:08;-37.811099809999995,144.96475721,65,network,19:21:53";
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
