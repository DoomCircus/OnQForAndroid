package com.example.onq;

import java.io.ByteArrayOutputStream;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;

public class Login extends Activity {
	
	protected SharedPreferences prefs;

	protected EditText usernameText;
	protected EditText passwordText;
	protected Button loginButton;
	protected volatile TextView errorText;
	protected Intent intent;
	
	protected volatile boolean tokenReceived;
	protected volatile boolean waiting;
	protected volatile String error;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_login);
		usernameText = (EditText)findViewById(R.id.username);
		passwordText = (EditText)findViewById(R.id.password);
		loginButton = (Button)findViewById(R.id.LoginButton);
		errorText = (TextView)findViewById(R.id.errorText);
		
		//Add offline mode option
		
		loginButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				
				tokenReceived = false;
				waiting = true;
				
				InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
				
				final String username = usernameText.getText().toString();
				final String password = passwordText.getText().toString();
				
				new Thread(new Runnable()
				{
				    //Thread to stop network calls on the UI thread
				    public void run()
				    {
				        try
				        {
				        	ValidateUser(username, password);
				        }
				        catch (Exception e)
				        {
				            e.printStackTrace();
				            error = "[Login]" + e.getMessage();
				        }
				    }
				}).start();
				
				while(waiting)
				{
					if(tokenReceived)
					{
						Intent intent = new Intent(Login.this, MainActivity.class);
						startActivity(intent);
						waiting = false;
					}
				}
				
				errorText.setText(error);
			}
		});
	}
	
	protected void ValidateUser(String username, String password)
	{
		String onqURL = "http://192.168.0.15:1337/onq/qmobile/login/"+username+"/"+password;
		//String onqURL = "http://142.156.75.146:1337/onq/qmobile/login/"+username+"/"+password;
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
				String tmp = serverString.substring(1, serverString.length()-1);
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
				}
			}
		}
		catch(Exception ex){
			ex.printStackTrace();
			error = "[Login]Exception occured while attempting to login: "+ex.getMessage();
			waiting = false;
		}
	}
	
	@Override
	public void onBackPressed(){
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_HOME);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
	}

}
