
package com.autovend.software;

import java.math.BigDecimal;

/**
 * TEMPORARY LISTENER THAT SHOULD BE MERGED INTO ATTENDANT IO
 *
 */
public interface AttendantIOTempPurchaseOwnBags extends AttendantIO{
	
	/**
	 * Simulates an attendant being informed of a change discrepancy.
	 * Returns nothing.
	 * 
	 * @param bigDecimal
	 * 				Amount of change left that must be given to customer
	 */
	public void changeRemainsNoDenom(BigDecimal changeLeft);
	
	/**
	 * Simulates informing the attendant that a duplicate receipt 
	 * must be printed because the printer is out of ink or paper.
	 * Printing is aborted, the station will be suspended and the
	 * attendant will also be informed that the station needs maintenance.
	 */
	public void printDuplicateReceipt();
}
