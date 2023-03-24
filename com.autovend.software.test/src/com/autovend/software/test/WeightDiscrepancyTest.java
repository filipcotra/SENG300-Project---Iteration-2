package com.autovend.software.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.util.Currency;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.autovend.BarcodedUnit;
import com.autovend.devices.BillSlot;
import com.autovend.devices.SelfCheckoutStation;
import com.autovend.software.AttendantIO;
import com.autovend.software.AttendantIOTempPurchaseOwnBags;
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
	WeightDiscrepancyController baggingAreaController;

	@Before
	public void SetUp() {
		this.station = new SelfCheckoutStation(Currency.getInstance("CAD"), new int[] {10,20}, new BigDecimal[] {BigDecimal.ONE}, 999, 1);
		this.attendantIO = new MyAttendantIO();
		this.customerIO = new MyCustomerIO();
		this.printerController = new PrintReceipt(station, station.printer, customerIO, attendantIO);
		this.paymentController = new PaymentControllerLogic(station, customerIO, attendantIO, printerController);
		this.baggingAreaController = new WeightDiscrepancyController(station, customerIO, attendantIO, paymentController);
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
		
	class MyCustomerIO implements CustomerIO {

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
		
	}
	
	class MyAttendantIO implements AttendantIO {

		@Override
		public void notifyWeightDiscrepancyAttendantIO() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public boolean approveWeightDiscrepancy() {
			// TODO Auto-generated method stub
			return false;
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
	
