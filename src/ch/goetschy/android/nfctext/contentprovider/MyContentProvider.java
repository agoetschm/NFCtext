package ch.goetschy.android.nfctext.contentprovider;


import ch.goetschy.android.nfctext.BuildConfig;
import ch.goetschy.android.nfctext.database.MyDatabaseHelper;
import ch.goetschy.android.nfctext.database.TextsTable;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

public class MyContentProvider extends ContentProvider {
	public static String TAG = "ContentProvider";

	private static final String AUTHORITY = "ch.goetschy.android.nfctext.contentprovider";
	private static final String PATH_TEXTS = TextsTable.TABLE_NAME;
	public static final String TEXT_AUTHORITY = AUTHORITY + "/" + PATH_TEXTS;

	public final static Uri CONTENT_URI_TEXTS = Uri.parse("content://"
			+ TEXT_AUTHORITY);
	public final static Uri CONTENT_URI_BASE = Uri.parse("content://"
			+ AUTHORITY);

	public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
			+ "/items";
	public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
			+ "/item";

	private static final int TEXTS = 10;
	private static final int TEXT_ID = 20;

	private static final UriMatcher sUriMatcher = new UriMatcher(
			UriMatcher.NO_MATCH);
	static {
		sUriMatcher.addURI(AUTHORITY, PATH_TEXTS, TEXTS);
		sUriMatcher.addURI(AUTHORITY, PATH_TEXTS + "/#", TEXT_ID);
	}

	private static final int ITEMS = 100;
	private static final int ITEM_ID = 110;
	private static final UriMatcher sTypeUriMatcher = new UriMatcher(
			UriMatcher.NO_MATCH);
	static {
		sTypeUriMatcher.addURI(AUTHORITY, PATH_TEXTS, ITEMS);
		sTypeUriMatcher.addURI(AUTHORITY, PATH_TEXTS + "/#", ITEM_ID);

	}

	private MyDatabaseHelper mDatabase;

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		return 0;
	}

	@Override
	public String getType(Uri uri) {
		int uriType = sUriMatcher.match(uri);

		if (uriType == TEXTS)
			return CONTENT_TYPE;
		else if (uriType == TEXT_ID)
			return CONTENT_ITEM_TYPE;
		else
			return "unknown type";
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		if (BuildConfig.DEBUG)
			Log.w(TAG, "insert : " + uri);
		
		SQLiteDatabase sqlDB = mDatabase.getWritableDatabase();
		long id = 0;
		String table = getTable(uri);
		
		
		id = sqlDB.insertOrThrow(table, null, values);
		this.getContext().getContentResolver().notifyChange(uri, null);
		
		return Uri.parse(CONTENT_URI_BASE + "/" + table + "/" + id);
	}

	@Override
	public boolean onCreate() {
		mDatabase = new MyDatabaseHelper(this.getContext());
		return false;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		if (BuildConfig.DEBUG)
			Log.w(TAG, "query : " + uri);

		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

		// set table
		String table = getTable(uri);
		queryBuilder.setTables(table);

		// depends if query for one element or not
		int uriType = sUriMatcher.match(uri);
		switch (uriType) {
		case ITEMS:
			if (BuildConfig.DEBUG)
				Log.w("contentProvider", "type : items");
			break;
		case ITEM_ID:
			if (BuildConfig.DEBUG)
				Log.w("contentProvider", "type : item_id");
			String id = uri.getLastPathSegment();
			queryBuilder.appendWhere(TextsTable.COLUMN_ID + "=" + id);
			break;
		}

		SQLiteDatabase sqlDB = mDatabase.getWritableDatabase();
		Cursor cursor = queryBuilder.query(sqlDB, projection, selection,
				selectionArgs, null, null, sortOrder);
		cursor.setNotificationUri(this.getContext().getContentResolver(), uri);

		return cursor;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		int uriType = sTypeUriMatcher.match(uri);
		SQLiteDatabase sqlDB = mDatabase.getWritableDatabase();
		int rowUpdated = 0;

		String table = getTable(uri);

		switch (uriType) {
		case ITEMS:
			rowUpdated = sqlDB.update(table, values, selection, selectionArgs);
			break;
		case ITEM_ID:
			String id = uri.getLastPathSegment();
			if (TextUtils.isEmpty(selection)) {
				rowUpdated = sqlDB.update(table, values, TextsTable.COLUMN_ID + "="
						+ id, null);
			} else {
				rowUpdated = sqlDB.update(table, values, TextsTable.COLUMN_ID + "="
						+ id + " and " + selection, selectionArgs);
			}
			break;
		default:
			throw new IllegalArgumentException("Unknown URI : " + uri);
		}

		this.getContext().getContentResolver().notifyChange(uri, null);

		return rowUpdated;
	}

	private String getTable(Uri uri) {
		String table = uri.getPathSegments().get(0);
		if (!table.equals(PATH_TEXTS)) {
			if (BuildConfig.DEBUG)
				Log.w("contentProvider", "path : " + table);
			throw new IllegalArgumentException("Unknown URI : " + uri);
		}
		return table;
	}
}
