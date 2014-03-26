package com.example.onq;

import java.util.ArrayList;
import java.util.List;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Paint;

public class MainActivity extends Activity {

	private List<QCard> javaCardList = new ArrayList<QCard>();
	private List<QCard> cPlusCardList = new ArrayList<QCard>();
	private List<QCard> beerCardList = new ArrayList<QCard>();
	private List<QCardSet> qCardSetList = new ArrayList<QCardSet>();
	private ArrayList<QCard> cardList = new ArrayList<QCard>();
	private Button studyButton;
	private Button bumpButton;
	private Button exitButton;
	private TextView errorText;
	private Paint paint;
	public static Bitmap tmpB;
	public static Activity tmpActivity;
	private String theSetName;
	private DeckManager deckManager;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_main);
		tmpActivity = MainActivity.this;
		paint = new Paint();
        paint.setColor(Color.GREEN);
        tmpB = BitmapFactory.decodeResource(getResources(),R.drawable.card);
        errorText = (TextView) findViewById(R.id.errorText);
        try {
        	deckManager = new DeckManager();
        }
        catch (NullPointerException npe) {
        	npe.printStackTrace();
        	errorText.setText(npe.getMessage() + "\nPlease reinstall the OnQ app and try logging in again.");
        }
        
        /*setTheSetName("BeerQuestions");
        populateBeerCards();
        populateCPlusCards();
		populateJavaCards();*/
		
		studyButton = (Button) findViewById(R.id.StudyButton);
	
		studyButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				tmpActivity = MainActivity.this;
				Intent intent = new Intent(MainActivity.this, FlipActivity.class);
				startActivityForResult(intent,0);
			}
		});
		
		
		bumpButton = (Button) findViewById(R.id.BumpButton);
				
		bumpButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				//**********************************************
				//->Set cardList to current QCardSet
				//->Add check to Bump to see if received deck already exists in the users decks
				//**********************************************
				//if (userAllowsNetworkUser)
				deckManager.PullDecksFromServer();
				deckManager.SaveDecksToPrefs();
				//else
				//deckManager.LoadDecksFromPrefs();
				Toast.makeText(MainActivity.this, deckManager.getToastMessage(), Toast.LENGTH_LONG).show();
				
				Intent intent = new Intent(MainActivity.this, BumpDeck.class);
				intent = intent.putExtra("Cards", cardList);
				startActivityForResult(intent,0);
			}
		});
		
		//**********************************************
		//----->>>>>>> NEW BUTTON: SAVE DECKS
		//->if (userAllowsNetworkUser)
		//->deckManager.LoadDecksFromPrefs();
		//->deckManager.PushDecksToServer();
		//->else
		//->deckManager.SaveDecksToPrefs();
		//->
		//->Toast.makeText(MainActivity.this, deckManager.getToastMessage(), Toast.LENGTH_LONG).show();
		//**********************************************
		
		exitButton = (Button) findViewById(R.id.ExitButton);
		
		exitButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				deckManager.LoadDecksFromPrefs();
				deckManager.PushDecksToServer();
				//Add optional offline save
				Intent intent = new Intent(Intent.ACTION_MAIN);
				intent.addCategory(Intent.CATEGORY_HOME);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intent);
			}
		});
	}
	
	private void populateBeerCards(){
		QCard tmp = new QCard();
		
		Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.beer);
		Bitmap currentThumb = ThumbnailUtils.extractThumbnail(bm, 50, 50);
		
		tmp.setCardID(1);
		tmp.setQuestion("The ancient Babylonians were the first to brew beer. If you brewed a bad batch back then what was the punishment?");
		tmp.setAnswer("If you brewed a bad batch you would be drowned in it.");
		tmp.setqPic(currentThumb);
		tmp.setSetName("BeerQuestions");
		beerCardList.add(tmp);
		
		tmp = new QCard();
		tmp.setCardID(2);
		tmp.setQuestion("Why should you store your beer upright?");
		tmp.setAnswer("Upright storage minimizes oxidation and contamination from the cap.");
		tmp.setqPic(currentThumb);
		tmp.setSetName("BeerQuestions");
		beerCardList.add(tmp);
		
		tmp = new QCard();
		tmp.setCardID(3);
		tmp.setQuestion("What did the Vikings believe would provide them with an endless supply of beer when they reached Valhalla?");
		tmp.setAnswer("A giant goat whose udders provided the beer.");
		tmp.setqPic(currentThumb);
		tmp.setSetName("BeerQuestions");
		beerCardList.add(tmp);
		
		tmp = new QCard();
		tmp.setCardID(4);
		tmp.setQuestion("When prohibition ended in the US what was the first thing President Roosevelt said?");
		tmp.setAnswer("What America needs now is a drink-Roosevelt.");
		tmp.setqPic(currentThumb);
		tmp.setSetName("BeerQuestions");
		beerCardList.add(tmp);
		
		tmp = new QCard();
		tmp.setCardID(5);
		tmp.setQuestion("What is the most expensive kind of beer sold in the world today?");
		tmp.setAnswer("The most expensive beer sold in the world today is Vielle Bon Secours selling at approximately 1000 dollars per bottle.");
		tmp.setqPic(currentThumb);
		tmp.setSetName("BeerQuestions");
		beerCardList.add(tmp);
		
		QCardSet tmpSet = new QCardSet();
		tmpSet.setDeckID(1);
		tmpSet.setDeckType("BeerQuestions");
		tmpSet.setCardListName("BeerQuestions");
		tmpSet.setDescription("BeerQuestions");
		tmpSet.setPrivatePublic(1);
		tmpSet.setqCardsList(beerCardList);
		
		qCardSetList.add(tmpSet);
		
	}
	
	private void populateCPlusCards(){
		QCard tmp = new QCard();
		
		tmp.setCardID(6);
		tmp.setQuestion("Give an example for a variable CONST and VOLATILE. Is it possible?");
		tmp.setAnswer("Yes, a status register for a microcontroller.");
		tmp.setSetName("C++Questions");
		cPlusCardList.add(tmp);
		
		tmp = new QCard();
		tmp.setCardID(7);
		tmp.setQuestion("How do you detect if a linked list is circular?");
		tmp.setAnswer("You need to use 2 pointers, one incrementing by 1 and another by 2. If the list is circular, then pointer that is incremented by 2 elements will pass over the first pointer.");
		tmp.setSetName("C++Questions");
		cPlusCardList.add(tmp);
		
		tmp = new QCard();
		tmp.setCardID(8);
		tmp.setQuestion("Define a dangling pointer?");
		tmp.setAnswer("Dangling pointer is obtained by using the address of an object which was freed.");
		tmp.setSetName("C++Questions");
		cPlusCardList.add(tmp);
		
		tmp = new QCard();
		tmp.setCardID(9);
		tmp.setQuestion("Define Encapsulation?");
		tmp.setAnswer("Part of the information can be hidden about an object. Encapsulation isolates the internal functionality from the rest of the application.");
		tmp.setSetName("C++Questions");
		cPlusCardList.add(tmp);
		
		tmp = new QCard();
		tmp.setCardID(10);
		tmp.setQuestion("Define Inheritance?");
		tmp.setAnswer("One class, called derived, can reuse the behavior of another class, known as base class. Methods of the base class can be extended by adding new proprieties or methods.");
		tmp.setSetName("C++Questions");
		cPlusCardList.add(tmp);
		
		QCardSet tmpSet = new QCardSet();
		tmpSet.setDeckID(2);
		tmpSet.setDeckType("C++Questions");
		tmpSet.setCardListName("C++Questions");
		tmpSet.setDescription("C++Questions");
		tmpSet.setPrivatePublic(1);
		tmpSet.setqCardsList(cPlusCardList);
		
		qCardSetList.add(tmpSet);
	}
	
	private void populateJavaCards(){
		
		QCard tmp = new QCard();
		
		tmp.setCardID(11);
		tmp.setQuestion("How do you deal with dependency issues?");
		tmp.setAnswer("This question is purposely ambiguous. It can refer to solving the dependency injection problem (Guice is a standard tool to help). It can also refer to project dependencies — using external, third-party libraries. Tools like Maven and Gradle help manage them. You should consider learning more about Maven as a way to prepare for this question.");
		tmp.setSetName("JavaQuestions");
		javaCardList.add(tmp);
		
		tmp = new QCard();
		tmp.setCardID(12);
		tmp.setQuestion("When and why are getters and setters important?");
		tmp.setAnswer("Setters and getters can be put in interfaces and can hide implementation details, so that you do not have to make member variables public (which makes your class dangerously brittle).");
		tmp.setSetName("JavaQuestions");
		javaCardList.add(tmp);
		
		tmp = new QCard();
		tmp.setCardID(13);
		tmp.setQuestion("How would you go about deciding between SOAP based web service and RESTful web service?");
		tmp.setAnswer("Web services are very popular and widely used to integrate disparate systems. It is imperative to understand the differences, pros, and cons between each approach. ");
		tmp.setSetName("JavaQuestions");
		javaCardList.add(tmp);
		
		tmp = new QCard();
		tmp.setCardID(14);
		tmp.setQuestion("How to compare strings? Use “==” or use equals()?");
		tmp.setAnswer("In brief, “==” tests if references are equal and equals() tests if values are equal. Unless you want to check if two strings are the same object, you should always use equals().");
		tmp.setSetName("JavaQuestions");
		javaCardList.add(tmp);
		
		tmp = new QCard();
		tmp.setCardID(15);
		tmp.setQuestion("What is the purpose of default constructor?");
		tmp.setAnswer("The default constructor provides the default values to the objects. The java compiler creates a default constructor only if there is no constructor in the class.");
		tmp.setSetName("JavaQuestions");
		javaCardList.add(tmp);
		
		QCardSet tmpSet = new QCardSet();
		tmpSet.setDeckID(3);
		tmpSet.setDeckType("JavaQuestions");
		tmpSet.setCardListName("JavaQuestions");
		tmpSet.setDescription("JavaQuestions");
		tmpSet.setPrivatePublic(1);
		tmpSet.setqCardsList(javaCardList);
		
		qCardSetList.add(tmpSet);
    	
    }
	public List<QCardSet> getqCardSetList() {
		return qCardSetList;
	}
	public void setqCardSetList(List<QCardSet> qCardSetList) {
		this.qCardSetList = qCardSetList;
	}
	public String getTheSetName() {
		return theSetName;
	}
	public void setTheSetName(String theSetName) {
		this.theSetName = theSetName;
	}
	
	@Override
	public void onBackPressed(){
		deckManager.LoadDecksFromPrefs();
		deckManager.PushDecksToServer();
		//Add optional offline save
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_HOME);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
	}

}
