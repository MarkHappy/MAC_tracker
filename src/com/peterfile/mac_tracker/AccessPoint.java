package com.peterfile.mac_tracker;

import java.util.ArrayList;
import java.util.List;

import android.location.Location;
import android.net.wifi.ScanResult;
import android.text.format.Time;
import android.util.Log;

public class AccessPoint {
	private static int id;
	private long seenTime;
	private String essid;
	private String bssid;
	private String capabilities;
	private int powerLevel;
	private int frequency;
	private double latitude;
	private double longitude;
	private float accuracy;
	
	public AccessPoint (String essid, String bssid, int pwrlvl, int freq, long seen, double lat, double lon, float acc) {
		this.essid = essid;
		this.bssid = bssid;
		this.powerLevel = pwrlvl;
		this.frequency = freq;
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
	
	public static AccessPoint convertFromScanResult(ScanResult sr, Location loc) {
		AccessPoint ap = new AccessPoint();
		ap.setApEssid(sr.SSID);
		ap.setApBssid(sr.BSSID);
		//ap.setApCapabilities(sr.capabilities);
		ap.setApPowerLevel(sr.level);
		ap.setSeenTime(getSystemTime());
		ap.setApFrequency(sr.frequency);
		Log.w("", sr.frequency + " " + sr.level);
		ap.setLat(loc.getLatitude());
		ap.setLon(loc.getLongitude());
		ap.setAcc(loc.getAccuracy());
		return ap;
	}
	
	public static ArrayList<AccessPoint> convertFromListScanResults (List<ScanResult> sr, Location loc) {
		ArrayList<AccessPoint> apList = new ArrayList<AccessPoint>();
		for (ScanResult s : sr) {
			apList.add(convertFromScanResult(s, loc));
		}
		return apList;
	}

	public static long getSystemTime () {
		Time now = new Time();
		now.setToNow();
		return now.toMillis(false);
	}

}