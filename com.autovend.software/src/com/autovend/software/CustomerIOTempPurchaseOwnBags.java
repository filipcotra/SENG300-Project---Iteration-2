
package com.autovend.software;

import java.math.BigDecimal;

import com.autovend.BarcodedUnit;
import com.autovend.devices.BillSlot;

/**
 * TEMPORARY LISTENER THAT SHOULD BE MERGED INTO CUSTOMERIO
 *
 */
public interface CustomerIOTempPurchaseOwnBags extends CustomerIO{
		
	/**
	 * Simulates a customer scanning an item.
	 * This interaction is on Step 1 of add item by scanning.
	 */
	public void scanItem(BarcodedUnit item);

	/**
	 * Simulates the customer receiving a signal from the system to place the scanned item into
	 * the bagging area. This is step 5 in "Add Item By Scanning".
	 */
	public void notifyPlaceItemCustomerIO();
	
	/**
	 * Simulates a customer placing their scanned item in the bagging area.
	 * This interaction is on Step 5 of add item by scanning.
	 */
	public void placeScannedItemInBaggingArea(BarcodedUnit item);

	/**
	 * Simulates a customer being informed of the updated total due for their
	 * cart based on how much they have paid. This is step 4 in "Pay with Cash."
	 * Returns nothing.
	 * 
	 * @param bigDecimal
	 * 					The total remaining to pay from the customers cart
	 */
	public void showUpdatedTotal(BigDecimal total);
	
	/**
	 * Simulates steps 5 and 6 of the Print Receipt use case by thanking the
	 * customer, ending the current session and getting ready for the next one. 
	 */
	public void thankCustomer();
	
	/**
	 * Simulates the customer removing the bill from the slot
	 */
	public void removeBill(BillSlot slot);

	/**
	 * A signal from the Customer IO to the station, notifying the station that the Customer wants
	 * to purchase re-usable bags.
	 * 
	 * @param quantity
	 * 		The number of bags the customer wants to purchase.
	 */
	public void signalPurchaseBags(int quantity);
	

	/**
	 * A signal from the station to Customer IO, notifying the Customer that the operation was successful.
	 */
	public void signalFinishedPurchasingBags();
	
	/**
	 * A signal from the station to Customer IO, notifying that the station is ready for interaction.
	 */
	public void signalReadyForInteraction();
	
}
