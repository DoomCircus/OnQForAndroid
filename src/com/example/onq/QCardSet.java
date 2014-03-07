package com.example.onq;

import java.util.ArrayList;
import java.util.List;

public class QCardSet {

	private String cardListName;
	private List<QCard> qCardsList = new ArrayList<QCard>();
	//Constructor
	public QCardSet() {
		super();
	}
	public String getCardListName() {
		return cardListName;
	}
	public void setCardListName(String cardListName) {
		this.cardListName = cardListName;
	}
	public List<QCard> getqCardsList() {
		return qCardsList;
	}
	public void setqCardsList(List<QCard> qCardsList) {
		this.qCardsList = qCardsList;
	}
}
