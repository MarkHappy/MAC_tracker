package com.peterfile.mac_tracker;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.net.wifi.ScanResult;
import android.text.format.Time;

public class AccessPoint {
	private int id;
	private long seenTime;
	private String essid;
	private String bssid;
	private String capabilities;
	private int powerLevel;
	private int frequency;
	private double latitude;
	private double longitude;
	private float accuracy;
	
	public AccessPoint (int id, String essid, String bssid, int pwrlvl, long seen, double lat, double lon, float acc) {
		this.id = id;
		this.essid = essid;
		this.bssid = bssid;
		this.powerLevel = pwrlvl;
		this.seenTime = seen;
		this.latitude = lat;
		this.longitude = lon;
		this.accuracy = acc;
	}
		
	public AccessPoint() {
	}

	public int getId () {
		return id;
	}
	
	public void setId (int i) {
		id = i;
	}
	public long getSeenTime () {
		return seenTime;
	}
	
	public void setSeenTime (long time) {
		seenTime = time;
	}
	
	public String getApEssid() {
		return essid;
	}

	public void setApEssid(String s) {
		essid = s;
	}
	
	public String getApBssid() {
		return bssid;
	}
	
	public void setApBssid(String s) {
		bssid = s.toUpperCase();
	}	
	
	public String getApCapabilities() {
		return capabilities;
	}
	
	public void setApCapabilities(String s) {
		capabilities = s;
	}
	
	public int getApPowerLevel(){
		return powerLevel;
	}
	
	public void setApPowerLevel(int i) {
		powerLevel = i;
	}
	
	public int getApFrequency(){
		return frequency;
	}
	
	public void setApFrequency(int i) {
		frequency = i;
	}
	
	public double getLat () {
		return latitude;
	}
	
	public void setLat (double d) {
		latitude = d;
	}
	
	public double getLon () {
		return longitude;
	}
	
	public void setLon (double d) {
		longitude = d;
	}
	
	public float getAcc () {
		return accuracy;
	}
	
	public void setAcc (float f) {
		accuracy = f;
	}
	
	public static AccessPoint convertFromScanResult(ScanResult sr) {
		AccessPoint ap = new AccessPoint();
		ap.setApEssid(sr.SSID);
		ap.setApBssid(sr.BSSID);
		//ap.setApCapabilities(sr.capabilities);
		ap.setApPowerLevel(sr.level);
		ap.setSeenTime(getSystemTime());
		//ap.setApFrequency(sr.frequency);
//		ap.setLat(MainActivity.mCurrentLocation.getLatitude());
//		ap.setLon(MainActivity.mCurrentLocation.getLongitude());
//		ap.setAcc(MainActivity.mCurrentLocation.getAccuracy());
		return ap;
	}
	
	public static ArrayList<AccessPoint> convertFromListScanResults (List<ScanResult> sr) {
		ArrayList<AccessPoint> apList = new ArrayList<AccessPoint>();
		for (ScanResult s : sr) {
			apList.add(convertFromScanResult(s));
		}
		return apList;
	}
	
	public static String getDate (long milliSeconds, String dateFormat) {
	    DateFormat formatter = new SimpleDateFormat(dateFormat); 
	     Calendar calendar = Calendar.getInstance();
	     calendar.setTimeInMillis(milliSeconds);
	     return formatter.format(calendar.getTime());
	}
	
	public static long getSystemTime () {
		Time now = new Time();
		now.setToNow();
		return now.toMillis(false);
	}
}