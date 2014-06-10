package com.peterfile.mac_tracker;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

public class MainActivity extends Activity {

	static final String TAG = MainActivity.class.getSimpleName();
	android.os.Handler customHandler;
	private long last_updated_location = 0;
	public static boolean keepAwake;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
//		Toast.makeText(getBaseContext(), TAG + " onCreate",  Toast.LENGTH_SHORT).show();
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
//		Check if to keep awake
		keepAwake = getSharedPreferences("MAC_tracker", Context.MODE_PRIVATE).getBoolean("stayAwake", false);
		if (keepAwake) {
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		}
		
		initMapButton();
		initSettingsButton();
		
		customHandler = new android.os.Handler();
        customHandler.postDelayed(updateTimerThread, 0);
        
//      DEBUG
//      Toast.makeText(getBaseContext(), " " + String.valueOf(calculateDistance(-99, 2447)),  Toast.LENGTH_SHORT).show();
        
	}
	
	public double calculateDistance(double signalLevelInDb, double freqInMHz) {
	    double exp = (27.55 - (20 * Math.log10(freqInMHz)) - signalLevelInDb) / 20.0;
//	    double exp = (27.55 - (20 * Math.log10(freqInMHz)) - Math.abs(signalLevelInDb)) / 20.0;
	    return Math.pow(10.0, exp);
	}
	
	@Override
	protected void onResume() {
//		Toast.makeText(getBaseContext(), TAG + " onResume",  Toast.LENGTH_SHORT).show();
		super.onResume();
		registerReceiver(serverStatusReceiver, new IntentFilter(LocationService.SERVICE_STATUS));
		registerReceiver(serverLocationReceiver, new IntentFilter(LocationService.SERVICE_LOCATION));
		initLocationServiceStateButton();

		Bundle b = new Bundle();
		b.putDouble(LocationService.KEY_LAT, Double.valueOf((String) getSharedPreferences("MAC_tracker", Context.MODE_PRIVATE).getString(LocationService.KEY_LAT, "0")));
		b.putDouble(LocationService.KEY_LON, Double.valueOf((String) getSharedPreferences("MAC_tracker", Context.MODE_PRIVATE).getString(LocationService.KEY_LON, "0")));
		b.putFloat(LocationService.KEY_ACC, Float.valueOf((String) getSharedPreferences("MAC_tracker", Context.MODE_PRIVATE).getString(LocationService.KEY_ACC, "0")));
		b.putLong(LocationService.KEY_SEEN, Long.valueOf(getSharedPreferences("MAC_tracker", Context.MODE_PRIVATE).getString(LocationService.KEY_SEEN, "0")));
		updateCoordinates(b);
		
	}
	
	@Override
	protected void onPause() {
//		Toast.makeText(getBaseContext(), TAG + " onPause",  Toast.LENGTH_SHORT).show();

		unregisterReceiver(serverLocationReceiver);
		unregisterReceiver(serverStatusReceiver);

		Button bLat = (Button) findViewById(R.id.button_Latitude);
		Button bLon = (Button) findViewById(R.id.button_Longitude);
		Button bAcc = (Button) findViewById(R.id.button_Accuracy);
		getSharedPreferences("MAC_tracker", Context.MODE_PRIVATE).edit().putString(LocationService.KEY_LAT, String.valueOf(bLat.getText())).commit();
		getSharedPreferences("MAC_tracker", Context.MODE_PRIVATE).edit().putString(LocationService.KEY_LON, String.valueOf(bLon.getText())).commit();
		getSharedPreferences("MAC_tracker", Context.MODE_PRIVATE).edit().putString(LocationService.KEY_ACC, String.valueOf(bAcc.getText())).commit();
		getSharedPreferences("MAC_tracker", Context.MODE_PRIVATE).edit().putString(LocationService.KEY_SEEN, String.valueOf(last_updated_location)).commit();

		super.onPause();
	}
	
	
//	@Override
//	protected void onRestoreInstanceState(Bundle savedInstanceState) {
//		Log.w(TAG, "onRestoreInstanceState");
//		updateCoordinates(savedInstanceState);
//		super.onRestoreInstanceState(savedInstanceState);
//	}
//
//	@Override
//	protected void onSaveInstanceState(Bundle outState) {
//		Log.w(TAG, "onSaveInstanceState");
//		Button bLat = (Button) findViewById(R.id.button_Latitude);
//		Button bLon = (Button) findViewById(R.id.button_Longitude);
//		Button bAcc = (Button) findViewById(R.id.button_Accuracy);
//		if (!bLat.getText().equals("")) {
//			outState.putDouble(LocationService.KEY_LAT, Double.parseDouble((String) bLat.getText()));
//			outState.putDouble(LocationService.KEY_LON, Double.parseDouble((String) bLon.getText()));
//			outState.putFloat(LocationService.KEY_ACC, Float.parseFloat((String) bAcc.getText()));
//			outState.putLong(LocationService.KEY_SEEN, last_updated_location);
//		}
//		super.onSaveInstanceState(outState);
//		Log.w(TAG, "Saved state: " + outState.getDouble(LocationService.KEY_LAT) + " " + outState.getDouble(LocationService.KEY_LON) + " " + outState.getFloat(LocationService.KEY_ACC));
//	}
	
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
		if ((Float.valueOf((String) bAcc.getText()) < 10) && (Float.valueOf((String) bAcc.getText()) != 0.0)) {
			bAcc.setBackgroundColor(getResources().getColor(R.color.Green));
			bLat.setBackgroundColor(getResources().getColor(R.color.Green));
			bLon.setBackgroundColor(getResources().getColor(R.color.Green));
		} else {
			bAcc.setBackgroundColor(getResources().getColor(R.color.Red));
			bLat.setBackgroundColor(getResources().getColor(R.color.Red));
			bLon.setBackgroundColor(getResources().getColor(R.color.Red));
		}
		
		last_updated_location = b.getLong(LocationService.KEY_SEEN);
	}
	
	private Runnable updateTimerThread = new Runnable()	{
		public void run() {
			if (last_updated_location != 0) {
				Button bSeen = (Button) findViewById(R.id.button_Time_Since_Update);
				bSeen.setText(String.valueOf ((System.currentTimeMillis() - last_updated_location) / 1000));
			}
			customHandler.postDelayed(this, 2000);
		}
	};

	private void saveDummyAp (Bundle b) {
		AccessPoint ap = new AccessPoint(
				"Dummy", 
				"00:00:00:00:00:00", 
				0,
				0,
				b.getLong(LocationService.KEY_SEEN), 
				b.getDouble(LocationService.KEY_LAT), 
				b.getDouble(LocationService.KEY_LON), 
				b.getFloat(LocationService.KEY_ACC)
		);
		DataSource ds = new DataSource(getApplicationContext());
		ds.openDB();
		ds.addAccessPoint(ap);
		ds.closeDB();		
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
      getMenuInflater().inflate(R.menu.main, menu);
      return super.onCreateOptionsMenu(menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	return super.onOptionsItemSelected(item);
    }

	private void initMapButton() {
		ImageButton imageButton = (ImageButton) findViewById(R.id.imageButton_Map);
		imageButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this, ActivityMap.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
			}
		});
	}
	
	private void initSettingsButton() {
		ImageButton imageButton = (ImageButton) findViewById(R.id.imageButton_Settings);
		imageButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this, ActivitySettings.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
			}
		});
	}
		
}
