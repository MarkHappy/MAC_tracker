package com.peterfile.mac_tracker;

import java.util.ArrayList;
import java.util.List;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;

import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends ListActivity implements GooglePlayServicesClient.ConnectionCallbacks, GooglePlayServicesClient.OnConnectionFailedListener{

	static final String TAG = MainActivity.class.getSimpleName();
	List<ScanResult> scannedList;
	WifiListAdapter adapter;
	ArrayList<AccessPoint> apList;
	LocationClient mLocationClient;
	Location mCurrentLocation;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		initToggleWifiButton();
		initToggleScanButton();
		
		apList = new ArrayList<AccessPoint>();
		apList.clear();
		
		mLocationClient = new LocationClient(this, this, this);

		this.registerReceiver(this.WifiStateChangedReceiver, new IntentFilter(
				WifiManager.WIFI_STATE_CHANGED_ACTION));

	}
	
	@Override
	protected void onStart() {
		super.onStart();
		mLocationClient.connect();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		adapter = new WifiListAdapter(this, apList);
		adapter.sortByPowerDsc();
		setListAdapter(adapter);
		
		ListView listView = getListView();
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,int position, long id) {
				Log.w(TAG, "onItemClick");
				DataSource ds = new DataSource(MainActivity.this);
				if (ds.addAccessPoint(adapter.getItem(position))) {
					Toast.makeText(getApplicationContext(), adapter.getItem(position).getApEssid() + "was added", Toast.LENGTH_LONG).show();
					mCurrentLocation = mLocationClient.getLastLocation();
					Log.w(TAG, "LAT: " + mCurrentLocation.getLatitude() + " LON: " + mCurrentLocation.getLongitude() + " ACC: " + mCurrentLocation.getAccuracy());
				} else {
					Toast.makeText(getApplicationContext(), "Failed to add AP", Toast.LENGTH_LONG).show();
				}
			}
		});
	}

	@Override
	protected void onStop() {
		mLocationClient.disconnect();
		super.onStop();
	}
	
	private void initToggleScanButton() {
		final Button bScan = (Button) findViewById(R.id.button_Scan);

		bScan.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				apList = AccessPoint.convertFromListScanResults(scanNow());
				adapter.clear();
				adapter.addAll(apList);
				adapter.sortByPowerDsc();
			}
		});
	}

	private void initToggleWifiButton() {
		final Button bToggleWifiState = (Button) findViewById(R.id.button_Toggle_Wifi_State);
		bToggleWifiState.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				WifiManager wifiManager = (WifiManager) getBaseContext()
						.getSystemService(Context.WIFI_SERVICE);
				String[] wifiStatesArray = getResources().getStringArray(
						R.array.wifi_states);
				if (String.valueOf(bToggleWifiState.getText()).equals(
						wifiStatesArray[1])) {
					Log.d(TAG, "Wifi seems disabled");
					wifiManager.setWifiEnabled(true);
				}
				if ((String.valueOf(bToggleWifiState.getText())
						.equals(wifiStatesArray[3]))) {
					Log.d(TAG, "Wifi seems enabled");
					wifiManager.setWifiEnabled(false);
				}
			}
		});
	}
	
	private BroadcastReceiver WifiStateChangedReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			//TextView tvWIFI_State = (TextView) findViewById(R.id.textView_WIFI_status_indicator);
			final Button bToggleWifiState = (Button) findViewById(R.id.button_Toggle_Wifi_State);
			Button bScan = (Button) findViewById(R.id.button_Scan);
			int extraWifiState = intent.getIntExtra(
					WifiManager.EXTRA_WIFI_STATE,
					WifiManager.WIFI_STATE_UNKNOWN);
			String[] wifiStatesArray = getResources().getStringArray(
					R.array.wifi_states);

			switch (extraWifiState) {
			case WifiManager.WIFI_STATE_DISABLED:
				bToggleWifiState.setText(wifiStatesArray[1]);
				bToggleWifiState.setBackgroundColor(getResources().getColor(
						R.color.Gray));
				bScan.setEnabled(false);
				break;
			case WifiManager.WIFI_STATE_DISABLING:
				bToggleWifiState.setText(wifiStatesArray[0]);
				bToggleWifiState.setBackgroundColor(getResources().getColor(
						R.color.Olive));
				break;
			case WifiManager.WIFI_STATE_ENABLED:
				bToggleWifiState.setText(wifiStatesArray[3]);
				bToggleWifiState.setBackgroundColor(getResources().getColor(
						R.color.Green));
				bScan.setEnabled(true);
				break;
			case WifiManager.WIFI_STATE_ENABLING:
				bToggleWifiState.setText(wifiStatesArray[2]);
				bToggleWifiState.setBackgroundColor(getResources().getColor(
						R.color.Olive));
				break;
			case WifiManager.WIFI_STATE_UNKNOWN:
				bToggleWifiState.setText(wifiStatesArray[4]);
				bToggleWifiState.setBackgroundColor(getResources().getColor(
						R.color.Red));
				break;
			}
		}
	};

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
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private List<ScanResult> scanNow() {
		WifiManager wifiManager = (WifiManager) getBaseContext().getSystemService(Context.WIFI_SERVICE);
		wifiManager.startScan();
		return wifiManager.getScanResults();
		
	}

	
	@Override
	public void onConnectionFailed(ConnectionResult arg0) {
		Toast.makeText(this, "Failed to connect", Toast.LENGTH_SHORT).show();		
	}
	

	@Override
    public void onConnected(Bundle dataBundle) {
        Toast.makeText(this, "Connected", Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onDisconnected() {
        // Display the connection status
        Toast.makeText(this, "Disconnected. Please re-connect.",
                Toast.LENGTH_SHORT).show();
    }

}
