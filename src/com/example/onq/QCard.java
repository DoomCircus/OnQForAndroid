package com.example.onq;

import java.io.Serializable;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

@SuppressWarnings("serial")
public class QCard implements Parcelable,Serializable {

	private int cardID;
	private String setName;
	private String question;
	private String answer;
	private Bitmap qPic;
	
	//Constructor
	public QCard() {
		super();
	}
	
	private QCard(Parcel pc)
	{
		this.question = pc.readString();
		this.answer = pc.readString();
	
	}
	
	//Getters and Setters
	public int getCardID() {
		return cardID;
	}
	public void setCardID(int cardID) {
		this.cardID = cardID;
	}
	public String getSetName() {
		return setName;
	}
	public void setSetName(String setName) {
		this.setName = setName;
	}
	public String getQuestion() {
		return question;
	}
	public void setQuestion(String question) {
		this.question = question;
	}
	public String getAnswer() {
		return answer;
	}
	public void setAnswer(String answer) {
		this.answer = answer;
	}
	public Bitmap getqPic() {
		return qPic;
	}
	public void setqPic(Bitmap qPic) {
		this.qPic = qPic;
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		// TODO Auto-generated method stub
		dest.writeString(getQuestion());
		dest.writeString(getAnswer());
	
		
	}

	public static final Parcelable.Creator<QCard> CREATOR = 
			new Parcelable.Creator<QCard>() {

				@Override
				public QCard createFromParcel(Parcel source) {
					// TODO Auto-generated method stub
					return new QCard(source);
				}

				@Override
				public QCard[] newArray(int size) {
					// TODO Auto-generated method stub
					return new QCard[size];
				}
		
		
		
			};
}
