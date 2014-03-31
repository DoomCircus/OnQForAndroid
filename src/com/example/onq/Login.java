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
	
	/*protected volatile boolean tokenReceived;
	protected volatile boolean waiting;
	protected volatile String error;*/
	
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
						
						String tmp = ConnectionManager.serverResponse.substring(1,
								ConnectionManager.serverResponse.length()-1);
						String[]resPieces = tmp.split(",");
						
						for (int i = 0; i < resPieces.length; ++i)
						{
							resPieces[i] = resPieces[i].substring(1, resPieces[i].length()-1);
						}
						
						if (resPieces[0].equals("SecurityToken"))
						{
							prefs.edit().putString("SecurityToken", resPieces[1]).commit();
							prefs.edit().putString("Username", username).commit();
							prefs.edit().putString("Password", password).commit();
							ConnectionManager.responseReceived = true;
						}
						else if (resPieces[0].equals("Error"))
						{
							ConnectionManager.toastMsg = resPieces[1];
							break;
						}
						else
						{
							ConnectionManager.toastMsg = 
									"Unexpected issue encountered, please contact the OnQ development team.";
							break;
						}
						
						Intent intent = new Intent(Login.this, MainActivity.class);
						startActivity(intent);
					}
				}
				
				errorText.setText("[Login]Exception occured while attempting to login: "+
									ConnectionManager.toastMsg);
			}
		});
	}
	
	@Override
	public void onBackPressed(){
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_HOME);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
	}

}
