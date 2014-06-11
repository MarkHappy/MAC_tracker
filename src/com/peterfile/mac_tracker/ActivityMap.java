package com.peterfile.mac_tracker;

import java.util.ArrayList;
import java.util.List;

import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.GoogleMap.OnMyLocationChangeListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.Toast;

public class ActivityMap extends FragmentActivity {
	static final String TAG = ActivityMap.class.getSimpleName();
	GoogleMap googleMap;
	boolean firstZoom = true;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		Toast.makeText(getBaseContext(), TAG + " onCreate",  Toast.LENGTH_SHORT).show();
		setContentView(R.layout.activity_map);

//		Check if to keep awake
		if (MainActivity.keepAwake) {
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		}
		
		initHomeButton();
		initSettingsButton();

	}

	@Override
	public void onResume() {
		super.onResume();
		
		int mapPointsToDisplay = Integer.parseInt(getSharedPreferences("MAC_tracker", Context.MODE_PRIVATE).getString("points", "50"));
		
		googleMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
		googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
		googleMap.setMyLocationEnabled(true);
		
		initCameraPosition();
		
//		if (googleMap.getMyLocation() != null) {
//			LatLng myLocation = new LatLng(googleMap.getMyLocation().getLatitude(), googleMap.getMyLocation().getLongitude());
//			googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 15));
//		}
		
//		Read all APs from the db
        DataSource ds = new DataSource(getApplicationContext());
        ds.openDB();
        List<AccessPoint> apList = ds.getAllAccessPoints();
        ds.closeDB();
        
//      Clean up duplicate coordinates
        List<AccessPoint> apListToDisplay = new ArrayList<AccessPoint>();
        LatLng previousll = new LatLng(0, 0);
        for (AccessPoint ap: apList) {
        	LatLng currentll = new LatLng(ap.getLat(), ap.getLon());
        	if (!currentll.equals(previousll)) {
        		apListToDisplay.add(ap);
        		previousll = currentll;
        	}
        }
        
//      Calculate the number of points to display
        int lowestPoints;
        if (apListToDisplay.size() < mapPointsToDisplay) {
        	lowestPoints = apListToDisplay.size();
        } else {
        	lowestPoints = mapPointsToDisplay;
        }
        
//      Add points to the map
        if (apList.size() == 0) {
        	Toast.makeText(getBaseContext(), "No points to display",  Toast.LENGTH_SHORT).show();
        } else {
        	LatLng oldll = new LatLng(0, 0);
        	for (int i = 0; i < lowestPoints; i++) {
        		LatLng ll = new LatLng(apListToDisplay.get(i).getLat(), apListToDisplay.get(i).getLon());
        		if (!ll.equals(oldll)) {
        			googleMap.addMarker(new MarkerOptions().position(ll).title(apListToDisplay.get(i).getApEssid() + " " + apListToDisplay.get(i).getId()));
        			oldll = ll;
        		}
        	}
        }
		
//		googleMap.setOnMyLocationChangeListener(new OnMyLocationChangeListener() {
//					
//			@Override
//			public void onMyLocationChange(Location location) {
//				if (firstZoom) {
//					LatLng point = new LatLng(location.getLatitude(), location.getLongitude());
//					googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(point, 15));
//					firstZoom = false;
//				}
//			}
//		});
		
		if (googleMap.getMyLocation() != null) {
			Log.w(TAG, "not null");
//			Toast.makeText(getBaseContext(), googleMap.getMyLocation().getLatitude() + " " + googleMap.getMyLocation().getLongitude() + " " + googleMap.getMyLocation().getAccuracy(),  Toast.LENGTH_SHORT).show();
		}
		
//		Toast zoom level
//		googleMap.setOnCameraChangeListener(new OnCameraChangeListener() {
//			
//			@Override
//			public void onCameraChange(CameraPosition arg0) {
//				//Toast.makeText(getBaseContext(), "Zoom " + googleMap.getCameraPosition().zoom,  Toast.LENGTH_SHORT).show();
//				if (googleMap.getMyLocation() != null) {
////					Toast.makeText(getBaseContext(), googleMap.getMyLocation().getLatitude() + " " + googleMap.getMyLocation().getLongitude() + " " + googleMap.getMyLocation().getAccuracy(),  Toast.LENGTH_SHORT).show();
//				}	
//			}
//		});
		
	}

	@Override
	protected void onPause() {
		CameraPosition cp = googleMap.getCameraPosition();
		getSharedPreferences("MAC_tracker", Context.MODE_PRIVATE).edit().putString("cameraPosition_Lat", String.valueOf(cp.target.latitude)).commit();
		getSharedPreferences("MAC_tracker", Context.MODE_PRIVATE).edit().putString("cameraPosition_Lon", String.valueOf(cp.target.longitude)).commit();
		getSharedPreferences("MAC_tracker", Context.MODE_PRIVATE).edit().putFloat("cameraPosition_Zoom", cp.zoom).commit();
		getSharedPreferences("MAC_tracker", Context.MODE_PRIVATE).edit().putFloat("cameraPosition_Tilt", cp.tilt).commit();
		getSharedPreferences("MAC_tracker", Context.MODE_PRIVATE).edit().putFloat("cameraPosition_Bearing", cp.bearing).commit();
		
		super.onPause();
	}

	public static class ErrorDialogFragment extends DialogFragment {
		static final String ARG_STATUS = "status";

		static ErrorDialogFragment newInstance(int status) {
			Bundle args = new Bundle();
			args.putInt(ARG_STATUS, status);
			ErrorDialogFragment result = new ErrorDialogFragment();
			result.setArguments(args);
			return (result);
		}

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
		Bundle args=getArguments();
		return GooglePlayServicesUtil.getErrorDialog(args.getInt(ARG_STATUS),getActivity(), 0);
		}

		@Override
		public void onDismiss(DialogInterface dlg) {
			if (getActivity() != null) {
				getActivity().finish();
			}
		}
	}
	
	private void initHomeButton() {
		ImageButton mapIB = (ImageButton) findViewById(R.id.imageButton_List);
		mapIB.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(ActivityMap.this, MainActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
			}
		});
	}
	
	private void initSettingsButton() {
		ImageButton settingsIB = (ImageButton) findViewById(R.id.imageButton_Settings);
		settingsIB.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(ActivityMap.this, ActivitySettings.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
			}
		});
	}
	
	private void initCameraPosition() {
		googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition (
				new LatLng (Double.valueOf(getSharedPreferences("MAC_tracker", Context.MODE_PRIVATE).getString("cameraPosition_Lat", "0.0")), 
						Double.valueOf(getSharedPreferences("MAC_tracker", Context.MODE_PRIVATE).getString("cameraPosition_Lon", "0.0"))), 
					getSharedPreferences("MAC_tracker", Context.MODE_PRIVATE).getFloat("cameraPosition_Zoom", googleMap.getMaxZoomLevel()),
					getSharedPreferences("MAC_tracker", Context.MODE_PRIVATE).getFloat("cameraPosition_Tilt", 0),
					getSharedPreferences("MAC_tracker", Context.MODE_PRIVATE).getFloat("cameraPosition_Bearing", 0)
				)), 100, null);
		
	}
}