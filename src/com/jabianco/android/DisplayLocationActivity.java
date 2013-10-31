package com.jabianco.android;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class DisplayLocationActivity extends Activity {

	/*
	 * @Override protected void onCreate(Bundle savedInstanceState) {
	 * super.onCreate(savedInstanceState);
	 * setContentView(R.layout.activity_display_location);
	 * 
	 * WebView webview = new WebView(this); setContentView(webview);
	 * webview.loadUrl("http://google.com/");
	 * 
	 * }
	 */

	private WebView mWebview ;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		mWebview = new WebView(this);

		mWebview.getSettings().setJavaScriptEnabled(true); // enable javascript

		final Activity activity = this;

		mWebview.setWebViewClient(new WebViewClient() {
			public void onReceivedError(WebView view, int errorCode,
					String description, String failingUrl) {
				Toast.makeText(activity, description, Toast.LENGTH_SHORT)
						.show();
			}
		});
		//mWebview.loadUrl("http://maps.google.com");
		mWebview.loadUrl("file:///android_asset/RenderLocation.html");
		setContentView(mWebview);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.display_location, menu);
		return true;
	}

}
