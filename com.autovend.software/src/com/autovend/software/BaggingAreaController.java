package com.autovend.software;

import com.autovend.devices.AbstractDevice;
import com.autovend.devices.ElectronicScale;
import com.autovend.devices.SelfCheckoutStation;
import com.autovend.devices.observers.AbstractDeviceObserver;
import com.autovend.devices.observers.ElectronicScaleObserver;

import java.math.BigDecimal;
import java.util.ArrayList;

import com.autovend.*;

public class BaggingAreaController implements ElectronicScaleObserver{
	
	SelfCheckoutStation station;
	CustomerIO customerIO;
	AttendantIO attendantIO;
	PaymentControllerLogic paymentController;
	public boolean ownBags = false;
	public boolean bagAccept = false;
	public double expectedWeight; // The expected weight of the self checkout station when an item is scanned
	public double actualWeight; // The actual weight of the self checkout station when an item is scanned
	public boolean weightDiscrepancy = false;
	public boolean purchasingBags;
	
	public double REUSABLE_BAG_WEIGHT = 5;
	public BigDecimal REUSABLE_BAG_COST = BigDecimal.valueOf(4.99);
	
	public BaggingAreaController(SelfCheckoutStation station, CustomerIO customerIO, AttendantIO attendantIO, PaymentControllerLogic paymentController) {
		this.station = station;
		this.customerIO = customerIO;
		this.attendantIO = attendantIO;
		this.paymentController = paymentController;
		this.station.baggingArea.register(this);
	}
	
	/**
	 * Helper function to block the system by disabling all devices besides the bagging area.
	 */
	public void blockSystem() {
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
	public void unblockSystem() {
		this.station.printer.enable();
		this.station.mainScanner.enable();
		this.station.handheldScanner.enable();
		this.station.billInput.enable();
		this.station.billOutput.enable();
		this.station.billStorage.enable();
		this.station.billValidator.enable();
	}
	
	/**
	 * Getter for expectedWeight. Returns expectedWeight.
	 */
	public double getExpectedWeight() {
		return this.expectedWeight;
	}
	
	/**
	 * Getter for actualWeight. Returns expectedWeight.
	 */
	public double getActualWeight() {
		return this.actualWeight;
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
	
	/**
	 * This function is called when an attendant has approved a weight discrepancy noticed
	 * and the customer is allowed to proceed as usual by unblocking the system and updating the
	 * weight.
	 * if any purchase of bags caused the discrepancy, signal that the bags were purchased
	 * successfully
	 */
	public void weightDiscrepancyApproved() {
		this.unblockSystem();
		this.expectedWeight = this.actualWeight;
		if (purchasingBags == true) {
			this.finishedPurchasingBags();
		}
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
		this.blockSystem();		// block system when a weight change is detected
		this.actualWeight = weightInGrams;
		if (actualWeight != this.expectedWeight) {
			weightDiscrepancy = true;
			// Step 1. Block self checkout system (already done)
			// Step 2. Notify CustomerIO
			customerIO.notifyWeightDiscrepancyCustomerIO();
			// Step 3. Notify Attendant
			attendantIO.notifyWeightDiscrepancyAttendantIO();
		} else { // If there is no discrepancy then unblock the system
			if (purchasingBags == true) {	// if purchased bags, call to finishedPurchasingBags
				this.finishedPurchasingBags();
			}
			this.unblockSystem(); // unblock the system 
		}
		
	}

	@Override
	public void reactToOverloadEvent(ElectronicScale scale) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void reactToOutOfOverloadEvent(ElectronicScale scale) {
		// TODO Auto-generated method stub
		
	}
	
	public boolean addOwnBags() {
		attendantIO.checkAddedOwnBags();
		if (this.ownBags == true) {
			return true;
		}
		else {
			return false;
		}
	}
}
