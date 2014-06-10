package com.peterfile.mac_tracker;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBHelper extends SQLiteOpenHelper {
	
	private static final String DATABASE_NAME = "mactracker.db";
	private static final int DATABASE_VERSION = 1;
	public static final String TABLE_ACCESSPOINTS = "accesspoints";
	static final String TAG = DBHelper.class.getSimpleName();
	
    // AccessPoints Table Columns names
	public static final String KEY_ID 		= "_id";
    public static final String KEY_ESSID 	= "essid";
    public static final String KEY_BSSID 	= "bssid";
    public static final String KEY_PWR_LVL 	= "power_level";
    public static final String KEY_FRQ 		= "frequency";
    public static final String KEY_SEEN 	= "seen";
    public static final String KEY_LAT 		= "lat";
    public static final String KEY_LON 		= "lon";
    public static final String KEY_ACC 		= "acc";
	
	public DBHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}
	
	// Database access point table 
	private static final String CREATE_ACCESS_POINTS_TABLE = "CREATE TABLE " + TABLE_ACCESSPOINTS + "(" 
			+ KEY_ID 		+ " INTEGER PRIMARY KEY,"
			+ KEY_ESSID 	+ " TEXT," 
			+ KEY_BSSID 	+ " TEXT," 
			+ KEY_PWR_LVL 	+ " INTEGER,"
			+ KEY_FRQ		+ " INTEGER,"
			+ KEY_SEEN 		+ " INTEGER," 
			+ KEY_LAT 		+ " INTEGER,"
			+ KEY_LON 		+ " INTEGER,"
			+ KEY_ACC 		+ " REAL"
			+ ")";
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		Log.w(TAG, "onCreate");
		db.execSQL(CREATE_ACCESS_POINTS_TABLE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w(DBHelper.class.getName(),
				"Upgrading database from version " + oldVersion + " to "
						+ newVersion + ", which will destroy all old data");
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_ACCESSPOINTS);
		onCreate(db);
	}
}
