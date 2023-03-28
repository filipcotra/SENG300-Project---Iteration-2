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
import com.autovend.software.*;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.util.Currency;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.autovend.BarcodedUnit;
import com.autovend.devices.*;

public class AddOwnBagsTest {
	SelfCheckoutStation station;
	AttendantIO attendantIO;
	CustomerIO customer;
	PaymentControllerLogic paymentController;
	PrintReceipt pr;
	BaggingAreaController bag;
	boolean wasCustomerIndicatedToUseOwnBags = false;
	boolean wasCustomerIndicatedToContinueAfterAttendantApproveOrDenyAddedBags = false;
	
	
	class myAttendantIO implements AttendantIO{
		

	@Override
	public void notifyWeightDiscrepancyAttendantIO() {
		// TODO Auto-generated method stub
		
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
		bag.blockSystem();
		this.acceptOwnBags();
	}

	@Override
	public void acceptOwnBags() {
		if (bag.bagAccept) {
			bag.unblockSystem();
			bag.ownBags = true;
		}
		else {
			bag.ownBags = false;
		}
		customer.indicateToCustomerToContinueAfterAttendantApproveOrDenyAddedBags();
	}

	@Override
	public void approveWeightDiscrepancy(CustomerIO customerIO) {
		// TODO Auto-generated method stub
		
	}
	}
	
	class myCustomerIO implements CustomerIO{

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
		public void removeBill(BillSlot slot) {
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
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public boolean selectAddOwnBags(){
			return bag.addOwnBags();
		}
		
		@Override
		public void indicateToCustomerToContinueAfterAttendantApproveOrDenyAddedBags() {
			wasCustomerIndicatedToContinueAfterAttendantApproveOrDenyAddedBags = true;
		}
		
	}


@Before
public void setup() {
	station = new SelfCheckoutStation(Currency.getInstance("CAD"), new int[] {5,10,20}, 
			new BigDecimal[] {new BigDecimal(1),new BigDecimal(2)}, 10000, 5);
	attendantIO = new myAttendantIO();
	customer = new myCustomerIO();
	pr = new PrintReceipt(station, station.printer, customer, attendantIO);
	paymentController = new PaymentControllerLogic(station, customer, attendantIO, pr);
	bag = new BaggingAreaController(station, customer, attendantIO, paymentController);
}

@After
public void tearDown() {
	station = null;
	attendantIO = null;
	customer = null;
	pr = null;
	paymentController = null;
	bag = null;
	wasCustomerIndicatedToUseOwnBags = false;
	wasCustomerIndicatedToContinueAfterAttendantApproveOrDenyAddedBags = false;
}

@Test
public void bagsAccepted() {
	bag.bagAccept = true;
	assertTrue(customer.selectAddOwnBags());
	assertTrue(bag.ownBags);
}

	@Test ()
	public void bagsReject() {
		bag.bagAccept = false;
		assertFalse(customer.selectAddOwnBags());
		assertFalse(bag.ownBags);
	}
	

	@Test
	public void wasSystemBlockedAfterFinishedAddingOwnBagsTest() {
		bag.blockSystem();
		assertTrue(station.printer.isDisabled());
		assertTrue(station.mainScanner.isDisabled());
		assertTrue(station.handheldScanner.isDisabled());
		assertTrue(station.billInput.isDisabled());
		assertTrue(station.billOutput.isDisabled());
		assertTrue(station.billStorage.isDisabled());
		assertTrue(station.billValidator.isDisabled());
		
	}

	
	@Test
	public void wasSystemUnblockedAfterAttendantApproveOrDenyAddedBagsTest() {
		bag.bagAccept = true;
		attendantIO.checkAddedOwnBags();
		assertFalse(station.printer.isDisabled());
		assertFalse(station.mainScanner.isDisabled());
		assertFalse(station.handheldScanner.isDisabled());
		assertFalse(station.billInput.isDisabled());
		assertFalse(station.billOutput.isDisabled());
		assertFalse(station.billStorage.isDisabled());
		assertFalse(station.billValidator.isDisabled());
	}
	
	@Test
	public void wasCustomerIndicatedToContinueAfterAttendantApproveOrDenyAddedBags() {
		bag.bagAccept = true;
		customer.selectAddOwnBags();
		assertTrue(wasCustomerIndicatedToContinueAfterAttendantApproveOrDenyAddedBags);
	}
	
}


