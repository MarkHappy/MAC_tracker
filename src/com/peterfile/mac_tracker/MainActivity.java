package com.peterfile.mac_tracker;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.format.Time;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity {

	static final String TAG = MainActivity.class.getSimpleName();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		initLocationServiceStateButton();
		registerReceiver(serverStatusReceiver, new IntentFilter(LocationService.SERVICE_STATUS));
		registerReceiver(serverLocationReceiver, new IntentFilter(LocationService.SERVICE_LOCATION));
	}

	@Override
	protected void onStop() {
		super.onStop();
	}
	
	private void initLocationServiceStateButton() {
		final Button b = (Button) findViewById(R.id.button_Location_Service_State);
		final Intent serviceIntent = new Intent(this, LocationService.class);
		b.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (isMyServiceRunning()) {
					stopService(serviceIntent);
				} else {
					startService(serviceIntent);
				}
			}
		});
		if (isMyServiceRunning()) {
			setLocationServiceEnabled();
		} else {
			setLocationServiceDisabled();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.settings_start_location_service) {
			startService(new Intent(this, LocationService.class));
		}
		if (id == R.id.settings_stop_location_service) {
			stopService(new Intent(this, LocationService.class));
		}
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	private boolean isMyServiceRunning() {
	    ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
	    for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
	        if (LocationService.class.getName().equals(service.service.getClassName())) {
	            return true;
	        }
	    }
	    return false;
	}

	public void setLocationServiceEnabled() {
		final Button b = (Button) findViewById(R.id.button_Location_Service_State);
		b.setText(R.string.enabled);
	}
	
	public void setLocationServiceDisabled() {
		final Button b = (Button) findViewById(R.id.button_Location_Service_State);
		b.setText(R.string.disabled);
	}

	private BroadcastReceiver serverStatusReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Bundle bundle = intent.getExtras();
			if (bundle != null) {
				if (bundle.getString("Location_Service_Status").equals("stop")) {
					setLocationServiceDisabled();
				} else if (bundle.getString("Location_Service_Status").equals("start")){
					setLocationServiceEnabled();
				}
			}
		}
	};
	
	
	private BroadcastReceiver serverLocationReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Bundle bundle = intent.getExtras();
			if (bundle != null) {				
				updateCoordinates(bundle);
			}
		}
	};
	
	private void updateCoordinates (Bundle b) {
		Button bLat = (Button) findViewById(R.id.button_Latitude);
		Button bLon = (Button) findViewById(R.id.button_Longitude);
		Button bAcc = (Button) findViewById(R.id.button_Accuracy);
		bLat.setText(String.valueOf(b.getDouble(LocationService.KEY_LAT)));
		bLon.setText(String.valueOf(b.getDouble(LocationService.KEY_LON)));
		bAcc.setText(String.valueOf(b.getFloat(LocationService.KEY_ACC)));
		Log.w(TAG, "Location is " + (getSystemTime() - b.getLong(LocationService.KEY_SEEN)) / 1000 + " seconds old");
		if (Float.valueOf((String) bAcc.getText()) < 5) {
			bAcc.setBackgroundColor(getResources().getColor(R.color.Green));
			bLat.setBackgroundColor(getResources().getColor(R.color.Green));
			bLon.setBackgroundColor(getResources().getColor(R.color.Green));
		} else {
			bAcc.setBackgroundColor(getResources().getColor(R.color.Red));
			bLat.setBackgroundColor(getResources().getColor(R.color.Red));
			bLon.setBackgroundColor(getResources().getColor(R.color.Red));
		}
		
		TextView tvPro = (TextView) findViewById(R.id.textView1);
		tvPro.setText(b.getString(LocationService.KEY_PRO));
	}

	public static long getSystemTime() {
		Time now = new Time();
		now.setToNow();
		return now.toMillis(false);
	}
}
