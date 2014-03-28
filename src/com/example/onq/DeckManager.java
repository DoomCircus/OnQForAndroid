package com.example.onq;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

public class DeckManager {

	private MainActivity m;
	private List<QCardSet> localSet;
	private SharedPreferences prefs;
	private volatile boolean responseReceived;
	private volatile boolean waiting;
	private volatile String jsonStr;
	private volatile String serverResponse;
	private boolean decksParsed;
	private volatile String toastMsg;
	
	public String getToastMessage() {
		return toastMsg;
	}

	public DeckManager() {
		m = (MainActivity) MainActivity.tmpActivity;
		localSet = m.getqCardSetList();
		prefs = PreferenceManager.getDefaultSharedPreferences(m);
		if (prefs.equals(null)) {
			toastMsg = "A problem has occured. If you are a registered user of OnQ, delete this app"
					+ "and try reinstalling it.";
			throw new NullPointerException("[DeckManager]No StoredPreferences exist for OnQ");
		}
	}

	public void LoadDecksFromPrefs() {
		String jsonDecks = prefs.getString("UserDecks", "");
		if (!jsonDecks.isEmpty())
		{
			ParseDecks(jsonDecks);
			if (decksParsed)
			{
				m.setqCardSetList(localSet);
				//toastMsg = "Your decks were successfully loaded from app data!";
			}
		}
		else
		{
			toastMsg = "No decks found in app data!";
		}
	}
	
	public void SaveDecksToPrefs() {
		jsonStr = EncodeJSONDecks();
		if (jsonStr.equals("FAIL"))
		{
			toastMsg = "Your decks failed to save to app data.";
		}
		else
		{
			prefs.edit().putString("UserDecks", jsonStr).commit();
			//toastMsg = "Your decks were successfully saved to app data!";
		}
	}
	
	public void PushDecksToServer() {
		
		final String username = prefs.getString("Username", "");
		final String password = prefs.getString("Password", "");
		
		if (!username.isEmpty() && !password.isEmpty()) {
			jsonStr = EncodeJSONDecks();
			try {
				jsonStr = URLEncoder.encode(jsonStr, "ISO-8859-1");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			
			responseReceived = false;
			waiting = true;

			Thread t = new Thread(new Runnable() {
				// Thread to stop network calls on the UI thread
				public void run() {
					try {
						CallServer("PUSH", username, password, jsonStr);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});

			t.start();

			while (waiting) {
				if (responseReceived) {
					/*if(t != null)
					{
						t.interrupt();
						t = null;
					}*/
					waiting = false;
					// decode JSON object returned from server
					if (DecodeJSONResponse(serverResponse) == 2)
					{
						ParseDecks(serverResponse);
						if (decksParsed)
						{
							m.setqCardSetList(localSet);
							//toastMsg = "Your decks were successfully saved to the OnQ server!";
						}
					}
				}
			}
		}
	}
	
	public String EncodeJSONDecks() {
		try {
			JSONArray userDecks = new JSONArray();
			for(QCardSet qcs : localSet)
			{
				JSONObject completeDeck = new JSONObject();
				JSONObject Qdecks = new JSONObject();
				
				Qdecks.put("deckID", qcs.getDeckID());
				Qdecks.put("deckType", qcs.getDeckType());
				Qdecks.put("title", qcs.getCardListName());
				Qdecks.put("description", qcs.getDescription());
				Qdecks.put("privatePublic", qcs.getPrivatePublic());
				
				completeDeck.put("Qdecks", Qdecks);
				
				JSONArray Qdeckcards = new JSONArray();
				List<QCard> cards = qcs.getqCardsList();
				for(QCard qc : cards)
				{
					JSONObject Qcards = new JSONObject();
					
					Qcards.put("cardID", qc.getCardID());
					Qcards.put("cardType", qc.getSetName());
					Qcards.put("question", qc.getQuestion());
					Qcards.put("answer", qc.getAnswer());
					
					JSONObject tmp = new JSONObject();
					tmp.put("Qcards", Qcards);
					Qdeckcards.put(tmp);
				}
				completeDeck.put("Qdeckcards", Qdeckcards);
				userDecks.put(completeDeck);
			}
			return userDecks.toString();
		} catch (JSONException je) {
			je.printStackTrace();
			return "FAIL";
		}
	}

	public void PullDecksFromServer() {
		final String username = prefs.getString("Username", "");
		final String password = prefs.getString("Password", "");

		if (!username.isEmpty() && !password.isEmpty()) {
			responseReceived = false;
			waiting = true;

			Thread t = new Thread(new Runnable() {
				// Thread to stop network calls on the UI thread
				public void run() {
					try {
						CallServer("PULL", username, password, null);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});

			t.start();

			while (waiting) {
				if (responseReceived) {
					/*if(t != null)
					{
						t.interrupt();
						t = null;
					}*/
					waiting = false;
					// decode JSON object returned from server
					int ret = DecodeJSONResponse(serverResponse);
					if (ret == 1 || ret == 2)
					{
						if (ret == 2)
						{
							ParseDecks(serverResponse);
							if (decksParsed)
							{
								m.setqCardSetList(localSet);
								//Update StoredPreferences with parsed decks
								toastMsg = "Your decks were successfully loaded from the OnQ server!";
							}
						}
					}
				}
			}
		}
	}

	private void CallServer(String action, String username, String password, String jsonDecks) {
		String onqURL = "";

		if (action.equals("PULL")) {
			onqURL = "http://192.168.0.29:1337/onq/qmobile/pullDecks/" + username + "/" + password;
			//onqURL = "http://142.156.74.223:1337/onq/qmobile/pullDecks/"+username+"/"+password;
		} else if (action.equals("PUSH")) {
			String token = prefs.getString("SecurityToken", "");
			String asciiToken = "";
			
			for (int i = 0; i < token.length(); ++i)
			{
				asciiToken += (int)token.charAt(i);
				if (i < token.length() - 1)
				{
					asciiToken += "+";
				}
			}
			
			onqURL = "http://192.168.0.29:1337/onq/qmobile/uploadDecks/" + username + "/" + password
					+ "/" + asciiToken + "/" + jsonDecks;
			//onqURL = "http://142.156.74.223:1337/onq/qmobile/uploadDecks/" + username + "/" + password
			//		+ "/" + asciiToken + "/" + jsonDecks;
		}
		HttpClient Client = new DefaultHttpClient();
		try {
			HttpGet httpget = new HttpGet(onqURL);
			HttpResponse response = Client.execute(httpget);
			StatusLine statusLine = response.getStatusLine();

			if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				response.getEntity().writeTo(out);
				out.close();
				serverResponse = out.toString();
				responseReceived = true;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			Log.e("CallServer", ex.getMessage());
			waiting = false;
		}
	}

	public int DecodeJSONResponse(String jsonStr) {
		try {
			Object obj = new JSONTokener(jsonStr).nextValue();
			
			if (obj instanceof JSONObject)
			{
				return 2;
			}
			else if (obj instanceof JSONArray)
			{
				//Parse the response code from the server
				JSONArray jsonArr = new JSONArray(jsonStr);
				
				if (jsonArr.getString(0).equals("Error")) {
					toastMsg = jsonArr.getString(1);
					return 0;
				} else if (jsonArr.getString(0).equals("Success")) {
					toastMsg = jsonArr.getString(1);
					return 1;
				}
			}
			toastMsg = "The OnQ server returned an unexpected error, please contact an OnQ administrator.";
			return -1;

		} catch (JSONException je) {
			je.printStackTrace();
			toastMsg = je.getMessage();
			return -1;
		}
	}
	
	public void ParseDecks(String jsonStr)
	{
		decksParsed = false;
		try {
			int numDecks = countMatches(jsonStr, "|");
			String[] deckStrings = new String[numDecks + 1];
			int startOfDeck = 0;
			
			//Parse decks (String.split() had unexpected behaviour, so parsing the string manually)
			for (int i = 0; i < numDecks; ++i)
			{
				int endOfDeck = jsonStr.indexOf("|", startOfDeck);
				deckStrings[i] = jsonStr.substring(startOfDeck, endOfDeck);
				startOfDeck = endOfDeck + 1;
			}
			deckStrings[numDecks] = jsonStr.substring(startOfDeck, jsonStr.length());
			
			for (int i = 0; i < deckStrings.length; ++i)
			{
				// Convert String to json object
				JSONObject jsonDeck = new JSONObject(deckStrings[i]);
				JSONObject deck = jsonDeck.getJSONObject("Qdecks");
				
				//Parse deck data from JSONObject to QCardSet
				QCardSet newDeck = new QCardSet();
				newDeck.setDeckID(deck.getInt("deckID"));
				newDeck.setDeckType(deck.getString("deckType"));
				newDeck.setCardListName(deck.getString("title"));
				newDeck.setDescription(deck.getString("description"));
				newDeck.setPrivatePublic(deck.getInt("privatePublic"));
				
				JSONArray deckCards = jsonDeck.getJSONArray("Qdeckcards");
				List<QCard> newCards = new ArrayList<QCard>();
				
				//Parse card data from JSONArray of cards into List<QCard>
				for (int j = 0; j < deckCards.length(); ++j)
				{
					JSONObject card = deckCards.getJSONObject(j).getJSONObject("Qcards");
					QCard newCard = new QCard();
					
					newCard.setCardID(card.getInt("cardID"));
					newCard.setSetName(card.getString("cardType"));
					newCard.setQuestion(card.getString("question"));
					newCard.setAnswer(card.getString("answer"));
					
					newCards.add(newCard);
				}
				
				//Check if the deck already exists in the local set
				for (QCardSet qcs : localSet)
				{
					if (qcs.getDeckID() == newDeck.getDeckID())
					{
						List<QCard> oldCards = qcs.getqCardsList();
						//Check if the card already exists in the current deck
						for (QCard oldQC : oldCards)
						{
							boolean exists = false;
							for (QCard newQC : newCards)
							{
								if (oldQC.getCardID() == newQC.getCardID())
								{
									exists = true;
									break;
								}
							}
							if (!exists)
							{
								newCards.add(oldQC);
							}
						}
						newDeck.setqCardsList(newCards);
						localSet.remove(qcs);
						break;
					}
				}
				localSet.add(newDeck);
			}
			decksParsed = true;
		} catch (JSONException je) {
			je.printStackTrace();
		}
		
	}
	
	//Source: http://stackoverflow.com/questions/6100712/simple-way-to-count-character-occurrences-in-a-string
	//Poster: Casey - http://stackoverflow.com/users/147373/casey
	//Poster's Source: http://www.docjar.com/html/api/org/apache/commons/lang/StringUtils.java.html
	//Date Used: March 10, 2014
	public static int countMatches(String str, String sub) {
	    if (str.isEmpty() || sub.isEmpty()) {
	        return 0;
	    }
	    int count = 0;
	    int idx = 0;
	    while ((idx = str.indexOf(sub, idx)) != -1) {
	        count++;
	        idx += sub.length();
	    }
	    return count;
	}
}
