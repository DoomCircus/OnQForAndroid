package com.example.onq;

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
	private final int toastTime = 5;
	
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
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		
		//Add offline mode option
		
		loginButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				
				ConnectionManager.responseReceived = false;
				ConnectionManager.waiting = true;
				
				InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
				
				final String username = usernameText.getText().toString();
				final String password = passwordText.getText().toString();
				
				Thread t = new Thread(new Runnable()
				{
				    //Thread to stop network calls on the UI thread
				    public void run()
				    {
				        try
				        {
				        	ConnectionManager.CallServer("LOGIN", username, password, null, null);
				        }
				        catch (Exception e)
				        {
				            e.printStackTrace();
				            Toast.makeText(Login.this, e.getMessage(), toastTime).show();
				        }
				    }
				});
				
				t.start();
				
				boolean loginSucceeded = false;
				
				while(ConnectionManager.waiting)
				{
					if(ConnectionManager.responseReceived)
					{
						/*if(t != null)
						{
						      t.interrupt();
						      t = null;
						}*/
						ConnectionManager.waiting = false;
						
						if (ConnectionManager.DecodeJSONResponse(ConnectionManager.serverResponse) == 2)
						{
							loginSucceeded = true;
							prefs.edit().putString("SecurityToken", ConnectionManager.toastMsg).commit();
							prefs.edit().putString("Username", usernameText.getText().toString()).commit();
							prefs.edit().putString("Password", passwordText.getText().toString()).commit();
							
							Intent intent = new Intent(Login.this, MainActivity.class);
							startActivity(intent);
							finish();
						}
						else if (ConnectionManager.DecodeJSONResponse(ConnectionManager.serverResponse) == 0)
						{
							loginSucceeded = false;
							break;
						}
						else
						{
							ConnectionManager.toastMsg = 
									"Unexpected issue encountered, please contact the OnQ development team.";
							break;
						}
					}
				}
				
				if (!loginSucceeded)
				{
					errorText.setText("[Login]Exception occured while attempting to login: "+
									ConnectionManager.toastMsg);
				}
			}
		});
	}
	
	@Override
	public void onBackPressed(){
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_HOME);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
		finish();
	}

}
