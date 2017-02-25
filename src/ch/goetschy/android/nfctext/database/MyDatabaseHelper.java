package ch.goetschy.android.nfctext.database;

import ch.goetschy.android.nfctext.BuildConfig;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class MyDatabaseHelper extends SQLiteOpenHelper {
	private static final String DATABASE_NAME = "nfctext.db";
	private static final int DATABASE_VERSION = 1;

	public MyDatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		if (BuildConfig.DEBUG)
			Log.w("DB helper", "constructor " + DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		new TextsTable().onCreate(db);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w("DB helper", "upgrade");
	}
}
