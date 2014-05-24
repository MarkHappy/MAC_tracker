package com.peterfile.mac_tracker;

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
			values.put(DBHelper.KEY_SEEN, ap.getSeenTime());
			values.put(DBHelper.KEY_LAT, ap.getLat());
			values.put(DBHelper.KEY_LON, ap.getLon());
			values.put(DBHelper.KEY_ACC, ap.getAcc());
			Log.w(TAG, "Values to add: " + values.toString());
			Log.w(TAG, "null, creating a new row");
			// Returns the numbers of rows successfully inserted.
			didSucceed = database.insert(DBHelper.TABLE_ACCESSPOINTS, null, values) > 0;
			Log.w(TAG, "didSucceed is: " + String.valueOf(didSucceed));
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
				Integer.parseInt(cursor.getString(0)), 
				cursor.getString(1),
				cursor.getString(2),
				Integer.parseInt(cursor.getString(3)), 
				Long.parseLong(cursor.getString(4)), 
				Double.parseDouble(cursor.getString(5)), 
				Double.parseDouble(cursor.getString(6)), 
				Float.parseFloat(cursor.getString(7))
		);
		cursor.close();
		return ap;
	}
	
	public void openDB() throws SQLException {
		database = dbHelper.getWritableDatabase();
		Log.d(TAG, "DataBase open: " + database.getPath());
	}

	public void closeDB() {
		Log.d(TAG, "closeDB");
		dbHelper.close();
	}

}
