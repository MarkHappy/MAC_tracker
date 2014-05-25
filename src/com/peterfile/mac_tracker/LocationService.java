package com.peterfile.mac_tracker;

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
	private static final int TWO_MINUTES = 1000 * 60 * 2;
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
		//Toast.makeText(getApplicationContext(), "Location service was started", Toast.LENGTH_SHORT).show();
		
		//Broadcast back to the activity
	    Intent i = new Intent(SERVICE_STATUS);
	    i.putExtra("Location_Service_Status", "start");
	    sendBroadcast(i);
		
	    locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
	    listener = new MyLocationListener();        
	    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 4000, 2, listener);
	    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 2, listener);
	    
	    locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
	    locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
	    if (isBetterLocation (locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER), locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER))) {
	    	sendLocationIntent(locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER));
	    } else {
	    	sendLocationIntent(locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER));
	    }
	    return START_NOT_STICKY;
//	    return super.onStartCommand(intent, flags, startId);
	}
	
	protected boolean isBetterLocation(Location location, Location currentBestLocation) {
	    if (currentBestLocation == null) {
	        // A new location is always better than no location
	        return true;
	    }

	    // Check whether the new location fix is newer or older
	    long timeDelta = location.getTime() - currentBestLocation.getTime();
	    boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
	    boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
	    boolean isNewer = timeDelta > 0;

	    // If it's been more than two minutes since the current location, use the new location
	    // because the user has likely moved
	    if (isSignificantlyNewer) {
	        return true;
	    // If the new location is more than two minutes older, it must be worse
	    } else if (isSignificantlyOlder) {
	        return false;
	    }

	    // Check whether the new location fix is more or less accurate
	    int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
	    boolean isLessAccurate = accuracyDelta > 0;
	    boolean isMoreAccurate = accuracyDelta < 0;
	    boolean isSignificantlyLessAccurate = accuracyDelta > 200;

	    // Check if the old and new location are from the same provider
	    boolean isFromSameProvider = isSameProvider(location.getProvider(),
	            currentBestLocation.getProvider());

	    // Determine location quality using a combination of timeliness and accuracy
	    if (isMoreAccurate) {
	        return true;
	    } else if (isNewer && !isLessAccurate) {
	        return true;
	    } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
	        return true;
	    }
	    return false;
	}

	private boolean isSameProvider(String provider1, String provider2) {
	    if (provider1 == null) {
	      return provider2 == null;
	    }
	    return provider1.equals(provider2);
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

	    public void onLocationChanged(final Location loc)
	    {
	        if(isBetterLocation(loc, previousBestLocation)) {
	            loc.getLatitude();
	            loc.getLongitude();
	            sendLocationIntent(loc);
	        }                               
	    }

	    public void onProviderDisabled(String provider)
	    {
	        Toast.makeText( getApplicationContext(), "Gps disabled, stopping service.", Toast.LENGTH_LONG ).show();
	        stopSelf();
	    }

	    public void onProviderEnabled(String provider)
	    {
	        Toast.makeText( getApplicationContext(), "Gps Enabled", Toast.LENGTH_SHORT).show();
	    }

	    public void onStatusChanged(String provider, int status, Bundle extras)
	    {

	    }
	}
	
	@Override
	public IBinder onBind(Intent intent) {
	    return null;
	}
}
