/*
  AuditPRO Mobile Client Android
  Copyright 2018 AuditPRO All Rights Reserved
 */
package com.auditpro.mobile_client.scanner;


/**
 * Implemented by client applications that want to provide a delegate to receive events from
 * a scanner.
 * @author Eric Ruck
 */
public interface ScannerDelegate {
	/**
	 * Handles connection state change.
	 * @param isConnected Current connected state
	 * @param details Connection details
	 */
	void onConnected(boolean isConnected, String details);

	/**
	 * Handles an error from the device.
	 * @param message Display message
	 * @param details Internal details for debugging
	 */
	void onError(String message, String details);

	/**
	 * Handles button state change.  Return false for default behavior, to allow the scanner
	 * driver to initiate a scan.
	 *
	 * @param isLeft Left (vs right) button flag
	 * @param isPressed Pressed (vs released) flag
	 * @return Handled flag
	 */
	boolean onButton(boolean isLeft, boolean isPressed);

	/**
	 * Handles scanning state change.
	 * @param isScanning Is scanning now flag
	 * @param details Internal details for debugging
	 */
	void onScanning(boolean isScanning, String details);

	/**
	 * Handles receipt of a barcode from the scanner.
	 * @param barcode Scanned barcode
	 * @param symbology Symbology description
	 */
	void onBarcode(String barcode, String symbology);
}
