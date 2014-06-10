package com.peterfile.mac_tracker;

import java.util.ArrayList;
import java.util.List;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class LocationService extends Service {
	static final String TAG = LocationService.class.getSimpleName();
	public static final String BROADCAST_ACTION = "Hello World";
	public LocationManager locationManager;
	public MyLocationListener listener;
	public Location previousBestLocation = null;
	public static final String SERVICE_STATUS = "com.peterfile.android.service.status";
	public static final String SERVICE_LOCATION = "com.peterfile.android.service.location";
	public final static String KEY_LAT = "Latitude";
	public final static String KEY_LON = "Longitude";
	public final static String KEY_PRO = "Provider";
	public final static String KEY_ACC = "Accuracy";
	public final static String KEY_SEEN = "TimeSeen";

	Intent intent;
	int counter = 0;

	@Override
	public void onCreate() {
	    super.onCreate();
	    intent = new Intent(BROADCAST_ACTION);      
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.w(TAG, "onStart");
		
//		Broadcast back to the activity
	    Intent i = new Intent(SERVICE_STATUS);
	    i.putExtra("Location_Service_Status", "start");
	    sendBroadcast(i);
		
	    locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
	    listener = new MyLocationListener();
	    
//	    Get last known location
	    previousBestLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
	    long timeDiff = (System.currentTimeMillis() - previousBestLocation.getTime()) / 1000;
	    if ((previousBestLocation != null) && (timeDiff < 120)) {
	    	Log.w(TAG, "Sending location (GPS): " + previousBestLocation.getLatitude() + " " + previousBestLocation.getLongitude() + " " + previousBestLocation.getAccuracy() + " " + (System.currentTimeMillis() - previousBestLocation.getTime()) / 1000);
	    	sendLocationIntent(previousBestLocation);
	    } else {
	    	previousBestLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
	    	if (previousBestLocation != null) {
	    		Log.w(TAG, "Sending location (network): " + previousBestLocation.getLatitude() + " " + previousBestLocation.getLongitude() + " " + previousBestLocation.getAccuracy() + " " + (System.currentTimeMillis() - previousBestLocation.getTime()) / 1000);
	    		sendLocationIntent(previousBestLocation);
	    	}
	    }	    
	    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 2, listener);

	    return START_NOT_STICKY;
//	    return super.onStartCommand(intent, flags, startId);
	}
	
	@Override
	public void onDestroy() {       
	   // handler.removeCallbacks(sendUpdatesToUI);     
	    super.onDestroy();
	  //Broadcast back to the activity
	    Intent intent = new Intent(SERVICE_STATUS);
	    intent.putExtra("Location_Service_Status", "stop");
	    sendBroadcast(intent);
	    
	    locationManager.removeUpdates(listener);
		Log.w(TAG, "onDestroy");
		Toast.makeText(getApplicationContext(), "Location service was stopped", Toast.LENGTH_SHORT).show();
	}   

    public void sendLocationIntent(Location loc) {
    	Intent i = new Intent(SERVICE_LOCATION);
        i.putExtra(KEY_LAT, loc.getLatitude());
        i.putExtra(KEY_LON, loc.getLongitude());     
        i.putExtra(KEY_PRO, loc.getProvider());
        i.putExtra(KEY_ACC, loc.getAccuracy());
        i.putExtra(KEY_SEEN, loc.getTime());
        i.putExtra(KEY_PRO, loc.getProvider());
        sendBroadcast(i);
    }
	
	public class MyLocationListener implements LocationListener {

	    public void onLocationChanged(final Location loc) {
	    	List<AccessPoint> apList = new ArrayList<AccessPoint>();
//	    	loc.getLatitude();
//	    	loc.getLongitude();
	    	if (loc.getAccuracy() < 10) {
		    	apList = AccessPoint.convertFromListScanResults(WifiHandler.scanNow(getApplicationContext()), loc);
		    	Log.w(TAG, apList.size() + " access points were found (apList.size)");
		    	Toast.makeText( getApplicationContext(), apList.size() + " access points were found", Toast.LENGTH_SHORT ).show();
		    	DataSource ds = new DataSource(getApplicationContext());
		    	ds.openDB();
		    	for (AccessPoint ap : apList) {
		    		ds.addAccessPoint(ap);
		    	}
		    	ds.closeDB();
	    	}
	    	sendLocationIntent(loc);
	    }

	    public void onProviderDisabled(String provider) {
	        Toast.makeText( getApplicationContext(), "Gps disabled, stopping service.", Toast.LENGTH_LONG ).show();
	        stopSelf();
	    }

	    public void onProviderEnabled(String provider) {
	        Toast.makeText( getApplicationContext(), "Gps Enabled", Toast.LENGTH_SHORT).show();
	    }

	    public void onStatusChanged(String provider, int status, Bundle extras) {
	    }
	}
	
	@Override
	public IBinder onBind(Intent intent) {
	    return null;
	}
}
