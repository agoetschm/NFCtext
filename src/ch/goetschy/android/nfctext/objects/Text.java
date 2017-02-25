package ch.goetschy.android.nfctext.objects;

import ch.goetschy.android.nfctext.contentprovider.MyContentProvider;
import ch.goetschy.android.nfctext.database.TextsTable;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.text.format.Time;

public class Text {
	public static int NO_ID = -1;

	// final vars
	private final long mTime;
	private final String mContent;
	private final boolean mSent;

	// non final var
	private int mId;
	private boolean mRead;

	public Text(long time, String content, boolean sent) {
		mTime = time;
		mContent = content;
		mSent = sent;
	}

	public Text(int id, long time, String content, boolean sent, boolean read) {
		this(time, content, sent);
		mId = id;
		mRead = read;
	}

	public static Text newText(Cursor cursor) {
		String content = cursor.getString(cursor
				.getColumnIndex(TextsTable.COLUMN_CONTENT));
		long time = cursor.getLong(cursor
				.getColumnIndex(TextsTable.COLUMN_TIME));
		int readInt = cursor.getInt(cursor
				.getColumnIndex(TextsTable.COLUMN_READ));
		int id = cursor.getInt(cursor.getColumnIndex(TextsTable.COLUMN_ID));

		// read or sent
		boolean read, sent;
		if (readInt == -1) {
			read = false;
			sent = true;
		} else {
			sent = false;
			if (readInt == 1)
				read = true;
			else
				read = false;
		}

		return new Text(id, time, content, sent, read);
	}

	// DB functions ----------------------------

	public void insertInDB(ContentResolver contentResolver) {
		ContentValues values = new ContentValues();
		values.put(TextsTable.COLUMN_CONTENT, this.getContent());
		values.put(TextsTable.COLUMN_TIME, this.getTime());

		// set read field
		int dbReadField;
		if (this.isSent())
			dbReadField = -1;
		else if (this.isRead())
			dbReadField = 1;
		else
			dbReadField = 0;

		values.put(TextsTable.COLUMN_READ, dbReadField);

		Uri uri = contentResolver.insert(MyContentProvider.CONTENT_URI_TEXTS,
				values);
		// set the id
		setId(Integer.valueOf(uri.getLastPathSegment()));
	}

	public String getTimeString() {

		// time object
		Time time = new Time();
		time.set(getTime());

		return time.format("%H:%M %d/%m/%Y");
	}

	public void setRead(boolean read, ContentResolver contentResolver) {
		int readInt = (read ? 1 : 0);

		ContentValues values = new ContentValues();
		values.put(TextsTable.COLUMN_READ, readInt); // value for "read"
		contentResolver.update(
				Uri.parse(MyContentProvider.CONTENT_URI_TEXTS + "/" + getId()),
				values, null, null);

		setRead(read);
	}

	// GETTERS AND SETTERS ---------------------

	public int getId() {
		return mId;
	}

	public void setId(int id) {
		this.mId = id;
		;
	}

	public long getTime() {
		return mTime;
	}

	public String getContent() {
		return mContent;
	}

	public boolean isRead() {
		return mRead;
	}

	public void setRead(boolean read) {
		this.mRead = read;
	}

	public boolean isSent() {
		return mSent;
	}

}
