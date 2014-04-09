package com.example.onq;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcAdapter.CreateNdefMessageCallback;
import android.nfc.NfcAdapter.OnNdefPushCompleteCallback;
import android.nfc.NfcEvent;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;


public class BumpDeck extends Activity implements CreateNdefMessageCallback,OnNdefPushCompleteCallback{


	private NfcAdapter mNfcAdapter;
	private Intent nfcIntent;
	private PendingIntent pendingIntent;
	private MainActivity m;
	private int cardSetId = 0;
	private static final int MESSAGE_SENT = 1;
	private QCardSet newSet = new QCardSet();
	private List<String> setNames = new ArrayList<String>();
	private ListView lv;
	private List<QCard> cardsextra = new ArrayList<QCard>();
	private List<QCard> reccard = new ArrayList<QCard>();
	//private ImageView ad;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_bump);

		// Check for available NFC Adapter
		mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
		pendingIntent = PendingIntent.getActivity(
				this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
		if (mNfcAdapter == null) {
			AlertDialog.Builder adb = new AlertDialog.Builder(
					BumpDeck.this);
			adb.setTitle("NFC");
			adb.setMessage("NFC is not available on this device.");
			adb.setPositiveButton("Ok", null);
			adb.show(); 
		}
		// Register callback to set NDEF message
		mNfcAdapter.setNdefPushMessageCallback(this, this);
		// Register callback to listen for message-sent success
		mNfcAdapter.setOnNdefPushCompleteCallback(this, this);
		cardsextra =  getIntent().getParcelableArrayListExtra("Cards");
		//ad = (ImageView) findViewById(R.id.adView);
		m =(MainActivity) MainActivity.tmpActivity; 
		int i = 0;
		while( i < m.getqCardSetList().size())
		{
			setNames.add(m.getqCardSetList().get(i).getCardListName());
			i++;
		}
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(BumpDeck.this, R.layout.custom_textview, setNames);
		lv = (ListView) findViewById(R.id.listview);
		lv.setAdapter(adapter);
		lv.setTextFilterEnabled(true);

		lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				AlertDialog.Builder adb = new AlertDialog.Builder(
						BumpDeck.this);
				adb.setTitle("Current Card Deck");
				adb.setMessage("" + lv.getItemAtPosition(arg2));
				adb.setPositiveButton("Ok", null);
				adb.show();   
				m.setTheSetName(lv.getItemAtPosition(arg2).toString());
				/*if(m.getTheSetName() == "BeerQuestions"){
					ad.setBackgroundResource(R.drawable.beerad);
				}
				else if(m.getTheSetName() == "C++Questions"){
					ad.setBackgroundResource(R.drawable.progad);
				}
				else if(m.getTheSetName() == "JavaQuestions"){
					ad.setBackgroundResource(R.drawable.javaad);
				}*/
			}

		});


	}

	@SuppressWarnings("deprecation")
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		if (mNfcAdapter != null) {
			mNfcAdapter.disableForegroundDispatch(this);
			mNfcAdapter.disableForegroundNdefPush(this);
		}
		nfcIntent = getIntent();
		setIntent(nfcIntent);
	}

	/**
	 * Implementation for the CreateNdefMessageCallback interface
	 */
	@Override
	public NdefMessage createNdefMessage(NfcEvent event) {

		//EditText txt = (EditText)findViewById(R.id.editText1);

		int i = 0;
		while( i < m.getqCardSetList().size())
		{
			if(m.getqCardSetList().get(i).getCardListName() == m.getTheSetName()){
				cardSetId = i;
				break;
			}
			i++;
		}
		newSet = m.getqCardSetList().get(cardSetId);
		cardsextra = m.getqCardSetList().get(cardSetId).getqCardsList();
		//serialize
		byte[] data = serializeObject(cardsextra);
		//String text = cardsextra.toString();


		NdefMessage msg = new NdefMessage(
				new NdefRecord[] { createMimeRecord(
						"application/com.example.onq",data )
				});
		return msg;
	}




	/**
	 * Implementation for the OnNdefPushCompleteCallback interface
	 */
	@Override
	public void onNdefPushComplete(NfcEvent arg0) {
		// A handler is needed to send messages to the activity when this
		// callback occurs, because it happens from a binder thread
		mHandler.obtainMessage(MESSAGE_SENT).sendToTarget();
	}

	/** This handler receives a message from onNdefPushComplete */
	@SuppressLint("HandlerLeak")
	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MESSAGE_SENT:
				Toast.makeText(getApplicationContext(), "Cards sent!", Toast.LENGTH_LONG).show();
				break;
			}
		}
	};

	@Override
	public void onResume() {
		super.onResume();
		// Check to see that the Activity started due to an Android Beam
		mNfcAdapter.enableForegroundDispatch(this, pendingIntent, null, null);
		if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction())) {
			processIntent(nfcIntent);
		}
	}

	@Override
	public void onNewIntent(Intent intent) {
		// onResume gets called after this to handle the intent
		nfcIntent = intent;
		setIntent(intent);
	}

	/**
	 * Parses the NDEF Message from the intent and prints to the TextView
	 */
	@SuppressWarnings("unchecked")
	void processIntent(Intent intent) {
		Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
		// only one message sent during the beam
		NdefMessage msg = (NdefMessage) rawMsgs[0];
		// record 0 contains the MIME type, record 1 is the AAR, if present
		//mInfoText.setText(new String(msg.getRecords()[0].getPayload()));


		//deserialize
		byte[] data = msg.getRecords()[0].getPayload();
		reccard = (ArrayList<QCard>) deserializeObject(data);
		newSet.setCardListName(reccard.get(0).getSetName());
		newSet.setqCardsList(reccard);
		m.getqCardSetList().add(newSet);

	}

	/**
	 * Creates a custom MIME type encapsulated in an NDEF record
	 *
	 * @param mimeType
	 */
	public NdefRecord createMimeRecord(String mimeType, byte[] payload) {
		byte[] mimeBytes = mimeType.getBytes(Charset.forName("US-ASCII"));
		NdefRecord mimeRecord = new NdefRecord(
				NdefRecord.TNF_MIME_MEDIA, mimeBytes, new byte[0], payload);
		return mimeRecord;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// If NFC is not available, we won't be needing this menu
		if (mNfcAdapter == null) {
			return super.onCreateOptionsMenu(menu);
		}
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.nfc, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_settings:
			Intent intent = new Intent(Settings.ACTION_NFCSHARING_SETTINGS);
			startActivity(intent);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}


	public static byte[] serializeObject(Object o) { 
		ByteArrayOutputStream bos = new ByteArrayOutputStream(); 

		try { 
			ObjectOutput out = new ObjectOutputStream(bos); 
			out.writeObject(o); 
			out.close(); 

			// Get the bytes of the serialized object 
			byte[] buf = bos.toByteArray(); 

			return buf; 
		} catch(IOException ioe) { 
			Log.e("serializeObject", "error", ioe); 

			return null; 
		} 
	} 


	public static Object deserializeObject(byte[] b) { 
		try { 
			ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(b)); 
			Object object = in.readObject(); 
			in.close(); 

			return object; 
		} catch(ClassNotFoundException cnfe) { 
			Log.e("deserializeObject", "class not found error", cnfe); 

			return null; 
		} catch(IOException ioe) { 
			Log.e("deserializeObject", "io error", ioe); 

			return null; 
		} 
	}
}
