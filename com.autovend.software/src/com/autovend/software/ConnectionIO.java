package com.autovend.software;

import com.autovend.external.CardIssuer;

public interface ConnectionIO {

	/** Simulates a connection to a bank */
	public boolean connectTo(CardIssuer bank);
	
}
