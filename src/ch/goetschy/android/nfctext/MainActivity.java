package ch.goetschy.android.nfctext;

import java.nio.charset.Charset;

import ch.goetschy.android.nfctext.objects.Text;
import ch.goetschy.android.nfctext.objects.TextsManager;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcAdapter.OnNdefPushCompleteCallback;
import android.nfc.NfcEvent;
import android.nfc.NfcAdapter.CreateNdefMessageCallback;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.Time;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements
		CreateNdefMessageCallback, OnNdefPushCompleteCallback {

	public static final String TAG = "NfcText";
	public static final String APP_NAME = "ch.goetschy.android.nfctext";

	private NfcAdapter mNfcAdapter;

	private boolean mNfcOn = false;
	private boolean mIntentProcessed = false;
	private String mSentText = "";

	private EditText mEditText;
	private TextView mTextNfcState;
	private Switch mEnableNfcBut;

	// test
	// private TextView mTextView;
	// private Button mButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// mTextView = (TextView) findViewById(R.id.text);
		// mButton = (Button) findViewById(R.id.loopbut);
		mTextNfcState = (TextView) findViewById(R.id.nfc_state);
		mEditText = (EditText) findViewById(R.id.send_text);
		mEnableNfcBut = (Switch) findViewById(R.id.switch_enable_nfc);

		mNfcAdapter = NfcAdapter.getDefaultAdapter(this);

		// test if device support nfc
		if (mNfcAdapter == null) {
			// Stop here, we definitely need NFC
			Toast.makeText(this, "This device doesn't support NFC.",
					Toast.LENGTH_LONG).show();
			finish();
		}

		// test if nfc enabled
		checkNfc();

		// add events
		mEnableNfcBut.setOnClickListener(new EnableNfcListener());
		mEditText.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				if (mNfcOn && s.toString().length() != 0)
					findViewById(R.id.instructions).setVisibility(View.VISIBLE);
				else
					findViewById(R.id.instructions).setVisibility(
							View.INVISIBLE);
			}
		});

		// mButton.setOnClickListener(new OnClickListener() {
		// @Override
		// public void onClick(View v) {
		// addTextToList(mSendText.getText().toString(), true);
		// }
		// });

		// register callback
		mNfcAdapter.setNdefPushMessageCallback(this, this);
		mNfcAdapter.setOnNdefPushCompleteCallback(this, this);
	}

	private class EnableNfcListener implements OnClickListener {
		@Override
		public void onClick(View v) {
			// instruction toast
			Toast.makeText(
					getApplicationContext(),
					"Please activate NFC and press Back to return to the application.",
					Toast.LENGTH_LONG).show();
			// start settings activity
			startActivity(new Intent(
					android.provider.Settings.ACTION_NFC_SETTINGS));
		}
	}

	public void checkNfc() {
		// test if nfc enabled
		if (!mNfcAdapter.isEnabled()) {
			mTextNfcState
					.setText("NFC is disabled.\nIt must by enabled to send a text.");
			mEnableNfcBut.setChecked(false);
			mNfcOn = false;
		} else {
			mTextNfcState.setText("NFC is enabled.");
			mEnableNfcBut.setChecked(true);
			mNfcOn = true;
		}

	}

	// ACTION BAR ------------------------

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_go_texts:
			goToMsgList();
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	// --------------------

	public void addTextToList(String text, boolean received) {
		// new text
		Text newtext = new Text(getCurrentTime(), text, !received);
		newtext.setId(Text.NO_ID);
		TextsManager.add(getContentResolver(), newtext);

		// switch activity
		if (received)
			goToMsgList();
	}

	private void goToMsgList() {
		Intent intent = new Intent(MainActivity.this, MsgListActivity.class);
		startActivity(intent);
		overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
	}

	private long getCurrentTime() {
		Time now = new Time();
		now.setToNow();
		return now.toMillis(false);
	}

	// ACTIVITY LIFE CYCLE -------------
	@Override
	public void onNewIntent(Intent intent) {
		Log.w(TAG, "on new intent");
		// onResume gets called after this to handle the intent
		setIntent(intent);
	}

	@Override
	public void onResume() {
		super.onResume();
		if (BuildConfig.DEBUG)
			Log.w(TAG, "on resume");
		// check for nfc enabled
		checkNfc();
		// Check to see that the Activity started due to an Android Beam
		// but not twice the same intent !
		if (mIntentProcessed == false
				&& NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent()
						.getAction())) {
			if (BuildConfig.DEBUG)
				Log.w(TAG, "on resume with intent");
			processIntent(getIntent());
		}
	}

	/**
	 * Parses the NDEF Message from the intent and prints to the TextView
	 */
	void processIntent(Intent intent) {
		Parcelable[] rawMsgs = intent
				.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
		// only one message sent during the beam
		NdefMessage msg = (NdefMessage) rawMsgs[0];
		// record 0 contains the MIME type, record 1 is the AAR, if present
		this.mSentText = new String(msg.getRecords()[0].getPayload());

		addTextToList(mSentText, true);

		// set text processed
		mIntentProcessed = true;

		// test
		// mTextView.setText(payload);
		// Log.w(TAG, "processIntent with paload " + payload);
	}

	// FROM CreateNdefMessageCallback -------------

	@Override
	public NdefMessage createNdefMessage(NfcEvent event) {
		Log.w(TAG, "createNdefMessage");
		String text = mEditText.getText().toString();
		// create message
		NdefMessage msg = new NdefMessage(new NdefRecord[] {
				createMimeRecord("application/" + APP_NAME, text.getBytes()),
				NdefRecord.createApplicationRecord(APP_NAME) });

		return msg;
	}

	/**
	 * Creates a custom MIME type encapsulated in an NDEF record
	 * 
	 * @param mimeType
	 */
	public NdefRecord createMimeRecord(String mimeType, byte[] payload) {
		byte[] mimeBytes = mimeType.getBytes(Charset.forName("US-ASCII"));
		NdefRecord mimeRecord = new NdefRecord(NdefRecord.TNF_MIME_MEDIA,
				mimeBytes, new byte[0], payload);
		return mimeRecord;
	}

	@Override
	public void onNdefPushComplete(NfcEvent event) {
		if (BuildConfig.DEBUG)
			Log.w(TAG, "onNdefPushComplete");
		// add to sent messages
//		addTextToList(mSentText, false);
	}
}
