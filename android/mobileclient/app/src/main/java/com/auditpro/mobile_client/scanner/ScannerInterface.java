/*
  AuditPRO Mobile Client Android
  Copyright 2018 AuditPRO All Rights Reserved
 */
package com.auditpro.mobile_client.scanner;

import android.content.Context;


/**
 * Provides a generic scanner interface to access any supported device.
 * @author Eric Ruck
 */
@SuppressWarnings("unused")
public abstract class ScannerInterface {
	ScannerDelegate delegate;
	protected Context context;

	/**
	 * Base class instantiation receives minimum inputs and stores in class.
	 * @param context  Parent activity context
	 * @param delegate Delegate handles scanner events
	 */
	ScannerInterface(Context context, ScannerDelegate delegate) {
		this.context = context;
		this.delegate = delegate;
	}

	/**
	 * Gets the current connected state.
	 * @return Connected state
	 */
	abstract public boolean isConnected();

	/**
	 * Gets the current connection details.
	 * @return Current connection details
	 */
	abstract public String getConnectedDetails();

	/**
	 * Handles application resume by reconnecting to the device.
	 */
	abstract public void resume();

	/**
	 * Handles application pause by disconnecting from the device.
	 */
	abstract public void pause();
}