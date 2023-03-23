package com.autovend.software.test;
import com.autovend.devices.SelfCheckoutStation;
import com.autovend.software.*;
import com.autovend.devices.*;

SelfCheckoutStation station;
CustomerIO customerIO;
AttendantIO attendantIO;
PaymentControllerLogic paymentController;

public class AddOwnBagsTest {
	BaggingAreaController bag = new BaggingAreaController(station, customerIO, attendantIO, paymentController);
	
	
}
