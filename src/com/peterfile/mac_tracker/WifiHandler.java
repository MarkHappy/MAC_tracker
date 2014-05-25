package com.peterfile.mac_tracker;

import java.util.List;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;

public class WifiHandler {
	
	private List<ScanResult> scanNow(Context context) {
		WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		wifiManager.startScan();
		return wifiManager.getScanResults();
	}
}
