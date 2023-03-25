package com.autovend.software.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.util.Currency;
import java.util.Scanner;

import org.junit.*;

import com.autovend.BarcodedUnit;
import com.autovend.devices.BillSlot;
import com.autovend.devices.SelfCheckoutStation;
import com.autovend.software.AttendantIO;
import com.autovend.software.CustomerIO;
import com.autovend.software.PaymentControllerLogic;
import com.autovend.software.PrintReceipt;

public class EnterMembershipNumberTest {
	SelfCheckoutStation scs;
	PaymentControllerLogic controller;
	PrintReceipt printLogic;
	CustomerIO customer;
	AttendantIO attendant;
	String membershipGood;
	String membershipBad;
	String inputData;
	boolean cancelDecision;
	boolean valid;
	
	
	class ThisCustomer implements CustomerIO {

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
			System.setIn(new ByteArrayInputStream(inputData.getBytes()));
			try (Scanner keyboard = new Scanner(System.in)) {
				return keyboard.nextLine();
			}
		}

		@Override
		public boolean cancelMembershipInput() {
			return true;
		}

		@Override
		public void notifyBadMembershipNumberCustomerIO() {
			System.out.println("Invalid Membership Number. Please try again or cancel.");
			valid = false;
		}
		
	}
	
	class ThisAttendant implements AttendantIO {

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
	
	@Before
	public void setup() {
		// Setting up the self checkout station and its controller
		scs = new SelfCheckoutStation(Currency.getInstance("CAD"), new int[] {5, 10}, new BigDecimal[] {new BigDecimal(1)}, 11111, 1);
		attendant = new ThisAttendant();
		customer = new ThisCustomer();
		printLogic = new PrintReceipt(scs, scs.printer, customer, attendant);
		controller = new PaymentControllerLogic(scs, customer, attendant, printLogic);
		
		// Setting the membership numbers
		membershipGood = "1234567890"; // Good memberships only contain digits
		membershipBad = "123bad7890"; // Bad memberships contain letters and other symbols
		valid = true; // Default valid is true, is set to false if bad membership number is entered
	}
	

	/**
	 * Tests the getMembershipNumber function in the CustomerIO interface 
	 * Expected result should be the same as the input string read from the Customer
	 */
	@Test
	public void testGetMembership() {
		inputData = membershipGood; // Sets the customer's input to good membership number
		assertEquals(membershipGood, customer.getMembershipNumber()); // 
	}
	
	
	/**
	 * Tests the addMembershipNumber in PaymentControllerLogic for when a good membership number is entered
	 * Expected result should be the same as the membership field in PaymentControllerLogic
	 */
	@Test
	public void testValidMembershipEntered() {
		inputData = membershipGood; // Sets the customer's input to good membership number
		controller.addMembershipNumber();
		assertEquals(membershipGood, controller.getMembershipNumber());
	}
	
	/**
	 * Tests the addMembershipNumber function in PaymentControllerLogic for when a bad membership number is entered
	 * Result should set the valid variable to false when notifyBadMembershipNumberCustomerIO is called
	 */
	@Test
	public void testBadMembershipEntered() {
		inputData = membershipBad;
		controller.addMembershipNumber();
		assertFalse(valid);
	}

}
