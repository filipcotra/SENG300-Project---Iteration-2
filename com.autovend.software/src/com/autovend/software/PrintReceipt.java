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

import java.util.ArrayList;

import com.autovend.devices.AbstractDevice;
import com.autovend.devices.ReceiptPrinter;
import com.autovend.devices.SelfCheckoutStation;
import com.autovend.devices.observers.AbstractDeviceObserver;
import com.autovend.devices.observers.ReceiptPrinterObserver;

/**
 * Control software for the Print Receipt use case.
 */
public class PrintReceipt implements ReceiptPrinterObserver {

	ReceiptPrinter printer;
	double totalVal; // Total value of the items
	String amountPaidbyUser = "";
	String changeNeeded = "";
	CustomerIO customer;
	AttendantIO attendant;
	SelfCheckoutStation station;
	boolean flagInk = true;
	boolean flagPaper = true;
	char[] totalChars = { 'T', 'o', 't', 'a', 'l', ':', ' ', '$' };
	char[] paidChars = { 'P', 'a', 'i', 'd', ':', ' ', '$' };
	char[] changeChars = { 'C', 'h', 'a', 'n', 'g', 'e', ':', ' ', '$' };
	char[] priceSpace = { ' ', ' ', ' ', ' ', ' ', ' ', '$' };
	int initialInk;
	int initialPaper;
	public int inkRemaining;
	public int paperRemaining;
	boolean lowPaper = false;
	boolean lowInk = false;

	/**
	 * Initialize a printer for the Print Receipt use case. Also registers this
	 * class as an observer for the station's main scanner.
	 * 
	 * @param station The Self Checkout Station being used
	 * @param printer The Receipt Printer to be used
	 * @param c       The Customer that is interacting with the Print Receipt use
	 *                case
	 * @param a       The Attendant that is interacting with the Print Receipt use
	 *                case
	 */
	public PrintReceipt(SelfCheckoutStation station, ReceiptPrinter printer, CustomerIO c, AttendantIO a) {
		this.station = station;
		this.printer = printer;
		// Register this class as an observer of the printer
		this.printer.register(this);
		this.customer = c;
		this.attendant = a;
	}

	/**
	 * Sets the total value
	 * 
	 * @param totalVal The total value of all the items scanned by the customer
	 */
	public void setTotalVal(double totalVal) {
		this.totalVal = totalVal;
	}

	/**
	 * Gets the total value
	 * 
	 * @return totalVal The total value of all the items scanned by the customer
	 */
	public double getTotalVal() {
		return this.totalVal;
	}

	/**
	 * Method to suspend all the hardware components of the self checkout system
	 */
	public void suspendSystem() {
		this.station.printer.disable();
		this.station.baggingArea.disable();
		this.station.mainScanner.disable();
		this.station.handheldScanner.disable();
		this.station.billInput.disable();
		this.station.billOutput.disable();
		this.station.billStorage.disable();
		this.station.billValidator.disable();
	}

	/**
	 * Method to unsuspend all the hardware components of the self checkout system
	 */
	public void unSuspendSystem() {
		this.station.printer.enable();
		this.station.baggingArea.enable();
		this.station.mainScanner.enable();
		this.station.handheldScanner.enable();
		this.station.billInput.enable();
		this.station.billOutput.enable();
		this.station.billStorage.enable();
		this.station.billValidator.enable();
	}


	/**
	 * The method that prints out the receipt for the customer. Starts by printing
	 * the items and their corresponding prices, and then the total, amount paid by
	 * the customer, and the change they were given. Before print is called, each
	 * time flags for paper and ink should be checked.
	 * 
	 * @param items      The items bought by the customer
	 * @param prices     The price of the item bought by the customer
	 * @param change     The change due after the customer has paid
	 * @param amountPaid The amount that the customer paid
	 */
	public void print(ArrayList<String> items, ArrayList<String> prices, String change, String amountPaid) {
		this.totalVal = 0;
		try {

			// Loops through and prints the item, its price, and the total amount it adds up
			// to
			for (int i = 0; i < items.size(); i++) {
				// Print item name
				for (int k = 0; k < items.get(i).length(); k++) {
					this.callPrint(items.get(i).charAt(k));
				}

				// Creating some spacing between the items and their respective prices.
				for (char ch : this.priceSpace) {
					this.callPrint(ch);
				}

				// Print item price
				for (int k = 0; k < prices.get(i).length(); k++) {
					this.callPrint(prices.get(i).charAt(k));
				}

				// Update totalVal to reflect the new item
				setTotalVal(totalVal += (Double.parseDouble(prices.get(i))));
				// Print a newline character after each item
				this.callPrint('\n');
			}

			// Printing "Total: $"
			for (char ch : this.totalChars) {
				this.callPrint(ch);
			}

			// Storing the totalVal as a string
			String strTotalVal = Double.toString(totalVal);
			if (strTotalVal.charAt(strTotalVal.length() - 1) == '0') {
				strTotalVal += '0';
			}

			// Printing the amount in totalVal
			for (int i = 0; i < strTotalVal.length(); i++) {
				this.callPrint(strTotalVal.charAt(i));
			}

			// Print a newline character after the total
			this.callPrint('\n');

			// Printing "Paid: $"
			for (char ch : this.paidChars) {
				this.callPrint(ch);
			}

			// Taking the total amount paid by the user and printing it
			for (int l = 0; l < amountPaid.length(); l++) {
				this.callPrint(amountPaid.charAt(l));
			}

			// Printing a newline character
			this.callPrint('\n');

			// Taking the change due for the user and storing it as a string
			for (int l = 0; l < change.length(); l++) {
				changeNeeded += (String.valueOf(change));
			}

			// Printing a newline character
			this.callPrint('\n');

			// Printing "Change: $"
			for (char ch : this.changeChars) {
				this.callPrint(ch);
			}

			// Printing the change due
			for (int m = 0; m < change.length(); m++) {
				this.callPrint(change.charAt(m));
			}

			// Printing a newline character
			this.callPrint('\n');

			// Cut the paper
			printer.cutPaper();

			// Step 4: Once the receipt is printed, signals to Customer I/O that session is
			// complete.
			this.customer.thankCustomer();
			if (lowPaper || lowInk){
				suspendSystem();
				refillPaper();
				refillInk();
			}
		}
		// Catch any exceptions
		catch (Exception e) {
			// Unspecified function
		}
	}

	/**
	 * Method for calling print specifically, connecting software to hardware.
	 * Checks the ink and paper flags before printing.
	 * @param ch the character to be printed
	 */
	public void callPrint(char ch) throws Exception {
		if (!(this.flagPaper == false || this.flagInk == false)) {
			try {
				this.printer.print(ch);
				if (ch == '\n'){
					paperRemaining -=1;
				} else if (!Character.isWhitespace(ch)) {
					inkRemaining -=1;
				}
				lowPaper();
				lowInk();
			} catch (Exception e) {
				throw e;
			}
		} else {
			// This should hopefully abort the print method
			throw new Exception();
		}
	}


	/**
	 * Method to store the amount of ink and paper initially in the printer so the software can estimate when ink/paper is low
	 * also initializes variables to store how much ink/paper is remaining
	 * 
	 * @param ink the amount of ink added to the printer
	 * @param paper the amount of paper added to the printer
	 */
	public void setContents(int ink, int paper){
		this.initialInk += ink;
		this.initialPaper += paper;
		this.inkRemaining += ink;
		this.paperRemaining += paper;
	}

	/**
	 * This method calculates if the amount of paper in the printer is low and sets an appropriate flag
	 */
	public void lowPaper(){
		if (paperRemaining <= initialPaper*.25){
			this.lowPaper = true;
		}
	}

	/**
	 * This method calculates if the amount of ink in the printer is low and sets an appropriate flag
	 */
	public void lowInk(){
		if (inkRemaining <= initialInk*.25){
			this.lowInk = true;
		}
	}
	
	/**
	 * Method used to refillInk once lowInk is detected, calls the attendant to refill the ink and unsuspends the system
	 */
	public void refillInk(){
		if(this.lowInk == true) {
			this.attendant.acknowledgeLowInk();
			unSuspendSystem();
			this.lowInk = false;
		}
	}
	
	/**
	 * Method used to refillInk once lowPaper is detected, calls the attendant to refill the paper and unsuspends the system
	 */
	public void refillPaper(){
		if(this.lowPaper == true) {
			this.attendant.acknowledgeLowPaper();
			unSuspendSystem();
			this.lowPaper = false;
		}
	}
	
	/**
	 * A getter to return this.lowInk to check if lowInk() is called
	 * @return this.lowInk
	 */
	public boolean getLowInk(){
		return this.lowInk;
	}
	
	/**
	 * A getter to return this.lowPaper to check if lowPaper() is called
	 * @return this.lowPaper
	 */
	public boolean getLowPaper(){
		return this.lowPaper;
	}


	// Implement methods from the ReceiptPrinterObserver interface

	/**
	 * Print duplicate receipt for the attendant if the printer is out of ink
	 */
	@Override
	public void reactToOutOfInkEvent(ReceiptPrinter printer) {
		this.flagInk = false;
		this.suspendSystem();
		this.attendant.printDuplicateReceipt();
	}

	/**
	 * Print duplicate receipt for the attendant if the printer is out of paper
	 */
	@Override
	public void reactToOutOfPaperEvent(ReceiptPrinter printer) {
		this.flagPaper = false;
		this.suspendSystem();
		this.attendant.printDuplicateReceipt();
	}

	/**
	 * Sets the flagInk to true when ink is added
	 */
	@Override
	public void reactToInkAddedEvent(ReceiptPrinter printer) {
		flagInk = true;
	}

	// Implement methods from the AbstractDeviceObserver interface (unused in this
	// class)
	@Override
	public void reactToEnabledEvent(AbstractDevice<? extends AbstractDeviceObserver> device) {
		// TODO Auto-generated method stub
	}

	// Implement methods from the AbstractDeviceObserver interface (unused in this
	// class)
	@Override
	public void reactToDisabledEvent(AbstractDevice<? extends AbstractDeviceObserver> device) {
		// TODO Auto-generated method stub
	}

	/**
	 * Sets flagPaper to true when paper is added
	 */
	@Override
	public void reactToPaperAddedEvent(ReceiptPrinter printer) {
		flagPaper = true;
	}
}
