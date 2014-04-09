package com.example.onq;

import java.util.ArrayList;
import java.util.List;

import com.example.onq.AnimationFactory.FlipDirection;

import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewAnimator;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;

public class FlipActivity extends Activity implements SensorEventListener, GestureDetector.OnGestureListener{
	
	int state_machine = 0;
	private long lastUpdate;
	private SensorManager sensorManager;
	ViewAnimator viewAnimator;
	boolean isQuestion = true;
	ArrayList<QCardSet>tmpSet = new ArrayList<QCardSet>();
	private MainActivity m;
	
	private static final int SWIPE_MIN_DISTANCE = 120;
	private static final int SWIPE_MAX_OFF_PATH = 250;
	private static final int SWIPE_THRESHOLD_VELOCITY = 200;
	private GestureDetector _gestureDetector;
	
	private int cardSetId = 0;
	private int questionCounter = 0;
	private int answerCounter = 0;
	
	private ImageView first;
	
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_flip);
				
		sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
		lastUpdate = System.currentTimeMillis();
		m =(MainActivity) MainActivity.tmpActivity; 
		viewAnimator = (ViewAnimator) this.findViewById(R.id.viewFlipper);
		TextView textview1 = (TextView)findViewById(R.id.txt1);
		TextView textview2 = (TextView)findViewById(R.id.txt2);
		textview1.setTextColor(Color.BLACK);
		textview2.setTextColor(Color.BLUE);
		first = (ImageView) findViewById(R.id.frontView);
		int i = 0;
		while( i < m.getqCardSetList().size())
		{
			if(m.getqCardSetList().get(i).getCardListName() == m.getTheSetName()){
				cardSetId = i;
				break;
			}
			i++;
		}
		List<QCardSet> qCardSetList = m.getqCardSetList();
		QCardSet qCardSet = qCardSetList.get(cardSetId);
		List<QCard> qCards = qCardSet.getqCardsList();
		QCard qCard = qCards.get(questionCounter);
		textview1.setText("Q:" + qCard.getQuestion());
		textview2.setText("A:" + qCard.getAnswer());
		Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.beer);
		Bitmap currentThumb = ThumbnailUtils.extractThumbnail(bm, 100, 100);
		first.setImageBitmap(currentThumb);
		first.bringToFront();
		first.invalidate();
		
		_gestureDetector = new GestureDetector(this);
		
		viewAnimator.setOnTouchListener(new OnTouchListener(){

			@Override
			public boolean onTouch(View arg0, MotionEvent arg1) {
				//AnimationFactory.flipTransition(viewAnimator, FlipDirection.LEFT_RIGHT);
				return false;
			}
		});
		
		this.findViewById(R.id.txt1).setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				if(isQuestion == true)
				{
					AnimationFactory.flipTransition(viewAnimator, FlipDirection.LEFT_RIGHT);
					isQuestion = false;
				}
				else
				{
					AnimationFactory.flipTransition(viewAnimator, FlipDirection.RIGHT_LEFT);
					isQuestion = true;
				}
			}
		});
		
		
		this.findViewById(R.id.txt2).setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				if(isQuestion == true)
				{
					AnimationFactory.flipTransition(viewAnimator, FlipDirection.LEFT_RIGHT);
					isQuestion = false;
				}
				else
				{
					AnimationFactory.flipTransition(viewAnimator, FlipDirection.RIGHT_LEFT);
					isQuestion = true;
				}
			}
		});
		
		
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		
		_gestureDetector.onTouchEvent(event);
		return super.onTouchEvent(event);
		
	}
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		sensorManager.unregisterListener(this);
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),SensorManager.SENSOR_DELAY_NORMAL);	
	}

	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void onSensorChanged(SensorEvent event) {
		// TODO Auto-generated method stub
		
		if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
		{
			getAccelerometer(event);
		}

	}
	
	private void getAccelerometer(SensorEvent event){
		float[] values = event.values;
		
		float x = values[0];
		float y = values[1];
		float z = values[2];
		
		float accelationSquareRoot = (x * x + y * y + z * z) / (SensorManager.GRAVITY_EARTH * SensorManager.GRAVITY_EARTH);
		long actualTime = System.currentTimeMillis();
		if(accelationSquareRoot >= 2)
		{
			if(actualTime - lastUpdate < 200)
			{
				return;
			}
			lastUpdate = actualTime;
			if(isQuestion == true)
			{
				AnimationFactory.flipTransition(viewAnimator, FlipDirection.LEFT_RIGHT);
				isQuestion = false;
			}
			else
			{
				AnimationFactory.flipTransition(viewAnimator, FlipDirection.RIGHT_LEFT);
				isQuestion = true;
			}
			
		}	
	}

	@Override
	public boolean onDown(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		// TODO Auto-generated method stub
		
        try {
            if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
                return false;
            // right to left swipe
            if(e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                
				if(questionCounter < m.getqCardSetList().get(cardSetId).getqCardsList().size()-1){
	                questionCounter++;
	                answerCounter++;
	                
	    			if(isQuestion == false)
	    			{
	    				AnimationFactory.flipTransition(viewAnimator, FlipDirection.RIGHT_LEFT);
	    				isQuestion = true;
	    			}
	
	                Animation animationOutToLeft = AnimationUtils.loadAnimation(this, R.anim.out_left);
	                AnimationSet s = new AnimationSet(false);
	                s.addAnimation(animationOutToLeft); 
	                viewAnimator.startAnimation(s);
	                s.setAnimationListener(new Animation.AnimationListener() {
	                    @Override
	                    public void onAnimationStart(Animation animation) {

	                    }

	                    @Override
	                    public void onAnimationEnd(Animation animation) {
	                    	TextView textview1 = (TextView)findViewById(R.id.txt1);
	                		TextView textview2 = (TextView)findViewById(R.id.txt2);   
	                		textview1.setText("Q:" + m.getqCardSetList().get(cardSetId).getqCardsList().get(questionCounter).getQuestion());
	                		textview2.setText("A:" + m.getqCardSetList().get(cardSetId).getqCardsList().get(answerCounter).getAnswer());
	                		Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.beer);
	                		Bitmap currentThumb = ThumbnailUtils.extractThumbnail(bm, 100, 100);
	                	
	                		first.setImageBitmap(currentThumb);
	                		first.bringToFront();
	                		first.invalidate();
	                    }

	                    @Override
	                    public void onAnimationRepeat(Animation animation) {

	                    }
	                });
				}
				else{
					Toast.makeText(FlipActivity.this, "End Of Set", Toast.LENGTH_SHORT).show();
				}
                
            }  else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                
                if(questionCounter > 0){
	                questionCounter--;
	                answerCounter--;
	                
	    			if(isQuestion == false)
	    			{
	    				AnimationFactory.flipTransition(viewAnimator, FlipDirection.LEFT_RIGHT);
	    				isQuestion = true;
	    			}
	
	                Animation animationOutToRight = AnimationUtils.loadAnimation(this, R.anim.out_right);
	                AnimationSet s = new AnimationSet(false);
	                s.addAnimation(animationOutToRight); 
	                viewAnimator.startAnimation(s);
	                s.setAnimationListener(new Animation.AnimationListener() {
	                    @Override
	                    public void onAnimationStart(Animation animation) {

	                    }

	                    @Override
	                    public void onAnimationEnd(Animation animation) {
	                    	TextView textview1 = (TextView)findViewById(R.id.txt1);
	                		TextView textview2 = (TextView)findViewById(R.id.txt2);   
	                		textview1.setText("");
	                		textview2.setText("");
	                		textview1.setText("Q:" + m.getqCardSetList().get(cardSetId).getqCardsList().get(questionCounter).getQuestion());
	                		textview2.setText("A:" + m.getqCardSetList().get(cardSetId).getqCardsList().get(answerCounter).getAnswer());
	                    }

	                    @Override
	                    public void onAnimationRepeat(Animation animation) {

	                    }
	                });
                }
                else{
                	Toast.makeText(FlipActivity.this, "Beginning Of Set", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception e) {
            // nothing
        }
        
        return false;		
	}

	
	
	
	@Override
	public void onLongPress(MotionEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onShowPress(MotionEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}




}
