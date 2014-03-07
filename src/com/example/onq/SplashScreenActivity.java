package com.example.onq;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Window;
import android.view.WindowManager;

public class SplashScreenActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_splash_screen);
		Thread timer = new Thread(){
			public void run(){
				try{
					sleep(5000);
				}catch(InterruptedException e){
					e.printStackTrace();
				}finally{
					Intent intent = new Intent(SplashScreenActivity.this, CheckUser.class);
					startActivity(intent);
				}
			}
		};
		timer.start();
	}
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		finish();
	}


}
