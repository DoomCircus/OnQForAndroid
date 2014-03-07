package com.example.onq;

import java.io.ByteArrayOutputStream;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class DeckManager {
	
	private MainActivity m;
	protected SharedPreferences prefs;
	protected volatile boolean decksReceived;
	protected volatile boolean waiting;
	
	public DeckManager(){
		m = (MainActivity) MainActivity.tmpActivity;
		prefs = PreferenceManager.getDefaultSharedPreferences(m);
		if(prefs.equals(null)) {
			throw new NullPointerException("[DeckManager]No StoredPreferences exist for OnQ");
		}
	}
	
	public void LoadDecksFromPrefs(){
		
	}
	
	public void PullDecksFromServer(){
		final String username = prefs.getString("Username", "");
		final String password = prefs.getString("Password", "");
		
		if (!username.equals(""))
		{
			if (!password.equals(""))
			{
				decksReceived = false;
				waiting = true;
				
				new Thread(new Runnable()
				{
				    //Thread to stop network calls on the UI thread
				    public void run()
				    {
				        try
				        {
				        	ServerPull(username, password);
				        }
				        catch (Exception e)
				        {
				            e.printStackTrace();
				        }
				    }
				}).start();
				
				while(waiting)
				{
					if(decksReceived)
					{
						//start checking local decks and adding missing decks that
						//were pulled from the server
					}
				}
			}
		}
	}
	
	private void ServerPull(String username, String password)
	{
		String onqURL = "http://192.168.0.23:1337/onq/qmobile/pullDecks/"+username+"/"+password;
		//String onqURL = "http://142.156.75.146:1337/onq/qmobile/pullDecks/"+username+"/"+password;
		HttpClient Client = new DefaultHttpClient();
		try
		{
			HttpGet httpget = new HttpGet(onqURL);
			String serverString = "";
			HttpResponse response = Client.execute(httpget);
			StatusLine statusLine = response.getStatusLine();
			
			if (statusLine.getStatusCode() == HttpStatus.SC_OK)
			{
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				response.getEntity().writeTo(out);
				out.close();
				serverString = out.toString();
				
				//decode JSON object in serverString
				DecodeJSONDecks(serverString);
				
				/*String tmp = serverString.substring(1, serverString.length()-1);
				String[]resPieces = tmp.split(",");
				
				for (int i = 0; i < resPieces.length; ++i)
				{
					resPieces[i] = resPieces[i].substring(1, resPieces[i].length()-1);
				}
				
				if (resPieces[0].equals("SecurityToken"))
				{
					prefs = PreferenceManager.getDefaultSharedPreferences(this);
					prefs.edit().putString("SecurityToken", resPieces[1]).commit();
					prefs.edit().putString("Username", username).commit();
					prefs.edit().putString("Password", password).commit();
					tokenReceived = true;
				}
				else if (resPieces[0].equals("Error"))
				{
					error = resPieces[1];
					waiting = false;
				}
				else
				{
					throw new Exception("Unexpected issue encountered, please contact the OnQ development team.");
				}*/
			}
		}
		catch(Exception ex){
			ex.printStackTrace();
		}
	}
	
	public void DecodeJSONDecks(String jsonStr){
		try {
			// Convert String to json object
			JSONObject json = new JSONObject(jsonStr);
			JSONArray jsonDecks = new JSONArray(json.getString("Qdecks"));
			
			for (int i = 0; i < jsonDecks.length(); ++i) {
				
			}
		}
		catch (JSONException je) {
			je.printStackTrace();
		}
	}
	
	public void StoreDecksInPrefs(){
		
	}
	
	public void PushDecksToServer(){
		
	}
	
	public String EncodeJSONDecks(){
		return "";
	}
}
