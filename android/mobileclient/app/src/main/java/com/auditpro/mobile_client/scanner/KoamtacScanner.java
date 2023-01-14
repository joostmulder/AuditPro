/*
  AuditPRO Mobile Client Android
  Copyright 2018 AuditPRO All Rights Reserved
 */
package com.auditpro.mobile_client.scanner;

import android.bluetooth.BluetoothDevice;
import android.content.Context;

import java.util.Timer;

import koamtac.kdc.sdk.KDCBarcodeDataReceivedListener;
import koamtac.kdc.sdk.KDCConnectionListener;
import koamtac.kdc.sdk.KDCConstants;
import koamtac.kdc.sdk.KDCData;
import koamtac.kdc.sdk.KDCReader;


/**
 * Implements the scanner interface for all Koamtac devices.
 * @author Eric Ruck
 */
public class KoamtacScanner extends ScannerInterface
		implements KDCConnectionListener, KDCBarcodeDataReceivedListener {
	private String connectedDetails;
	private KDCReader reader;
	private Timer reconnectTimer;

	/**
	 * Initializes a new instance of the Koamtac scanner.
	 *
	 * @param context Parent activity context
	 * @param delegate Delegate handles scanner events
	 */
	KoamtacScanner(Context context, ScannerDelegate delegate) {
		super(context, delegate);
		connectedDetails = "Scanner connector idle";
	}

	/**
	 * Gets the current connected state.
	 * @return Connected state
	 */
	@Override
	public boolean isConnected() {
		return connectedDetails == null;
	}

	/**
	 * Gets the current connection details.
	 * @return Current connection details
	 */
	@Override
	public String getConnectedDetails() {
		return connectedDetails != null ? connectedDetails : "Connected";
	}

	/**
	 * Handles application resume by reconnecting to the device.
	 */
	@Override
	public void resume() {
		if (reader == null) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					reader = new KDCReader(null, KoamtacScanner.this, null, null, null, null, KoamtacScanner.this, false);
				}
			}).start();
		}
	}

	/**
	 * Handles application pause by disconnecting from the device.
	 */
	@Override
	public void pause() {
		if (reader != null) {
//			reader.Disconnect();
			reader.Dispose();
			reader = null;
		}
		if (reconnectTimer != null) {
			reconnectTimer.cancel();
			reconnectTimer = null;
		}
	}

	@Override
	public void BarcodeDataReceived(KDCData kdcData) {
		String barcode = kdcData.GetData();
		String symbology = kdcData.GetBarcodeSymbology().GetName();
		delegate.onBarcode(barcode, symbology);
	}

	/**
	 * Handles scanner connection state change.
	 * @param bluetoothDevice Device on which scanner is connected
	 * @param state Current state
	 */
	@SuppressWarnings({"UnusedAssignment", "unused"})
	@Override
	public void ConnectionChanged(BluetoothDevice bluetoothDevice, int state) {
		boolean reconnect = false;
		switch (state) {
			case KDCConstants.CONNECTION_STATE_CONNECTED:
			case KDCConstants.CONNECTION_STATE_CONNECTED_XP67:
				connectedDetails = null;
				break;
			case KDCConstants.CONNECTION_STATE_FAILED:
				connectedDetails = "Failed to connect to scanner";
				reconnect = true;
				break;
			case KDCConstants.CONNECTION_STATE_LISTEN:
				connectedDetails = "Listening for scanner";
				break;
			case KDCConstants.CONNECTION_STATE_LOST:
				connectedDetails = "Lost connection to scanner";
				reconnect = true;
				break;
			case KDCConstants.CONNECTION_STATE_CONNECTING:
				connectedDetails = "Connecting to scanner";
				break;
			case KDCConstants.CONNECTION_STATE_NONE:
				connectedDetails = "No connection to scanner";
				reconnect = true;
				break;
			default:
				// Unknown status
				return;
		}

		// Send the connection update to the delegate
		delegate.onConnected(isConnected(), getConnectedDetails());
/*
		if ((reconnect == true) && (reconnectTimer == null)) {
			pause();
			reconnectTimer = new Timer();
			reconnectTimer.schedule(new TimerTask() {
				@Override
				public void run() {
					resume();
					reconnectTimer.cancel();
					reconnectTimer = null;
				}
			}, 30000);
		}
*/
	}
}
