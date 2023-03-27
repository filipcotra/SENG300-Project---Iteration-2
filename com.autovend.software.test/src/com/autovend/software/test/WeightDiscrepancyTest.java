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
import com.autovend.Numeral;
import com.autovend.SellableUnit;
import com.autovend.devices.BillSlot;
import com.autovend.devices.SelfCheckoutStation;
import com.autovend.external.ProductDatabases;
import com.autovend.products.BarcodedProduct;
import com.autovend.software.AttendantIO;
import com.autovend.software.AttendantIOTempPurchaseOwnBags;
import com.autovend.software.BaggingAreaController;
import com.autovend.software.CustomerIO;
import com.autovend.software.CustomerIOTempPurchaseOwnBags;
import com.autovend.software.PaymentControllerLogic;
import com.autovend.software.PrintReceipt;
import com.autovend.software.WeightDiscrepancyController;

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

	@Before
	public void SetUp() {
		ProductDatabases.BARCODED_PRODUCT_DATABASE.clear();
		scaleMaximumWeight = 999;
		scaleSensitivity = 2;
		this.station = new SelfCheckoutStation(Currency.getInstance("CAD"), new int[] {10,20}, new BigDecimal[] {BigDecimal.ONE}, scaleMaximumWeight, scaleSensitivity);
		this.attendantIO = new MyAttendantIO();
		this.customerIO = new MyCustomerIO();
		this.printerController = new PrintReceipt(station, station.printer, customerIO, attendantIO);
		this.paymentController = new PaymentControllerLogic(station, customerIO, attendantIO, printerController);
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
	}
		
	class MyCustomerIO implements CustomerIO {
		
		boolean customerWeightDiscrepancySignal;

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
		public void notifyWeightDiscrepancyApprovedCustomerIO() {
			this.customerWeightDiscrepancySignal = false;
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
		
	}
}
	
