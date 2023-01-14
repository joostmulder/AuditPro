/*
  AuditPRO Mobile Client Android
  Copyright 2018 AuditPRO All Rights Reserved
 */
package com.auditpro.mobile_client;

import android.util.Log;


/**
 * Supplies application specific exception details and logging.
 * @author Eric Ruck
 */
public class MobileClientException extends Exception {
	private static final String LOG_TAG = "MobileClientException";
	public MobileClientException(String message) {
		super(message);
		Log.e(LOG_TAG, message);
	}
	public MobileClientException(String message, Exception inner) {
		super(message, inner);
		Log.e(LOG_TAG, message, inner);
	}
}
