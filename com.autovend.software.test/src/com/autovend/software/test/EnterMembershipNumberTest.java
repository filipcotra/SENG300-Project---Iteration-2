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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.util.Currency;
import java.util.Scanner;

import org.junit.*;

import com.autovend.BarcodedUnit;
import com.autovend.Card;
import com.autovend.devices.BillSlot;
import com.autovend.devices.CardReader;
import com.autovend.devices.CoinTray;
import com.autovend.devices.SelfCheckoutStation;
import com.autovend.software.AttendantIO;
import com.autovend.software.CustomerIO;
import com.autovend.software.MembershipNumberController;
import com.autovend.software.PaymentControllerLogic;
import com.autovend.software.PrintReceipt;

public class EnterMembershipNumberTest {
	MembershipNumberController controller;
	int validLength;
	CustomerIO customer;
	String membershipGood;
	String membershipBad;
	String membershipShort;
	String membershipLong;
	String inputData;
	
	
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

		/*
		 * Simulates a membership number being entered by the customer via a touch screen and read by the system for testing purposes
		 */
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

		/*
		 * Simulates a response to the customer given by the system via a touch screen when it rejects the entered membership number
		 */
		@Override
		public void notifyBadMembershipNumberCustomerIO() {
			System.out.println("Invalid Membership Number. Please try again or cancel.");
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
		public boolean selectAddOwnBags() {
			return true;
		}
		
		
		@Override
		public void indicateToCustomerToContinueAfterAttendantApproveOrDenyAddedBags() {
			// TODO Auto-generated method stub
		}
		
		public void removeCoin(CoinTray tray) {
			// TODO Auto-generated method stub
		}
		
		
		public void payWithCreditComplete(BigDecimal amountDue) {
			// TODO Auto-generated method stub
		}
		
		public void payWithDebitComplete(BigDecimal amountDue) {
			// TODO Auto-generated method stub
		}
		
		public void selectPaymentMethod(String paymentMethod, PaymentControllerLogic instance) {
			// TODO Auto-generated method stub
		}
		
		public void transactionFailure() {
			// TODO Auto-generated method stub
		}
		
		public void setCardPaymentAmount(BigDecimal amount) {
			// TODO Auto-generated method stub
		}
		
		public void insertCard(Card card, String pin) {
			// TODO Auto-generated method stub
		}
		
		@Override
		public void removeCard(CardReader reader) {
			// TODO Auto-generated method stub
		}	
		
	}
	
	
	@Before
	public void setup() {
		// Setting up the parameters for the membership number controller
		validLength = 10;
		char[] validChars = {'1', '2', '3', '4', '5', '6', '7', '8', '9', '0'};
		customer = new ThisCustomer();
		controller = new MembershipNumberController(validChars, validLength, customer);
		
		// Setting the membership numbers
		membershipGood = "1231231231"; // Good memberships only contain digits
		membershipBad = "123bad7890"; // Bad memberships contain letters and other symbols
		membershipShort = "123"; // Membership numbers have a specific length, cannot be too short or too long
		membershipLong = "11223344556677889900";
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
		assertFalse(controller.checkValidMembershipNumber());
	}
	
	/**
	 * Tests the addMembershipNumber function in PaymentControllerLogic for when a membership number of invalid length is entered (too short)
	 * Result should set the valid variable to false when notifyBadMembershipNumberCustomerIO is called
	 */
	@Test
	public void testShortMembershipEntered() {
		inputData = membershipShort;
		controller.addMembershipNumber();
		assertFalse(controller.checkValidMembershipNumber());
	}
	
	/**
	 * Tests the addMembershipNumber function in PaymentControllerLogic for when a membership number of invalid length is entered (too long)
	 * Result should set the valid variable to false when notifyBadMembershipNumberCustomerIO is called
	 */
	@Test
	public void testLongMembershipEntered() {
		inputData = membershipLong;
		controller.addMembershipNumber();
		assertFalse(controller.checkValidMembershipNumber());
	}

}
