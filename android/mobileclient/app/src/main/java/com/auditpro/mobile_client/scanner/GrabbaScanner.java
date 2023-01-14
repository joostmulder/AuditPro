package com.auditpro.mobile_client.scanner;

import android.content.Context;

import com.grabba.Grabba;
import com.grabba.GrabbaBarcode;
import com.grabba.GrabbaBarcodeListener;
import com.grabba.GrabbaBarcodeSymbology;
import com.grabba.GrabbaButtonListener;
import com.grabba.GrabbaConnectionListener;
import com.grabba.GrabbaDriverNotInstalledException;
import com.grabba.GrabbaException;


/**
 * Simplified interface for Grabba scanner.
 * Created by ericruck on 10/14/17.
 * @author Eric Ruck
 */
public class GrabbaScanner extends ScannerInterface {
	private String connectedDetails;
	private boolean installed;

	/**
	 * Initializes a new instance of the Grabba scanner.
	 *
	 * @param context Parent activity context
	 * @param delegate Delegate handles scanner events
	 */
	public GrabbaScanner(Context context, ScannerDelegate delegate) {
		super(context, delegate);
		try {
			// Open access to the scanner
			Grabba.open(context, "AuditPro Mobile Client");
			connectedDetails = "Internal Error, Access Not Resumed";
			installed = true;
		} catch (GrabbaDriverNotInstalledException exc) {
			// The driver is not installed
			connectedDetails = "Grabba Driver Not Installed";
		}
	}

	/**
	 * Gets the current connected state.
	 * @return Connected state
	 */
	public boolean isConnected() {
		return connectedDetails == null;
	}

	/**
	 * Gets the current connection details.
	 * @return Current connection details
	 */
	public String getConnectedDetails() {
		return connectedDetails != null ? connectedDetails : "Connected";
	}

	/**
	 * Handles application resume by reconnecting to the device.
	 */
	public void resume() {
		if (installed) {
			// Register for Grabba events
			Grabba g = Grabba.getInstance();
			g.addConnectionListener(grabbaConnectionListener);
			g.addButtonListener(grabbaButtonListener);

			// Register for barcode events
			GrabbaBarcode.getInstance().addEventListener(grabbaBarcodeListener);

			// Get the device as soon as it is available
			connectedDetails = "Not Connected";
			g.acquireGrabba();
		}
	}

	/**
	 * Handles application pause by disconnecting from the device.
	 */
	public void pause() {
		if (installed) {
			// Release Grabba to allow other applications access and prevent unintentional triggers
			Grabba g = Grabba.getInstance();
			g.removeConnectionListener(grabbaConnectionListener);
			g.removeButtonListener(grabbaButtonListener);
			GrabbaBarcode.getInstance().removeEventListener(grabbaBarcodeListener);
			g.releaseGrabba();
			connectedDetails = "Internal Error, Access Not Resumed";
		}
	}

	/**
	 * Implements button listener.
	 */
	private final GrabbaButtonListener grabbaButtonListener = new GrabbaButtonListener() {

		/**
		 * Handles right button event.
		 * @param pressed Right button pressed (vs released) flag
		 */
		@Override
		public void grabbaRightButtonEvent(boolean pressed) {
			handleButton(false, pressed);
		}

		/**
		 * Handles left button event.
		 * @param pressed Left button pressed (vs released) flag
		 */
		@Override
		public void grabbaLeftButtonEvent(boolean pressed) {
			handleButton(true, pressed);
		}

		/**
		 * Provides common handling for both buttons.
		 * @param isLeft Left (vs right) flag
		 * @param isPressed Pressed (vs released) flag
		 */
		private void handleButton(boolean isLeft, boolean isPressed) {
			// Offer to delegate
			if (delegate.onButton(isLeft, isPressed)) {
				// Handled by delegate
				return;
			}
			if (isPressed) {
				try {
					// The button events don't fire on the UI thread so we can safely call the Grabba barcode trigger function
					GrabbaBarcode.getInstance().trigger(true);
				} catch (GrabbaException e) {
					delegate.onError(e.getMessage(), e.toString());
				}
			}
		}
	};

	/**
	 * Implements barcode listener.
	 */
	private final GrabbaBarcodeListener grabbaBarcodeListener = new GrabbaBarcodeListener() {

		@Override
		public void barcodeTriggeredEvent() {
			delegate.onScanning(true, "Scanning");
		}

		@Override
		public void barcodeTimeoutEvent() {
			delegate.onScanning(false, "Timeout");
		}

		@Override
		public void barcodeScanningStopped() {
			delegate.onScanning(false, "Stopped");
		}

		@Override
		public void barcodeScannedEvent(String barcode, int symbologyType) {
			delegate.onBarcode(barcode,
				GrabbaBarcodeSymbology.getSymbologyStringFromIndex(symbologyType));
		}
	};

	/**
	 * Implements scanner connection listener.
	 */
	private final GrabbaConnectionListener grabbaConnectionListener = new GrabbaConnectionListener() {

		@Override
		public void grabbaDisconnectedEvent() {
			updateConnection("Disconnected");
		}

		@Override
		public void grabbaConnectedEvent() {
			updateConnection(null);
		}

		private void updateConnection(String details) {
			connectedDetails = details;
			delegate.onConnected(isConnected(), getConnectedDetails());
		}
	};
}
