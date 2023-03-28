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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.util.Currency;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.autovend.Barcode;
import com.autovend.BarcodedUnit;
import com.autovend.Card;
import com.autovend.Numeral;
import com.autovend.SellableUnit;
import com.autovend.Card.CardData;
import com.autovend.devices.BillSlot;
import com.autovend.devices.CardReader;
import com.autovend.devices.CoinTray;
import com.autovend.devices.SelfCheckoutStation;
import com.autovend.external.CardIssuer;
import com.autovend.external.ProductDatabases;
import com.autovend.products.BarcodedProduct;
import com.autovend.software.AttendantIO;
import com.autovend.software.BaggingAreaController;
import com.autovend.software.ConnectionIO;
import com.autovend.software.CustomerIO;
import com.autovend.software.PaymentControllerLogic;
import com.autovend.software.PrintReceipt;

/**
 * This test class is temporary and should be merged into a single BaggingAreaController test class much like the related controller software classes
 *
 */
public class WeightDiscrepancyTest {
	SelfCheckoutStation station;
	MyAttendantIO attendantIO;
	MyCustomerIO customerIO;
	PaymentControllerLogic paymentController;
	PrintReceipt printerController;
	BaggingAreaController baggingAreaController;
	public int scaleMaximumWeight;
	public int scaleSensitivity;
	public BarcodedProduct marsBar;
	public Barcode marsBarBarcode;
	public SellableUnit mb;
	public BigDecimal mbPrice;
	MyConnectionIO connection;

	@Before
	public void SetUp() {
		ProductDatabases.BARCODED_PRODUCT_DATABASE.clear();
		scaleMaximumWeight = 999;
		scaleSensitivity = 2;
		this.station = new SelfCheckoutStation(Currency.getInstance("CAD"), new int[] {10,20}, new BigDecimal[] {BigDecimal.ONE}, scaleMaximumWeight, scaleSensitivity);
		this.attendantIO = new MyAttendantIO();
		this.customerIO = new MyCustomerIO();
		this.connection = new MyConnectionIO();
		this.printerController = new PrintReceipt(station, station.printer, customerIO, attendantIO);
		this.paymentController = new PaymentControllerLogic(station, customerIO, attendantIO, connection, printerController);
		this.baggingAreaController = new BaggingAreaController(station, customerIO, attendantIO, paymentController);
		marsBarBarcode = new Barcode(Numeral.zero,Numeral.zero,Numeral.one);
		mbPrice = new BigDecimal(1.25);
		marsBar = new BarcodedProduct(marsBarBarcode,"chocolate",mbPrice,15);
		mb = new BarcodedUnit(marsBarBarcode,15);
		ProductDatabases.BARCODED_PRODUCT_DATABASE.put(marsBarBarcode, marsBar);
		ProductDatabases.INVENTORY.put(marsBar, 5);
	}
	
	@After
	public void tearDown() {

		this.station = null;
		this.attendantIO = null;
		this.customerIO = null;
		this.printerController = null;
		this.paymentController = null;
		this.baggingAreaController = null;
		
	}
	
	@Test
	public void weightDiscrepancyNormalTest() {
		Barcode marsBarWeightedBarcode = new Barcode(Numeral.zero,Numeral.zero,Numeral.one);
		BarcodedUnit mbWeighted = new BarcodedUnit(marsBarWeightedBarcode, 20);
		station.mainScanner.scan(mb);
		station.baggingArea.add(mbWeighted);
		assertTrue(customerIO.customerWeightDiscrepancySignal);
		assertTrue(attendantIO.attendantWeightDiscrepancySignal);
	}
	
	@Test
	public void weightDiscrepancyApproveTest() {
		Barcode marsBarWeightedBarcode = new Barcode(Numeral.zero,Numeral.zero,Numeral.one);
		BarcodedUnit mbWeighted = new BarcodedUnit(marsBarWeightedBarcode, 20);
		station.mainScanner.scan(mb);
		station.baggingArea.add(mbWeighted);
		assertTrue(customerIO.customerWeightDiscrepancySignal);
		assertTrue(attendantIO.attendantWeightDiscrepancySignal);
		attendantIO.approveWeightDiscrepancy(customerIO);
		assertFalse(customerIO.customerWeightDiscrepancySignal);
		assertFalse(attendantIO.attendantWeightDiscrepancySignal);
		assertEquals(mbWeighted.getWeight(), baggingAreaController.expectedWeight, 0.001d);
		assertEquals(mbWeighted.getWeight(), baggingAreaController.actualWeight, 0.001d);
	}
	
	@Test
	public void weightDiscrepancyDueToBagPurchased() {
		
	}
		
	class MyCustomerIO implements CustomerIO {
		
		boolean customerWeightDiscrepancySignal;
		
		boolean purchaseBagsSignal = false;
		int purchaseBagsQuantity = 0;
		boolean finishedPurchasingBagsSignal = false;
		boolean interactionReadySignal = false;
		boolean placePurchasedBagsSignal = false;

		@Override
		public void scanItem(BarcodedUnit item) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void notifyPlaceItemCustomerIO() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void notifyWeightDiscrepancyCustomerIO() {
			// TODO Auto-generated method stub
			this.customerWeightDiscrepancySignal = true;
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
		public void thankCustomer() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void removeBill(BillSlot slot) {
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
		public void signalPurchaseBags(int quantity) {
			// TODO Auto-generated method stub
			this.purchaseBagsSignal = true;
			this.purchaseBagsQuantity = quantity;
			
			baggingAreaController.purchaseBags(quantity);
			
		}

		@Override
		public void signalFinishedPurchasingBags() {
			// TODO Auto-generated method stub
			this.finishedPurchasingBagsSignal = true;
			
			
		}

		@Override
		public void signalReadyForInteraction() {
			// TODO Auto-generated method stub
			this.interactionReadySignal = true;
			
		}

		@Override
		public void signalPutPurchasedBagsOnBaggingArea() {
			// TODO Auto-generated method stub
			this.placePurchasedBagsSignal = true;
			
		}

		@Override
		public void notifyWeightDiscrepancyApprovedCustomerIO() {
			this.customerWeightDiscrepancySignal = false;
			// TODO Auto-generated method stub
			
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
		
		public void removeCoin(CoinTray tray) {
			// TODO Auto-generated method stub
		}
		
		
		public void payWithCreditComplete(BigDecimal amountDue) {
			// TODO Auto-generated method stub
		}
		
		public void payWithDebitComplete(BigDecimal amountDue) {
			// TODO Auto-generated method stub
		}
		
		public void selectPaymentMethod(String paymentMethod, PaymentControllerLogic instance) {
			// TODO Auto-generated method stub
		}
		
		public void transactionFailure() {
			// TODO Auto-generated method stub
		}
		
		public void setCardPaymentAmount(BigDecimal amount) {
			// TODO Auto-generated method stub
		}
		
		public void insertCard(Card card, String pin) {
			// TODO Auto-generated method stub
		}
		
		@Override
		public void removeCard(CardReader reader) {
			// TODO Auto-generated method stub
		}	
		
	}
	
	class MyAttendantIO implements AttendantIO {
		
		boolean attendantWeightDiscrepancySignal;

		@Override
		public void notifyWeightDiscrepancyAttendantIO() {
			// TODO Auto-generated method stub
			this.attendantWeightDiscrepancySignal = true;
			
		}

		@Override
		public void approveWeightDiscrepancy(CustomerIO customerIO) {
			// TODO Auto-generated method stub
			this.attendantWeightDiscrepancySignal = false;
			customerIO.notifyWeightDiscrepancyApprovedCustomerIO();
			baggingAreaController.weightDiscrepancyApproved();
		}

		@Override
		public void changeRemainsNoDenom(BigDecimal changeLeft) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void printDuplicateReceipt() {
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
	
	class MyConnectionIO implements ConnectionIO{

		@Override
		public boolean connectTo(CardIssuer bank) {
			// TODO Auto-generated method stub
			return false;
		}
		
	}
}
	
