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
import com.autovend.software.AttendantIOTempPurchaseOwnBags;
import com.autovend.software.BaggingAreaController;
import com.autovend.software.CustomerIO;
import com.autovend.software.CustomerIOTempPurchaseOwnBags;
import com.autovend.software.PaymentControllerLogic;
import com.autovend.software.PrintReceipt;
import com.autovend.software.PurchaseBags;

/**
 * This test class is temporary and should be merged into a single BaggingAreaController test class much like the related controller software classes
 *
 */
public class PurchaseBagsTest {
	SelfCheckoutStation station;
	MyAttendantIO attendantIO;
	MyCustomerIO customerIO;
	BaggingAreaController baggingAreaController;
	PaymentControllerLogic paymentController;
	PrintReceipt printerController;

	@Before
	public void SetUp() {
		this.station = new SelfCheckoutStation(Currency.getInstance("CAD"), new int[] {10,20}, new BigDecimal[] {BigDecimal.ONE}, 999, 1);
		this.attendantIO = new MyAttendantIO();
		this.customerIO = new MyCustomerIO();
		this.printerController = new PrintReceipt(station, station.printer, customerIO, attendantIO);
		this.paymentController = new PaymentControllerLogic(station, customerIO, attendantIO, printerController);
		this.baggingAreaController = new BaggingAreaController(station, customerIO, attendantIO, paymentController);
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
	
	/**
	 * This test will check to see that the purchaseBags method behaves properly when given a quantity of at least 1.
	 * A signal from the CustomerIO will be called to star the method.
	 * 
	 * Expected Results:
	 * 		- No Exceptions.
	 * 		- The Bagging Area's ExpectedWeight should match the total weight of the bags.
	 * 		- The Payment Controller's CartTotal should match the total cost of the bags.
	 * 		- CustomerIO should have received a signal to inform Customer to put bags in bagging area.
	 * 		- purchasingBags flag should be set
	 */
	@Test
	public void purchaseBags_ValidAmount() {
		//Ensure that purchasingBags flag is false initially.
		assertFalse("purchasingBags flag should not be true initially.", baggingAreaController.purchasingBags);
		
		//Ensure that CustomerIO has not received a signal to place purchased bags yet
		assertFalse("CustomerIO should not have already reacted to a placePurchasedBagsSignal yet.", customerIO.placePurchasedBagsSignal);
		
		//Three bags will be purchased.
		customerIO.signalPurchaseBags(3);
		
		//Compute the total expected costs and weights of the bag.
		BigDecimal expectedTotalCost = baggingAreaController.REUSABLE_BAG_COST.multiply(BigDecimal.valueOf(3));
		double expectedTotalBagWeight = baggingAreaController.REUSABLE_BAG_WEIGHT * (double)3;
		
		//Assert for matching weights
		assertEquals("Bagging Area expected weight should match total weight of bags.", expectedTotalBagWeight, baggingAreaController.expectedWeight, 0.001d);
		
		//Assert for matching costs
		assertTrue("Payment Cart Total should match total cost of bags.", expectedTotalCost.compareTo(paymentController.getCartTotal()) == 0);
		
		//Assert that CustomerIO received a signal to place purchased bags
		assertTrue("CustomerIO should have reacted to a placePurchasedBagsSignal.", customerIO.placePurchasedBagsSignal);
		
		//Assert that the purchasingBags flag is now set to true
		assertTrue("purchasingBags flag should be set to true.", baggingAreaController.purchasingBags);
	}
	
	/**
	 * This test will check to see that the purchaseBags method behaves properly when given a quantity of less than 1.
	 * 
	 * Expected Results:
	 * 		- An IllegalArgumentException is thrown.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void purchaseBags_InvalidAmount() {
		//Zero bags will be purchased
		customerIO.signalPurchaseBags(0);
	}
	
	/**
	 * This test will simply check if finishedPurchasingBags signals the CustomerIO correctly.
	 * 
	 * Expected Results:
	 * 		- No Exceptions.
	 * 		- purchasingBags flag should be set to false.
	 * 		- CustomerIO has been signalled that the purchasing bags operation has been completed.
	 * 		- CustomerIO has been signalled that the station is ready for interaction.
	 */
	@Test
	public void finishedPurchasingBags() {
		
		//Initially going to set purchasingBags flag to true. The flag being initially true or false does not change the results.
		baggingAreaController.purchasingBags = true;
		
		//Ensure that CustomerIO has not yet received a signal that the operation has been completed.
		assertFalse("CustomerIO should not have reacted to finishedPurchasingBagsSignal yet.", customerIO.finishedPurchasingBagsSignal);
		
		//Ensure that the CustomerIO has not yet received a signal that the station is ready for interaction.
		assertFalse("CustomerIO should not have reacted to interactionReadySignal yet.", customerIO.interactionReadySignal);
		
		//finishedPurchasingBags will be called
		baggingAreaController.finishedPurchasingBags();
		
		//Assert that the purchasingBags flag is set to false
		assertFalse("purchasingBags flag should be set to false.", baggingAreaController.purchasingBags);
		
		//Assert that CustomerIO received a signal that the operation has been completed.
		assertTrue("CustomerIO should have reacted to finishedPurchasingBagsSignal.", customerIO.finishedPurchasingBagsSignal);
		
		//Assert that the CustomerIO received a signal that the station is ready for interaction.
		assertTrue("CustomerIO should have reacted to interactionReadySignal.", customerIO.interactionReadySignal);
	}
	
	class MyCustomerIO implements CustomerIO {
		
		boolean purchaseBagsSignal = false;
		int purchaseBagsQuantity = 0;
		boolean finishedPurchasingBagsSignal = false;
		boolean interactionReadySignal = false;
		boolean placePurchasedBagsSignal = false;
		
		@Override
		public void notifyWeightDiscrepancyCustomerIO() {
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
		public void thankCustomer() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void removeBill(BillSlot slot) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void signalPurchaseBags(int quantity) {
			this.purchaseBagsSignal = true;
			this.purchaseBagsQuantity = quantity;
			
			baggingAreaController.purchaseBags(quantity);
			
		}

		@Override
		public void signalFinishedPurchasingBags() {
			this.finishedPurchasingBagsSignal = true;
			
		}

		@Override
		public void signalReadyForInteraction() {
			this.interactionReadySignal = true;
			
		}

		@Override
		public void signalPutPurchasedBagsOnBaggingArea() {
			this.placePurchasedBagsSignal = true;
			
		}

		@Override
		public void notifyWeightDiscrepancyApprovedCustomerIO() {
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
		
	}
	
	class MyAttendantIO implements AttendantIOTempPurchaseOwnBags {

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
			// TODO Auto-generated method stub
			
		}

		@Override
		public void acceptOwnBags() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void approveWeightDiscrepancy(CustomerIO customerIO) {
			// TODO Auto-generated method stub
			
		}

		
	}


}





