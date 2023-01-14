/*
  AuditPRO Mobile Client Android
  Copyright 2018 AuditPRO All Rights Reserved
 */
package com.auditpro.mobile_client.oldstuff;

/*
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.ParcelUuid;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.auditpro.mobile_client.entities.ReceiptTest;
import com.auditpro.mobile_client.test.R;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Locale;
import java.util.Set;


public class MainActivity extends AppCompatActivity {
	// PARTIAL
	onCreate {
// Note: Assuming we never need the floating button, kill it?
//		FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//		fab.setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View view) {
//				Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//						.setAction("Action", null).show();
//			}
//		});
//
//		findViewById(R.id.findButton).setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View view) {
//				onFindDevice(null);
//			}
//		});
//		findViewById(R.id.shortButton).setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View view) {
//				onShortReceipt();
//			}
//		});
//		findViewById(R.id.longButton).setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View view) {
//				onLongReceipt();
//			}
//		});
//
//		// Connect to the scanner
//		scanner = ScannerFactory.getScanner(this, this);
//		updateScannerConnection();
	}

	@Override
	protected void onResume() {
		super.onResume();
//		scanner.resume();
//		updateScannerConnection();
	}

	@Override
	protected void onPostResume() {
		super.onPostResume();
//		scanner.resume();
	}

	@Override
	protected void onPause() {
		super.onPause();
//		scanner.pause();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		//noinspection SimplifiableIfStatement
		if (id == R.id.action_settings) {
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	 * Prints a short test receipt.
	private void onShortReceipt() {
		ReceiptTest receipt = new ReceiptTest("Demo Short ReceiptTest", "My Favorite Store #1", null);
		receipt.addItem("10001", "Cadbury Cream Eggs");
		receipt.addItem("123456", "Kraft Candy Corn");
		receipt.addItem("456", "This Is A Really Long Product Name, Vanilla");
		onFindDevice(receipt);
	}

	 * Prints a long test receipt.
	@SuppressLint("SetTextI18n")
	private void onLongReceipt() {
		// Find out how many items
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("How Many Items?");
		final EditText input = new EditText(this);
		input.setInputType(InputType.TYPE_CLASS_NUMBER);
		input.setText("50");
		builder.setView(input);
		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// Parse the result
				int countItems;
				try {
					countItems = Integer.parseInt(input.getText().toString());
				} catch (NumberFormatException e) {
					countItems = 0;
				}
				if (countItems > 0) {
					// Print the receipt
					ReceiptTest receipt = new ReceiptTest("Demo Long ReceiptTest", "Some Store PA/MD/VA/DE #1234", null);
					for (int index = 0; index < countItems; ++index) {
						receipt.addItem(
								String.format(Locale.getDefault(), "10%03d", index),
								String.format(Locale.getDefault(), "Some Product %d", index));
					}
					onFindDevice(receipt);
				}
				dialog.dismiss();
			}
		});
		builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});
		builder.show();
	}

	 * Finds the printer device.
	private void onFindDevice(ReceiptTest receipt) {
		try {
			BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();

			if(adapter == null) {
				Log.w(LOG_TAG, "No Bluetooth adapter available");
				Toast.makeText(this, R.string.error_no_bt_adapter, Toast.LENGTH_LONG).show();
				return;
			}

			if(!adapter.isEnabled()) {
				// Note: Resume after enabled?
				Log.d(LOG_TAG, "Requesting Bluetooth enable");
				Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
				startActivityForResult(enableBluetooth, 0);
				return;
			}

			// Pick through the paired devices
			BluetoothDevice printer = null;
			Set<BluetoothDevice> pairedDevices = adapter.getBondedDevices();
			if(pairedDevices.size() > 0) {
				for (BluetoothDevice device : pairedDevices) {
					// Collect information about the device
					Log.i(LOG_TAG, "Found device: " + device.getName());

					// Log the class
					BluetoothClass btclass = device.getBluetoothClass();
					int maj = btclass.getMajorDeviceClass();
					int ful = btclass.getDeviceClass();
					Log.i(LOG_TAG, "   major class: x" + Integer.toHexString(maj) + " - " + Integer.toString(maj));
					Log.i(LOG_TAG, "   class: x" + Integer.toHexString(ful) + " - " + Integer.toString(ful));

					// Log the features
					ParcelUuid[] features = device.getUuids();
					Log.i(LOG_TAG, "   features: " + TextUtils.join(", ", features));

					// Is this device a printer?
					if (((ful & 0x680) == 0x680) && Arrays.asList(features).contains(UUID_SERIAL)) {
						// Yes, do we already have a printer?
						if (printer == null) {
							// No, but we do now
							Log.i(LOG_TAG, "   Printer found!");
							printer = device;
						} else {
							// Additional printer
							Log.i(LOG_TAG, "   Additional printer found");
						}
					} else {
						// Not a printer
						Log.i(LOG_TAG, "   Not a printer");
					}
				}
			}

			if (printer == null) {
				// No printer found
				Toast.makeText(this, R.string.msg_find_no_printer, Toast.LENGTH_LONG).show();
				return;
			}

			// Connect to the printer
			Log.d(LOG_TAG, "Creating socket to printer " + printer.getName());
			BluetoothSocket socket = printer.createRfcommSocketToServiceRecord(UUID_SERIAL.getUuid());
			socket.connect();
			Log.d(LOG_TAG, "Writing comment to printer");
			OutputStream output = socket.getOutputStream();
			String test;
			if (receipt == null) {
				// Print sample hello world output
				test = "! U1 setvar \"device.languages\" \"zpl\"\r\n ^XA^LL50^FO25,0^FB516,3,0,C,0^CF0,40" +
						"^FDThis Should Be Centered Hopefully Across Two Lines If This Text Is Long Enough^FS" +
						"^XZ";
			} else {
				// Print from receipt
				test = receipt.formatZpl();
			}

//			String test = "! U1 setvar \"device.languages\" \"zpl\"\r\n ^XA^LL200^FO50,50^ADN,36,20^FDHello World^FS^XZ";
//			String test = "! U1 setvar \"device.languages\" \"zpl\"\r\n ^XA^LL300^FO25,0^FB516,3,0,C,0^CFA,20" +
//					"^FDThis Should Be Centered Hopefully Across Two Lines If This Text Is Long Enough^FS" +
//					"^FO25,60^FD12345^FS" +
//					"^FO160,60^FDProduct Name^FS" +
//					"^XZ";

//			String test = "! U1 setvar \"device.languages\" \"line_print\"\r\nThis is test output\n";
//			String test = "! U1 setvar \"device.languages\" \"zpl\"\r\n ^XA^CF0,15,10^FO0,25^FB250,100,0,C,0^FDHello World^FS^XZ";
//			String test = "^XA^CF0,20,15^FO0,25^FB400,100,0,C,0^FDHello World^FS^XZ";

//			String test = "! U1 setvar \"device.languages\" \"epl\"\r\n " +
//					"N\n" +
//					"A50,0,0,1,1,1,N,\"Example 1\"\n" +
//					"A50,50,0,2,1,1,N,\"Example 2\"\n" +
//					"A50,100,0,3,1,1,N,\"Example 3\"\n" +
//					"A50,150,0,4,1,1,N,\"Example 4\"\n" +
//					"A50,200,0,5,1,1,N,\"Example 5\"\n" +
//					"A50,300,0,3,2,2,R,\"Example 6\"\n" +
//					"P1\n";

			byte[] data = StandardCharsets.US_ASCII.encode(test).array();
			int offset = 0;
			while (offset < data.length) {
				int length = data.length - offset;
				if (length > 1000) {
					length = 1000;
				}
				output.write(data, offset, length);
				offset += length;
				Thread.sleep(250);
			}

			// Complete the process
			Log.d(LOG_TAG, "Closing printer");
			output.close();
			socket.close();
			Toast.makeText(this, R.string.msg_find_complete, Toast.LENGTH_LONG).show();

		} catch(Exception e) {
			e.printStackTrace();
			Log.d(LOG_TAG, "Failed to print");
			Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
		}
	}

	 * Displays the current scanner state.
	private void updateScannerConnection() {
		((TextView)findViewById(R.id.grabbaState)).setText(scanner.getConnectedDetails());
	}

	 * Handles connection state change.
	 *
	 * @param isConnected Current connected state
	 * @param details     Connection details
	@Override
	public void onConnected(boolean isConnected, String details) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				updateScannerConnection();
			}
		});
	}

	 * Handles an error from the device.
	 *
	 * @param message Display message
	 * @param details Internal details for debugging
	@Override
	public void onError(final String message, String details) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				((TextView)findViewById(R.id.grabbaError)).setText(message);
			}
		});
	}

	 * Handles button state change.  Return false for default behavior, to allow the scanner
	 * driver to initiate a scan.
	 *
	 * @param isLeft    Left (vs right) button flag
	 * @param isPressed Pressed (vs released) flag
	 * @return Handled flag
	@Override
	public boolean onButton(final boolean isLeft, final boolean isPressed) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				((TextView)findViewById(R.id.grabbaEvent)).setText(String.format("%s button %s",
						isLeft ? "left" : "right", isPressed ? "pressed" : "released"));
			}
		});
		return false;
	}

	 * Handles scanning state change.
	 *
	 * @param isScanning Is scanning now flag
	 * @param details    Internal details for debugging
	@Override
	public void onScanning(final boolean isScanning, final String details) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				((TextView)findViewById(R.id.grabbaEvent)).setText("Barcode " + details);
				if (isScanning) {
					((TextView)findViewById(R.id.grabbaBarcode)).setText("");
				}
			}
		});
	}

	 * Handles receipt of a barcode from the scanner.
	 *
	 * @param barcode   Scanned barcode
	 * @param symbology Symbology description
	@Override
	public void onBarcode(final String barcode, final String symbology) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				((TextView)findViewById(R.id.grabbaBarcode)).setText(barcode);
			}
		});
	}
}
 */