/**
 * Temporary separate class that will be combined into a BaggingAreaController class once everything is finished involving the BaggingArea
 */

package com.autovend.software;

import java.math.BigDecimal;

import com.autovend.devices.AbstractDevice;
import com.autovend.devices.ElectronicScale;
import com.autovend.devices.SelfCheckoutStation;
import com.autovend.devices.observers.AbstractDeviceObserver;
import com.autovend.devices.observers.ElectronicScaleObserver;

public class PurchaseBags implements ElectronicScaleObserver{
	
	public SelfCheckoutStation station;
	public CustomerIOTempPurchaseOwnBags customerIO;
	public AttendantIOTempPurchaseOwnBags attendantIO;
	public PaymentControllerLogic paymentController;
	public double expectedWeight; // The expected weight of the self checkout station when an item is scanned
	
	public double REUSABLE_BAG_WEIGHT = 5;
	public BigDecimal REUSABLE_BAG_COST = new BigDecimal(4.99);
	
	public boolean purchasingBags;
	
	public PurchaseBags(SelfCheckoutStation station, CustomerIO customerIO, AttendantIO attendantIO, PaymentControllerLogic paymentController) {
		this.station = station;
		this.customerIO = (CustomerIOTempPurchaseOwnBags) customerIO;
		this.attendantIO = (AttendantIOTempPurchaseOwnBags) attendantIO;
		this.paymentController = paymentController;
	}
	
	/**
	 * Helper function to block the system by disabling all devices besides the bagging area.
	 */
	private void blockSystem() {
		this.station.printer.disable();
		this.station.mainScanner.disable();
		this.station.handheldScanner.disable();
		this.station.billInput.disable();
		this.station.billOutput.disable();
		this.station.billStorage.disable();
		this.station.billValidator.disable();
	}
	
	/**
	 * Helper function to unblock the system by enabling all devices besides the bagging area.
	 */
	private void unblockSystem() {
		this.station.printer.enable();
		this.station.mainScanner.enable();
		this.station.handheldScanner.enable();
		this.station.billInput.enable();
		this.station.billOutput.enable();
		this.station.billStorage.enable();
		this.station.billValidator.enable();
	}
	
	/**
	 * This function will handle purchasing a certain amount of reusable bags. These bags will
	 * be added to the Customer's bill. The bags will also be added to the expected weight of the bagging area.
	 * Due to miscommunication with the hardware department, the simulation does not have anything related
	 * to the Bag Dispenser. Therefore a signal to the CustomerIO will be sent instead.
	 * 
	 * An IllegalArgumentException will be thrown if the quantity is less than 1. A Customer should not be able to purchase zero or a negative number of bags.
	 * 
	 * @param quantity
	 * 		The number of bags to purchase. Must be at least one.
	 */
	public void purchaseBags(int quantity) {
		if(quantity < 1) {
			throw new IllegalArgumentException("Number of bags should not be less than 1.");
		}
		for(int i = 0; i < quantity; i++) {
			this.paymentController.updateCartTotal(REUSABLE_BAG_COST);
			this.paymentController.updateItemCostList("Reusable Bag", REUSABLE_BAG_COST.toString());
			this.expectedWeight += REUSABLE_BAG_WEIGHT;
		}
		purchasingBags = true;
		
		//Signal to CustomerIO will be sent to notify customer to put the purchased bags in the bagging area.
		customerIO.signalPutPurchasedBagsOnBaggingArea();
		
	}
	
	/**
	 * This function should be called when there is no weight discrepancy once the Customer puts their
	 * dispensed bags onto the bagging area.
	 * This function will handle notifying the Customer that bags have been purchased and added to the bill
	 *  and will signal that it is ready for interaction.
	 */
	public void finishedPurchasingBags() {
		purchasingBags = false;
		customerIO.signalFinishedPurchasingBags();
		customerIO.signalReadyForInteraction();
	}

	@Override
	public void reactToEnabledEvent(AbstractDevice<? extends AbstractDeviceObserver> device) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void reactToDisabledEvent(AbstractDevice<? extends AbstractDeviceObserver> device) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void reactToWeightChangedEvent(ElectronicScale scale, double weightInGrams) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void reactToOverloadEvent(ElectronicScale scale) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void reactToOutOfOverloadEvent(ElectronicScale scale) {
		// TODO Auto-generated method stub
		
	}
	
	

}
