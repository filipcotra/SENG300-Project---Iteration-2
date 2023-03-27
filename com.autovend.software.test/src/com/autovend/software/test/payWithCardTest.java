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
	
	class MyCustomerIO implements CustomerIO {
		@Override
		public void thankCustomer() {
			// TODO Auto-generated method stub
			
		}
		
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

		@Override
		public void removeCoin(CoinTray tray) {
			tray.collectCoins();
			
		}

		@Override
		public void payWithCreditComplete(BigDecimal amountDue) {
			CCardComplete++;
		}

		@Override
		public void payWithDebitComplete(BigDecimal amountDue) {
			DCardComplete++;
		}
		
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
		
		@Override
		public void transactionFailure() {
			failedTransaction++;
		}
		@Override
		public void setCardPaymentAmount(BigDecimal amount) {
			paymentController.setCardPaymentAmount(amount);
			
		}
		
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
	}
		
	class MyBankIO implements BankIO{
		
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
			return connectionStatus;
		}
		
	}
	
	class MyAttendantIO implements AttendantIO {

		@Override
		public boolean approveWeightDiscrepancy() {
			return false;
		}

		@Override
		public void printDuplicateReceipt() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void changeRemainsNoDenom(BigDecimal changeLeft) {
			
		}
		
	}
	
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

		@Override
		public void reactToCardDataReadEvent(CardReader reader, CardData data) {
			falseNegative = false;
		}
		
	}

/* ---------------------------------- SetUp ---------------------------------------------------*/	
	
	@Before
	public void setUp() {
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
		connectionStatus = true;
		falseNegative = true;
		selfCheckoutStation.cardReader.register(new cardReaderObserverStub());
	}
	
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
		while(falseNegative) {
			customer.insertCard(CCard, "4321");
		}
		assertEquals(CCardComplete, 1);
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
		while(falseNegative) {
			customer.insertCard(DCard, "4321");
		}
		assertEquals(DCardComplete, 1);
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
		connectionStatus = false;
		customer.selectPaymentMethod("Debit", paymentController);
		customer.setCardPaymentAmount(BigDecimal.valueOf(20.0));
		paymentController.setCartTotal(BigDecimal.valueOf(20.0));
		while(falseNegative) {
			customer.insertCard(DCard, "4321");
		}
		assertEquals(failedTransaction, 1);
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
		while(falseNegative) {
			customer.insertCard(CCard, "4321");
		}
		assertEquals(failedTransaction, 1);
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
		while(falseNegative) {
			selfCheckoutStation2.cardReader.insert(CCard, "4321");
		}
		assertEquals(failedTransaction, 1);
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
		while(falseNegative) {
			selfCheckoutStation2.cardReader.insert(DCard, "4321");
		}
		assertEquals(failedTransaction, 1);
	}
	
	/**
	 * Test to see if a connection error occurs after holding, should result in the hold
	 * being released. This is the test for credit exception 3. Should result in no 
	 * transaction being completed, but the connection should be attempted 5 times.
	 * 
	 * @throws IOException
	 */
//	@Test
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
		while(falseNegative) {
			selfCheckoutStation2.cardReader.insert(CCard, "4321");
		}
		assertEquals(CCardComplete, 0);
		// Checking to see if the connection was attempted a total of 6 times.
		// This includes 1 check where it will return true, and then 5 where
		// it will return false.
		assertEquals(stubBank.times, 6);
	}
	
	/**
	 * Test to see if a connection error occurs after holding, should result in the hold
	 * being released. This is the test for debit exception 3. Should result in no 
	 * transaction being completed, but the connection should be attempted 5 times.
	 * 
	 * @throws IOException
	 */
//	@Test
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
		while(falseNegative) {
			selfCheckoutStation2.cardReader.insert(DCard, "4321");
		}
		assertEquals(DCardComplete, 0);
		// Checking to see if the connection was attempted a total of 6 times.
		// This includes 1 check where it will return true, and then 5 where
		// it will return false.
		assertEquals(stubBank.times, 6);
	}
	
	/**
	 * Testing if the customer enters in the wrong pin. If this happens 4 times, the card
	 * should be blocked, but not before.
	 */
	@Test
	public void wrongPinFourTimes() {
		for(int i = 0 ; i < 4 ; i++) {
			customer.insertCard(CCard,  "1");
			selfCheckoutStation.cardReader.remove();
		}
		assertEquals(this.BlockedCard, 1);
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
		while(falseNegative) {
			customer.insertCard(DCard, "4321");
		}
		assertEquals(DCardComplete, 0);
		assertEquals(CCardComplete, 0);
		assertEquals(paymentController.getCartTotal(), BigDecimal.valueOf(20.0));
		assertEquals("0.0",paymentController.getAmountPaid());
	}
	
	/**
	 * Testing that if debit is selected, credit payment will not occur.
	 */
	@Test
	public void selectDebitInsertCredit() {
		customer.selectPaymentMethod("Debit", paymentController);
		customer.setCardPaymentAmount(BigDecimal.valueOf(20.0));
		paymentController.setCartTotal(BigDecimal.valueOf(20.0));
		while(falseNegative) {
			customer.insertCard(CCard, "4321");
		}
		assertEquals(DCardComplete, 0);
		assertEquals(CCardComplete, 0);
		assertEquals(paymentController.getCartTotal(), BigDecimal.valueOf(20.0));
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
}
