package com.example.onq;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

public class CheckUser extends Activity {
	
	protected SharedPreferences prefs;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Intent intent;
		if (TokenPresent())
		{
			intent = new Intent(CheckUser.this, MainActivity.class);
		}
		else
		{
			intent = new Intent(CheckUser.this, Login.class);
		}
		startActivity(intent);
	}
	
	protected boolean TokenPresent()
	{
		boolean tokenFound = true;
		
		try
		{
			prefs = PreferenceManager.getDefaultSharedPreferences(this);
			
			if(prefs.equals(null))
			{
				tokenFound = false;
			}
			if (prefs.getString("SecurityToken", "").equals(""))
			{
				tokenFound = false;
			}
		}
		catch (ClassCastException cce)
		{
			cce.printStackTrace();
			tokenFound = false;
		}
		
		return tokenFound;
	}
}
