package com.jwetherell.augmented_reality.activity;

import system.Setup;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.maps.MapActivity;


public class RoutingActivity extends Activity{
	
	private static final String LOG_TAG = "RoutingActivity";
	
	private static Setup setupToUse;

	private Setup mySetupToUse;

	private boolean isRouteDisplayed;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (setupToUse != null) {
			mySetupToUse = setupToUse;
			setupToUse = null;
			runSetup();
		} else {
			Log.e(LOG_TAG, "There was no Setup specified to use. "
					+ "Please use ArActivity.show(..) when you "
					+ "want to use this way of starting the AR-view!");
			this.finish();
		}
	}
	
	public static void startWithSetup(Activity currentActivity, Setup setupToUse) {
		RoutingActivity.setupToUse = setupToUse;
		currentActivity.startActivity(new Intent(currentActivity,
				RoutingActivity.class));
	}

	private void runSetup() {
		mySetupToUse.run(this);
	}
	
	@Override
	protected void onRestart() {
		if (mySetupToUse != null)
			mySetupToUse.onRestart(this);
		super.onRestart();
	}

	@Override
	protected void onResume() {
		if (mySetupToUse != null)
			mySetupToUse.onResume(this);
		super.onResume();
	}

	@Override
	protected void onStart() {
		if (mySetupToUse != null)
			mySetupToUse.onStart(this);
		super.onStart();
	}

	@Override
	protected void onStop() {
		if (mySetupToUse != null)
			mySetupToUse.onStop(this);
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		if (mySetupToUse != null)
			mySetupToUse.onDestroy(this);
		super.onDestroy();
	}

	@Override
	protected void onPause() {
		if (mySetupToUse != null)
			mySetupToUse.onPause(this);
		super.onPause();
	}



}
