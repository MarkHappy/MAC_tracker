package com.peterfile.mac_tracker;

import java.util.List;

import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMyLocationChangeListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
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
		Toast.makeText(getBaseContext(), TAG + " onCreate",  Toast.LENGTH_SHORT).show();
		setContentView(R.layout.activity_map);
		
		initHomeButton();
		initSettingsButton();
//		final LatLng ll = new LatLng(32.098001, 34.800092);
//		final LatLng ll2 = new LatLng(32.098021, 34.800481);

		googleMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
		googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
		googleMap.setMyLocationEnabled(true);
		
		if (googleMap.getMyLocation() != null) {
			LatLng myLocation = new LatLng(googleMap.getMyLocation().getLatitude(), googleMap.getMyLocation().getLongitude());
			googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 15));
		}

//		googleMap.addMarker(new MarkerOptions().position(ll).title("Home"));
//		googleMap.addMarker(new MarkerOptions().position(ll2).title("Near"));
		
		//Add all APs in db to the map
        DataSource ds = new DataSource(getApplicationContext());
        ds.openDB();
        List<AccessPoint> apList = ds.getAllAccessPoints();
        ds.closeDB();
        for (AccessPoint ap : apList) {
        	Log.w(TAG, ap.getApEssid() + " " + ap.getLat() + " " + ap.getLon() + " " + ap.getApPowerLevel() + " " + ap.getAcc());
        	LatLng ll = new LatLng(ap.getLat(), ap.getLon());
        	googleMap.addMarker(new MarkerOptions().position(ll).title(ap.getApEssid() + " " + ap.getId()));
        }
		//Add all APs in db to the map
		
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
	}

	public void onPause() {
		 super.onPause();
	}

	@Override
	public void onResume() {
		super.onResume();
		Toast.makeText(getBaseContext(), TAG + " onResume",  Toast.LENGTH_SHORT).show();
		
		
//		final String TAG_ERROR_DIALOG_FRAGMENT = "errorDialog";
//		int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
//		if (status == ConnectionResult.SUCCESS) {
//			// no problems just work
//		} else if (GooglePlayServicesUtil.isUserRecoverableError(status)) {
//		     ErrorDialogFragment.newInstance(status).show(getSupportFragmentManager(),TAG_ERROR_DIALOG_FRAGMENT);
//		} else {
//			Toast.makeText(this, "Google Maps V2 is not available!",Toast.LENGTH_LONG).show();
//			finish();
//		}
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