package ch.goetschy.android.nfctext;

import ch.goetschy.android.nfctext.objects.Text;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

public class TextAdapter extends CursorAdapter {
	public final static String TAG = "TextAdapter";

	private LayoutInflater cursorInflater;

	public TextAdapter(Context context, Cursor cursor, int flags) {
		super(context, cursor, flags);
		cursorInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		return cursorInflater.inflate(R.layout.activity_list_item, parent,
				false);
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		// get textviews
		TextView tvContent = (TextView) view
				.findViewById(R.id.activity_list_content);
		TextView tvTime = (TextView) view.findViewById(R.id.activity_list_time);

		// text object
		Text text = Text.newText(cursor);

		// read or sent
		String timeLabel;
		if (text.isSent()) {
			timeLabel = "sent at ";
			// other background for sent :
			view.setBackgroundColor(context.getResources().getColor(
					R.color.light_grey));
		} else
			timeLabel = "received at ";

		// not read
		if (text.isRead() == false) {
			tvContent.setTypeface(null, Typeface.BOLD);
			// now it's read
			text.setRead(true, context.getContentResolver());
		}

		// set views
		tvContent.setText(text.getContent());
		tvTime.setText(timeLabel + text.getTimeString());
	}

}
