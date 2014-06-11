package com.peterfile.mac_tracker;

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

	public void onPause() {
		 super.onPause();
	}

	@Override
	public void onResume() {
		super.onResume();
		int mapPointsToDisplay = Integer.parseInt(getSharedPreferences("MAC_tracker", Context.MODE_PRIVATE).getString("points", "50"));
		
		googleMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
		googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
		googleMap.setMyLocationEnabled(true);
		
		if (googleMap.getMyLocation() != null) {
			LatLng myLocation = new LatLng(googleMap.getMyLocation().getLatitude(), googleMap.getMyLocation().getLongitude());
			googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 15));
		}
		
//		Add all APs in db to the map
        DataSource ds = new DataSource(getApplicationContext());
        ds.openDB();
        List<AccessPoint> apList = ds.getAllAccessPoints();
        ds.closeDB();
        
//      Show toast with the number of points
//      Toast.makeText(getBaseContext(), "Drawing " + mapPointsToDisplay + " points",  Toast.LENGTH_SHORT).show();
        
        int lowestPoints;
        if (apList.size() < mapPointsToDisplay) {
        	lowestPoints = apList.size();
        } else {
        	lowestPoints = mapPointsToDisplay;
        }
        if (apList.size() == 0) {
        	Toast.makeText(getBaseContext(), "No points to display",  Toast.LENGTH_SHORT).show();
        } else {
        	for (int i = 0; i < lowestPoints; i++) {
        		LatLng oldll = new LatLng(0, 0);
//   	     	Log.w(TAG, apList.get(i).getApEssid() + " " + apList.get(i).getLat() + " " + apList.get(i).getLon() + " " + apList.get(i).getApPowerLevel() + " " + apList.get(i).getAcc());
        		LatLng ll = new LatLng(apList.get(i).getLat(), apList.get(i).getLon());
        		if (ll != oldll) {
        			googleMap.addMarker(new MarkerOptions().position(ll).title(apList.get(i).getApEssid() + " " + apList.get(i).getId()));
        			oldll = ll;
        		} else {
        			i--;
        		}
        	}
        }
		
		googleMap.setOnMyLocationChangeListener(new OnMyLocationChangeListener() {
					
			@Override
			public void onMyLocationChange(Location location) {
				if (firstZoom) {
					LatLng point = new LatLng(location.getLatitude(), location.getLongitude());
					googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(point, 15));
					firstZoom = false;
				}

				
			}
		});
		
		if (googleMap.getMyLocation() != null) {
			Log.w(TAG, "not null");
//			Toast.makeText(getBaseContext(), googleMap.getMyLocation().getLatitude() + " " + googleMap.getMyLocation().getLongitude() + " " + googleMap.getMyLocation().getAccuracy(),  Toast.LENGTH_SHORT).show();
		}
		
//		Toast zoom level
		googleMap.setOnCameraChangeListener(new OnCameraChangeListener() {
			
			@Override
			public void onCameraChange(CameraPosition arg0) {
				//Toast.makeText(getBaseContext(), "Zoom " + googleMap.getCameraPosition().zoom,  Toast.LENGTH_SHORT).show();
				if (googleMap.getMyLocation() != null) {
//					Toast.makeText(getBaseContext(), googleMap.getMyLocation().getLatitude() + " " + googleMap.getMyLocation().getLongitude() + " " + googleMap.getMyLocation().getAccuracy(),  Toast.LENGTH_SHORT).show();
				}	
			}
		});
		
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
	
}