package ch.goetschy.android.nfctext.database;

import ch.goetschy.android.nfctext.BuildConfig;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class TextsTable extends Table {
	public static final String TABLE_NAME = "texts";

	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_TIME = "time";
	public static final String COLUMN_CONTENT = "content";
	public static final String COLUMN_READ = "read"; // if text was read or not
														// (boolean)

	private static final String DATATBASE_CREATE = "create table " + TABLE_NAME
			+ "( " + COLUMN_ID + " integer primary key autoincrement,"
			+ COLUMN_TIME + " integer, " + COLUMN_READ + " integer, "
			+ COLUMN_CONTENT + " text );";

	@Override
	public void onCreate(SQLiteDatabase database) {
		database.execSQL(DATATBASE_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase database, int oldVersion,
			int newVersion) {
		if (BuildConfig.DEBUG)
			Log.w(TextsTable.class.toString(), "Upgrading database...");
		database.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
		onCreate(database);
	}

}
