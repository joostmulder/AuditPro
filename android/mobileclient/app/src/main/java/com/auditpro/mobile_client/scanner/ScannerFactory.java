/*
  AuditPRO Mobile Client Android
  Copyright 2018 AuditPRO All Rights Reserved
 */
package com.auditpro.mobile_client.scanner;

import android.content.Context;


/**
 * Creates an instance of a scanner interface to access the hardware services.
 * @author Eric Ruck
 */
public class ScannerFactory {
	/**
	 * Prevents instantiation.
	 */
	private ScannerFactory() { }

	/**
	 * Gets an instance of a potentially available scanner.
	 * TODO: Make this smarter to figure out which scanners we might be able to access.
	 * Could be by configuration, looking through the paired devices, whatever.  Also might
	 * have to be smart about turning on Bluetooth here.  We're going to punt on this logic
	 * for now because the initial application will only support Koamtac, probably.
	 * @return ScannerInterface instance or null if none.
	 */
	public static ScannerInterface getScanner(Context context, ScannerDelegate delegate) {
		// TODO
		return new KoamtacScanner(context, delegate);
	}
}
