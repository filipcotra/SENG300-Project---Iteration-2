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
import com.autovend.Card;
import com.autovend.CreditCard;
import com.autovend.DebitCard;
import com.autovend.Card.CardData;
import com.autovend.devices.BillSlot;
import com.autovend.devices.CoinTray;
import com.autovend.devices.SelfCheckoutStation;
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
			// TODO Auto-generated method stub
			CCardComplete++;
		}

		@Override
		public void payWithDebitComplete(BigDecimal amountDue) {
			// TODO Auto-generated method stub
			DCardComplete++;
		}
		
		@Override
		public void selectPaymentMethod(String paymentMethod) {
			if (paymentMethod.equals("Cash")) {
				paymentController.enableCashPayment();
			}
			else if(paymentMethod.equals("Credit") || paymentMethod.equals("Debit")) {
				paymentController.enableCardPayment();
			}
			
		}
		@Override
		public void transactionFailure() {
			// TODO Auto-generated method stub
			failedTransaction++;
		}
		@Override
		public void setCardPaymentAmount(BigDecimal amount) {
			paymentController.setCardPaymentAmount(amount);
			
		}
		@Override
		public Card getCustomerCard() {
			// TODO Auto-generated method stub
			return null;
		}
	}
		
	class MyBankIO implements BankIO{
		
		int holdNumber;

		@Override
		public void completeTransaction(int holdNumber) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void blockCard(Card card) {
			// TODO Auto-generated method stub
			BlockedCard++;
		}

		@Override
		public int creditCardTransaction(CardData card, BigDecimal amountPaid) {
			// TODO Auto-generated method stub
			return this.holdNumber = 1;
		}

		@Override
		public int debitCardTransaction(CardData card, BigDecimal amountPaid) {
			// TODO Auto-generated method stub
			return this.holdNumber = 1;
		}

		@Override
		public void releaseHold(CardData data) {
			this.holdNumber = 0;
			
		}

		@Override
		public boolean connectionStatus() {
			// TODO Auto-generated method stub
			return true;
		}
		
	}
	
	class MyAttendantIO implements AttendantIO {

		@Override
		public boolean approveWeightDiscrepancy() {
			// TODO Auto-generated method stub
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
		customer.selectPaymentMethod("Credit");
		customer.setCardPaymentAmount(BigDecimal.valueOf(20.0));
		paymentController.setCartTotal(BigDecimal.valueOf(20.0));
		selfCheckoutStation.cardReader.insert(CCard, "4321");
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
	public void payDeditCard() throws IOException {
		customer.selectPaymentMethod("Debit");
		customer.setCardPaymentAmount(BigDecimal.valueOf(20.0));
		paymentController.setCartTotal(BigDecimal.valueOf(20.0));
		selfCheckoutStation.cardReader.insert(DCard, "4321");
		assertEquals(DCardComplete, 1);
		assertTrue(BigDecimal.ZERO.compareTo(paymentController.getCartTotal()) == 0);
		assertEquals("20.0",paymentController.getAmountPaid());
	}
}
