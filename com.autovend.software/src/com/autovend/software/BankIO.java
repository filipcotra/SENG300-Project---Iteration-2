package com.autovend.software;

import java.math.BigDecimal;

import com.autovend.Card;

public interface BankIO {
	public int creditCardTranscation(Card card, BigDecimal amountPaid);
	
	public int debitCardTranscation(Card card, BigDecimal amountPaid);
	
	public void completeTransaction(int holdNumber);
	
	public void blockCard(Card card);
}
