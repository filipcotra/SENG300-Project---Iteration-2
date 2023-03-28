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

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Currency;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.autovend.BarcodedUnit;
import com.autovend.BlockedCardException;
import com.autovend.Card;
import com.autovend.CreditCard;
import com.autovend.DebitCard;
import com.autovend.Card.CardData;
import com.autovend.devices.AbstractDevice;
import com.autovend.devices.BillSlot;
import com.autovend.devices.CardReader;
import com.autovend.devices.CoinTray;
import com.autovend.devices.DisabledException;
import com.autovend.devices.SelfCheckoutStation;
import com.autovend.devices.observers.AbstractDeviceObserver;
import com.autovend.devices.observers.CardReaderObserver;
import com.autovend.software.AttendantIO;
import com.autovend.software.BankIO;
import com.autovend.software.CustomerIO;
import com.autovend.software.PaymentControllerLogic;
import com.autovend.software.PrintReceipt;
import com.autovend.software.test.PaymentWithCashTest.MyAttendantIO;
import com.autovend.software.test.PaymentWithCashTest.MyCustomerIO;

public class payWithCardTest {
	
	// Create variables to be used
	int CCardComplete = 0;
	int DCardComplete = 0;
	int BlockedCard = 0;
	int failedTransaction = 0;
	SelfCheckoutStation selfCheckoutStation;
	CreditCard CCard;
	DebitCard DCard;
	MyCustomerIO customer;
	MyAttendantIO attendant;
	PaymentControllerLogic paymentController;
	PrintReceipt receiptPrinterController;
	MyBankIO bank;
	boolean connectionStatus;
	boolean falseNegative = true;
	String paymentMethodSelected;
	
	// Set up a customerIO stub to use in test cases
	class MyCustomerIO implements CustomerIO {
		@Override
		public void thankCustomer() {
			// TODO Auto-generated method stub
			
		}
		
		// Allow customer to remove bill from slot
		@Override
		public void removeBill(BillSlot slot) {
			slot.removeDanglingBill();
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

		// Allow customer to remove coins from tray
		@Override
		public void removeCoin(CoinTray tray) {
			tray.collectCoins();
			
		}

		// Confirm credit payment has been completed
		@Override
		public void payWithCreditComplete(BigDecimal amountDue) {
			CCardComplete++;
		}

		// Confirm debit payment has been completed
		@Override
		public void payWithDebitComplete(BigDecimal amountDue) {
			DCardComplete++;
		}
		
		// Allow customer to choose their choice of payment method, enabling the required
		// hardware components depending on the selected method
		@Override
		public void selectPaymentMethod(String paymentMethod, PaymentControllerLogic instance) {
			if (paymentMethod.equals("Cash")) {
				instance.enableCashPayment();
			}
			else if(paymentMethod.equals("Credit") || paymentMethod.equals("Debit")) {
				instance.enableCardPayment(paymentMethod);
			}
			paymentMethodSelected = paymentMethod;
		}
		
		// Confirm if transaction failed
		@Override
		public void transactionFailure() {
			failedTransaction++;
		}
		
		// Allow customer to choose amount to pay with card
		@Override
		public void setCardPaymentAmount(BigDecimal amount) {
			paymentController.setCardPaymentAmount(amount);
			
		}
		
		// Allow customer to insert card into card reader, catching exceptions
		// if the card should be blocked, or if the card reader is disabled
		@Override
		public void insertCard(Card card, String pin) {
			try {
				selfCheckoutStation.cardReader.insert(card, pin);
			}
			catch(BlockedCardException e) {
				paymentController.blockCardAtBank(card);
			}
			catch(DisabledException e) {
				throw new DisabledException();
			}
			catch(Exception e) {
			}
		}

		@Override
		public void notifyWeightDiscrepancyCustomerIO() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void notifyWeightDiscrepancyApprovedCustomerIO() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void signalPurchaseBags(int quantity) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void signalFinishedPurchasingBags() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void signalReadyForInteraction() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void signalPutPurchasedBagsOnBaggingArea() {
			// TODO Auto-generated method stub
			
		}

		@Override
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

		@Override
		public boolean selectAddOwnBags() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public void indicateToCustomerToContinueAfterAttendantApproveOrDenyAddedBags() {
			// TODO Auto-generated method stub
			
		}
	}
		
	// Set up bankIO stub to use in test cases
	class MyBankIO implements BankIO{
		
		// hold number passed to and from bank before releasing funds on authorized transactions
		public int holdNumber;

		@Override
		public void completeTransaction(int holdNumber) {
			// TODO Auto-generated method stub
			
		}

		// Track if card is to be blocked
		@Override
		public void blockCard(Card card) {
			BlockedCard++;
		}

		// Set test hold number to indicate that there is a hold for credit payment
		@Override
		public int creditCardTransaction(CardData card, BigDecimal amountPaid) {
			return this.holdNumber = 1;
		}

		// Set test hold number to indicate that there is a hold for debit payment
		@Override
		public int debitCardTransaction(CardData card, BigDecimal amountPaid) {
			return this.holdNumber = 1;
		}

		// Set test hold number to indicate that the hold has been released
		@Override
		public void releaseHold(CardData data) {
			this.holdNumber = 0;
			
		}

		// Confirm if the connection to the bank has been successfully made
		@Override
		public boolean connectionStatus() {
			return connectionStatus;
		}
		
	}
	
	// Set up attendantIO stub to use in test cases
	class MyAttendantIO implements AttendantIO {

		@Override
		public void printDuplicateReceipt() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void changeRemainsNoDenom(BigDecimal changeLeft) {
			
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

/* ---------------------------------- SetUp ---------------------------------------------------*/	
	
	// Set up before each test case
	@Before
	public void setUp() {
		// Initialize an instance of our test classes to be used
		selfCheckoutStation = new SelfCheckoutStation(Currency.getInstance("CAD"), new int[] {5,10,20,50}, 
				new BigDecimal[] {new BigDecimal("0.05"),new BigDecimal("0.10"), new BigDecimal("0.25"),
						new BigDecimal("1.00"), new BigDecimal("2.00")}, 10000, 5);
		CCard = new CreditCard("Credit", "123456", "Jeff", "456", "4321", true, true);
		DCard = new DebitCard("Debit", "123456", "Jeff", "456", "4321", true, true);
		customer = new MyCustomerIO();
		attendant = new MyAttendantIO();
		bank = new MyBankIO();
		receiptPrinterController = new PrintReceipt(selfCheckoutStation, selfCheckoutStation.printer, customer, attendant);
		paymentController = new PaymentControllerLogic(selfCheckoutStation, customer, attendant, bank, receiptPrinterController);
		paymentController.setCartTotal(BigDecimal.ZERO);
		// Turn on connection to bank to start
		connectionStatus = true;
		// Loop variable to ensure that random chip failures do not interfere with testing
		falseNegative = true;
		selfCheckoutStation.cardReader.register(new cardReaderObserverStub());
	}
	
	// Tear down after each test case
	@After
	public void teardown() {
		selfCheckoutStation = null;
		CCard = null;
		DCard = null;
		receiptPrinterController = null;
		paymentController = null;
	}
	
/* ---------------------------------- Tests ---------------------------------------------------*/	
	
	/**
	 * Test to see if a successful credit card payment can be made. Expected that
	 * no exceptions will occur, and payment information will be correctly updated.
	 * 
	 * @throws IOException
	 */
	@Test
	public void payCreditCard() throws IOException {
		customer.selectPaymentMethod("Credit", paymentController);
		customer.setCardPaymentAmount(BigDecimal.valueOf(20.0));
		paymentController.setCartTotal(BigDecimal.valueOf(20.0));
		// Loop until a successful insertion event occurs, to avoid random failures from chip errors
		while(falseNegative) {
			customer.insertCard(CCard, "4321");
		}
		assertEquals(1, CCardComplete);
		assertTrue(BigDecimal.ZERO.compareTo(paymentController.getCartTotal()) == 0);
		assertEquals("20.0",paymentController.getAmountPaid());
	}
	
	/**
	 * Test to see if a successful debit card payment can be made. Expected that
	 * no exceptions will occur, and payment information will be correctly updated.
	 * 
	 * @throws IOException
	 */
	@Test
	public void payDebitCard() throws IOException {
		customer.selectPaymentMethod("Debit", paymentController);
		customer.setCardPaymentAmount(BigDecimal.valueOf(20.0));
		paymentController.setCartTotal(BigDecimal.valueOf(20.0));
		// Loop until a successful insertion event occurs, to avoid random failures from chip errors
		while(falseNegative) {
			customer.insertCard(DCard, "4321");
		}
		assertEquals(1, DCardComplete);
		assertTrue(BigDecimal.ZERO.compareTo(paymentController.getCartTotal()) == 0);
		assertEquals("20.0",paymentController.getAmountPaid());
	}
	
	/**
	 * Test to see if a connection failure from the beginning will cause a transaction
	 * failure. This is testing for exception 2. This test is for debit payment.
	 * 
	 * @throws IOException
	 */
	@Test
	public void failedConnectionExc2_Debit() throws IOException {
		// Turn off connection to bank to start
		connectionStatus = false;
		customer.selectPaymentMethod("Debit", paymentController);
		customer.setCardPaymentAmount(BigDecimal.valueOf(20.0));
		paymentController.setCartTotal(BigDecimal.valueOf(20.0));
		// Loop until a successful insertion event occurs, to avoid random failures from chip errors
		while(falseNegative) {
			customer.insertCard(DCard, "4321");
		}
		assertEquals(1, failedTransaction);
	}
	
	/**
	 * Test to see if a connection failure from the beginning will cause a transaction
	 * failure. This is testing for exception 2. This test is for credit payment.
	 * 
	 * @throws IOException
	 */
	@Test
	public void failedConnectionExc2_Credit() throws IOException {
		connectionStatus = false;
		customer.selectPaymentMethod("Credit", paymentController);
		customer.setCardPaymentAmount(BigDecimal.valueOf(20.0));
		paymentController.setCartTotal(BigDecimal.valueOf(20.0));
		// Loop until a successful insertion event occurs, to avoid random failures from chip errors
		while(falseNegative) {
			customer.insertCard(CCard, "4321");
		}
		assertEquals(1, failedTransaction);
	}
	
	/**
	 * Test to see if a unauthorized holdNumber will cause a failed transaction. Expecting
	 * to see this. This test is for credit exception 1.
	 * 
	 * @throws IOException
	 */
	@Test
	public void unauthorizedExc1_Credit() throws IOException {
		class MyBankIOStub implements BankIO{
			
			public int holdNumber;

			@Override
			public void completeTransaction(int holdNumber) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void blockCard(Card card) {
				BlockedCard++;
			}

			@Override
			public int creditCardTransaction(CardData card, BigDecimal amountPaid) {
				return this.holdNumber = 0; // To fail it
			}

			@Override
			public int debitCardTransaction(CardData card, BigDecimal amountPaid) {
				return this.holdNumber = 1;
			}

			@Override
			public void releaseHold(CardData data) {
				this.holdNumber = 0;
				
			}

			@Override
			public boolean connectionStatus() {
				return connectionStatus;
			}
			
		}
		MyBankIOStub stubBank = new MyBankIOStub();
		// Replacing everything important to start fresh with this new stub
		CustomerIO customer2 = new MyCustomerIO();
		AttendantIO attendant2 = new MyAttendantIO();
		SelfCheckoutStation selfCheckoutStation2 = new SelfCheckoutStation(Currency.getInstance("CAD"), new int[] {5,10,20,50}, 
				new BigDecimal[] {new BigDecimal("0.05"),new BigDecimal("0.10"), new BigDecimal("0.25"),
						new BigDecimal("1.00"), new BigDecimal("2.00")}, 10000, 5);
		PaymentControllerLogic paymentController2 = new PaymentControllerLogic(selfCheckoutStation2, customer2, attendant2, stubBank, receiptPrinterController);
		customer2.selectPaymentMethod("Credit", paymentController2);
		customer2.setCardPaymentAmount(BigDecimal.valueOf(20.0));
		paymentController2.setCartTotal(BigDecimal.valueOf(20.0));
		selfCheckoutStation2.cardReader.register(new cardReaderObserverStub());
		// Loop until a successful insertion event occurs, to avoid random failures from chip errors
		while(falseNegative) {
			selfCheckoutStation2.cardReader.insert(CCard, "4321");
		}
		assertEquals(1, failedTransaction);
	}
	
	/**
	 * Test to see if a unauthorized holdNumber will cause a failed transaction. Expecting
	 * to see this. This test is for debit exception 1.
	 * 
	 * @throws IOException
	 */
	@Test
	public void unauthorizedExc1_Debit() throws IOException {
		class MyBankIOStub implements BankIO{
			
			public int holdNumber;

			@Override
			public void completeTransaction(int holdNumber) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void blockCard(Card card) {
				BlockedCard++;
			}

			@Override
			public int creditCardTransaction(CardData card, BigDecimal amountPaid) {
				return this.holdNumber = 1;
			}

			@Override
			public int debitCardTransaction(CardData card, BigDecimal amountPaid) {
				return this.holdNumber = 0; // To fail it
			}

			@Override
			public void releaseHold(CardData data) {
				this.holdNumber = 0;
				
			}

			@Override
			public boolean connectionStatus() {
				return connectionStatus;
			}
			
		}
		MyBankIOStub stubBank = new MyBankIOStub();
		// Replacing everything important to start fresh with this new stub
		CustomerIO customer2 = new MyCustomerIO();
		AttendantIO attendant2 = new MyAttendantIO();
		SelfCheckoutStation selfCheckoutStation2 = new SelfCheckoutStation(Currency.getInstance("CAD"), new int[] {5,10,20,50}, 
				new BigDecimal[] {new BigDecimal("0.05"),new BigDecimal("0.10"), new BigDecimal("0.25"),
						new BigDecimal("1.00"), new BigDecimal("2.00")}, 10000, 5);
		PaymentControllerLogic paymentController2 = new PaymentControllerLogic(selfCheckoutStation2, customer2, attendant2, stubBank, receiptPrinterController);
		customer2.selectPaymentMethod("Debit", paymentController2);
		customer2.setCardPaymentAmount(BigDecimal.valueOf(20.0));
		paymentController2.setCartTotal(BigDecimal.valueOf(20.0));
		selfCheckoutStation2.cardReader.register(new cardReaderObserverStub());
		// Loop until a successful insertion event occurs, to avoid random failures from chip errors
		while(falseNegative) {
			selfCheckoutStation2.cardReader.insert(DCard, "4321");
		}
		assertEquals(1, failedTransaction);
	}
	
	/**
	 * Test to see if a connection error occurs after holding, should result in the hold
	 * being released. This is the test for credit exception 3. Should result in no 
	 * transaction being completed, but the connection should be attempted 5 times.
	 * 
	 * @throws IOException
	 */
	@Test
	public void connectionErrorCredit_Exc3() throws IOException {
		class MyBankIOStub implements BankIO{
			public int times = 0;
			public int holdNumber;

			@Override
			public void completeTransaction(int holdNumber) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void blockCard(Card card) {
				BlockedCard++;
			}

			@Override
			public int creditCardTransaction(CardData card, BigDecimal amountPaid) {
				return this.holdNumber = 1;
			}

			@Override
			public int debitCardTransaction(CardData card, BigDecimal amountPaid) {
				return this.holdNumber = 1;
			}

			@Override
			public void releaseHold(CardData data) {
				this.holdNumber = 0;
				
			}

			@Override
			public boolean connectionStatus() {
				// This is making sure that after the first time is called (which should pass), there will be a connection issue.
				if(this.times == 1) {
					connectionStatus = false;
				}
				this.times++;
				return connectionStatus;
			}
			
		}
		MyBankIOStub stubBank = new MyBankIOStub();
		// Replacing everything important to start fresh with this new stub
		CustomerIO customer2 = new MyCustomerIO();
		AttendantIO attendant2 = new MyAttendantIO();
		SelfCheckoutStation selfCheckoutStation2 = new SelfCheckoutStation(Currency.getInstance("CAD"), new int[] {5,10,20,50}, 
				new BigDecimal[] {new BigDecimal("0.05"),new BigDecimal("0.10"), new BigDecimal("0.25"),
						new BigDecimal("1.00"), new BigDecimal("2.00")}, 10000, 5);
		PaymentControllerLogic paymentController2 = new PaymentControllerLogic(selfCheckoutStation2, customer2, attendant2, stubBank, receiptPrinterController);
		customer2.selectPaymentMethod("Credit", paymentController2);
		customer2.setCardPaymentAmount(BigDecimal.valueOf(20.0));
		paymentController2.setCartTotal(BigDecimal.valueOf(20.0));
		selfCheckoutStation2.cardReader.register(new cardReaderObserverStub());
		// Loop until a successful insertion event occurs, to avoid random failures from chip errors
		while(falseNegative) {
			selfCheckoutStation2.cardReader.insert(CCard, "4321");
		}
		assertEquals(0, CCardComplete);
		// Checking to see if the connection was attempted a total of 6 times.
		// This includes 1 check where it will return true, and then 5 where
		// it will return false.
		assertEquals(6, stubBank.times);
	}
	
	/**
	 * Test to see if a connection error occurs after holding, should result in the hold
	 * being released. This is the test for debit exception 3. Should result in no 
	 * transaction being completed, but the connection should be attempted 5 times.
	 * 
	 * @throws IOException
	 */
	@Test
	public void connectionErrorDebit_Exc3() throws IOException {
		class MyBankIOStub implements BankIO{
			public int times = 0;
			public int holdNumber;

			@Override
			public void completeTransaction(int holdNumber) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void blockCard(Card card) {
				BlockedCard++;
			}

			@Override
			public int creditCardTransaction(CardData card, BigDecimal amountPaid) {
				return this.holdNumber = 1;
			}

			@Override
			public int debitCardTransaction(CardData card, BigDecimal amountPaid) {
				return this.holdNumber = 1;
			}

			@Override
			public void releaseHold(CardData data) {
				this.holdNumber = 0;
				
			}

			@Override
			public boolean connectionStatus() {
				// This is making sure that after the first time is called (which should pass), there will be a connection issue.
				if(this.times == 1) {
					connectionStatus = false;
				}
				this.times++;
				return connectionStatus;
			}
			
		}
		MyBankIOStub stubBank = new MyBankIOStub();
		// Replacing everything important to start fresh with this new stub
		CustomerIO customer2 = new MyCustomerIO();
		AttendantIO attendant2 = new MyAttendantIO();
		SelfCheckoutStation selfCheckoutStation2 = new SelfCheckoutStation(Currency.getInstance("CAD"), new int[] {5,10,20,50}, 
				new BigDecimal[] {new BigDecimal("0.05"),new BigDecimal("0.10"), new BigDecimal("0.25"),
						new BigDecimal("1.00"), new BigDecimal("2.00")}, 10000, 5);
		PaymentControllerLogic paymentController2 = new PaymentControllerLogic(selfCheckoutStation2, customer2, attendant2, stubBank, receiptPrinterController);
		customer2.selectPaymentMethod("Debit", paymentController2);
		customer2.setCardPaymentAmount(BigDecimal.valueOf(20.0));
		paymentController2.setCartTotal(BigDecimal.valueOf(20.0));
		selfCheckoutStation2.cardReader.register(new cardReaderObserverStub());
		// Loop until a successful insertion event occurs, to avoid random failures from chip errors
		while(falseNegative) {
			selfCheckoutStation2.cardReader.insert(DCard, "4321");
		}
		assertEquals(0, DCardComplete);
		// Checking to see if the connection was attempted a total of 6 times.
		// This includes 1 check where it will return true, and then 5 where
		// it will return false.
		assertEquals(6, stubBank.times);
	}
	
	/**
	 * Testing if the customer enters in the wrong pin. If this happens 4 times, the card
	 * should be blocked, but not before.
	 */
	@Test
	public void wrongPinFourTimes() {
		customer.selectPaymentMethod("Credit", paymentController);
		customer.setCardPaymentAmount(BigDecimal.valueOf(20.0));
		paymentController.setCartTotal(BigDecimal.valueOf(20.0));
		for(int i = 0 ; i < 4 ; i++) {
			customer.insertCard(CCard,  "1");
			selfCheckoutStation.cardReader.remove();
		}
		assertEquals(1, this.BlockedCard);
	}
	
	/**
	 * Testing that if credit is selected, debit payment will not occur. Cart total should not
	 * change at all.
	 */
	@Test
	public void selectCreditInsertDebit() {
		customer.selectPaymentMethod("Credit", paymentController);
		customer.setCardPaymentAmount(BigDecimal.valueOf(20.0));
		paymentController.setCartTotal(BigDecimal.valueOf(20.0));
		// Loop until a successful insertion event occurs, to avoid random failures from chip errors
		while(falseNegative) {
			customer.insertCard(DCard, "4321");
		}
		assertEquals(0, DCardComplete);
		assertEquals(0, CCardComplete);
		assertEquals(BigDecimal.valueOf(20.0), paymentController.getCartTotal());
		assertEquals("0.0",paymentController.getAmountPaid());
	}
	
	/**
	 * Testing that if debit is selected, credit payment will not occur. Cart total should not 
	 * change at all.
	 */
	@Test
	public void selectDebitInsertCredit() {
		customer.selectPaymentMethod("Debit", paymentController);
		customer.setCardPaymentAmount(BigDecimal.valueOf(20.0));
		paymentController.setCartTotal(BigDecimal.valueOf(20.0));
		// Loop until a successful insertion event occurs, to avoid random failures from chip errors
		while(falseNegative) {
			customer.insertCard(CCard, "4321");
		}
		assertEquals(0, DCardComplete);
		assertEquals(0, CCardComplete);
		assertEquals(BigDecimal.valueOf(20.0), paymentController.getCartTotal());
		assertEquals("0.0",paymentController.getAmountPaid());
	}
	
	/**
	 * Testing that if cash payment is selected, disabled exception will be thrown.
	 * 
	 * BUG: Despite being disabled, the cardReader is still working. This does not seem
	 * right. Seems to be a hardware bug.
	 */
	@Test (expected = DisabledException.class)
	public void selectCashInsertCard() {
		customer.selectPaymentMethod("Cash", paymentController);
		customer.setCardPaymentAmount(BigDecimal.valueOf(20.0));
		paymentController.setCartTotal(BigDecimal.valueOf(20.0));
		customer.insertCard(DCard, "4321");
	}
	
	/**
	 * Testing if the customer enters in the wrong pin, but the bank connection is faulty. 
	 * If this happens 4 times, the card should be blocked normally, but expected not to
	 * be since the bank was attempted to be reached more than 10 times to issue the block
	 * unsuccessfully.
	 */
	@Test
	public void wrongPinFourTimesNoBankConnnection() {
		customer.selectPaymentMethod("Credit", paymentController);
		connectionStatus = false;
		for(int i = 0 ; i < 4 ; i++) {
			customer.insertCard(CCard,  "1");
			selfCheckoutStation.cardReader.remove();
		}
		assertEquals(0, this.BlockedCard);
	}
	
	/**
	 * Test to see if a connection error occurs after holding, should result in the hold
	 * being released. This is the test for debit exception 3. Should result in no 
	 * transaction being completed, but the connection should be attempted 5 times.
	 * 
	 * @throws IOException
	 */
	@Test
	public void wrongPinFourTimesBankConnnectionRestoredMidwayThrough() throws IOException {
		// Create new bankIO stub that will start without connection, but regain connection midway
		// through the attempts to establish a connection
		class MyBankIOStub implements BankIO{
			public int times = 0;
			public int holdNumber;

			@Override
			public void completeTransaction(int holdNumber) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void blockCard(Card card) {
				BlockedCard++;
			}

			@Override
			public int creditCardTransaction(CardData card, BigDecimal amountPaid) {
				return this.holdNumber = 1;
			}

			@Override
			public int debitCardTransaction(CardData card, BigDecimal amountPaid) {
				return this.holdNumber = 1;
			}

			@Override
			public void releaseHold(CardData data) {
				this.holdNumber = 0;
				
			}

			@Override
			public boolean connectionStatus() {
				// This is making sure that after the fifth time is called, there will no longer be a connection issue.
				if(this.times == 5) {
					connectionStatus = true;
				}
				this.times++;
				return connectionStatus;
			}
			
		}
		
		MyBankIOStub stubBank = new MyBankIOStub();
		// Creating new payment controller using the stub bank
		paymentController = new PaymentControllerLogic(selfCheckoutStation, customer, attendant, stubBank, receiptPrinterController);
		customer.selectPaymentMethod("Credit", paymentController);
		connectionStatus = false;
		// Attempt incorrect pin until it will become blocked
		for(int i = 0 ; i < 4 ; i++) {
			customer.insertCard(CCard,  "1");
			selfCheckoutStation.cardReader.remove();
		}
		// Check that the card did become blocked, and that the bank was contacted 6 times before the connection
		// was successful in order to issue the block
		assertEquals(1, this.BlockedCard);
		assertEquals(6, stubBank.times);
	}
}
