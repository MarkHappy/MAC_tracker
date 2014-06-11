package com.peterfile.mac_tracker;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

public class ActivitySettings extends Activity{

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.activity_settings);
		
//		Check if to keep awake
		if (MainActivity.keepAwake) {
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		}
		
		initHomeButton();
		initMapButton();
		
		initSettings();
		initPointsToDisplay();
		initStayAwake();
		initAccuracyThreshold();
	
		super.onCreate(savedInstanceState);
	}
	
	private void initHomeButton() {
		ImageButton imageButton = (ImageButton) findViewById(R.id.imageButton_List);
		imageButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(ActivitySettings.this, MainActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
			}
		});
	}
	
	private void initMapButton() {
		ImageButton imageButton = (ImageButton) findViewById(R.id.imageButton_Map);
		imageButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(ActivitySettings.this, ActivityMap.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
			}
		});
	}
	
	private void initSettings() {
		String mapPointsToDisplay = getSharedPreferences("MAC_tracker", Context.MODE_PRIVATE).getString("points", "50");

		RadioButton rb50 = (RadioButton) findViewById(R.id.radio_Settings_50);
		RadioButton rb250 = (RadioButton) findViewById(R.id.radio_Settings_250);
		RadioButton rb500 = (RadioButton) findViewById(R.id.radio_Settings_500);
		
		if (mapPointsToDisplay.equals("500")) {
			rb500.setChecked(true);
		} else if (mapPointsToDisplay.equals("250")) {
			rb250.setChecked(true);
		} else {
			rb50.setChecked(true);
		}
		
		boolean stayAwake = getSharedPreferences("MAC_tracker", Context.MODE_PRIVATE).getBoolean("stayAwake", false);
		CheckBox cbAwake = (CheckBox) findViewById(R.id.checkBox_StayAwake);
		if (stayAwake) {
			cbAwake.setChecked(true);
		} else {
			cbAwake.setChecked(false);
		}
		
		EditText etAccuracyThreshold = (EditText) findViewById(R.id.editText_AccuracyThreshold);
		int i = getSharedPreferences("MAC_tracker", Context.MODE_PRIVATE).getInt("accuracyThreshold", 12);
		etAccuracyThreshold.setText(String.valueOf(i));
	}
	
	private void initStayAwake() {
		final CheckBox cbAwake = (CheckBox) findViewById(R.id.checkBox_StayAwake);
		cbAwake.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (cbAwake.isChecked()) {
					getSharedPreferences("MAC_tracker", Context.MODE_PRIVATE).edit().putBoolean("stayAwake", true).commit();
				} else {
					getSharedPreferences("MAC_tracker", Context.MODE_PRIVATE).edit().putBoolean("stayAwake", false).commit();
				}
			}
		});
	}
	
	private void initPointsToDisplay() {
		RadioGroup rgPoints = (RadioGroup) findViewById(R.id.radioGroup_Settings_Points_to_display);
		rgPoints.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				RadioButton rb250 = (RadioButton) findViewById(R.id.radio_Settings_250);
				RadioButton rb500 = (RadioButton) findViewById(R.id.radio_Settings_500);
				if (rb500.isChecked()) {
					getSharedPreferences("MAC_tracker", Context.MODE_PRIVATE).edit().putString("points", "500").commit();
				} else if (rb250.isChecked()) {
					getSharedPreferences("MAC_tracker", Context.MODE_PRIVATE).edit().putString("points", "250").commit();
				} else {
					getSharedPreferences("MAC_tracker", Context.MODE_PRIVATE).edit().putString("points", "50").commit();
				}
			}
		});
	}

	private void initAccuracyThreshold() {
		final EditText etAccuracyThreshold = (EditText) findViewById(R.id.editText_AccuracyThreshold);
		etAccuracyThreshold.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				getSharedPreferences("MAC_tracker", Context.MODE_PRIVATE).edit().putInt("accuracyThreshold", Integer.valueOf(etAccuracyThreshold.getText().toString())).commit();
			}
		});
	}

}
