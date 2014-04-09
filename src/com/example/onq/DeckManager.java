package com.example.onq;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class DeckManager {

	private MainActivity m;
	private List<QCardSet> localSet;
	private SharedPreferences prefs;
	private volatile String jsonStr;
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
			throw new NullPointerException("[DeckManager] No StoredPreferences exist for OnQ");
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
		for(QCardSet qcs : localSet)
		{
			jsonStr = EncodeJSONDecks(qcs);
			if (jsonStr.equals("FAIL"))
			{
				toastMsg = "Your decks failed to save to app data.";
			}
			else
			{
				jsonStr += jsonStr;
			}
		}
		prefs.edit().putString("UserDecks", jsonStr).commit();
		//toastMsg = "Your decks were successfully saved to app data!";
	}
	
	public void PushDecksToServer() {
		
		final String username = prefs.getString("Username", "");
		final String password = prefs.getString("Password", "");
		
		if (!username.isEmpty() && !password.isEmpty()) {
			for(QCardSet qcs : localSet)
			{
				jsonStr = EncodeJSONDecks(qcs);
				try {
					jsonStr = URLEncoder.encode(jsonStr, "ISO-8859-1");
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
				
				ConnectionManager.responseReceived = false;
				ConnectionManager.waiting = true;
	
				Thread t = new Thread(new Runnable() {
					// Thread to stop network calls on the UI thread
					public void run() {
						try {
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
							ConnectionManager.CallServer("PUSH", username, password, asciiToken, jsonStr);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				});
	
				t.start();
	
				boolean responseOK = false;
				
				while (ConnectionManager.waiting) {
					if (ConnectionManager.responseReceived) {
						/*if(t != null)
						{
							t.interrupt();
							t = null;
						}*/
						ConnectionManager.waiting = false;
						// decode JSON object returned from server
						if (ConnectionManager.DecodeJSONResponse(ConnectionManager.serverResponse) == 1)
						{
							responseOK = true;
							toastMsg = ConnectionManager.toastMsg;
						}
					}
				}
				if (!responseOK)
				{
					toastMsg = ConnectionManager.toastMsg;
				}
			}
		}
	}
	
	public String EncodeJSONDecks(QCardSet qcs) {
		try {
			JSONArray userDecks = new JSONArray();
			JSONObject qDeck = new JSONObject();
			
			qDeck.put("deckID", qcs.getDeckID());
			qDeck.put("deckType", qcs.getDeckType());
			qDeck.put("title", qcs.getCardListName());
			qDeck.put("description", qcs.getDescription());
			qDeck.put("privatePublic", qcs.getPrivatePublic());
			
			JSONArray qDeckCards = new JSONArray();
			List<QCard> cards = qcs.getqCardsList();
			for(QCard qc : cards)
			{
				JSONObject qCard = new JSONObject();
				
				qCard.put("cardID", qc.getCardID());
				qCard.put("cardType", qc.getSetName());
				qCard.put("question", qc.getQuestion());
				qCard.put("answer", qc.getAnswer());
				
				qDeckCards.put(qCard);
			}
			qDeck.put("Qdeckcards", qDeckCards);
			userDecks.put(qDeck);
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
			ConnectionManager.responseReceived = false;
			ConnectionManager.waiting = true;

			Thread t = new Thread(new Runnable() {
				// Thread to stop network calls on the UI thread
				public void run() {
					try {
						ConnectionManager.CallServer("PULL", username, password, null, null);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});

			t.start();

			boolean responseOK = false;
			
			while (ConnectionManager.waiting) {
				if (ConnectionManager.responseReceived) {
					/*if(t != null)
					{
						t.interrupt();
						t = null;
					}*/
					ConnectionManager.waiting = false;
					// decode JSON object returned from server
					if (ConnectionManager.DecodeJSONResponse(ConnectionManager.serverResponse) == 2)
					{
						responseOK = true;
						ParseDecks(ConnectionManager.serverResponse);
						if (decksParsed)
						{
							m.setqCardSetList(localSet);
							//Update StoredPreferences with parsed decks
							toastMsg = "Your decks were successfully loaded from the OnQ server!";
						}
					}
				}
			}
			if (!responseOK)
			{
				toastMsg = ConnectionManager.toastMsg;
			}
		}
	}
	
	public void ParseDecks(String jsonStr)
	{
		decksParsed = false;
		try {
			// Convert String to json array
			JSONArray jsonDecks = new JSONArray(jsonStr);
			List<QCardSet> newUserDecks = new ArrayList<QCardSet>();
			
			for (int i = 0; i < jsonDecks.length(); ++i)
			{
				//Pull out each deck one at a time
				JSONObject deck = jsonDecks.getJSONObject(i);
				
				//Parse deck data from JSONObject to QCardSet
				QCardSet newDeck = new QCardSet();
				newDeck.setDeckID(deck.getInt("deckID"));
				newDeck.setDeckType(deck.getString("deckType"));
				newDeck.setCardListName(deck.getString("title"));
				newDeck.setDescription(deck.getString("description"));
				newDeck.setPrivatePublic(deck.getInt("privatePublic"));
				
				JSONArray deckCards = deck.getJSONArray("Qdeckcards");
				List<QCard> newCards = new ArrayList<QCard>();
				
				//Parse card data from JSONArray of cards into List<QCard>
				for (int j = 0; j < deckCards.length(); ++j)
				{
					JSONObject card = deckCards.getJSONObject(j);
					QCard newCard = new QCard();
					
					newCard.setCardID(card.getInt("cardID"));
					newCard.setSetName(card.getString("cardType"));
					newCard.setQuestion(card.getString("question"));
					newCard.setAnswer(card.getString("answer"));
					
					newCards.add(newCard);
				}
				newDeck.setqCardsList(newCards);
				newUserDecks.add(newDeck);
			}
			
			List<QCardSet> currentSet = new ArrayList<QCardSet>();
			
			//Check if the deck already exists in the local set
			for (QCardSet qcs : newUserDecks)
			{
				boolean deckExists = false;
				for (QCardSet lqcs : localSet)
				{
					if (lqcs.getDeckID() == qcs.getDeckID())
					{
						deckExists = true;
						QCardSet localDeck = lqcs;
						List<QCard> localCards = localDeck.getqCardsList();
						List<QCard> newCards = qcs.getqCardsList();
						//Check if the card already exists in the current deck
						for (QCard newQC : newCards)
						{
							boolean cardExists = false;
							for (QCard localQC : localCards)
							{
								if (localQC.getCardID() == newQC.getCardID())
								{
									cardExists = true;
								}
							}
							if (!cardExists)
							{
								localCards.add(newQC);
							}
						}
						localDeck.setqCardsList(localCards);
						currentSet.add(localDeck);
					}
				}
				if (!deckExists)
				{
					currentSet.add(qcs);
				}
			}
			localSet = currentSet;
			decksParsed = true;
		} catch (JSONException je) {
			je.printStackTrace();
		}
		
	}
	
	//Source: http://stackoverflow.com/questions/6100712/simple-way-to-count-character-occurrences-in-a-string
	//Poster: Casey - http://stackoverflow.com/users/147373/casey
	//Poster's Source: http://www.docjar.com/html/api/org/apache/commons/lang/StringUtils.java.html
	//Date Used: March 10, 2014
	/*public static int countMatches(String str, String sub) {
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
	}*/
	
	public void ErasePrefs()
	{
		prefs.edit().clear().apply();
	}
}
