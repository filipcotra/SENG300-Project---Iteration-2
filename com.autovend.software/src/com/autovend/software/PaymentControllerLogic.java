/** 
 * Filip Cotra - 30086750
 *  Khondaker Samin Rashid - 30143490
 *  Nishan Soni - 30147289
 *  Aaron Tigley - 30159927
 *  Zainab Bari - 30154224
 */

package com.autovend.software;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Currency;
import java.util.List;
import java.util.Map;
import java.util.Arrays;
import java.util.Collections;

import com.autovend.Bill;
import com.autovend.BlockedCardException;
import com.autovend.Card;
import com.autovend.Coin;
import com.autovend.CreditCard;
import com.autovend.DebitCard;
import com.autovend.InvalidPINException;
import com.autovend.Card.CardData;
import com.autovend.ChipFailureException;
import com.autovend.devices.AbstractDevice;
import com.autovend.devices.BillDispenser;
import com.autovend.devices.BillSlot;
import com.autovend.devices.BillValidator;
import com.autovend.devices.CardReader;
import com.autovend.devices.CoinDispenser;
import com.autovend.devices.CoinTray;
import com.autovend.devices.CoinValidator;
import com.autovend.devices.DisabledException;
import com.autovend.devices.EmptyException;
import com.autovend.devices.ReceiptPrinter;
import com.autovend.devices.observers.AbstractDeviceObserver;
import com.autovend.devices.observers.BillValidatorObserver;
import com.autovend.devices.observers.CoinDispenserObserver;
import com.autovend.devices.observers.CoinTrayObserver;
import com.autovend.devices.observers.CoinValidatorObserver;
import com.autovend.devices.observers.BillDispenserObserver;
import com.autovend.devices.observers.BillSlotObserver;
import com.autovend.devices.observers.BillStorageObserver;
import com.autovend.devices.observers.CardReaderObserver;
import com.autovend.devices.SelfCheckoutStation;
import com.autovend.devices.SimulationException;

/**
 * Control software for payment use-cases in self checkout station.
 * 
 * @author Filip Cotra
 */
public class PaymentControllerLogic implements BillValidatorObserver, BillDispenserObserver, BillSlotObserver, 
CoinValidatorObserver, CoinTrayObserver, CoinDispenserObserver, CardReaderObserver {
	private BigDecimal cartTotal;
	private BigDecimal changeDue;
	private SelfCheckoutStation station;
	private int[] denominations;
	private Map<Integer, BillDispenser> dispensers;
	private BigDecimal maxDenom;
	private BigDecimal minDenom;
	private ReceiptPrinter printer;
	private CustomerIO myCustomer;
	private AttendantIO myAttendant;
	private PrintReceipt printerLogic;
	private ArrayList<String> itemNameList = new ArrayList<String>();
	private ArrayList<String> itemCostList = new ArrayList<String>();
	private BigDecimal amountPaid;
	private BigDecimal totalChange;
	private Map<BigDecimal, CoinDispenser> coinDispensers;
	private List<BigDecimal> coinDenominations;
	private BigDecimal maxCoinDenom;
	private BigDecimal minCoinDenom;
	private BillSlot output;
	private BillSlot input;
	private Boolean suspended;
	
	/**
	 * Constructor. Takes a Self-Checkout Station  and initializes
	 * fields while also registering the logic as an observer of 
	 * BillSlot, BillValidator, and BillDispenser objects of the 
	 * station. Sorts denominations in ascending order to facilitate
	 * dispensing change later.
	 * 
	 * @param SCS
	 * 		Self-Checkout Station on which to install the logic
	 * @param customer
	 * 		CustomerIO interface to represent customer session
	 * @param attendant
	 * 		AttendantIO interface that is monitoring the machine
	 */
	public PaymentControllerLogic(SelfCheckoutStation SCS, CustomerIO customer, AttendantIO attendant, PrintReceipt printerLogic) {
		this.station = SCS;
		this.station.billValidator.register(this);
		this.station.coinValidator.register(this);
		this.denominations = station.billDenominations;
		Arrays.sort(this.denominations);
		this.maxDenom = BigDecimal.valueOf(Arrays.stream(this.denominations).max().getAsInt());
		this.minDenom = BigDecimal.valueOf(Arrays.stream(this.denominations).min().getAsInt());
		this.dispensers = station.billDispensers;
		this.printer = station.printer;
		for (int value : this.denominations) {
			this.dispensers.get(value).register(this);
		}
		this.myCustomer = customer;
		this.myAttendant = attendant;
		this.printerLogic = printerLogic;
		this.coinDenominations = station.coinDenominations;
		Collections.sort(this.coinDenominations);
		this.maxCoinDenom = Collections.max(this.coinDenominations);
		this.minCoinDenom = Collections.min(this.coinDenominations);
		this.coinDispensers = station.coinDispensers;
		for (BigDecimal value : this.coinDenominations) {
			this.coinDispensers.get(value).register(this);
		}
		this.amountPaid = BigDecimal.valueOf(0.0);
		this.cartTotal = BigDecimal.valueOf(0.0);
		this.totalChange = BigDecimal.valueOf(0.0);
		this.changeDue = BigDecimal.valueOf(0.0);
		this.output = station.billOutput;
		this.input = station.billInput;
		this.output.register(this);
		this.input.register(this);
		this.suspended = false;
	}

/* ------------------------ General Methods --------------------------------------------------*/
	
	/**
	 * Updates the amount paid by the customer.
	 * 
	 * @param billValue
	 * 			The value of the bill inserted
	 */
	public void updateAmountPaid(BigDecimal value) {
		this.amountPaid = this.amountPaid.add(value);
	}
	
	/**
	 * Basic getter to return amount paid field.
	 */
	public String getAmountPaid() {
		return "" + this.amountPaid;
	}

	/**
	 * Sets the total amount of change due to the customer.
	 * This value will not be updated, and is related to 
	 * printing the receipt.
	 * 
	 * @param amount
	 * 			Amount of change to be due
	 */
	public void setTotalChange(BigDecimal amount) {
		this.totalChange = amount;
	}
	
	/**
	 * Basic getter to return total change.
	 */
	public String getTotalChange() {
		return "" + this.totalChange;
	}
	
	/**
	 * Updates cartTotal. Takes any double. Should be called by AddItemByScanningController
	 * to update, as that is where the cost of each item is being determined.
	 * Updates by item, not all at once, so will have to be called numerous
	 * times.
	 * 
	 * @param price
	 * 		amount to be added to running total
	 */
	public void updateCartTotal(BigDecimal price) {
		this.cartTotal = this.cartTotal.add(price);
	}

	/**
	 * Sets cart total. Takes a double. Only to be called within this
	 * class.
	 * 
	 * @param total
	 * 		amount to set the total cost of the cart
	 */
	
	public void setCartTotal(BigDecimal total) {
		this.cartTotal = total;
	}

	/**
	 * Builds the lists for the item names and costs in the cart. Should
	 * be called by AddItemByScanningController as needed to update
	 * this info.
	 * 
	 * @param itemName
	 * 			The name of the item added
	 * @param itemCost
	 * 			The cost of the item added
	 */
	public void updateItemCostList(String itemName, String itemCost) {
		this.itemNameList.add(itemName);
		this.itemCostList.add(itemCost);
	}
	
	/**
	 * Getter for cartTotal. Just returns value.
	 */
	public BigDecimal getCartTotal() {
		return this.cartTotal;
	}
	
	/**
	 * Setter for changeDue. Takes any double. Is only to be called within this
	 * class.
	 * 
	 * @param change
	 * 		amount of change that is due
	 */
	public void setChangeDue(BigDecimal change) {
		this.changeDue = change;
	}
	
	/**
	 * Getter for cartTotal. Returns double. No need to call from any other class.
	 */
	public BigDecimal getChangeDue() {
		return this.changeDue;
	}

	/**
	 * Suspends machine. This is updated with the new SelfCheckoutMachine objects now.
	 */
	private void suspendMachine() {
		this.suspended = true;
		this.station.baggingArea.disable();
		for(int denom : this.denominations) {
			this.station.billDispensers.get(denom).disable();
		}
		this.station.billInput.disable();
		this.station.billOutput.disable();
		this.station.handheldScanner.disable();
		this.station.mainScanner.disable();
		this.station.cardReader.disable();
		this.station.coinSlot.disable();
		this.station.coinValidator.disable();
		for(BigDecimal denom : this.coinDenominations) {
			this.station.coinDispensers.get(denom).disable();
		}
	}
	
	/**
	 * Enables cash payment, which should be disabled by default.
	 * This should be called by the CustomerIO when they select a payment
	 * method. To do so, it simply enables all cash payment devices.
	 */
	public void enableCashPayment() {
		this.station.coinSlot.enable();
		this.station.coinTray.enable();
		this.station.coinStorage.enable();
		this.station.coinValidator.enable();
		for(BigDecimal denom : this.coinDenominations) {
			this.station.coinDispensers.get(denom).enable();
		}
		for(int denom : this.denominations) {
			this.station.billDispensers.get(denom).enable();
		}
		this.station.billInput.enable();
		this.station.billOutput.enable();
		this.station.billStorage.enable();
		this.station.billValidator.enable();
	}
	
	/**
	 * This enables the card payments. As with cash payment method
	 * above, this should be called when payment is selected, and
	 * enables all necessary devices (just CardReader in this case).
	 */
	public void enableCardPayment() {
		this.station.cardReader.enable(); 
	}
	
/* ------------------------ Cash Payment Methods ---------------------------------------------*/	
	
	/**
	 * Dispenses change based on denominations. Looks through denominations in the 
	 * installed station and finds dispensers for each based on value, and determines
	 * first whether the denomination is appropriate (the largest bill smaller than the
	 * change left) and then if the dispenser is empty or not. If both conditions are met, 
	 * it dispenses. If not, it finds the next best denomination before dispensing.
	 * (Step 7). If the amount of change due cannot be dispensed by the bills, due to
	 * either too small of change due or due to empty dispensers, it will call for
	 * coins to be dispensed as change.
	 */
	private void dispenseChange() {
		BillDispenser dispenser;
		if(this.getChangeDue().compareTo(BigDecimal.valueOf(0.0)) == 0) {
			throw new SimulationException(new Exception("This should never happen"));
		}
		/** If the changeDue is less than the lowest denom, dispense coins */
		else if(this.getChangeDue().compareTo(this.minDenom) == -1) {
			this.dispenseCoins();
		}
		/** Go through denominations backwards, largest to smallest */
		for(int index = this.denominations.length-1 ; index >= 0 ; index--) {
			dispenser = this.dispensers.get(this.denominations[index]);
			/** If the value of the bill is less than or equal to the change and change is payable */
			if(BigDecimal.valueOf(this.denominations[index]).compareTo(this.getChangeDue()) <= 0) {
				try {
					dispenser.emit();
					index++;
				}
				/** If empty and not the smallest denom, move on. If the smallest denom, dispense coins */
				catch(EmptyException e) {
					if(BigDecimal.valueOf(this.denominations[index]).compareTo(this.minDenom) == 0) {
						/** In this case change will be larger than smallest denom but unpayable through bills */
						this.dispenseCoins();
						break;
					}
					else {
						continue;
					}
				}
				catch(Exception e) {
					break;
				}
			}
		}
	}

	/**
	 * Dispenses change based on denominations in the form of coins. If unable
	 * to do so, it will inform the attendant because 
	 */	
	private void dispenseCoins() {
		CoinDispenser dispenser;
		if(this.getChangeDue().compareTo(BigDecimal.valueOf(0.0)) == 0) {
			throw new SimulationException(new Exception("This should never happen"));
		}
		else if(this.getChangeDue().compareTo(this.minCoinDenom) == -1) {
			this.myAttendant.changeRemainsNoDenom(this.getChangeDue());
			/** No need to suspend machine, nothing is empty its just a lack of denoms */
		}
		/** Go through denominations backwards, largest to smallest */
		for(int index = this.coinDenominations.size()-1 ; index >= 0 ; index--) {
			dispenser = this.coinDispensers.get(this.coinDenominations.get(index));
			/** If the value of the coin is less than or equal to the change and change is payable */
			if(this.coinDenominations.get(index).compareTo(this.getChangeDue()) <= 0) {
				try {
					dispenser.emit();
					index++;
				}
				/** If empty and not the smallest denom, move on. If the smallest denom, inform attendant */
				catch(EmptyException e) {
					if(this.coinDenominations.get(index).compareTo(this.minCoinDenom) == 0) {
						/** In this case change will be larger than smallest denom but unpayable */
						this.myAttendant.changeRemainsNoDenom(this.getChangeDue());
						this.suspendMachine();
						break;
					}
					else {
						continue;
					}
				}
				catch(Exception e) {
					break;
				}				
			}
		}
	}
	
	/**
	 * Subtracts value from the cart based on the value of the bill
	 * added. (Step 2, Step 3, Step 5, Step 6)
	 */
	public void payCash(BigDecimal cashValue) {
		this.enableCashPayment();
		this.updateAmountPaid(cashValue);
		this.setCartTotal(this.getCartTotal().subtract(cashValue));
		myCustomer.showUpdatedTotal(this.getCartTotal());
		/** If the customer has paid their cart, check for change */
		if(this.getCartTotal().compareTo(BigDecimal.valueOf(0.0)) <= 0) {
			this.setChangeDue(BigDecimal.valueOf(0.0).subtract(this.getCartTotal()));
			this.setTotalChange(this.getChangeDue());
			this.setCartTotal(BigDecimal.valueOf(0.0)); //Set cart total after change has been calculated.
			/** this.suspend should never be true here */
			if(this.getChangeDue().compareTo(BigDecimal.valueOf(0.0)) > 0 && this.suspended == false) {
				this.dispenseChange();
			}
			/** this.suspend should never be true here */
			else if(this.suspended == false) {
				this.printerLogic.print(this.itemNameList,this.itemCostList,this.getTotalChange(),this.getAmountPaid());
			}
		}
	}
	
/* ------------------------ Pay with Credit --------------------------------------------------*/
	// implements the pay with credit use case. trigger: customer must with to pay with credit
	public void payCredit(BigDecimal amountPaid, CreditCard card, String pin, BankIO bank) throws IOException {
		//step 2: enable card reader
		this.enableCardPayment();
		//step 3: passing card and PIN to card reader
		while (true) {
			try {
				station.cardReader.insert(card, pin);
				break;
			//if the pin is entered wrong, retry
			} catch (InvalidPINException e) {
				station.cardReader.remove();
				pin = myCustomer.getPin();
			//random probability of failure, just retry
			} catch (ChipFailureException e1) {
				station.cardReader.remove();
				continue;
			//step 5: if the pin is entered wrong three times, move on to RepeatedBadPin use case
			} catch (BlockedCardException e2) {
				RepeatedBadPin(bank, card);
				station.cardReader.disable();
				return;
			}
		}
		//step 5 and 7: signal to the bank with card data and amount to be paid, and the bank
		//should signal back the hold number
		int holdNumber = bank.creditCardTranscation(card, amountPaid);
		//hold number of 0 means the transaction failed
		if(holdNumber == 0) {
			myCustomer.transactionFailure();
			station.cardReader.remove();
			station.cardReader.disable();
			return;
		}
		//step 8: the system should signal the bank to complete the transaction
		bank.completeTransaction(holdNumber);
		//step 10: Reduces the remaining amount due by the amount of the transaction
		this.updateAmountPaid(amountPaid);
		this.setCartTotal(this.getCartTotal().subtract(amountPaid));
		//step 11: Signals to the Card Reader to release the card
		boolean success = station.cardReader.remove();
		//step 13: Card reader signals to the System that the operation is complete
		if (success) {
			//step 14: Signals to Customer I/O that the operation is complete and the remaining 
			//amount due is reduced.
			myCustomer.payWithCreditComplete(this.getCartTotal());
		}
		station.cardReader.disable();
	}
	
	// implements the pay with debit use case. trigger: customer must with to pay with debit
		public void payDebit(BigDecimal amountPaid, DebitCard card, String pin, BankIO bank) throws IOException {
			//step 2: enable card reader
			this.enableCardPayment();
			//step 3: passing card and PIN to card reader
			while (true) {
				try {
					station.cardReader.insert(card, pin);
					break;
				//if the pin is entered wrong, retry
				} catch (InvalidPINException e) {
					station.cardReader.remove();
					pin = myCustomer.getPin();
				//random probability of failure, just retry
				} catch (ChipFailureException e1) {
					station.cardReader.remove();
					continue;
				//step 5: if the pin is entered wrong three times, move on to RepeatedBadPin use case
				} catch (BlockedCardException e2) {
					myCustomer.transactionFailure();
					RepeatedBadPin(bank, card);
					station.cardReader.disable();
					return;
				}
			}
			//step 5 and 7: signal to the bank with card data and amount to be paid, and the bank
			//should signal back the hold number
			int holdNumber = bank.debitCardTranscation(card, amountPaid);
			//hold number of 0 means the transaction failed
			if(holdNumber == 0) {
				myCustomer.transactionFailure();
				station.cardReader.remove();
				station.cardReader.disable();
				return;
			}
			//step 8: the system should signal the bank to complete the transaction
			bank.completeTransaction(holdNumber);
			//step 10: Reduces the remaining amount due by the amount of the transaction
			this.updateAmountPaid(amountPaid);
			this.setCartTotal(this.getCartTotal().subtract(amountPaid));
			//step 11: Signals to the Card Reader to release the card
			boolean success = station.cardReader.remove();
			//step 13: Card reader signals to the System that the operation is complete
			if (success) {
				//step 14: Signals to Customer I/O that the operation is complete and the remaining 
				//amount due is reduced.
				myCustomer.payWithDebitComplete(this.getCartTotal());
			}
			station.cardReader.disable();
		}

	// implements the repeated bad pin use case. called by pay with credit or pay with debit
	private void RepeatedBadPin(BankIO bank, Card card) {
		bank.blockCard(card);
	}
	
/* ------------------------ Observer Overrides -----------------------------------------------*/
	
	/* ---------------- Abstract --------------------------------*/
	@Override
	public void reactToEnabledEvent(AbstractDevice<? extends AbstractDeviceObserver> device) {
		// Ignoring in this iteration
		
	}

	@Override
	public void reactToDisabledEvent(AbstractDevice<? extends AbstractDeviceObserver> device) {
		// Ignoring in this iteration
		
	}

	/* ---------------- Bill Validator -------------------*/
	/**
	 * Making this observer call the payment method. This makes sense, as it is the only input
	 * observer that actually has the bill and its value, and it only makes sense that payment
	 * calculations are made after the bill has actually been validated. (Step 1)
	 */
	@Override
	public void reactToValidBillDetectedEvent(BillValidator validator, Currency currency, int value) {
		this.payCash(BigDecimal.valueOf(value));
	}

	@Override
	public void reactToInvalidBillDetectedEvent(BillValidator validator) {
		// Ignoring in this iteration
	}

	/* ---------------- Bill Dispenser -------------------*/
	@Override
	public void reactToBillsFullEvent(BillDispenser dispenser) {
		// Ignoring in this iteration
	}
	
	/**
	 * This does the same thing as reactToBillRemovedEvent, just in the case that
	 * the dispenser becomes empty. Change is still updated, just in a bit of a 
	 * roundabout way.
	 */
	@Override
	public void reactToBillsEmptyEvent(BillDispenser dispenser) {
		for(int denom : this.denominations) {
			if(this.dispensers.get(denom).equals(dispenser)) {
				this.setChangeDue(this.getChangeDue().subtract(BigDecimal.valueOf(denom)));
				/** this.suspend should never be true here */
				if(this.getChangeDue().compareTo(BigDecimal.valueOf(0.0)) > 0 && this.suspended == false) {
					this.dispenseChange();
				}
				/** this.suspend should never be true here */
				else if(this.suspended == false) {
					this.printerLogic.print(this.itemNameList,this.itemCostList,this.getTotalChange(),this.getAmountPaid());
				}
			}
		}
	}

	@Override
	public void reactToBillAddedEvent(BillDispenser dispenser, Bill bill) {
		// Ignoring in this iteration
	}

	/**
	 * Setting this observer to update the amount of change remaining, as it
	 * should not be updated until the actual bill has been dispensed to the
	 * customer. It presumably has to do nothing else, as after the change
	 * has been dispensed control goes over to the print receipt logic.
	 * If after dispensing there is no change left, life is good. If not,
	 * it calls dispense change again. (Step 6)
	 */
	@Override
	public void reactToBillRemovedEvent(BillDispenser dispenser, Bill bill) {
		this.setChangeDue(this.getChangeDue().subtract(BigDecimal.valueOf(bill.getValue())));
		/** this.suspend should never be true here */
		if(this.getChangeDue().compareTo(BigDecimal.valueOf(0.0)) > 0 && this.suspended == false) {
			this.dispenseChange();
		}
		/** this.suspend should never be true here */
		else if(this.suspended == false) {
			this.printerLogic.print(this.itemNameList,this.itemCostList,this.getTotalChange(),this.getAmountPaid());
		}
	}

	@Override
	public void reactToBillsLoadedEvent(BillDispenser dispenser, Bill... bills) {
		// Ignoring in this iteration
	}

	@Override
	public void reactToBillsUnloadedEvent(BillDispenser dispenser, Bill... bills) {
		// Ignoring in this iteration
	}

	/* ---------------- BillSlot ------------------------*/
	@Override
	public void reactToBillInsertedEvent(BillSlot slot) {
		// Ignoring in this iteration	
	}

	/**
	 * This is informing the customer that they have to remove the
	 * bill dangling from the slot.
	 */
	@Override
	public void reactToBillEjectedEvent(BillSlot slot) {
		this.myCustomer.removeBill(slot);
	}

	@Override
	public void reactToBillRemovedEvent(BillSlot slot) {
		// Ignoring in this iteration
	}

	/* ---------------- Coin Dispenser ------------------*/	
	@Override
	public void reactToCoinsFullEvent(CoinDispenser dispenser) {
		// Ignoring in this iteration
		
	}

	@Override
	public void reactToCoinsEmptyEvent(CoinDispenser dispenser) {
		// Ignoring in this iteration
		
	}

	@Override
	public void reactToCoinAddedEvent(CoinDispenser dispenser, Coin coin) {
		// Ignoring in this iteration
		
	}

	/**
	 * When a coin has been successfully removed, adjust the change
	 * accordingly. Basically functions identically to the equivalent
	 * BillDispenserObserver method that I have implemented above.
	 */
	@Override
	public void reactToCoinRemovedEvent(CoinDispenser dispenser, Coin coin) {
		this.setChangeDue(this.getChangeDue().subtract(coin.getValue()));
		if(this.getChangeDue().compareTo(BigDecimal.valueOf(0.0)) > 0 && this.suspended == false) {
			this.dispenseChange();
		}
		else if(this.suspended == false) {
			this.printerLogic.print(this.itemNameList,this.itemCostList,this.getTotalChange(),this.getAmountPaid());
		}
	}

	@Override
	public void reactToCoinsLoadedEvent(CoinDispenser dispenser, Coin... coins) {
		// Ignoring in this iteration
		
	}

	@Override
	public void reactToCoinsUnloadedEvent(CoinDispenser dispenser, Coin... coins) {
		// Ignoring in this iteration
		
	}

	/* ---------------- Coin Tray -----------------------*/
	/**
	 * When a coin is deposited to the tray, the customer should remove,
	 * or at least think about removing, the coin. This is the only way
	 * tray overload will be managed, as there is no specific documentation
	 * on what should be done to prevent overflow or in the case of overflow.
	 */
	@Override
	public void reactToCoinAddedEvent(CoinTray tray) {
		myCustomer.removeCoin(tray);
	}

	/* ---------------- Coin Validator ------------------*/
	/**
	 * When a valid coin is detected, make the payment. This is basically
	 * the same as the BillValidatorObserver method implemented above, just
	 * for coins.
	 */
	@Override
	public void reactToValidCoinDetectedEvent(CoinValidator validator, BigDecimal value) {
		this.payCash(value);
	}

	@Override
	public void reactToInvalidCoinDetectedEvent(CoinValidator validator) {
		// Ignoring in this iteration
		
	}
	/* ---------------- Card Reader --------------------*/
	@Override
	public void reactToCardInsertedEvent(CardReader reader) {
		
	}
	
	@Override
	public void reactToCardRemovedEvent(CardReader reader) {
		
	}
	
	@Override
	public void reactToCardTappedEvent(CardReader reader) {
		
	}
	
	@Override
	public void reactToCardSwipedEvent(CardReader reader) {
		
	}

	@Override
	public void reactToCardDataReadEvent(CardReader reader, CardData data) {
		
	}
}
