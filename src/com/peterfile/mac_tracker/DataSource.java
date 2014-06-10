package com.peterfile.mac_tracker;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class DataSource {
	static final String TAG = DataSource.class.getSimpleName();
	private SQLiteDatabase database;
	private DBHelper dbHelper;
	
	public DataSource(Context context) {
		dbHelper = new DBHelper(context);
	}
	
	public boolean addAccessPoint (AccessPoint ap) {
		boolean didSucceed = false;
		try {
			ContentValues values = new ContentValues();
			values.put(DBHelper.KEY_ESSID, ap.getApEssid());
			values.put(DBHelper.KEY_BSSID, ap.getApBssid());
			values.put(DBHelper.KEY_PWR_LVL, ap.getApPowerLevel());
			values.put(DBHelper.KEY_FRQ, ap.getApFrequency());
			values.put(DBHelper.KEY_SEEN, ap.getSeenTime());
			values.put(DBHelper.KEY_LAT, ap.getLat());
			values.put(DBHelper.KEY_LON, ap.getLon());
			values.put(DBHelper.KEY_ACC, ap.getAcc());
			Log.w(TAG, "Values to add: " + values.toString());
			Log.w(TAG, "null, creating a new row");
			// Returns the numbers of rows successfully inserted.
			didSucceed = database.insert(DBHelper.TABLE_ACCESSPOINTS, null, values) > 0;
		} catch (Exception e) {
			Log.w(TAG, "catch block for adding AP");
		}
		return didSucceed;
	}
	
	public AccessPoint getAccessPointByBSSID (String bssid) {
		String[] columns = new String [] { 
			DBHelper.KEY_ID,
			DBHelper.KEY_ESSID, 
			DBHelper.KEY_BSSID,
			DBHelper.KEY_PWR_LVL, 
			DBHelper.KEY_FRQ,
			DBHelper.KEY_SEEN,
			DBHelper.KEY_LAT,
			DBHelper.KEY_LON,
			DBHelper.KEY_ACC
		};
		Cursor cursor = database.query(DBHelper.TABLE_ACCESSPOINTS, columns, DBHelper.KEY_BSSID + "=?", new String[] {String.valueOf(bssid)}, null, null, null, null);
		if (cursor.getCount() != 0) {
			Log.w(TAG, "got DB cursor");
			cursor.moveToFirst();
		} else {
			Log.w(TAG, "DB cursor is empty");
			cursor.close();
			return null;
		}
		AccessPoint ap = new AccessPoint( 
				cursor.getString(1),
				cursor.getString(2),
				Integer.parseInt(cursor.getString(3)),
				Integer.parseInt(cursor.getString(4)),
				Long.parseLong(cursor.getString(5)), 
				Double.parseDouble(cursor.getString(6)), 
				Double.parseDouble(cursor.getString(7)), 
				Float.parseFloat(cursor.getString(8))
		);
		ap.setId(Integer.parseInt(cursor.getString(0)));
		cursor.close();
		return ap;
	}
	
	public List<AccessPoint> getAllAccessPoints	() {
		List<AccessPoint> apList = new ArrayList<AccessPoint>();
		String selectQuery = "SELECT * FROM " + DBHelper.TABLE_ACCESSPOINTS;
		Cursor cursor = database.rawQuery(selectQuery, null);
		if (cursor.moveToFirst()) {
			do {
				AccessPoint ap = new AccessPoint();
				ap.setId(cursor.getInt(0));
				ap.setApEssid(cursor.getString(1));
				ap.setApBssid(cursor.getString(2));
				ap.setApPowerLevel(cursor.getInt(3));
				ap.setApFrequency(cursor.getInt(4));
				ap.setSeenTime(cursor.getInt(5));
				ap.setLat(cursor.getFloat(6));
				ap.setLon(cursor.getFloat(7));
				ap.setAcc(cursor.getFloat(8));
				apList.add(ap);				
			} while (cursor.moveToNext());
		}
		return apList;
	}
	
	public void openDB() throws SQLException {
		database = dbHelper.getWritableDatabase();
		Log.d(TAG, "DataBase open: " + database.getPath());
	}

	public void closeDB() {
		Log.d(TAG, "closeDB");
		dbHelper.close();
	}

	public int itemsCount() {
		String countQuery = "SELECT * FROM " + DBHelper.TABLE_ACCESSPOINTS;
		Cursor cursor = database.rawQuery(countQuery, null);
		return cursor.getCount();
	}

}
