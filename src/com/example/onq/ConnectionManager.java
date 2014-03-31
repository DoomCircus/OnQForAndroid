package com.example.onq;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class ConnectionManager {
	
	//private final static String serverAddress = "192.168.0.29";
	private final static String serverAddress = "142.156.112.23";
	private final static int serverPort = 1337;
	private final static int timeout = 5000;
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
					Socket socket = new Socket();
					try {
					    SocketAddress socketAddress = new InetSocketAddress(InetAddress.getByName(serverAddress), serverPort);					    
				        socket.connect(socketAddress, timeout);
					} catch (IOException e) {
						e.printStackTrace();
						Log.e("ServerIsReachable", e.getMessage());
						toastMsg = "Server is unreachable: " + e.getMessage();
						serverReachable = false;
						waiting = false;
					} catch (Exception e) {
						e.printStackTrace();
						Log.e("ServerIsReachable", e.getMessage());
						toastMsg = "Server is unreachable: " + e.getMessage();
						serverReachable = false;
						waiting = false;
					} finally {
						// Always close the socket after we're done
				        if (socket.isConnected()) {
				        	toastMsg = "Server is reachable";
							serverReachable = true;
							waiting = false;
				            try {
				                socket.close();
				            }
				            catch (IOException e) {
				            	e.printStackTrace();
								Log.e("ServerIsReachable", e.getMessage());
								toastMsg = e.getMessage();
				            }
				        }
				        else
				        {
				        	toastMsg = "Server is unreachable";
							serverReachable = false;
							waiting = false;
				        }
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
			onqURL = "http://" + serverAddress + ":" + serverPort + "/onq/qmobile/login/"+username+"/"+password;
		}
		else if (action.equals("PULL")) {
			onqURL = "http://" + serverAddress + ":" + serverPort + "/onq/qmobile/pullDecks/" + username + "/" + password;
		}
		else if (action.equals("PUSH")) {
			onqURL = "http://" + serverAddress + ":" + serverPort + "/onq/qmobile/uploadDecks/" + username + "/" + password
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
		} catch (Exception ex) {
			ex.printStackTrace();
			Log.e("CallServer", ex.getMessage());
			toastMsg = ex.getMessage();
			waiting = false;
		}
	}
}
