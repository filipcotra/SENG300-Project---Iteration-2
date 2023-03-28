package com.autovend.software;

import java.math.BigDecimal;

import com.autovend.Card;
import com.autovend.Card.CardData;

public interface BankIO {
	//returns the hold number. If 0 is returned then the transaction has failed
	public int creditCardTransaction(CardData card, BigDecimal amountPaid);
	
	//returns the hold number. If 0 is returned then the transaction has failed
	public int debitCardTransaction(CardData card, BigDecimal amountPaid);
	
	public void completeTransaction(int holdNumber);
	
	public void blockCard(Card card);

	public void releaseHold(CardData data);

	public boolean connectionStatus();

}
