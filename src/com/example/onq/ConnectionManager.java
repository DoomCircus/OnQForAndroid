package com.example.onq;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class ConnectionManager {
	
	private final static String serverURL = "http://www.studywithonq.com";
	private final static int timeout = 10000;
	private static boolean serverReachable;
	public static String toastMsg;
	public static String serverResponse;
	public static boolean responseReceived;
	public static boolean waiting;
	
	public static boolean IsOnline(Context context) {
	    ConnectivityManager connectivityManager 
	          = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
	    return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}
	
	public static boolean ServerIsReachable(Context context) {
	    if (IsOnline(context)) {
	    	serverReachable = false;
			waiting = true;

			Thread t = new Thread(new Runnable() {
				// Thread to stop network calls on the UI thread
				public void run() {
					try {
			            URL url = new URL(serverURL);
			            HttpURLConnection urlc = (HttpURLConnection) url.openConnection();
			            urlc.setConnectTimeout(timeout);
			            urlc.connect();
			            if (urlc.getResponseCode() == 200) { // 200 = "OK" code (http connection is fine).
			                Log.e("ServerIsReachable", "Success!");
			                toastMsg = "Server is reachable";
							serverReachable = true;
							waiting = false;
			            } else {
			            	Log.e("ServerIsReachable", "Failure!");
			            	toastMsg = "Server replied with response code: " + urlc.getResponseCode();
							serverReachable = false;
							waiting = false;
			            }
			        } catch (MalformedURLException e) {
			        	e.printStackTrace();
						Log.e("ServerIsReachable", e.getMessage());
						toastMsg = "Server is unreachable: " + e.getMessage();
						serverReachable = false;
						waiting = false;
			        } catch (IOException e) {
			        	e.printStackTrace();
						Log.e("ServerIsReachable", e.getMessage());
						toastMsg = "Server is unreachable: " + e.getMessage();
						serverReachable = false;
						waiting = false;
			        }
				}
			});

			t.start();

			while (waiting) {
				if (serverReachable) {
					/*if(t != null)
					{
						t.interrupt();
						t = null;
					}*/
					waiting = false;
					return true;
				}
			}
	    }
	    return false;
	}
	
	public static void CallServer(String action, String username, String password, String asciiToken, String jsonDecks) {
		String onqURL = "";
		
		if (action.equals("LOGIN")) {
			onqURL = serverURL + "/qmobile/login/"+username+"/"+password;
		}
		else if (action.equals("PULL")) {
			onqURL = serverURL + "/qmobile/pullDecks/" + username + "/" + password;
		}
		else if (action.equals("PUSH")) {
			onqURL = serverURL + "/qmobile/uploadDecks/" + username + "/" + password
					+ "/" + asciiToken + "/" + jsonDecks;
		}
		try {
			HttpGet httpget = new HttpGet(onqURL);
			
			HttpParams httpParameters = new BasicHttpParams();
			// Set the default socket timeout (SO_TIMEOUT) 
			// in milliseconds which is the timeout for waiting for data.
			HttpConnectionParams.setSoTimeout(httpParameters, timeout);
			
			HttpClient Client = new DefaultHttpClient();
			HttpResponse response = Client.execute(httpget);
			StatusLine statusLine = response.getStatusLine();

			if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				response.getEntity().writeTo(out);
				out.close();
				serverResponse = out.toString();
				responseReceived = true;
			}
			else
			{
				Log.e("CallServer", statusLine.getReasonPhrase());
				toastMsg = statusLine.getReasonPhrase();
				waiting = false;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			Log.e("CallServer", ex.getMessage());
			toastMsg = ex.getMessage();
			waiting = false;
		}
	}
	
	public static int DecodeJSONResponse(String jsonStr) {
		try {
			Object obj = new JSONTokener(jsonStr).nextValue();
			
			if (obj instanceof JSONObject)
			{
				//Response object returned, not JSONArray of deck objects
				//Parse the response code from the server
				JSONObject jsonObj = new JSONObject(jsonStr);
				
				if (jsonObj.getString("0").equals("Error")) {
					toastMsg = jsonObj.getString("1");
					return 0;
				} else if (jsonObj.getString("0").equals("Success")) {
					toastMsg = jsonObj.getString("1");
					return 1;
				} else if (jsonObj.getString("0").equals("SecurityToken")) {
					toastMsg = jsonObj.getString("1");
					return 2;
				} else {
					toastMsg = "The OnQ server returned an unexpected error, please contact an OnQ administrator.";
					return -1;
				}
			}
			else if (obj instanceof JSONArray)
			{
				//JSONArray of deck objects present
				return 2;
			}
			toastMsg = "The OnQ server returned an unexpected error, please contact an OnQ administrator.";
			return -1;

		} catch (JSONException je) {
			je.printStackTrace();
			toastMsg = je.getMessage();
			return -1;
		}
	}
}
