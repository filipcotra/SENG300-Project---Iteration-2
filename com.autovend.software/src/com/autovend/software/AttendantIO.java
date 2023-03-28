/*
  * Brian Tran (30064686)
  * Filip Cotra (30086750)
  * Arian Safari (30161346)
  * Justin Clibbett (30128271)
  * Umar Ahmed (30145076)
  * Farbod Moghaddam (30115199)
  * Abdul Alkareem Biderkab (30156693)
  * Naheen Kabir (30142101)
  * Khalen Drissi (30133707)
  * Darren Roszell (30163669)
  * Justin Yee (30113485)
  * Christian Salvador (30089672)
  */

package com.autovend.software;

import java.math.BigDecimal;

/**
 * This interface can be used in testing to simulate attendant interactions in certain use cases.
 *
 */
public interface AttendantIO {
	
	/**
	 * Simulates the customer receiving a signal from the system about the weight of the scale
	 * not matching the expected weight when an item was placed
	 */
	public void notifyWeightDiscrepancyAttendantIO();
	
	/**
	 * Simulates an attendant approving a weight discrepancy
	 * This interaction is apart of the Weight Discrepancy use case
	 */
	public void approveWeightDiscrepancy(CustomerIO customerIO);
	
	/**
	 * Simulates an attendant being informed of a change discrepancy.
	 * Returns nothing.
	 * 
	 * @param bigDecimal
	 * 				Amount of change left that must be given to customer
	 */
	public void changeRemainsNoDenom(BigDecimal changeLeft);
	
	/**
	 * Simulates informing the attendant that a duplicate receipt 
	 * must be printed because the printer is out of ink or paper.
	 * Printing is aborted, the station will be suspended and the
	 * attendant will also be informed that the station needs maintenance.
	 */
	public void printDuplicateReceipt();
  

	
	/**
	 * Simulates the attendant checking the bags the customer wants to add
	 */
	public void checkAddedOwnBags();
	
	/**
	 * Simulates the attendant accepting or rejecting the customer's own bags
	 */
	public void acceptOwnBags();


	/*
	 * Simulates informing the attendant that the printer is low on ink
	 * This occurs after printing is completed.
	 * The system is then suspended and the attendant will refill the ink
	 */
	public void acknowledgeLowInk();

	/*
	 * Simulates informing the attendant that the printer is low on paper
	 * This occurs after printing is completed.
	 * The system is then suspended and the attendant will refill the paper
	 */
	public void acknowledgeLowPaper();

}
