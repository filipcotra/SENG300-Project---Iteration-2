/** 
 * Filip Cotra - 30086750
 * Khondaker Samin Rashid - 30143490
 * Nishan Soni - 30147280
 * Aaron Tigley - 30159927
 * Zainab Bari - 30154224
 */

package com.autovend.software.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Currency;

import org.junit.Before;
import org.junit.Test;

import com.autovend.BarcodedUnit;
import com.autovend.Bill;
import com.autovend.Card;
import com.autovend.devices.BillSlot;
import com.autovend.devices.CoinTray;
import com.autovend.devices.OverloadException;
import com.autovend.devices.SelfCheckoutStation;
import com.autovend.software.AttendantIO;
import com.autovend.software.CustomerIO;
import com.autovend.software.PaymentControllerLogic;
import com.autovend.software.PrintReceipt;
import com.autovend.software.test.PaymentWithCashTest.MyBillSlotObserver;
import com.autovend.devices.DisabledException;

public class ReceiptPrinterTest {

	PrintReceipt receiptPrinterController;
	SelfCheckoutStation selfCheckoutStation;
	MyBillSlotObserver billObserver;
	MyCustomerIO customer;
	MyAttendantIO attendant;
	ArrayList<String> itemNameList = new ArrayList<String>();
	ArrayList<String> itemCostList = new ArrayList<String>();
	String change;
	String amountPaid;
	String itemFmt1;
	String itemFmt2;
	String itemFmt3;
	final PrintStream originalOut = System.out;
	ByteArrayOutputStream baos;
	PrintStream ps;

	/**
	 * a stub to simulate interactions with the customer
	 */
	class MyCustomerIO implements CustomerIO {
	
			@Override
			public void thankCustomer() {
				System.out.print("thankCustomer Called");
			}

			@Override
			public void removeBill(BillSlot slot) {
				// TODO Auto-generated method stub	
			}

			@Override
			public void scanItem(BarcodedUnit item) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void notifyPlaceItemCustomerIO() {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void placeScannedItemInBaggingArea(BarcodedUnit item) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void showUpdatedTotal(BigDecimal total) {
				// TODO Auto-generated method stub
				
			}


			@Override
			public void removeCoin(CoinTray tray) {
			}

			
			@Override
			public boolean selectAddOwnBags() {
				// TODO Auto-generated method stub
				return true;
			}
			
			@Override
			public void indicateToCustomerToContinueAfterAttendantApproveOrDenyAddedBags() {
				// TODO Auto-generated method stub
			}

			@Override
			public void notifyWeightDiscrepancyCustomerIO() {

				// TODO Auto-generated method stub
				
			}

			@Override

			public void payWithCreditComplete(BigDecimal amountDue) {
			}

			public void notifyWeightDiscrepancyApprovedCustomerIO() {

				// TODO Auto-generated method stub
				
			}

			@Override
			public void payWithDebitComplete(BigDecimal amountDue) {
			}
      
			public void signalPurchaseBags(int quantity) {

				// TODO Auto-generated method stub
				
			}

			@Override

			public void transactionFailure() {
			}

			public void signalFinishedPurchasingBags() {

				// TODO Auto-generated method stub
				
			}

			@Override

			public void selectPaymentMethod(String paymentMethod, PaymentControllerLogic instance) {
			}

			public void signalReadyForInteraction() {

				// TODO Auto-generated method stub
				
			}

			@Override

			public void setCardPaymentAmount(BigDecimal amount) {
			}

			public void signalPutPurchasedBagsOnBaggingArea() {

				// TODO Auto-generated method stub
				
			}

			@Override

			public void insertCard(Card card, String pin) {
			}

			public String getMembershipNumber() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public boolean cancelMembershipInput() {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public void notifyBadMembershipNumberCustomerIO() {

				// TODO Auto-generated method stub
				
			}
		
		}
		
	
	/**
	 * A stub to simulate interactions with the attendant.
	 */
	class MyAttendantIO implements AttendantIO {
	
		@Override
		public void printDuplicateReceipt() {
			System.out.print("printDuplicate Called");	
		}

		@Override
		public void changeRemainsNoDenom(BigDecimal changeLeft) {
			// TODO Auto-generated method stub
			
		}


		@Override
		public void notifyWeightDiscrepancyAttendantIO() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void approveWeightDiscrepancy(CustomerIO customerIO) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void checkAddedOwnBags() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void acceptOwnBags() {
			// TODO Auto-generated method stub
		}
			

		/**
		 * Method that simulates the attendant refilling the ink physically and in software. 
		 * We assume that the ink is refilled to its maximum capacity.
		 */
		@Override
		public void acknowledgeLowInk() {
			try{
				selfCheckoutStation.printer.addInk(1048576 - receiptPrinterController.inkRemaining);
				receiptPrinterController.setContents(1048576 - receiptPrinterController.inkRemaining, 0);
			} catch (Exception e){
				e.printStackTrace();
			}

		}
		/**
		 * Method that simulates the attendant refilling the paper physically and in software. 
		 * We assume that the paper is refilled to its maximum capacity.
		 */
		@Override
		public void acknowledgeLowPaper() {
			try{
				selfCheckoutStation.printer.addPaper(1024 - receiptPrinterController.paperRemaining);
				receiptPrinterController.setContents(0, 1024 - receiptPrinterController.paperRemaining);
			} catch (Exception e){
				e.printStackTrace();
			}

		}
	}
	

	/**
	 * Method to setup before the tests
	 */
	@Before
	public void setUp() {
		// Setting up new print stream to catch printed output, used to test terminal output
		baos = new ByteArrayOutputStream();
		ps = new PrintStream(baos);
		System.setOut(ps);
		// Set up string array lists for items and their respective prices.
		// List of items:
		this.itemNameList.add("item 1");
		this.itemNameList.add("item 2");
		this.itemNameList.add("item 3");
		// List of item prices:
		this.itemCostList.add("5.00");
		this.itemCostList.add("17.00");
		this.itemCostList.add("20.00");
		
		selfCheckoutStation = new SelfCheckoutStation(Currency.getInstance("CAD"), new int[] {5,10,20,50}, 
				new BigDecimal[] {new BigDecimal(1),new BigDecimal(2)}, 10000, 5);
		customer = new MyCustomerIO();
		attendant = new MyAttendantIO();
		
		receiptPrinterController = new PrintReceipt(selfCheckoutStation, selfCheckoutStation.printer, customer, attendant);
		
	}
	
	/**
	 * Test: Given sufficient paper and ink, does the receipt get correctly printer
	 * when items and costs are provided, but there is no change.
	 * Expected: Receipt output in expected format (Which was undefined and so
	 * designed arbitrarily) with change equal to $0.00.
	 * Result: Test passes. This largely shows that steps 1-3 are occurring 
	 * correctly. They cannot really be individually tested because of how the
	 * software is working, but the end result is accurate.
	 */
	@Test
	public void printNoChange_Test(){
		// Add ink and paper to the receipt printer.
		try {
			selfCheckoutStation.printer.addInk(1048576);
			selfCheckoutStation.printer.addPaper(1024);
			receiptPrinterController.setContents(1048576, 1024);
		} catch (OverloadException e) {}
		change = "0.00";
		amountPaid = "75.00";
		receiptPrinterController.print(this.itemNameList, this.itemCostList, change, amountPaid);
		assertEquals(
				  "item 1      $5.00\n"
				+ "item 2      $17.00\n"
				+ "item 3      $20.00\n"
				+ "Total: $42.00\n"
				+ "Paid: $75.00\n\n"
				+ "Change: $0.00\n",
				selfCheckoutStation.printer.removeReceipt());
	}
	
	/**
	 * Test: Given sufficient paper and ink, does the receipt get correctly printer
	 * when items and costs are provided, and there was change dispensed.
	 * Expected: Receipt output in expected format (Which was undefined and so
	 * designed arbitrarily) with change equal to the total amount dispensed ($3.00).
	 * Result: Test passes. This largely shows that steps 1-3 are occurring 
	 * correctly. They cannot really be individually tested because of how the
	 * software is working, but the end result is accurate.
	 */
	@Test
	public void printWithChange_Test(){
		// Add ink and paper to the receipt printer.
		try {
			selfCheckoutStation.printer.addInk(1048576);
			selfCheckoutStation.printer.addPaper(1024);
			receiptPrinterController.setContents(1048576, 1024);
		} catch (OverloadException e) {}
		change = "3.00";
		amountPaid = "45.00";
		receiptPrinterController.print(this.itemNameList, this.itemCostList, change, amountPaid);
		assertEquals(
				  "item 1      $5.00\n"
				+ "item 2      $17.00\n"
				+ "item 3      $20.00\n"
				+ "Total: $42.00\n"
				+ "Paid: $45.00\n\n"
				+ "Change: $3.00\n",
				selfCheckoutStation.printer.removeReceipt());;
	}
	
	/**
	 * Test: Given a successful print, the CustomerIO should be informed.
	 * Expected: A call to CustomerIO.thankCustomer(), eliciting specified
	 * print.
	 * Result: Test passes. This shows that CustomerIO is successfully notified
	 * of the customers session being completed, demonstrating that step 4 is
	 * occurring correctly. Steps 5-6 cannot really be tested.
	 */
	@Test
	public void successfulPrintThankCustomer_Test(){
		// Add ink and paper to the receipt printer.
		try {
			selfCheckoutStation.printer.addInk(1048576);
			selfCheckoutStation.printer.addPaper(1024);
			receiptPrinterController.setContents(1048576, 1024);
		} catch (OverloadException e) {}
		change = "3.00";
		amountPaid = "45.00";
		receiptPrinterController.print(this.itemNameList, this.itemCostList, change, amountPaid);
		String expected = "thankCustomer Called";
		assertEquals(expected,baos.toString());
	}
	
	
	/**
	 * Test: If the printer runs out of ink mid print.
	 * Expected: No receipt should be produced, the attendant should be
	 * informed through IO, and the machine should be suspended (meaning that
	 * a disabled exception should be thrown when attempting to use it
	 * again).
	 * Result: Test passes.
	 */
	@Test (expected = DisabledException.class)
	public void printRunningOutOfInk_Test(){
		try {
			selfCheckoutStation.printer.addPaper(1024); // Add paper to printer
			selfCheckoutStation.printer.addInk(1); // Add insufficient ink for whole receipt
			receiptPrinterController.setContents(1024, 1);
		} catch (OverloadException e) {}
		change = "0.00";
		amountPaid = "75.00";
		receiptPrinterController.print(this.itemNameList, this.itemCostList, change, amountPaid);
		// This is making sure that the printing was aborted, as no receipt is produced
		assertEquals(null, selfCheckoutStation.printer.removeReceipt());
		// This is making sure that the attendantIO was called
		String expected = "printDuplicate Called";
		assertEquals(expected, baos.toString());
		// This is making sure that the system is suspended after running out of ink - disabledException should be thrown
		// To test this, BarcodeScanner.scan() will be tried, as that is the initiator of software interactions.
		selfCheckoutStation.mainScanner.scan(null);
	}
	
	/**
	 * Test: If the printer runs out of paper mid print.
	 * Expected: No receipt should be produced, the attendant should be
	 * informed through IO, and the machine should be suspended (meaning that
	 * a disabled exception should be thrown when attempting to use it
	 * again).
	 * Result: Test passes.
	 */
	@Test (expected = DisabledException.class)
	public void printRunningOutOfPaper_Test(){
		try {
			selfCheckoutStation.printer.addInk(1048576); // Add ink to printer
			selfCheckoutStation.printer.addPaper(1); // Add insufficient paper for whole receipt
			receiptPrinterController.setContents(1048576, 1);
		} catch (OverloadException e) {}
		change = "0.00";
		amountPaid = "75.00";
		receiptPrinterController.print(this.itemNameList, this.itemCostList, change, amountPaid);
		assertEquals(null, selfCheckoutStation.printer.removeReceipt());
		// This is making sure that the printing was aborted, as no receipt is produced
		assertEquals(null, selfCheckoutStation.printer.removeReceipt());
		// This is making sure that the attendantIO was called
		String expected = "printDuplicate Called";
		assertEquals(expected,baos.toString());
		// This is making sure that the system is suspended after running out of ink - disabledException should be thrown
		// To test this, BarcodeScanner.scan() will be tried, as that is the intiator of software interactions.
		selfCheckoutStation.mainScanner.scan(null);
	}
	
	/**
	 * Test: Just to improve branch coverage. Looking for a normal print
	 * where the total cost does not end in a 0.
	 * Expected: Appropriate receipt, no exceptions.
	 * Result: Test passes and improved coverage.
	 */
	@Test
	public void totalCostNotEndingZero_Test(){
		// Add ink and paper to the receipt printer.
		try {
			selfCheckoutStation.printer.addInk(1048576);
			selfCheckoutStation.printer.addPaper(1024);
			receiptPrinterController.setContents(1048576, 1024);
		} catch (OverloadException e) {}
		// Changing the price of one item cost so that it doesn't end in 0
		this.itemCostList.set(0,"5.35");
		change = "3.00";
		amountPaid = "45.00";
		receiptPrinterController.print(this.itemNameList, this.itemCostList, change, amountPaid);
		assertEquals(
				  "item 1      $5.35\n"
				+ "item 2      $17.00\n"
				+ "item 3      $20.00\n"
				+ "Total: $42.35\n"
				+ "Paid: $45.00\n\n"
				+ "Change: $3.00\n",
				selfCheckoutStation.printer.removeReceipt());;
	}
	
	/**
	 * Test: To see if getTotalVal returns appropriate value.
	 * Expected: That the total cost will match the expected value.
	 * Result: Test passes. This is a bit of a redundant test, but improves
	 * coverage and ensures for once and all that totalVal is being
	 * correctly updated.
	 */
	@Test
	public void totalVal_Test() {
		// Add ink and paper to the receipt printer.
		try {
			selfCheckoutStation.printer.addInk(1048576);
			selfCheckoutStation.printer.addPaper(1024);
			receiptPrinterController.setContents(1048576, 1024);
		} catch (OverloadException e) {}
		change = "3.00";
		amountPaid = "45.00";
		receiptPrinterController.print(this.itemNameList, this.itemCostList, change, amountPaid);
		assertTrue(42.0==receiptPrinterController.getTotalVal());
	}
	
	/*
	 * Test: to see if the low ink works
	 * Expected: low ink is called, attendant refills and therefore low ink should be false
	 * Result: low ink is false and the printer is filled up with ink
	 */
	@Test public void lowInkTest() {

		try {
			selfCheckoutStation.printer.addInk(70);
			selfCheckoutStation.printer.addPaper(1024);
			receiptPrinterController.setContents(70, 1024);
		} catch (OverloadException e) {}
		change = "3.00";
		amountPaid = "45.00";
		
		receiptPrinterController.print(this.itemNameList, this.itemCostList, change, amountPaid);
		assertTrue(!receiptPrinterController.getLowInk());
		assertTrue(receiptPrinterController.inkRemaining == 1048576);
	}
	
	/*
	 * Test: to see if the paper ink works
	 * Expected: low paper is called, attendant refills and therefore low paper should be false
	 * Result: low paper is false and the printer is filled up with paper
	 */
	@Test public void lowPaperTest() {
		int paper = 8;
		try {
			selfCheckoutStation.printer.addInk(1048576);
			selfCheckoutStation.printer.addPaper(paper);
			receiptPrinterController.setContents(1048576, paper);
		} catch (OverloadException e) {}
		change = "3.00";
		amountPaid = "45.00";
		receiptPrinterController.print(this.itemNameList, this.itemCostList, change, amountPaid);
		assertTrue(!receiptPrinterController.getLowPaper());
		assertTrue(receiptPrinterController.paperRemaining == 1024);
	}
	
	/*
	 * Test: to see if the paper ink and low paper works together
	 * Expected: low paper and low ink is called, attendant refills and therefore low paper and low ink should be false
	 * Result: low paper is false and low ink is false and the printer is full on ink and paper
	 */
	@Test public void lowBothTest() {
		int paper = 8;
		int ink = 70;
		try {
			selfCheckoutStation.printer.addInk(ink);
			selfCheckoutStation.printer.addPaper(paper);
			receiptPrinterController.setContents(ink, paper);
		} catch (OverloadException e) {}
		change = "3.00";
		amountPaid = "45.00";
		receiptPrinterController.print(this.itemNameList, this.itemCostList, change, amountPaid);
		assertTrue(!receiptPrinterController.getLowPaper());
		assertTrue(!receiptPrinterController.getLowInk());
		assertTrue(receiptPrinterController.paperRemaining == 1024);
		assertTrue(receiptPrinterController.inkRemaining == 1048576);
	}
	
	@Test public void checkPaperBeforeRefill() {
			int paper = 8;
			try {
				selfCheckoutStation.printer.addInk(1048576);
				selfCheckoutStation.printer.addPaper(paper);
				receiptPrinterController.setContents(1048576, paper);
			} catch (OverloadException e) {}
			change = "3.00";
			amountPaid = "45.00";
			assertTrue(receiptPrinterController.paperRemaining == 8);
			receiptPrinterController.print(this.itemNameList, this.itemCostList, change, amountPaid);
			assertTrue(!receiptPrinterController.getLowPaper());
			assertTrue(receiptPrinterController.paperRemaining == 1024);
			
	}
	
	@Test public void checkInkBeforeRefill() {

		try {
			selfCheckoutStation.printer.addInk(70);
			selfCheckoutStation.printer.addPaper(1024);
			receiptPrinterController.setContents(70, 1024);
		} catch (OverloadException e) {}
		change = "3.00";
		amountPaid = "45.00";
		
		assertTrue(receiptPrinterController.inkRemaining == 70);
		receiptPrinterController.print(this.itemNameList, this.itemCostList, change, amountPaid);
		assertTrue(!receiptPrinterController.getLowInk());
		assertTrue(receiptPrinterController.inkRemaining == 1048576);
	}
	
	@Test public void PrintTestAfterRefillPaperAndInk() {
		int paper = 8;
		int ink = 70;
		try {
			selfCheckoutStation.printer.addInk(ink);
			selfCheckoutStation.printer.addPaper(paper);
			receiptPrinterController.setContents(ink, paper);
		} catch (OverloadException e) {}
		this.itemCostList.set(0,"5.35");
		change = "3.00";
		amountPaid = "45.00";
		receiptPrinterController.print(this.itemNameList, this.itemCostList, change, amountPaid);
		assertTrue(!receiptPrinterController.getLowPaper());
		assertTrue(!receiptPrinterController.getLowInk());
		assertTrue(receiptPrinterController.paperRemaining == 1024);
		assertTrue(receiptPrinterController.inkRemaining == 1048576);
		assertEquals(
				  "item 1      $5.35\n"
				+ "item 2      $17.00\n"
				+ "item 3      $20.00\n"
				+ "Total: $42.35\n"
				+ "Paid: $45.00\n\n"
				+ "Change: $3.00\n",
				selfCheckoutStation.printer.removeReceipt());;
	}
	
	
}
