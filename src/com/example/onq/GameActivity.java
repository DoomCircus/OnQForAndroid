package com.example.onq;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.Button;

public class GameActivity extends Activity{

	private Button hostButton;
	private Button joinButton;
	private Button singleButton;
	private Intent intent;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_game);
	
		
		
		hostButton = (Button) findViewById(R.id.HostButton);
		hostButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				//Intent intent = new Intent(GameActivity.this, HostActivity.class);
				startActivityForResult(intent,0);
			}
		});
		
		joinButton = (Button) findViewById(R.id.JoinButton);
		joinButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				//Intent intent = new Intent(GameActivity.this, JoinActivity.class);
				startActivityForResult(intent,0);
			}
		});
		
		singleButton = (Button) findViewById(R.id.SingleButton);
		singleButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				//Intent intent = new Intent(GameActivity.this, SingleActivity.class);
				//startActivityForResult(intent,0);
			}
		});


	}

}
