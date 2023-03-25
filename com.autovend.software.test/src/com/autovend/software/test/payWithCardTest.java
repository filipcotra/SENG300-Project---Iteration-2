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
	
	class MyCustomerIO implements CustomerIO {

		String pin;
		public MyCustomerIO(String pin) {
			this.pin = pin;
		}
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
		public String getPin() {
			// TODO Auto-generated method stub
			return pin;
		}
		@Override
		public void selectPaymentMethod(String paymentMethod) {
			if (paymentMethod.equals("Cash")) {
				paymentController.enableCashPayment();
			}
			else if(paymentMethod.equals("Card")) {
				paymentController.enableCardPayment();
			}
			
		}
	}
		
	class myBankIO implements BankIO{

		@Override
		public int creditCardTranscation(Card card, BigDecimal amountPaid) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public int debitCardTranscation(Card card, BigDecimal amountPaid) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public void completeTransaction(int holdNumber) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void blockCard(Card card) {
			// TODO Auto-generated method stub
			BlockedCard++;
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
	
	
	SelfCheckoutStation selfCheckoutStation;
	CreditCard CCard;
	DebitCard DCard;
	MyCustomerIO customer;
	MyAttendantIO attendant;
	PaymentControllerLogic paymentController;
	PrintReceipt receiptPrinterController;
	myBankIO bank;
	@Before
	public void setUp() {
		selfCheckoutStation = new SelfCheckoutStation(Currency.getInstance("CAD"), new int[] {5,10,20,50}, 
				new BigDecimal[] {new BigDecimal("0.05"),new BigDecimal("0.10"), new BigDecimal("0.25"),
						new BigDecimal("1.00"), new BigDecimal("2.00")}, 10000, 5);
		CCard = new CreditCard("debit", "123456", "Jeff", "456", "4321", true, true);
		DCard = new DebitCard("debit", "123456", "Jeff", "456", "4321", true, true);
		customer = new MyCustomerIO("1111");
		attendant = new MyAttendantIO();
		receiptPrinterController = new PrintReceipt(selfCheckoutStation, selfCheckoutStation.printer, customer, attendant);
		paymentController = new PaymentControllerLogic(selfCheckoutStation, customer, attendant, receiptPrinterController);
		paymentController.setCartTotal(BigDecimal.ZERO);
		bank = new myBankIO();
	}
	
	@After
	public void teardown() {
		selfCheckoutStation = null;
		CCard = null;
		DCard = null;
		receiptPrinterController = null;
		paymentController = null;
	}
	
	@Test
	public void payCreditCard() throws IOException {
		customer.selectPaymentMethod("Card");
		paymentController.setCartTotal(new BigDecimal("20"));
		paymentController.payCredit(new BigDecimal("20"), CCard, "4321", bank);
		assertEquals(CCardComplete, 1);
		assertTrue(BigDecimal.ZERO.compareTo(paymentController.getCartTotal()) == 0);
		assertEquals("20.0",paymentController.getAmountPaid());
	}
	
	@Test
	public void payDebitCard() throws IOException {
		customer.selectPaymentMethod("Card");
		paymentController.setCartTotal(new BigDecimal("20"));
		paymentController.payDebit(new BigDecimal("20"), DCard, "4321", bank);
		assertEquals(DCardComplete, 1);
		assertTrue(BigDecimal.ZERO.compareTo(paymentController.getCartTotal()) == 0);
		assertEquals("20.0",paymentController.getAmountPaid());
	}
	
	@Test
	public void payCreditCardWrongPin() throws IOException {
		customer.selectPaymentMethod("Card");
		paymentController.setCartTotal(new BigDecimal("20"));
		paymentController.payCredit(new BigDecimal("20"), CCard, "1111", bank);
		assertEquals(BlockedCard, 1);
		assertEquals("20", paymentController.getCartTotal().toString());
		assertEquals("0.0",paymentController.getAmountPaid());
	}
	
	@Test
	public void payDebitCardWrongPin() throws IOException {
		customer.selectPaymentMethod("Card");
		paymentController.setCartTotal(new BigDecimal("20"));
		paymentController.payDebit(new BigDecimal("20"), DCard, "1111", bank);
		assertEquals(BlockedCard, 1);
		assertEquals("20", paymentController.getCartTotal().toString());
		assertEquals("0.0",paymentController.getAmountPaid());
	}
	
	@Test
	public void payCreditCardWrongPinThenCorrect() throws IOException {
		customer = new MyCustomerIO("4321");
		customer.selectPaymentMethod("Card");
		receiptPrinterController = new PrintReceipt(selfCheckoutStation, selfCheckoutStation.printer, customer, attendant);
		paymentController = new PaymentControllerLogic(selfCheckoutStation, customer, attendant, receiptPrinterController);
		
		paymentController.setCartTotal(new BigDecimal("20"));
		paymentController.payCredit(new BigDecimal("20"), CCard, "1111", bank);
		assertEquals(CCardComplete, 1);
		assertTrue(BigDecimal.ZERO.compareTo(paymentController.getCartTotal()) == 0);
		assertEquals("20.0",paymentController.getAmountPaid());
	}
	
	@Test
	public void payDebitCardWrongPinThenCorrect() throws IOException {
		customer = new MyCustomerIO("4321");
		customer.selectPaymentMethod("Card");
		receiptPrinterController = new PrintReceipt(selfCheckoutStation, selfCheckoutStation.printer, customer, attendant);
		paymentController = new PaymentControllerLogic(selfCheckoutStation, customer, attendant, receiptPrinterController);
		
		paymentController.setCartTotal(new BigDecimal("20"));
		paymentController.payDebit(new BigDecimal("20"), DCard, "1111", bank);
		assertEquals(DCardComplete, 1);
		assertTrue(BigDecimal.ZERO.compareTo(paymentController.getCartTotal()) == 0);
		assertEquals("20.0",paymentController.getAmountPaid());
	}
}
