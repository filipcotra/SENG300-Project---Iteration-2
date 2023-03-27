package com.autovend.software.test;
import com.autovend.software.*;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

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
		acceptOwnBags();
	}

	@Override
	public void acceptOwnBags() {
		if (bag.bagAccept) {
			bag.unblockSystem();
			bag.ownBags = true;
		}
		else {
			throw new DisabledException();
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
		public void selectAddOwnBags(){
			bag.selectAddOwnBags();
		}
		
		@Override
		public void indicateAddOwnBags() {
			wasCustomerIndicatedToUseOwnBags = true;
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
	assertTrue(bag.addOwnBags());
	assertTrue(bag.ownBags);
}

	@Test ()
	public void bagsReject() {
		bag.bagAccept = false;
		try {
			boolean accepted = bag.addOwnBags();
		}
		catch(DisabledException de) {
			return;
		}
		fail("DisabledException expected");
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
	public void wasCustomerIndicatedToUseOwnBagsTest() {
		customer.selectAddOwnBags();
		assertTrue(wasCustomerIndicatedToUseOwnBags);
	}
	
	@Test
	public void wasCustomerIndicatedToContinueAfterAttendantApproveOrDenyAddedBags() {
		bag.bagAccept = true;
		attendantIO.checkAddedOwnBags();
		assertTrue(wasCustomerIndicatedToContinueAfterAttendantApproveOrDenyAddedBags);
	}
	
}


