/** 
 * Filip Cotra - 30086750
 * Khondaker Samin Rashid - 30143490
 * Nishan Soni - 30147280
 * Aaron Tigley - 30159927
 * Zainab Bari - 30154224
 */

package com.autovend.software;

import java.math.BigDecimal;

import com.autovend.Barcode;
import com.autovend.BarcodedUnit;
import com.autovend.devices.AbstractDevice;
import com.autovend.devices.BarcodeScanner;
import com.autovend.devices.ElectronicScale;
import com.autovend.devices.OverloadException;
import com.autovend.devices.SelfCheckoutStation;
import com.autovend.devices.observers.AbstractDeviceObserver;
import com.autovend.devices.observers.BarcodeScannerObserver;
import com.autovend.devices.observers.ElectronicScaleObserver;
import com.autovend.external.ProductDatabases;
import com.autovend.products.BarcodedProduct;

/**
 * Control software for the Add Item By Scanning use case.
 */
public class AddItemByScanningController implements BarcodeScannerObserver {
	
	BarcodedProduct product;
	SelfCheckoutStation station;
	CustomerIO customerIO;
	AttendantIO attendantIO;
	BarcodedUnit scannedItem; // The current scanned item to be added to the bagging area
	PaymentControllerLogic paymentController;
	BaggingAreaController baggingAreaController;

	/**
	 * Initialize a controller for the Add Item by Scanning use case. 
	 * Also registers this class as an observer for the station's main scanner.
	 * @param station The self checkout station
	 * @param customerIO The customer interacting with the Add Item by Scanning use case.
	 * @param attendantIO The attendant interacting with the Add Item by Scanning use case.
	 */
	public AddItemByScanningController(SelfCheckoutStation station, CustomerIO customerIO, AttendantIO attendantIO, PaymentControllerLogic paymentController, BaggingAreaController baggingAreaController) {
		this.station = station;
		this.customerIO = customerIO;
		this.attendantIO = attendantIO;
		this.paymentController = paymentController;
		this.station.mainScanner.register(this);
		this.baggingAreaController = baggingAreaController;
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
	 * Setter for product. Takes any product. Is only to be called within thisclass.
	 * @param product The current product at the station
	 */
	private void setProduct(BarcodedProduct product) {
		this.product = product;
	}
	
	/**
	 * Getter for product. Returns Product.
	 */
	public BarcodedProduct getProduct() {
		return this.product;
	}


	/**
	 * Occurs after mainScanner successfully scans an item (Step 1)
	 */
	@Override
	public void reactToBarcodeScannedEvent(BarcodeScanner barcodeScanner, Barcode barcode) {
		
		// Block the self checkout station by disabling all abstract devices other than the bagging area. (Step 2)
		this.blockSystem();
		
		// Get product details from the barcode (Step 3)
		this.setProduct(ProductDatabases.BARCODED_PRODUCT_DATABASE.get(barcode));
		
		// Update cart total and item cost list
		BigDecimal price = product.getPrice();
		this.paymentController.updateCartTotal(price);
		this.paymentController.updateItemCostList(product.getDescription(), price.toString());

		
		// Calculating the expected weight of the bagging area (Step 4)
		double weight = product.getExpectedWeight();
		if (weight > station.baggingArea.getSensitivity()) {	// check if the expected weight to be added to bagging area would be detected by scale
			baggingAreaController.expectedWeight += weight; // if expected weight to be added is greater than sensitivity, update expectedWeight of scale
		} else { 	// if weight to be added won't be detected, unblock system
			this.unblockSystem();
		}
		
		// Notify Customer I/O to place scanned item in bagging area (Step 5) 
		customerIO.notifyPlaceItemCustomerIO();
		
	}

	
	// The methods below are not needed but required by the inherited interfaces
	
	@Override
	public void reactToEnabledEvent(AbstractDevice<? extends AbstractDeviceObserver> device) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void reactToDisabledEvent(AbstractDevice<? extends AbstractDeviceObserver> device) {
		// TODO Auto-generated method stub
		
	}

}
