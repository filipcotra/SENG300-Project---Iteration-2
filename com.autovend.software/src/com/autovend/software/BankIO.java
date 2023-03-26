package com.autovend.software;

import java.math.BigDecimal;

import com.autovend.Card;

public interface BankIO {
	//returns the hold number. If 0 is returned then the transaction has failed
	public int creditCardTranscation(Card card, BigDecimal amountPaid);
	
	//returns the hold number. If 0 is returned then the transaction has failed
	public int debitCardTranscation(Card card, BigDecimal amountPaid);
	
	public void completeTransaction(int holdNumber);
	
	public void blockCard(Card card);
}
