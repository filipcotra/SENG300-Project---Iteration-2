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

import com.autovend.BarcodedUnit;
import com.autovend.Card;
import com.autovend.devices.BillSlot;
import com.autovend.devices.CoinTray;

/**
 * This interface can be used in testing to simulate customer interactions in certain use cases.
 *
 */
public interface CustomerIO {
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
	 * Simulates the customer receiving a signal from the system about the weight of the scale
	 * not matching the expected weight when an item was placed
	 */
	public void notifyWeightDiscrepancyCustomerIO();
	
	/*
	 * Simulates the customer being notified the weight discrepancy issue has been resolved
	 * by the attendant and they can proceed
	 */
	public void notifyWeightDiscrepancyApprovedCustomerIO();
	
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


	/*
	 * Simulates the customer removing the coin from the tray
	 */
	public void removeCoin(CoinTray tray);
	
	
	/*
	 * Signals to Customer I/O that the operation is complete and the remaining 
	 * amount due is reduced.
	 */
	public void payWithCreditComplete(BigDecimal amountDue);
	
	/*
	 * Signals to Customer I/O that the operation is complete and the remaining 
	 * amount due is reduced.
	 */
	public void payWithDebitComplete(BigDecimal amountDue);
	
	/*
	 * Simualates the customer selecting a payment method, to enable required devices
	 */
	public void selectPaymentMethod(String paymentMethod, PaymentControllerLogic instance);
	
	/*
	 *signals that the transaction failed 
	 */
	public void transactionFailure();

	/* 
	 * Simulates the customer entering the amount they wish to pay by card
	 */
	public void setCardPaymentAmount(BigDecimal amount);

	/*
	 * Simulates the customer inserting a card.
	 */
	public void insertCard(Card card, String pin);

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
	
	/**
	 * A signal from the station to Customer IO, notifying the customer to put the purchased bags on the bagging area.
	 */
	public void signalPutPurchasedBagsOnBaggingArea();
	
	/**
	 * Simulates the customer wanting to add their membership number to the transaction
	 */
	public String getMembershipNumber();
	
	/**
	 * Simulates the customer wishing to cancel inputting their membership number
	 * @return
	 */
	public boolean cancelMembershipInput();
	
	/**
	 * Signals to the customer that the membership number entered was invalid
	 */
	public void notifyBadMembershipNumberCustomerIO();
	
	/**
	 * Simulates the customer selecting to use their own bags
	 */
	public boolean selectAddOwnBags();
	
	/**
	 * Simulates the customer being indicated/notified to contine after an attendant either approved or denied them from adding bags to bagging area
	 */
	public void indicateToCustomerToContinueAfterAttendantApproveOrDenyAddedBags();

}
