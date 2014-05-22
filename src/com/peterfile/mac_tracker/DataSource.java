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
		openDB();
		try {
			ContentValues values = new ContentValues();
			values.put(DBHelper.KEY_ESSID, ap.getApEssid());
			values.put(DBHelper.KEY_BSSID, ap.getApBssid());
			values.put(DBHelper.KEY_PWR_LVL, ap.getApPowerLevel());
			values.put(DBHelper.KEY_SEEN, ap.getSeenTime());
			
			Log.w(TAG, "Query to execute: " + values.toString());
			Log.w(TAG, "getAccessPointByBSSID(ap.getApBssid()) is: " + getAccessPointByBSSID(ap.getApBssid()).toString());
			if (getAccessPointByBSSID(ap.getApBssid()) == null) {
				Log.w(TAG, "null, creating a new row");
				// Returns the numbers of rows successfully inserted.
				didSucceed = database.insert(DBHelper.TABLE_ACCESSPOINTS, null, values) > 0;
				Log.w(TAG, "didSucceed is: " + String.valueOf(didSucceed));
			} else {
				Log.w(TAG, "updating a row");
				didSucceed = database.update(DBHelper.TABLE_ACCESSPOINTS, values, DBHelper.KEY_BSSID + " = ?", new String[] { String.valueOf(ap.getApBssid()) }) > 0;
			    Log.w(TAG, "didSucceed update is: " + String.valueOf(didSucceed));
			}
		} catch (Exception e) {
			
		}
		closeDB();
		return didSucceed;
	}
	
	public AccessPoint getAccessPointByBSSID (String bssid) {
		openDB();
			Cursor cursor = database.query(DBHelper.TABLE_ACCESSPOINTS, new String[] { DBHelper.KEY_ESSID, DBHelper.KEY_BSSID,
					DBHelper.KEY_PWR_LVL, DBHelper.KEY_SEEN}, DBHelper.KEY_BSSID + "=?", new String[] {String.valueOf(bssid)}, null, null, null, null);
			if (cursor != null) {
				cursor.moveToFirst();
			} else {
				return null;
			}
			AccessPoint ap = new AccessPoint(Long.parseLong(cursor.getString(3)), cursor.getString(0), 
					cursor.getString(1), Integer.parseInt(cursor.getString(2)));
		closeDB();
		return ap;
	}
	
	public void openDB() throws SQLException {
		database = dbHelper.getWritableDatabase();
	}

	public void closeDB() {
		dbHelper.close();
	}

}
