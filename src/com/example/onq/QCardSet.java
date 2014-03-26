package com.example.onq;

import java.util.ArrayList;
import java.util.List;

public class QCardSet {
	
	private int deckID;
	private String deckType;
	private String cardListName;
	private String description;
	private int privatePublic;
	private List<QCard> qCardsList = new ArrayList<QCard>();
	//Constructor
	public QCardSet() {
		super();
	}
	public int getDeckID() {
		return deckID;
	}
	public void setDeckID(int deckID) {
		this.deckID = deckID;
	}
	public String getDeckType() {
		return deckType;
	}
	public void setDeckType(String deckType) {
		this.deckType = deckType;
	}
	public String getCardListName() {
		return cardListName;
	}
	public void setCardListName(String cardListName) {
		this.cardListName = cardListName;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public int getPrivatePublic() {
		return privatePublic;
	}
	public void setPrivatePublic(int privatePublic) {
		this.privatePublic = privatePublic;
	}
	public List<QCard> getqCardsList() {
		return qCardsList;
	}
	public void setqCardsList(List<QCard> qCardsList) {
		this.qCardsList = qCardsList;
	}
}
