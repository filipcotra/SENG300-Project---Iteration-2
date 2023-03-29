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

package com.autovend.software.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Currency;
import java.util.Scanner;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.autovend.Barcode;
import com.autovend.BarcodedUnit;
import com.autovend.Bill;
import com.autovend.BlockedCardException;
import com.autovend.Card;
import com.autovend.CreditCard;
import com.autovend.DebitCard;
import com.autovend.InvalidPINException;
import com.autovend.Numeral;
import com.autovend.Card.CardData;
import com.autovend.devices.AbstractDevice;
import com.autovend.devices.BillDispenser;
import com.autovend.devices.BillSlot;
import com.autovend.devices.CardReader;
import com.autovend.devices.CoinTray;
import com.autovend.devices.DisabledException;
import com.autovend.devices.OverloadException;
import com.autovend.devices.SelfCheckoutStation;
import com.autovend.devices.SimulationException;
import com.autovend.devices.observers.AbstractDeviceObserver;
import com.autovend.devices.observers.BillDispenserObserver;
import com.autovend.devices.observers.BillSlotObserver;
import com.autovend.devices.observers.CardReaderObserver;
import com.autovend.external.CardIssuer;
import com.autovend.external.ProductDatabases;
import com.autovend.products.BarcodedProduct;
import com.autovend.software.AddItemByScanningController;
import com.autovend.software.AttendantIO;

import com.autovend.software.BaggingAreaController;
import com.autovend.software.ConnectionIO;
import com.autovend.software.CustomerIO;
import com.autovend.software.MembershipNumberController;
import com.autovend.software.PaymentControllerLogic;
import com.autovend.software.PrintReceipt;
import com.autovend.software.test.PaymentWithCashTest.BillDispenserStub;
import com.autovend.software.test.PaymentWithCashTest.MyAttendantIO;
import com.autovend.software.test.PaymentWithCashTest.MyBillSlotObserver;
import com.autovend.software.test.PaymentWithCashTest.MyCustomerIO;
import com.autovend.software.test.payWithCardTest.cardReaderObserverStub;

public class AllTogether {
	
	AddItemByScanningController addItemByScanningController;
	PaymentControllerLogic paymentController;
	PrintReceipt receiptPrinterController;
	BaggingAreaController baggingAreaController;
	SelfCheckoutStation selfCheckoutStation;
	MyBillSlotObserver billObserver;
	MyCustomerIO customer;
	MyAttendantIO attendant;
	Bill[] fiveDollarBills;
	Bill[] tenDollarBills;
	Bill[] twentyDollarBills;
	Bill[] fiftyDollarBills;
	BillSlot billSlot;
	Bill billFive;
	Bill billTen;
	Bill billTwenty;
	Bill billFifty;
	Bill billHundred;
	ArrayList<Integer> ejectedBills; 
	BillDispenserStub billObserverStub;
	CreditCard CCard;
	DebitCard DCard;
	CardIssuer creditBank;
	CardIssuer debitBank;
	Calendar calendar;
	int BlockedCard = 0;
	final PrintStream originalOut = System.out;
	ByteArrayOutputStream baos;
	PrintStream ps;
	Barcode barcode1;
	Barcode barcode2;
	Barcode barcode3;
	Barcode barcode4;
	// Create scanned items:
	BarcodedUnit scannedItem1;
	BarcodedUnit scannedItem2;
	BarcodedUnit scannedItem3;
	BarcodedUnit scannedItem4;
	// Create placed items:
	BarcodedUnit placedItem1;
	BarcodedUnit placedItem2;
	BarcodedUnit placedItem3;
	BarcodedUnit placedItem4;
	// Create test products:
	BarcodedProduct testProduct1;
	BarcodedProduct testProduct2;
	BarcodedProduct testProduct3;
	BarcodedProduct testProduct4;
	// Create bank connection variables
	MyConnectionIO connection;
	boolean falseNegative;
	boolean connectFirstTime;
	int connectionTries;
	boolean neverConnect;
	// Create membership number variables
	String inputData;
	String membershipGood;
	MembershipNumberController membershipController;
	
	class MyConnectionIO implements ConnectionIO{

		@Override
		public boolean connectTo(CardIssuer bank) {
			connectionTries++;
			if(connectFirstTime) {
				if(connectionTries == 1) {
					return true;
				}
				return false;
			}
			if(neverConnect) {
				return false;
			}
			if(bank == null) {
				return false;
			}
			return true;
		}
		
	}
	
	class MyCustomerIO implements CustomerIO {
		
		//Various signal flags for bag purchases
		boolean purchaseBagsSignal = false;
		int purchaseBagsQuantity = 0;
		boolean finishedPurchasingBagsSignal = false;
		boolean interactionReadySignal = false;
		boolean placePurchasedBagsSignal = false;

	@Override
	public void scanItem(BarcodedUnit item) {
		while(selfCheckoutStation.mainScanner.scan(item) == false);
	}

	@Override
	public void placeScannedItemInBaggingArea(BarcodedUnit item) {
		selfCheckoutStation.baggingArea.add(item);
	}

	@Override
	public void thankCustomer() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeBill(BillSlot slot) {
		// TODO Auto-generated method stub
		slot.removeDanglingBill();
	}

	@Override
	public void notifyPlaceItemCustomerIO() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void showUpdatedTotal(BigDecimal bigDecimal) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeCoin(CoinTray tray) {
	}
	
	@Override
	public boolean selectAddOwnBags() {
		return baggingAreaController.addOwnBags();
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
		this.purchaseBagsSignal = true;
		this.purchaseBagsQuantity = quantity;
		
		baggingAreaController.purchaseBags(quantity);
		
	}

	@Override

	public void transactionFailure() {
		
	}

	public void signalFinishedPurchasingBags() {
		this.finishedPurchasingBagsSignal = true;
		
	}

	@Override

	public void selectPaymentMethod(String paymentMethod, PaymentControllerLogic instance) {
		if (paymentMethod.equals("Cash")) {
			instance.enableCashPayment();
		}
		else if(paymentMethod.equals("Credit") || paymentMethod.equals("Debit")) {
			instance.enableCardPayment(paymentMethod);
		}
	}

	public void signalReadyForInteraction() {
		this.interactionReadySignal = true;
		
	}

	@Override

	public void setCardPaymentAmount(BigDecimal amount) {
		paymentController.setCardPaymentAmount(amount);
	}

	public void signalPutPurchasedBagsOnBaggingArea() {
		this.placePurchasedBagsSignal = true;
		
	}

	@Override

	public void insertCard(Card card, String pin) {
		CardData data = null;
		try {
			data = selfCheckoutStation.cardReader.insert(card, pin);
		}
		catch(InvalidPINException e) {
			//paymentController.blockCardAtBank(data);
		}
		catch(BlockedCardException e) {
			BlockedCard++;
		}
		catch(DisabledException e) {
			throw new DisabledException();
		}
		catch(Exception e) {
		}
		
	}
	// For all together test, assuming valid number entered and returned, since testing over validity
	// has already been tested individually
	public String getMembershipNumber() {
		inputData = membershipGood;
		return inputData;
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

	@Override
	public void removeCard(CardReader reader) {
		reader.remove();
		
	}	
		
	}
	
	class MyAttendantIO implements AttendantIO {
		
		@Override
		public void printDuplicateReceipt() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void changeRemainsNoDenom(BigDecimal bigDecimal) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void notifyWeightDiscrepancyAttendantIO() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void approveWeightDiscrepancy(CustomerIO customerIO) {
			///Calls weightDiscrepancyApproved in the bagging area controller for purchased bag test
			baggingAreaController.weightDiscrepancyApproved();
			
		}

		@Override
		public void checkAddedOwnBags() {
			baggingAreaController.blockSystem();
			this.acceptOwnBags();
			
		}

		// For all together tests, assume that attendant verifies it is customer own bag and approves
		@Override
		public void acceptOwnBags() {
			baggingAreaController.unblockSystem();
			
		}

		@Override
		public void acknowledgeLowInk() {
			// TODO Auto-generated method stub
		}

		@Override
		public void acknowledgeLowPaper() {
			// TODO Auto-generated method stub
		}

		
	}
	
	// Set up a card reader observer stub to use in test cases
		class cardReaderObserverStub implements CardReaderObserver{

			@Override
			public void reactToEnabledEvent(AbstractDevice<? extends AbstractDeviceObserver> device) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void reactToDisabledEvent(AbstractDevice<? extends AbstractDeviceObserver> device) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void reactToCardInsertedEvent(CardReader reader) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void reactToCardRemovedEvent(CardReader reader) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void reactToCardTappedEvent(CardReader reader) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void reactToCardSwipedEvent(CardReader reader) {
				// TODO Auto-generated method stub
				
			}

			// Track if card was successfully inserted, to ensure random chip errors
			// do not cause a test to fail when not being specifically tested
			@Override
			public void reactToCardDataReadEvent(CardReader reader, CardData data) {
				falseNegative = false;
			}
			
		}
	
	@Before
	public void setup() {
		// Setting up new print stream to catch printed output, used to test terminal output
				baos = new ByteArrayOutputStream();
				ps = new PrintStream(baos);
				System.setOut(ps);
				billFive = new Bill(5, Currency.getInstance("CAD"));
				billTen = new Bill(10, Currency.getInstance("CAD"));
				billTwenty = new Bill(20, Currency.getInstance("CAD"));
				billFifty = new Bill(50, Currency.getInstance("CAD"));
				billHundred = new Bill(100, Currency.getInstance("CAD"));
				CCard = new CreditCard("Credit", "123456", "Jeff", "456", "4321", true, true);
				DCard = new DebitCard("Debit", "123456", "Jeff", "456", "4321", true, true);
				creditBank = new CardIssuer("Credit Bank");
				debitBank = new CardIssuer("Debit Bank");
				selfCheckoutStation = new SelfCheckoutStation(Currency.getInstance("CAD"), new int[] {5,10,20,50,100}, 
						new BigDecimal[] {new BigDecimal(1),new BigDecimal(2)}, 10000, 5);
				customer = new MyCustomerIO();
				attendant = new MyAttendantIO();
				connection = new MyConnectionIO();
				falseNegative = true;
				ejectedBills = new ArrayList<Integer>();		
				/* Load one hundred, $5, $10, $20, $50 bills into the dispensers so we can dispense change during tests.
				 * Every dispenser has a max capacity of 100 
				 */
				fiveDollarBills = new Bill[100];
				tenDollarBills = new Bill[100];
				twentyDollarBills = new Bill[100];
				fiftyDollarBills = new Bill[100];
				for(int i = 0; i < 100; i++) {
					fiveDollarBills[i] = billFive;
					tenDollarBills[i] = billTen;
					twentyDollarBills[i] = billTwenty;
					fiftyDollarBills[i] = billFifty;
				}
				try {
					selfCheckoutStation.billDispensers.get(5).load(fiveDollarBills);
					selfCheckoutStation.billDispensers.get(10).load(tenDollarBills);
					selfCheckoutStation.billDispensers.get(20).load(twentyDollarBills);
					selfCheckoutStation.billDispensers.get(50).load(fiftyDollarBills);
					selfCheckoutStation.printer.addInk(1024);
					selfCheckoutStation.printer.addPaper(1024);
				} catch (SimulationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (OverloadException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				// Create barcodes:
				barcode1 = new Barcode(Numeral.three, Numeral.zero, Numeral.one, Numeral.five, Numeral.nine, Numeral.nine, Numeral.two, Numeral.seven);
				barcode2 = new Barcode(Numeral.seven, Numeral.nine, Numeral.eight, Numeral.five, Numeral.six, Numeral.eight, Numeral.seven, Numeral.three);
				barcode3 = new Barcode(Numeral.six, Numeral.six, Numeral.eight, Numeral.zero, Numeral.two, Numeral.eight, Numeral.six, Numeral.nine);
				barcode4 = new Barcode(Numeral.seven, Numeral.five, Numeral.two, Numeral.one, Numeral.seven, Numeral.two, Numeral.two, Numeral.four);
				// Create scanned items:
				scannedItem1 = new BarcodedUnit(barcode1, 12);
				scannedItem2 = new BarcodedUnit(barcode2, 48);
				scannedItem3 = new BarcodedUnit(barcode3, 20);
				scannedItem4 = new BarcodedUnit(barcode4, 83);
				// Create placed items:
				placedItem1 = scannedItem1;
				placedItem2 = scannedItem2;
				placedItem3 = scannedItem3;
				placedItem4 = scannedItem4;
				// Create test products:
				testProduct1 = new BarcodedProduct(barcode1, "Item 1", new BigDecimal(10.0), 12);
				testProduct2 = new BarcodedProduct(barcode2, "Item 2", new BigDecimal(68.0), 48);
				testProduct3 = new BarcodedProduct(barcode3, "Item 3", new BigDecimal(50.0), 20);
				double price4 = 23.0;
				testProduct4 = new BarcodedProduct(barcode4, "Item 4", new BigDecimal(25.0), 83);
							
				ProductDatabases.BARCODED_PRODUCT_DATABASE.put(barcode1, testProduct1);
				ProductDatabases.BARCODED_PRODUCT_DATABASE.put(barcode2, testProduct2);
				ProductDatabases.BARCODED_PRODUCT_DATABASE.put(barcode3, testProduct3);
				ProductDatabases.BARCODED_PRODUCT_DATABASE.put(barcode4, testProduct4);
				
				// Create and attach controllers to the station:
				this.receiptPrinterController = new PrintReceipt(selfCheckoutStation, selfCheckoutStation.printer, customer, attendant);
				this.paymentController = new PaymentControllerLogic(selfCheckoutStation, customer, attendant, connection, receiptPrinterController);
				this.baggingAreaController = new BaggingAreaController(selfCheckoutStation, customer, attendant, paymentController);
				this.addItemByScanningController = new AddItemByScanningController(selfCheckoutStation, customer, attendant, paymentController, baggingAreaController);
				
				int validMemberLength = 10;
				char[] validChars = {'1', '2', '3', '4', '5', '6', '7', '8', '9', '0'};
				this.membershipController = new MembershipNumberController(validChars, validMemberLength, customer);
				membershipGood = "1231231231"; // Good memberships only contain digits
				receiptPrinterController.setContents(1024, 1024);
				selfCheckoutStation.cardReader.register(new cardReaderObserverStub());
				paymentController.setBanks(creditBank, debitBank);
				calendar = Calendar.getInstance();
				// Expiry data is 3 years from now
				calendar.add(Calendar.YEAR, 3);
				BigDecimal creditLimit = BigDecimal.valueOf(1000);
				creditBank.addCardData("123456", "Jeff", calendar,"456", creditLimit);
				BigDecimal debitLimit = BigDecimal.valueOf(1000);
				debitBank.addCardData("123456", "Jeff", calendar,"456", debitLimit);
				connectionTries = 0;
				connectFirstTime = false;
				neverConnect = false;

	}
	/* 
	 * Test Case: The customer scans two items. 
	 * 
	 * Description: This test is to see if scanning an item updates the cart total for the 
	 * payment controller.
	 * 
	 * Expected Result: Before the scan, the cart total should be 0. After scanning scannedItem1,
	 * it should up date to 10 dollars. Then, after bagging the item they scanned, they will then
	 * scan scannedItem2. The final cart total expected should be 78 dollars.
	 */
	@Test
	public void updateCartTotal() {
		assertTrue(BigDecimal.ZERO.compareTo(paymentController.getCartTotal())==0);
		// scan first item
		customer.scanItem(scannedItem1);
		customer.placeScannedItemInBaggingArea(placedItem1);
		// scan second item
		customer.scanItem(scannedItem2);
		customer.placeScannedItemInBaggingArea(placedItem2);
		assertTrue(BigDecimal.valueOf(78).compareTo(paymentController.getCartTotal())==0);
	}
	
	/* 
	 * Test Case: The customer pays the cart total
	 */
	@Test
	public void payForCartTotal() {
		// scan item
		customer.scanItem(scannedItem3);
		customer.placeScannedItemInBaggingArea(placedItem3);
		customer.selectPaymentMethod("Cash", paymentController);
		// The customer inserts a one-hundred dollar bill
		try {
			this.selfCheckoutStation.billInput.accept(billFifty);
		} catch (Exception e) {fail();}
		assertEquals(	  
				  "Item 3      $50\n"
				+ "Total: $50.00\n"
				+ "Paid: $50.0\n\n"
				+ "Change: $0.0\n",
				selfCheckoutStation.printer.removeReceipt());
	}
	
	/* 
	 * Test Case: The customer pays the cart total
	 * with change
	 */
	@Test
	public void payForCartTotalWithChange() {
		customer.selectPaymentMethod("Cash", paymentController);
		// scan first item
		customer.scanItem(scannedItem1);
		customer.placeScannedItemInBaggingArea(placedItem1);
		// scan second item
		customer.scanItem(scannedItem3);
		customer.placeScannedItemInBaggingArea(placedItem3);
		// The customer inserts a one-hundred dollar bill
		try {
			this.selfCheckoutStation.billInput.accept(billHundred);
		} catch (Exception e) {fail();}
		assertEquals(	  
				  "Item 1      $10\n"
				+ "Item 3      $50\n"
				+ "Total: $60.00\n"
				+ "Paid: $100.0\n\n"
				+ "Change: $40.0\n",
				selfCheckoutStation.printer.removeReceipt());
	}
	
	/* 
	 * Test Case: The customer pays the cart total in cash, but not all at once.
	 * 
	 * Description: The user adds an item, then pays, then adds another item and pays once more.
	 * This was specified as necessary as per the discussion board.
	 */
	@Test
	public void addItemPayCashAddItemPayCash() {
		// scan first item
		customer.scanItem(scannedItem4);
		customer.placeScannedItemInBaggingArea(placedItem4);
		customer.selectPaymentMethod("Cash", paymentController);
		try {
			this.selfCheckoutStation.billInput.accept(billTwenty);
		} catch (Exception e) {fail();}
		// scan second item
		customer.scanItem(scannedItem1);
		customer.placeScannedItemInBaggingArea(placedItem1);
		customer.selectPaymentMethod("Cash", paymentController);
		try {
			this.selfCheckoutStation.billInput.accept(billTwenty);
		} catch (Exception e) {fail();}
	assertEquals(	  
				  "Item 4      $25\n"
				+ "Item 1      $10\n"
				+ "Total: $35.00\n"
				+ "Paid: $40.0\n\n"
				+ "Change: $5.0\n",
				selfCheckoutStation.printer.removeReceipt());
	}
	
	/* 
	 * Test Case: The customer pays the cart total in card, but not all at once.
	 * 
	 * Description: The user adds an item, then pays, then adds another item and pays once more.
	 * This was specified as necessary as per the discussion board.
	 */
	@Test
	public void addItemPayCardAddItemPayCard() {
		
		// scan first item
		customer.scanItem(scannedItem4);
		customer.placeScannedItemInBaggingArea(placedItem4);
		customer.selectPaymentMethod("Credit", paymentController);
		customer.setCardPaymentAmount(BigDecimal.valueOf(20.0));
		while(falseNegative) {
			customer.insertCard(CCard, "4321");
		}
		falseNegative = true;
		// scan second item
		customer.scanItem(scannedItem1);
		customer.placeScannedItemInBaggingArea(placedItem1);
		customer.selectPaymentMethod("Credit", paymentController);
		customer.setCardPaymentAmount(BigDecimal.valueOf(20.0));
		while(falseNegative) {
			customer.insertCard(CCard, "4321");
		}
	assertEquals(	  
				  "Item 4      $25\n"
				+ "Item 1      $10\n"
				+ "Total: $35.00\n"
				+ "Paid: $35.0\n\n"
				+ "Change: $0.0\n",
				selfCheckoutStation.printer.removeReceipt());
	}
	
	/* 
	 * Test Case: The customer pays part of the total in card, then adds another item, then pays the rest
	 * in cash.
	 * 
	 * Description: The user adds an item, then pays, then adds another item and pays once more.
	 * This was specified as necessary as per the discussion board.
	 */
	@Test
	public void addItemPayCardAddItemPayCash() {
		
		// scan first item
		customer.scanItem(scannedItem4);
		customer.placeScannedItemInBaggingArea(placedItem4);
		customer.selectPaymentMethod("Credit", paymentController);
		customer.setCardPaymentAmount(BigDecimal.valueOf(20.0));
		while(falseNegative) {
			customer.insertCard(CCard, "4321");
		}
		falseNegative = true;
		// scan second item
		customer.scanItem(scannedItem1);
		customer.placeScannedItemInBaggingArea(placedItem1);
		customer.selectPaymentMethod("Cash", paymentController);
		try {
			this.selfCheckoutStation.billInput.accept(billTwenty);
		} catch (Exception e) {fail();}
	assertEquals(	  
				  "Item 4      $25\n"
				+ "Item 1      $10\n"
				+ "Total: $35.00\n"
				+ "Paid: $40.0\n\n"
				+ "Change: $5.0\n",
				selfCheckoutStation.printer.removeReceipt());
	}
	
	/* 
	 * Test Case: The customer starts by adding membership number, then adds their own bag,
	 * then adds an item and then pays in full with cash, with no change.
	 */
	@Test
	public void memberNumberOwnBagAddItemPayCashNoChange() {
		// Customer starts by entering membership number
		customer.getMembershipNumber();
		// Then customer adds their own bag and attendant approves it
		customer.selectAddOwnBags();
		attendant.checkAddedOwnBags();
		// scan item
		customer.scanItem(scannedItem3);
		customer.placeScannedItemInBaggingArea(placedItem3);
		customer.selectPaymentMethod("Cash", paymentController);
		// The customer inserts a fifty dollar bill
		try {
			this.selfCheckoutStation.billInput.accept(billFifty);
		} catch (Exception e) {fail();}
		assertEquals(	  
				  "Item 3      $50\n"
				+ "Total: $50.00\n"
				+ "Paid: $50.0\n\n"
				+ "Change: $0.0\n",
				selfCheckoutStation.printer.removeReceipt());
	}
	
	/* 
	 * Test Case: The customer starts by purchasing a bag,
	 * then adds an item and then pays in full with cash, with no change.
	 */
	@Test
	public void purchaseBagAddItemPayCard() {
		// Customer purchases a bag and attendant approves it
		customer.signalPurchaseBags(1);
		attendant.approveWeightDiscrepancy(customer);
		// scan item
		customer.scanItem(scannedItem3);
		customer.placeScannedItemInBaggingArea(placedItem3);
		customer.selectPaymentMethod("Credit", paymentController);
		customer.setCardPaymentAmount(BigDecimal.valueOf(60.0));
		while(falseNegative) {
			customer.insertCard(CCard, "4321");
		}
		assertEquals(	
				"Reusable Bag      $4.99\n"
				+  "Item 3      $50\n"
				+ "Total: $54.99\n"
				+ "Paid: $54.99\n\n"
				+ "Change: $0.0\n",
				selfCheckoutStation.printer.removeReceipt());
	}
	
	
	/*
	 * Given that all of the individual controller classes were tested
	 * individually, these tests should be sufficient to ensure that
	 * they work as a whole. No additional tests are needed, as this
	 * test set is only to see that they all work together, which they
	 * happen to do based on the scenarios observed here.
	 */
	
}
