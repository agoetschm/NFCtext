package ch.goetschy.android.nfctext.objects;

import java.util.ArrayList;

import android.content.ContentResolver;
import android.text.format.Time;

public class TextsManager {
	private static ArrayList<Text> mTextsList = new ArrayList<Text>();


	public static void add(ContentResolver contentResolver, Text text) {
		mTextsList.add(text);

		// no id means not in db -> save in db
		if (text.getId() == Text.NO_ID)
			text.insertInDB(contentResolver);
	}
	

	public static long getCurrentTime() {
		Time now = new Time();
		now.setToNow();
		return now.toMillis(false);
	}

}
