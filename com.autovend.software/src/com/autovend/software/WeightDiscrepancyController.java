package com.autovend.software;

import com.autovend.devices.AbstractDevice;
import com.autovend.devices.ElectronicScale;
import com.autovend.devices.SelfCheckoutStation;
import com.autovend.devices.observers.AbstractDeviceObserver;
import com.autovend.devices.observers.ElectronicScaleObserver;

public class WeightDiscrepancyController implements ElectronicScaleObserver{
	
	SelfCheckoutStation station;
	CustomerIO customerIO;
	AttendantIO attendantIO;
	PaymentControllerLogic paymentController;
	double expectedWeight; // The expected weight of the self checkout station when an item is scanned
	double actualWeight; // The actual weight of the self checkout station when an item is scanned
	
	public WeightDiscrepancyController(SelfCheckoutStation station, CustomerIO customerIO, AttendantIO attendantIO, PaymentControllerLogic paymentController) {
		this.station = station;
		this.customerIO = customerIO;
		this.attendantIO = attendantIO;
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

