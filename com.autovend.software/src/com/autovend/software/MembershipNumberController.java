package com.autovend.software;

import java.util.Arrays;

public class MembershipNumberController {
	
	String membershipNumber;
	char[] validChars;
	int requiredLength;
	CustomerIO myCustomer;
	AttendantIO myAttendant;
	boolean validMembership;
	
	public MembershipNumberController(char[] validCharArray, int length, CustomerIO customer) {
		this.validChars = validCharArray;
		this.requiredLength = length;
		this.membershipNumber = "";
		this.myCustomer = customer;
		this.validMembership = false;
	}
	
	/*
	 * Verifies if the membership number entered by the customer is valid before adding it to the session
	 */
	public void addMembershipNumber() {

		String membershipInput = myCustomer.getMembershipNumber(); // Gets value entered by customer
		this.validMembership = true; // Boolean to decide if the membership number is valid
		boolean validChar;
		
		// Check if membership is a valid length
		if (membershipInput.length() == this.requiredLength) {
			
			// Loop to check if each character in the membership input string is a valid character for membership numbers
			for (int i = 0; i < membershipInput.length(); i++) {
				validChar = false;
				for (char c : this.validChars) {
					if (c == membershipInput.charAt(i)) {
						validChar = true;
					}
				}
				if (!validChar) {
					this.validMembership = false;
					break;
				}
			}
			
			// If bad membership
			if (!this.validMembership) {
				myCustomer.notifyBadMembershipNumberCustomerIO(); // Notify customer that the membership number is bad
			} else { // Else valid membership
				this.membershipNumber = membershipInput; // Add membership number to the session
			}
		} else {
			this.validMembership = false;
			myCustomer.notifyBadMembershipNumberCustomerIO();
		}
		
	}
	
	public String getMembershipNumber() {
		return this.membershipNumber;
	}
	
	public boolean checkValidMembershipNumber() {
		return this.validMembership;
	}

}
