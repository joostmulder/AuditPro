package com.auditpro.mobile_client.actions;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.ParcelUuid;
import android.util.Log;

import com.auditpro.mobile_client.entities.Receipt;
import com.auditpro.mobile_client.test.R;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Prints a receipt to a Bluetooth Zebra printer.
 * @author Eric Ruck
 */
@SuppressWarnings("WeakerAccess")
public class PrintReceiptAction {

	/**
	 * Provides explicit default constructor.
	 */
	public PrintReceiptAction() { }

	/**
	 * Gets the error message preventing printing.  Hardened as it looks like some versions of the OS
	 * under some circumstances can call us with a null context.
	 * @param context Application context for resources
	 * @return Error message or null if none
	 */
	@SuppressLint("DefaultLocale")
	public String getErrorMessage(Context context) {
		if (errorMessageId == 0) {
			// No error
			return null;
		}
		if (context == null) {
			// No context, use hard coded placeholder
			return String.format("Failed to access printer (%d)", errorMessageId);
		}
		try {
			// Load the string resource
			return context.getString(errorMessageId);
		} catch (Resources.NotFoundException exc) {
			// Failed to load the string resource
			return String.format("Failed to access printer, unknown error (%d)", errorMessageId);
		}
	}

	/**
	 * Gets the permissions message required to address before printing.
	 * @param context Application context for resources
	 * @return Permission message or null if none
	 */
	public String getPermissionMessage(Context context) {
		return (permissionMessageId == 0) ? null : context.getString(permissionMessageId);
	}

	/** Defines the no printer battery result code. */
	public static final int BATTERY_STATUS_NO_PRINTER = 0;

	/** Defines inability to communicate with printer code. */
	public static final int BATTERY_STATUS_PRINTER_ERROR = -1;

	/** Unexpected or invalid battery charge response from printer. */
	public static final int BATTERY_STATUS_PARSE_ERROR = -2;

	/** Unexpected interription attempting to read printer battery. */
	public static final int BATTERY_STATUS_INTERRUPTED_ERROR = -3;

	/** Timed out waiting for battery respnse from printer. */
	public static final int BATTERY_STATUS_TIMEOUT_ERROR = -4;

	/**
	 * Reads the battery state. This call is blocking, use on a worker thread only.
	 * @return Battery percentage 1..100 or a BATTERY_STATUS code
	 */
	public int readBattery() {
		// Get the printer
		BluetoothDevice printer = findPrinter();
		if (printer == null) {
			return BATTERY_STATUS_NO_PRINTER;
		}

		// Connect and query
		try (BluetoothSocket socket = printer.createRfcommSocketToServiceRecord(UUID_SERIAL)) {
			// Connect to the printer
			socket.connect();
			try (OutputStream output = socket.getOutputStream()) {
				// Write the battery command
				String doc = "! U1 getvar \"power.percent_full\"\r\n";
				output.write(doc.getBytes(StandardCharsets.US_ASCII));
				try (InputStream input = socket.getInputStream()) {
					// Wait max one second for response
					byte[] response = new byte[100];
					for (int i = 0; i < 10; ++i) {
						if (input.available() > 0) {
							// Read and parse the response
							int len = input.read(response);
							String parse = new String(response, 0, len, "UTF-8");
							Pattern pat = Pattern.compile("\\d+");
							Matcher m = pat.matcher(parse);
							if (m.find()) {
								// Return the battery percent found in the response
								String found = m.group(0);
								return NumberFormat.getInstance().parse(found).intValue();
							}

							// Invalid or unexpected response
							Log.e(LOG_TAG, "Error parsing battery charge response from printer");
							errorMessageId = R.string.print_battery_parse_error;
							return BATTERY_STATUS_PARSE_ERROR;
						}

						// Wait for data from the printer
						Thread.sleep(100);
					}
				}

				// Timed out
				Log.e(LOG_TAG, "Timeout accessing printer battery charge");
				errorMessageId = R.string.print_battery_timeout_error;
				return BATTERY_STATUS_TIMEOUT_ERROR;
			}
		} catch (IOException exc) {
			Log.e(LOG_TAG, "Error accessing printer", exc);
			errorMessageId = R.string.print_battery_io_error;
			return BATTERY_STATUS_PRINTER_ERROR;
		} catch (ParseException exc) {
			Log.e(LOG_TAG, "Error parsing battery charge response from printer", exc);
			errorMessageId = R.string.print_battery_parse_error;
			return BATTERY_STATUS_PARSE_ERROR;
		} catch (InterruptedException exc) {
			Log.e(LOG_TAG, "Error accessing printer battery charge interrupted", exc);
			errorMessageId = R.string.print_battery_interrupted_error;
			return BATTERY_STATUS_INTERRUPTED_ERROR;
		}
	}

	/**
	 * Prints a receipt. This call is blocking, use on a worker thread only.
	 * @param receipt Receipt to print
	 */
	public void printReceipt(Receipt receipt) {
		// Make sure our state is valid
		if (isComplete) {
			// Nothing to do, instance cannot be reused
			Log.w(LOG_TAG, "Attempted to reuse completed receipt printer");
		}

		// Get the printer
		BluetoothDevice printer = findPrinter();
		if (printer == null) {
			return;
		}

		// Connect and print
		try (BluetoothSocket socket = printer.createRfcommSocketToServiceRecord(UUID_SERIAL)) {

			// Connect to the printer
			socket.connect();
			try (OutputStream output = socket.getOutputStream()) {
				// Write the document to the printer in chunks
				int offset = 0;
				byte[] data = receipt.formatZpl().getBytes(StandardCharsets.US_ASCII);
				while (offset < data.length) {
					// Write the next chunk
					int length = data.length - offset;
					if (length > 1000) {
						length = 1000;
					}
					output.write(data, offset, length);
					offset += length;
					Thread.sleep(250);
				}
			}
			isComplete = true;
		} catch (IOException exc) {
			Log.e(LOG_TAG, "Error writing to printer", exc);
			errorMessageId = R.string.print_receipt_io_error;
		} catch (InterruptedException exc) {
			Log.e(LOG_TAG, "Interrupted writing to printer", exc);
			errorMessageId = R.string.print_receipt_interrupted;
		}

		// We're done
		isComplete = true;
	}

	/**
	 * Requests the Bluetooth settings from the operating system.
	 * @param context Application context
	 */
	public void requestBluetoothSettings(Context context) {
		// Reset permission state
		permissionMessageId = 0;

		// Show the Bluetooth settings
		Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
		context.startActivity(enableBluetooth);
	}

	/**
	 * Finds the Bluetooth printer.  Returns null and updates internal state if not found.
	 * @return Printer device or null
	 */
	private BluetoothDevice findPrinter() {
		// Do we have a Bluetooth adapter?
		BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
		if (adapter == null) {
			// Either we have no adapter, or do not have permission to use it
			Log.w(LOG_TAG, "No Bluetooth adapter available");
			errorMessageId = R.string.print_receipt_no_bluetooth;
			isComplete = true;
			return null;
		}

		// Is the Bluetooth adapter enabled?
		if (!adapter.isEnabled()) {
			// Ask permission to turn the Bluetooth adapter on
			Log.d(LOG_TAG, "Request permission to turn Bluetooth on");
			permissionMessageId = R.string.print_receipt_permission;
			return null;
		}

		// Pick through the paired devices
		for (BluetoothDevice device : adapter.getBondedDevices()) {
			// Is this device a printer?
			BluetoothClass btclass = device.getBluetoothClass();
			int ful = btclass.getDeviceClass();
			if ((ful & 0x680) == 0x680) {
				// Look for the printer feature ID
				for (ParcelUuid feature : device.getUuids()) {
					if (feature.getUuid().equals(UUID_SERIAL)) {
						// Printer found
						return device;
					}
				}
			}
		}

		// No printer found
		errorMessageId = R.string.print_receipt_no_printer;
		isComplete = true;
		return null;
	}

	private static final String LOG_TAG = "PrintReceiptAction";
	private static final UUID UUID_SERIAL = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

	private int errorMessageId;
	private int permissionMessageId;
	private boolean isComplete;
}
