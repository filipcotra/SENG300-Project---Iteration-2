/** 
 * Filip Cotra - 30086750
 * Khondaker Samin Rashid - 30143490
 * Nishan Soni - 30147280
 * Aaron Tigley - 30159927
 * Zainab Bari - 30154224
 */

package com.autovend.software.test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
/*
 * Open issues:
 * 1. The hardware has to handle invalid cash, to reject it without involving the control software.
 * 2. Should mixed modes of payment be supported?
 * 
 * Exceptions:
 * 1. If the customer inserts cash that is deemed unacceptable, this will be returned to the customer 
 * without involving the System, presumably handled in the hardware.
 * 2. If insufficient change is available, the attendant should be signaled so that maintenance can be conducted on it.
 */
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Currency;

import org.junit.Before;
import org.junit.After;
import org.junit.Test;

import com.autovend.BarcodedUnit;
import com.autovend.Bill;
import com.autovend.Coin;
import com.autovend.devices.AbstractDevice;
import com.autovend.devices.BillDispenser;
import com.autovend.devices.BillSlot;
import com.autovend.devices.BillValidator;
import com.autovend.devices.CoinDispenser;
import com.autovend.devices.CoinSlot;
import com.autovend.devices.CoinTray;
import com.autovend.devices.CoinValidator;
import com.autovend.devices.DisabledException;
import com.autovend.devices.OverloadException;
import com.autovend.devices.SelfCheckoutStation;
import com.autovend.devices.SimulationException;
import com.autovend.devices.observers.AbstractDeviceObserver;
import com.autovend.devices.observers.BillDispenserObserver;
import com.autovend.devices.observers.BillSlotObserver;
import com.autovend.devices.observers.BillValidatorObserver;
import com.autovend.devices.observers.CoinDispenserObserver;
import com.autovend.devices.observers.CoinSlotObserver;
import com.autovend.devices.observers.CoinTrayObserver;
import com.autovend.devices.observers.CoinValidatorObserver;
import com.autovend.software.AttendantIO;
import com.autovend.software.CustomerIO;
import com.autovend.software.PaymentControllerLogic;
import com.autovend.software.PrintReceipt;

public class PaymentWithCashTest {
	
	// Create variables to be used
	PaymentControllerLogic paymentController;
	PrintReceipt receiptPrinterController;
	SelfCheckoutStation selfCheckoutStation;
	MyBillSlotObserver billObserver;
	MyCoinSlotObserver coinSlotObserver;
	MyCoinTrayObserver coinTrayObserver;
	MyCustomerIO customer;
	MyAttendantIO attendant;
	Bill[] fiveDollarBills;
	Bill[] tenDollarBills;
	Bill[] twentyDollarBills;
	Bill[] fiftyDollarBills;
	BillSlot billSlot;
	Bill billFive;
	Bill billTen;
	Bill billTwenty;
	Bill billFifty;
	Bill billHundred;
	ArrayList<Integer> ejectedBills; 
	BillDispenserStub billObserverStub;
	Coin[] nickelCoins;
	Coin[] dimeCoins;
	Coin[] quarterCoins;
	Coin[] loonieCoins;
	Coin[] toonieCoins;
	CoinSlot coinSlot;
	Coin coinPenny;
	Coin coinNickel;
	Coin coinDime;
	Coin coinQuarter;
	Coin coinLoonie;
	Coin coinToonie;
	ArrayList<BigDecimal> ejectedCoins;
	CoinDispenserStub coinObserverStub;
	final PrintStream originalOut = System.out;
	ByteArrayOutputStream baos;
	PrintStream ps;
	Boolean attendantSignalled;
	Boolean coinFalseNegative;
	Boolean billFalseNegative;

/* ---------------------------------- Stubs ---------------------------------------------------*/
	
	class BillDispenserStub implements BillDispenserObserver {

		@Override
		public void reactToEnabledEvent(AbstractDevice<? extends AbstractDeviceObserver> device) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void reactToDisabledEvent(AbstractDevice<? extends AbstractDeviceObserver> device) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void reactToBillsFullEvent(BillDispenser dispenser) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void reactToBillsEmptyEvent(BillDispenser dispenser) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void reactToBillAddedEvent(BillDispenser dispenser, Bill bill) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void reactToBillRemovedEvent(BillDispenser dispenser, Bill bill) {
			ejectedBills.add(bill.getValue());		
			
		}

		@Override
		public void reactToBillsLoadedEvent(BillDispenser dispenser, Bill... bills) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void reactToBillsUnloadedEvent(BillDispenser dispenser, Bill... bills) {
			// TODO Auto-generated method stub
			
		}
		
	}
	
	class CoinDispenserStub implements CoinDispenserObserver{

		@Override
		public void reactToEnabledEvent(AbstractDevice<? extends AbstractDeviceObserver> device) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void reactToDisabledEvent(AbstractDevice<? extends AbstractDeviceObserver> device) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void reactToCoinsFullEvent(CoinDispenser dispenser) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void reactToCoinsEmptyEvent(CoinDispenser dispenser) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void reactToCoinAddedEvent(CoinDispenser dispenser, Coin coin) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void reactToCoinRemovedEvent(CoinDispenser dispenser, Coin coin) {
			ejectedCoins.add(coin.getValue());
			
		}

		@Override
		public void reactToCoinsLoadedEvent(CoinDispenser dispenser, Coin... coins) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void reactToCoinsUnloadedEvent(CoinDispenser dispenser, Coin... coins) {
			// TODO Auto-generated method stub
			
		}
		
	}
	
	class CoinValidatorStub implements CoinValidatorObserver{

		@Override
		public void reactToEnabledEvent(AbstractDevice<? extends AbstractDeviceObserver> device) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void reactToDisabledEvent(AbstractDevice<? extends AbstractDeviceObserver> device) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void reactToValidCoinDetectedEvent(CoinValidator validator, BigDecimal value) {
			coinFalseNegative = false;
			
		}

		@Override
		public void reactToInvalidCoinDetectedEvent(CoinValidator validator) {
			coinFalseNegative = true;
			
		}
	}
	
	class BillValidatorStub implements BillValidatorObserver{

		@Override
		public void reactToEnabledEvent(AbstractDevice<? extends AbstractDeviceObserver> device) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void reactToDisabledEvent(AbstractDevice<? extends AbstractDeviceObserver> device) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void reactToValidBillDetectedEvent(BillValidator validator, Currency currency, int value) {
			billFalseNegative = false;
			
		}

		@Override
		public void reactToInvalidBillDetectedEvent(BillValidator validator) {
			billFalseNegative = true;
			
		}

	}
	
	class MyCustomerIO implements CustomerIO {

		@Override
		public void thankCustomer() {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void removeBill(BillSlot slot) {
			slot.removeDanglingBill();
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
		public void removeCoin(CoinTray tray) {
			tray.collectCoins();
			
		}

		@Override
		public void payWithCreditComplete(BigDecimal amountDue) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void payWithDebitComplete(BigDecimal amountDue) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public String getPin() {
			// TODO Auto-generated method stub
			return null;
		}
	
	}
	
	class MyAttendantIO implements AttendantIO {

			@Override
			public boolean approveWeightDiscrepancy() {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public void printDuplicateReceipt() {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void changeRemainsNoDenom(BigDecimal changeLeft) {
				System.out.print("changeRemainsNoDenom Called: " + changeLeft);
				attendantSignalled = true;
				
			}
	}
	
	class MyBillSlotObserver implements BillSlotObserver{

		public AbstractDevice<? extends AbstractDeviceObserver> device = null;
		
		@Override
		public void reactToEnabledEvent(AbstractDevice<? extends AbstractDeviceObserver> device) {
			// TODO Auto-generated method stub
			this.device = device;
		}

		@Override
		public void reactToDisabledEvent(AbstractDevice<? extends AbstractDeviceObserver> device) {
			// TODO Auto-generated method stub
			this.device = device;
		}

		@Override
		public void reactToBillInsertedEvent(BillSlot slot) {
			// TODO Auto-generated method stub
			this.device = slot;
		}

		@Override
		public void reactToBillEjectedEvent(BillSlot slot) {
			// TODO Auto-generated method stub
			slot.removeDanglingBill();
			this.device = slot;
			System.out.println("Bill has been ejected from the bill slot.");
			
			
		}

		@Override
		public void reactToBillRemovedEvent(BillSlot slot) {
			// TODO Auto-generated method stub
			this.device = slot;
		}
		
	}
	
	class MyCoinSlotObserver implements CoinSlotObserver{

		public AbstractDevice<? extends AbstractDeviceObserver> device = null;
		
		@Override
		public void reactToEnabledEvent(AbstractDevice<? extends AbstractDeviceObserver> device) {
			// TODO Auto-generated method stub
			this.device = device;
			
		}

		@Override
		public void reactToDisabledEvent(AbstractDevice<? extends AbstractDeviceObserver> device) {
			// TODO Auto-generated method stub
			this.device = device;
			
		}

		@Override
		public void reactToCoinInsertedEvent(CoinSlot slot) {
			// TODO Auto-generated method stub
			this.device = slot;
			
		}
		
	}
	
	class MyCoinTrayObserver implements CoinTrayObserver{
		
		public AbstractDevice<? extends AbstractDeviceObserver> device = null;

		@Override
		public void reactToEnabledEvent(AbstractDevice<? extends AbstractDeviceObserver> device) {
			this.device = device;
			
		}

		@Override
		public void reactToDisabledEvent(AbstractDevice<? extends AbstractDeviceObserver> device) {
			this.device = device;
			
		}

		@Override
		public void reactToCoinAddedEvent(CoinTray tray) {
			tray.collectCoins();
			this.device = tray;
			System.out.println("Coin has been collected from coin tray.");
			
		}
		
	}

/* ---------------------------------- SetUp ---------------------------------------------------*/
	
	@Before
	public void setUp() {
		attendantSignalled = false;
		// Setting up new print stream to catch printed output, used to test terminal output
		baos = new ByteArrayOutputStream();
		ps = new PrintStream(baos);
		System.setOut(ps);
		billFive = new Bill(5, Currency.getInstance("CAD"));
		billTen = new Bill(10, Currency.getInstance("CAD"));
		billTwenty = new Bill(20, Currency.getInstance("CAD"));
		billFifty = new Bill(50, Currency.getInstance("CAD"));
		billHundred = new Bill(100, Currency.getInstance("CAD"));
		coinPenny = new Coin(new BigDecimal("0.01"), Currency.getInstance("CAD"));
		coinNickel = new Coin(new BigDecimal("0.05"), Currency.getInstance("CAD"));
		coinDime = new Coin(new BigDecimal("0.10"), Currency.getInstance("CAD"));
		coinQuarter = new Coin(new BigDecimal("0.25"), Currency.getInstance("CAD"));
		coinLoonie = new Coin(new BigDecimal("1.00"), Currency.getInstance("CAD"));
		coinToonie = new Coin(new BigDecimal("2.00"), Currency.getInstance("CAD"));

		selfCheckoutStation = new SelfCheckoutStation(Currency.getInstance("CAD"), new int[] {5,10,20,50}, 
				new BigDecimal[] {new BigDecimal("0.05"),new BigDecimal("0.10"), new BigDecimal("0.25"),
						new BigDecimal("1.00"), new BigDecimal("2.00")}, 10000, 5);
		customer = new MyCustomerIO();
		attendant = new MyAttendantIO();
		ejectedBills = new ArrayList<Integer>();
		ejectedCoins = new ArrayList<BigDecimal>();
		/* Load one hundred, $5, $10, $20, $50 bills into the dispensers so we can dispense change during tests.
		 * Also load two hundred $0.05, $0.10, $0.25, $1.00, and $2.00 coins into the dispensers for coin change.
		 * Every dispenser has a max capacity of 100 
		 */
		fiveDollarBills = new Bill[100];
		tenDollarBills = new Bill[100];
		twentyDollarBills = new Bill[100];
		fiftyDollarBills = new Bill[100];
		for(int i = 0; i < 100; i++) {
			fiveDollarBills[i] = billFive;
			tenDollarBills[i] = billTen;
			twentyDollarBills[i] = billTwenty;
			fiftyDollarBills[i] = billFifty;
		}
		nickelCoins = new Coin[200];
		dimeCoins = new Coin[200];
		quarterCoins = new Coin[200];
		loonieCoins = new Coin[200];
		toonieCoins = new Coin[200];
		for(int i = 0; i < 200; i++) {
			nickelCoins[i] = coinNickel;
			dimeCoins[i] = coinDime;
			quarterCoins[i] = coinQuarter;
			loonieCoins[i] = coinLoonie;
			toonieCoins[i] = coinToonie;
		}
		try {
			selfCheckoutStation.billDispensers.get(5).load(fiveDollarBills);
			selfCheckoutStation.billDispensers.get(10).load(tenDollarBills);
			selfCheckoutStation.billDispensers.get(20).load(twentyDollarBills);
			selfCheckoutStation.billDispensers.get(50).load(fiftyDollarBills);
			selfCheckoutStation.coinDispensers.get(new BigDecimal ("0.05")).load(nickelCoins);
			selfCheckoutStation.coinDispensers.get(new BigDecimal ("0.10")).load(dimeCoins);
			selfCheckoutStation.coinDispensers.get(new BigDecimal ("0.25")).load(quarterCoins);
			selfCheckoutStation.coinDispensers.get(new BigDecimal ("1.00")).load(loonieCoins);
			selfCheckoutStation.coinDispensers.get(new BigDecimal ("2.00")).load(toonieCoins);
		} catch (SimulationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OverloadException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		receiptPrinterController = new PrintReceipt(selfCheckoutStation, selfCheckoutStation.printer, customer, attendant);
		paymentController = new PaymentControllerLogic(selfCheckoutStation, customer, attendant, receiptPrinterController);
		paymentController.setCartTotal(BigDecimal.ZERO);
		coinFalseNegative = true;
		billFalseNegative = true;
		billObserver = new MyBillSlotObserver();
		selfCheckoutStation.billInput.register(billObserver);
	}

/* ---------------------------------- Teardown ------------------------------------------------*/
	
	// Teardown to set all objects to null or default values, and restore system.out to console
	@After
	public void teardown() {
		attendantSignalled = false;
		baos = null;
		ps = null;
		System.setOut(System.out);
		billFive = null;
		billTen = null;
		billTwenty = null;
		billFifty = null;
		billHundred = null;
		coinPenny = null;
		coinNickel = null;
		coinDime = null;
		coinQuarter = null;
		coinLoonie = null;
		coinToonie = null;
		selfCheckoutStation = null;
		customer = null;
		attendant = null;
		ejectedBills = null;
		fiveDollarBills = null;
		tenDollarBills = null;
		twentyDollarBills = null;
		fiftyDollarBills = null;
		nickelCoins = null;
		dimeCoins = null;
		quarterCoins = null;
		loonieCoins = null;
		toonieCoins = null;
		receiptPrinterController = null;
		paymentController = null;
		
	}

/* ---------------------------------- Tests ---------------------------------------------------*/
	
	/* Test Case: Inserting an invalid bill denomination to the self-checkout machine
	 * 
	 * Description: "If the customer inserts cash that is deemed unacceptable, 
	 * this will be returned to the customer without involving the System,
	 * presumably handled in hardware." What I mean by "invalid bill" is
	 * a bill that does not meet the set denominations of bills that the machine
	 * can accept.
	 * 
	 * Expected Result: The bill slot observer should call the 
	 * reactToBillEjectedEvent method and eject the bill from the machine.
	 * Further, the cart total should remain unchanged.
	 */
	@Test
	public void addInvalidBillDenom_Test() {
		paymentController.setCartTotal(BigDecimal.valueOf(100.00));
		try {
			selfCheckoutStation.billInput.accept(billHundred);
			assertEquals(selfCheckoutStation.billInput,billObserver.device);
			assertEquals(BigDecimal.valueOf(100.00),paymentController.getCartTotal());
		} catch (DisabledException e) {
			return;
		} catch (OverloadException e) {
			return;
		}
	}
	
	/* Test Case: Inserting an invalid bill currency to the self-checkout machine
	 * 
	 * Description: "If the customer inserts cash that is deemed unacceptable, 
	 * this will be returned to the customer without involving the System,
	 * presumably handled in hardware." What I mean by "invalid bill" is
	 * a bill that does not meet the currency of bills that the machine
	 * can accept.
	 * 
	 * Expected Result: The bill slot observer should call the 
	 * reactToBillEjectedEvent method and eject the bill from the machine.
	 * Further, the cart total should remain unchanged.
	 */
	@Test
	public void addInvalidBillCurr_Test() {
		paymentController.setCartTotal(BigDecimal.valueOf(100.00));
		Bill usdBillTwenty = new Bill (20, Currency.getInstance("USD"));
		try {
			selfCheckoutStation.billInput.accept(usdBillTwenty);
			assertEquals(selfCheckoutStation.billInput,billObserver.device);
			assertEquals(BigDecimal.valueOf(100.00),paymentController.getCartTotal());
		} catch (DisabledException e) {
			return;
		} catch (OverloadException e) {
			return;
		}
	}
	
	/* Test Case: Inserting an invalid coin denomination to the self-checkout machine
	 * 
	 * Description: "If the customer inserts cash that is deemed unacceptable, 
	 * this will be returned to the customer without involving the System,
	 * presumably handled in hardware." What I mean by "invalid coin" is
	 * a coin that does not meet the set denominations of coins that the machine
	 * can accept.
	 * 
	 * Expected Result: The coin tray observer should call the 
	 * reactToCoinAddedEvent method as the invalid coin added falls straight through the slot and validator
	 * and back to the tray. Further, the cart total should remain unchanged.
	 */
	@Test
	public void addInvalidCoinDenom_Test() {
		paymentController.setCartTotal(BigDecimal.valueOf(100.00));
		coinTrayObserver = new MyCoinTrayObserver();
		try {
			selfCheckoutStation.coinTray.register(coinTrayObserver);
			selfCheckoutStation.coinSlot.accept(coinPenny);
			assertEquals(selfCheckoutStation.coinTray,coinTrayObserver.device);
			assertEquals(BigDecimal.valueOf(100.00),paymentController.getCartTotal());
		} catch (DisabledException e) {
			return;
		}
	}
	
	/* Test Case: Inserting an invalid coin currency to the self-checkout machine
	 * 
	 * Description: "If the customer inserts cash that is deemed unacceptable, 
	 * this will be returned to the customer without involving the System,
	 * presumably handled in hardware." What I mean by "invalid coin" is
	 * a coin that does not meet the currency of coins that the machine
	 * can accept.
	 * 
	 * Expected Result: The coin tray observer should call the 
	 * reactToCoinAddedEvent method as the invalid coin added falls straight through the slot and validator
	 * and back to the tray. Further, the cart total should remain unchanged.
	 */
	@Test
	public void addInvalidCoinCurr_Test() {
		paymentController.setCartTotal(BigDecimal.valueOf(100.00));
		coinTrayObserver = new MyCoinTrayObserver();
		Coin usdCoinQuarter = new Coin(new BigDecimal(0.25), Currency.getInstance("USD"));
		try {
			selfCheckoutStation.coinTray.register(coinTrayObserver);
			selfCheckoutStation.coinSlot.accept(usdCoinQuarter);
			assertEquals(selfCheckoutStation.coinTray,coinTrayObserver.device);
			assertEquals(BigDecimal.valueOf(100.00),paymentController.getCartTotal());
		} catch (DisabledException e) {
			return;
		}
	}
	
	/* Test Case: An amount less than the cart total is paid via bill.
	 * 
	 * Description: The cart total is set at $100. $20 is paid in a single bill. 
	 * 
	 * Expected Result: The cart total should drop from $100 to $80. Checking the amount paid
	 * should return a string value of "20".
	 */
	@Test
	public void payBillLessThanTotal_Test() {
		paymentController.setCartTotal(BigDecimal.valueOf(100.00));
		selfCheckoutStation.billValidator.register(new BillValidatorStub());
		try {
			while(billFalseNegative) {	
				selfCheckoutStation.billInput.accept(billTwenty);
			}
		} catch (DisabledException e) {
			fail("A Disabled Exception should not have been thrown");
		} catch (OverloadException e) {
			fail("An OverloadException should not have been thrown");
		}
		assertTrue(BigDecimal.valueOf(80).compareTo(paymentController.getCartTotal()) == 0);
		assertEquals("20.0",paymentController.getAmountPaid());
	}
	
	/* Test Case: An amount less than the cart total is paid via coin.
	 * 
	 * Description: The cart total is set at $100. $2 is paid in a single coin. 
	 * 
	 * Expected Result: The cart total should drop from $100 to $80. Checking the amount paid
	 * should return a string value of "20".
	 */
	@Test
	public void payCoinLessThanTotal_Test() {
		paymentController.setCartTotal(BigDecimal.valueOf(100.00));
		selfCheckoutStation.coinValidator.register(new CoinValidatorStub());
		try {
			while(coinFalseNegative) {	
				selfCheckoutStation.coinSlot.accept(coinToonie);
			}
		} catch (DisabledException e) {
			fail("A Disabled Exception should not have been thrown");
		}
		assertTrue(BigDecimal.valueOf(98).compareTo(paymentController.getCartTotal()) == 0);
		assertEquals("2.00",paymentController.getAmountPaid());
	}
	
	/* Test Case: The customer pays with a single bill on two separate instances. 
	 * 
	 * Description: The cart total is set at $100. $20 is paid in a single bill and
	 * then $5 dollars is paid in a single bill. 
	 * 
	 * The purpose of this test is to see if any weird behaviors/occurrences happen to 
	 * either the cart total or the amount paid.
	 * 
	 * Expected Result: The cart total should drop from $100 to $80. Then it should drop 
	 * to $75 on the second bill insertion. Checking the amount paid should return a string
	 * value of "20", then after the second bill insertion update to a string value of "25".
	 */
	@Test
	public void payTwiceBills() {
		selfCheckoutStation.billValidator.register(new BillValidatorStub());
		paymentController.setCartTotal(BigDecimal.valueOf(100.00));
		try {
			// The customer first inserts a twenty dollar bill into the bill slot.
			while(billFalseNegative) {
				selfCheckoutStation.billInput.accept(billTwenty);
			}
			assertTrue(BigDecimal.valueOf(80.00).compareTo(paymentController.getCartTotal()) == 0);
			assertEquals("20.0",paymentController.getAmountPaid());
			
			billFalseNegative = true;
			
			// The customer then inserts a five dollar bill into the bill slot.
			while(billFalseNegative) {
				selfCheckoutStation.billInput.accept(billFive);
			}
			assertTrue(BigDecimal.valueOf(75.00).compareTo(paymentController.getCartTotal()) == 0);
			assertEquals("25.0",paymentController.getAmountPaid());
			
		} catch (DisabledException e) {
			fail("A Disabled Exception should not have been thrown");
		} catch (OverloadException e) {
			fail("An OverloadException should not have been thrown");
		}
	}
	
	/* Test Case: The customer pays with a single coin on two separate instances. 
	 * 
	 * Description: The cart total is set at $100. $1 is paid in a single coin and
	 * then $0.10 dollars is paid in a single coin. 
	 * 
	 * The purpose of this test is to see if any weird behaviors/occurrences happen to 
	 * either the cart total or the amount paid.
	 * 
	 * Expected Result: The cart total should drop from $100 to $80. Then it should drop 
	 * to $75 on the second bill insertion. Checking the amount paid should return a string
	 * value of "20", then after the second bill insertion update to a string value of "25".
	 */
	@Test
	public void payTwiceCoins() {
		selfCheckoutStation.coinValidator.register(new CoinValidatorStub());
		paymentController.setCartTotal(BigDecimal.valueOf(100.00));
		try {
			// The customer first inserts a $1 coin (loonie) into the coin slot.
			while(coinFalseNegative) {
				selfCheckoutStation.coinSlot.accept(coinLoonie);
			}
			assertTrue(BigDecimal.valueOf(99.00).compareTo(paymentController.getCartTotal()) == 0);
			assertEquals("1.00",paymentController.getAmountPaid());
			
			coinFalseNegative = true;
			
			// The customer then inserts a $0.10 coin (dime) into the coin slot.
			while(coinFalseNegative) {
				selfCheckoutStation.coinSlot.accept(coinDime);
			}
			assertTrue(BigDecimal.valueOf(98.90).compareTo(paymentController.getCartTotal()) == 0);
			assertEquals("1.10",paymentController.getAmountPaid());
			
		} catch (DisabledException e) {
			fail("A Disabled Exception should not have been thrown");
		} 
	}
	
	/* Test Case: The customer pays with a single coin followed by a single bill. 
	 * 
	 * Description: The cart total is set at $100. $0.25 is paid in a single coin and
	 * then $50 dollars is paid in a single bill. 
	 * 
	 * The purpose of this test is to see if any weird behaviors/occurrences happen to 
	 * either the cart total or the amount paid when mixing payment methods
	 * 
	 * Expected Result: The cart total should drop from $100 to $99.75. Then it should drop 
	 * to $49.75 on the second payment insertion. Checking the amount paid should return a string
	 * value of "0.25", then after the second payment insertion update to a string value of "50.25".
	 */
	@Test
	public void payTwiceCoinThenBill() {
		selfCheckoutStation.coinValidator.register(new CoinValidatorStub());
		selfCheckoutStation.billValidator.register(new BillValidatorStub());
		paymentController.setCartTotal(BigDecimal.valueOf(100.00));
		try {
			// The customer first inserts a $0.25 coin (quarter) into the coin slot.
			while(coinFalseNegative) {
				selfCheckoutStation.coinSlot.accept(coinQuarter);
			}
			assertTrue(BigDecimal.valueOf(99.75).compareTo(paymentController.getCartTotal()) == 0);
			assertEquals("0.25",paymentController.getAmountPaid());
			
			// The customer then inserts a 50 dollar bill into the bill slot.
			while(billFalseNegative) {
				selfCheckoutStation.billInput.accept(billFifty);
			}
			assertTrue(BigDecimal.valueOf(49.75).compareTo(paymentController.getCartTotal()) == 0);
			assertEquals("50.25",paymentController.getAmountPaid());
			
		} catch (DisabledException e) {
			fail("A Disabled Exception should not have been thrown");
		} catch (OverloadException e) {
			fail("An OverloadException should not have been thrown");
		}
	}
	
	/* Test Case: The customer pays with a single bill followed by a single coin. 
	 * 
	 * Description: The cart total is set at $100. $10 is paid in a single bill and
	 * then $2 dollar is paid in a single coin. 
	 * 
	 * The purpose of this test is to see if any weird behaviors/occurrences happen to 
	 * either the cart total or the amount paid when mixing payment methods
	 * 
	 * Expected Result: The cart total should drop from $100 to $90. Then it should drop 
	 * to $88 on the second payment insertion. Checking the amount paid should return a string
	 * value of "10", then after the second payment insertion update to a string value of "12".
	 */
	@Test
	public void payTwiceBillThenCoin() {
		selfCheckoutStation.coinValidator.register(new CoinValidatorStub());
		selfCheckoutStation.billValidator.register(new BillValidatorStub());
		paymentController.setCartTotal(BigDecimal.valueOf(100.00));
		try {
			// The customer first inserts a ten dollar bill into the bill slot.
			while(billFalseNegative) {
				selfCheckoutStation.billInput.accept(billTen);
			}
			assertTrue(BigDecimal.valueOf(90).compareTo(paymentController.getCartTotal()) == 0);
			assertEquals("10.0",paymentController.getAmountPaid());
			
			// The customer then inserts a $2 coin (toonie) into the coin slot.
			while(coinFalseNegative) {
				selfCheckoutStation.coinSlot.accept(coinToonie);
			}
			assertTrue(BigDecimal.valueOf(88).compareTo(paymentController.getCartTotal()) == 0);
			assertEquals("12.00",paymentController.getAmountPaid());
			
		} catch (DisabledException e) {
			fail("A Disabled Exception should not have been thrown");
		} catch (OverloadException e) {
			fail("An OverloadException should not have been thrown");
		}
	}
	
	/* Test Case: The customer pays exactly the total cart amount. 
	 * 
	 * Description: The cart total is set at $50. $50 is paid in a single bill.
	 * 
	 * There shouldn't be a need to test this with multiple instances of paying with cash
	 * as the previous test had covered any weird behaviors that could have occurred.
	 * 
	 * Expected Result: The cart total should drop from $50 to $0. 
	 * Checking the amount paid should return a string value of "50".
	 * Checking the total change should return a string value of be "0.00'.
	 */
	@Test
	public void payFullBillNoChange() {
		selfCheckoutStation.billValidator.register(new BillValidatorStub());
		paymentController.setCartTotal(BigDecimal.valueOf(50.00));
		try {
			// The customer pays the full fifty dollars using a single fifty dollar bill.
			while(billFalseNegative) {
				selfCheckoutStation.billInput.accept(billFifty);
			}
			assertTrue(BigDecimal.ZERO.compareTo(paymentController.getCartTotal()) == 0);
			assertEquals("50.0",paymentController.getAmountPaid());
			assertEquals("0.0",paymentController.getTotalChange());
			
		} catch (DisabledException e) {
			fail("A Disabled Exception should not have been thrown");
		} catch (OverloadException e) {
			fail("An OverloadException should not have been thrown");
		} 
	}
	
	/* Test Case: The customer pays exactly the total cart amount. 
	 * 
	 * Description: The cart total is set at $0.05. $0.05 is paid in a single nickel.
	 * 
	 * There shouldn't be a need to test this with multiple instances of paying with cash
	 * as the previous test had covered any weird behaviors that could have occurred.
	 * 
	 * Expected Result: The cart total should drop from $0.05 to $0. 
	 * Checking the amount paid should return a string value of "0.05".
	 * Checking the total change should return a string value of be "0.00'.
	 */
	@Test
	public void payFullCoinNoChange() {
		selfCheckoutStation.coinValidator.register(new CoinValidatorStub());
		paymentController.setCartTotal(BigDecimal.valueOf(0.05));
		try {
			// The customer pays the full $0.05 using a single nickel.
			while(coinFalseNegative) {
				selfCheckoutStation.coinSlot.accept(coinNickel);
			}
			assertTrue(BigDecimal.ZERO.compareTo(paymentController.getCartTotal()) == 0);
			assertEquals("0.05",paymentController.getAmountPaid());
			assertEquals("0.00",paymentController.getTotalChange());
			
		} catch (DisabledException e) {
			fail("A Disabled Exception should not have been thrown");
		}
	}
	
	/* Test Case: The customer pays over the total cart amount. 
	 * 
	 * Description: The cart total is set at $10. $50 is paid in a single bill.
	 * 
	 * There shouldn't be a need to test this with multiple instances of paying with cash
	 * as the previous test had covered any weird behaviors that could have occurred.
	 * 
	 * Expected Result: The cart total should drop from $10 to $0. 
	 * Checking the amount paid should return a string value of "50".
	 * Checking the total change should return a string value of be "40.00".
	 */
	@Test
	public void payFullBillWithChange_Test(){
		selfCheckoutStation.billValidator.register(new BillValidatorStub());
		paymentController.setCartTotal(BigDecimal.valueOf(10.00));
		try {
			// The customer pays the full fifty dollars using a single fifty dollar bill.
			System.out.println("Payment: " + billFifty.getValue());
			while(billFalseNegative) {
				selfCheckoutStation.billInput.accept(billFifty);
			}
			assertTrue(BigDecimal.ZERO.compareTo(paymentController.getCartTotal()) == 0);
			assertEquals("50.0",paymentController.getAmountPaid());
			assertEquals("40.0",paymentController.getTotalChange());
			
		} catch (DisabledException e) {
			fail("A Disabled Exception should not have been thrown");
		} catch (OverloadException e) {
			fail("An OverloadException should not have been thrown");
		} 
	}
	
	/* Test Case: The customer pays over the total cart amount. 
	 * 
	 * Description: The cart total is set at $1.75 and $2 is paid in a coin.
	 * 
	 * There shouldn't be a need to test this with multiple instances of paying with cash
	 * as the previous test had covered any weird behaviors that could have occurred.
	 * 
	 * Expected Result: The cart total should drop from $1.75 to $0. 
	 * Checking the amount paid should return a string value of "2".
	 * Checking the total change should return a string value of be "0.25".
	 */
	@Test
	public void payFullCoinWithChange_Test(){
		selfCheckoutStation.coinValidator.register(new CoinValidatorStub());
		paymentController.setCartTotal(BigDecimal.valueOf(1.75));
		try {
			// The customer pays the full balance using a single toonie ($2).
			System.out.println("Payment: " + coinToonie.getValue());
			while(coinFalseNegative) {
				selfCheckoutStation.coinSlot.accept(coinToonie);
			}
			assertTrue(BigDecimal.ZERO.compareTo(paymentController.getCartTotal()) == 0);
			assertEquals("2.00",paymentController.getAmountPaid());
			assertEquals("0.25",paymentController.getTotalChange());
			
		} catch (DisabledException e) {
			fail("A Disabled Exception should not have been thrown");
		}
	}

	/* Test Case: The customer pays over the total cart amount with a bill by 30 dollars and 
	 * the total change in bills only is dispensed. 
	 * 
	 * Description: The cart total is set at $20. $50 is paid in a single bill.
	 *    
	 * Whether or not the cart Total is dropping has been tested already. So it's not tested here.
	 * 
	 * Expected Result: The total change is calculated to 50-20 = 30
	 * Checking the total change should return a string value of "30.0".
	 * Checking the change due should return a double value of be "0.0" which is converted to string.
	 * The ejected bills should be $20 and $10, which is stored as an array converted to string.
	 */
	@Test
	public void totalChangeDueBillsThirtyDollars_Test() throws DisabledException, OverloadException{
		selfCheckoutStation.billValidator.register(new BillValidatorStub());
		billObserverStub = new BillDispenserStub();
		selfCheckoutStation.billDispensers.get(20).register(billObserverStub);
		selfCheckoutStation.billDispensers.get(10).register(billObserverStub);
		paymentController.setCartTotal(BigDecimal.valueOf(20.00));		
		while(billFalseNegative) {
			selfCheckoutStation.billInput.accept(billFifty);
		}
		assertEquals("30.0",paymentController.getTotalChange());
		assertEquals("0.0",""+paymentController.getChangeDue());
		assertEquals("[10, 20]",ejectedBills.toString());
	}
	
	/* Test Case: The customer pays over the total cart amount with a coin by $1.35 and 
	 * the total change in coins only is dispensed. 
	 * 
	 * Description: The cart total is set at $0.65. $2 is paid in a single coin.
	 *    
	 * Whether or not the cart Total is dropping has been tested already. So its not tested here.
	 * 
	 * Expected Result: The total change is calculated to 0.65-2.00 = 1.35
	 * Checking the total change should return a string value of "1.35".
	 * Checking the change due should return a double value of be "0.0" which is converted to string.
	 * The ejected coins should be $1, $0.25, and $0.10, which is stored as an array converted to string.
	 */
	@Test
	public void totalChangeDueCoinsDollarThirtyFive_Test() throws DisabledException{
		selfCheckoutStation.coinValidator.register(new CoinValidatorStub());
		coinObserverStub = new CoinDispenserStub();
		selfCheckoutStation.coinDispensers.get(new BigDecimal ("0.10")).register(coinObserverStub);
		selfCheckoutStation.coinDispensers.get(new BigDecimal ("0.25")).register(coinObserverStub);
		selfCheckoutStation.coinDispensers.get(new BigDecimal ("1.00")).register(coinObserverStub);
		paymentController.setCartTotal(BigDecimal.valueOf(0.65));		
		while(coinFalseNegative) {
			selfCheckoutStation.coinSlot.accept(coinToonie);
		}
		assertEquals("1.35",paymentController.getTotalChange());
		assertEquals("0.00",""+paymentController.getChangeDue());
		assertEquals("[0.10, 0.25, 1.00]",ejectedCoins.toString());
	}

	/* Test Case: To see if updateCartTotal functions properly.
	 * 
	 * Description: Update cart total will be called twice with 20.
	 * 
	 * Expected Result: The cart total should be 40.
	 */
	@Test
	public void cartUpdate_Test() {
		paymentController.updateCartTotal(BigDecimal.valueOf(20.00));
		paymentController.updateCartTotal(BigDecimal.valueOf(20.00));
		assertTrue(BigDecimal.valueOf(40.00).compareTo(paymentController.getCartTotal()) == 0);
	}
	
	/* Test Case: When the denom that should be emitted is empty, but this
	 * is not the smallest denom.
	 * 
	 * Description: Will pay $50 when the charge is $30, so that the change is
	 * $20. Thus, the denom $20 should be attempted to be ejected, but will be
	 * empty.
	 * 
	 * Expected: Expecting two tens to be emitted, and coverage to be improved.
	 */
	@Test
	public void emitEmptyNotSmallest_Test() throws OverloadException {
		selfCheckoutStation.billValidator.register(new BillValidatorStub());
		// Emptying billDispenser(20)
		selfCheckoutStation.billDispensers.get(20).unload();
		billObserverStub = new BillDispenserStub();
		selfCheckoutStation.billDispensers.get(20).register(billObserverStub);
		selfCheckoutStation.billDispensers.get(10).register(billObserverStub);
		paymentController.setCartTotal(BigDecimal.valueOf(30.00));		
		while(billFalseNegative) {
			selfCheckoutStation.billInput.accept(billFifty);
		}
		assertEquals("20.0",paymentController.getTotalChange());
		assertEquals("0.0",""+paymentController.getChangeDue());
		assertEquals("[10, 10]",ejectedBills.toString());
	}
	
	/* Test Case: The customer pays over the total cart amount by 5 dollars and the total change is dispensed. 
	 * 
	 * Description: The cart total is set at $45. $50 is paid in a single bill.So one 5 dollar bill is ejected for
	 * the customer.
	 *    
	 * There is no need to test for payments that would require coins, credit, or crypto.
	 * Whether or not the cart Total is dropping has been tested already. So its not tested here.
	 * The attendant should also not be notified in this boundary case, which is being tested here.
	 * 
	 * Expected Result: The total change is calculated to 50-45 = 5
	 * Checking the total change should return a string value of "5.0".
	 * Checking the change due should return a double value of be "0.0" which is converted to string.
	 */
	@Test
	public void totalChangeDueFiveDollars() throws DisabledException, OverloadException{
		selfCheckoutStation.billValidator.register(new BillValidatorStub());
		billObserverStub = new BillDispenserStub();
		selfCheckoutStation.billDispensers.get(5).register(billObserverStub);
		paymentController.setCartTotal(BigDecimal.valueOf(45.00));		
		while(billFalseNegative) {
			selfCheckoutStation.billInput.accept(billFifty);
		}
		assertEquals("5.0",paymentController.getTotalChange());	
		assertEquals("0.0",""+paymentController.getChangeDue());	
		assertEquals("[5]",ejectedBills.toString());
		assertFalse(attendantSignalled);
	}
	
}
