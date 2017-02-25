package ch.goetschy.android.nfctext;

import ch.goetschy.android.nfctext.contentprovider.MyContentProvider;
import ch.goetschy.android.nfctext.database.TextsTable;
import android.app.ListActivity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Layout;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MsgListActivity extends ListActivity implements
		LoaderCallbacks<Cursor> {
	public static String TAG = "MsgListActivity";

	private CursorAdapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_msg_list);

		this.getListView().setDividerHeight(1);

		fillData();

	}

	private void fillData() {
		// init loader
		getLoaderManager().initLoader(0, null, this);
		// init adapter
		adapter = new TextAdapter(this, null, 0);
		setListAdapter(adapter);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		// do a copy in clipboard
		ClipboardManager clipboard = (ClipboardManager)
		        getSystemService(Context.CLIPBOARD_SERVICE);
		
		ViewGroup layout = (ViewGroup)v;
		CharSequence content = ((TextView)layout.getChildAt(0)).getText();
		ClipData clip = ClipData.newPlainText("nfctext",content);
		
		clipboard.setPrimaryClip(clip);
		
		Toast.makeText(this, "Text copied to clipboard", Toast.LENGTH_LONG).show();
		
		
		super.onListItemClick(l, v, position, id);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		String[] projection = { TextsTable.COLUMN_ID,
				TextsTable.COLUMN_CONTENT, TextsTable.COLUMN_TIME,
				TextsTable.COLUMN_READ };
		String order = TextsTable.COLUMN_ID + " DESC";
		CursorLoader cursorLoader = new CursorLoader(this,
				MyContentProvider.CONTENT_URI_TEXTS, projection, null, null,
				order);
		return cursorLoader;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		adapter.swapCursor(data);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		// data is not available anymore, delete reference
		adapter.swapCursor(null);
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		overridePendingTransition(R.anim.pull_in_left, R.anim.push_out_right);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			overridePendingTransition(R.anim.pull_in_left,
					R.anim.push_out_right);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
