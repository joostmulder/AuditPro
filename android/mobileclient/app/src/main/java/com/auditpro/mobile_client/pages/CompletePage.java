/*
  AuditPRO Mobile Client Android
  Copyright 2018 AuditPRO All Rights Reserved
 */
package com.auditpro.mobile_client.pages;

import android.content.Context;
import android.content.DialogInterface;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.auditpro.mobile_client.Analytics;
import com.auditpro.mobile_client.MainActivity;
import com.auditpro.mobile_client.MobileClientException;
import com.auditpro.mobile_client.actions.PrintReceiptAction;
import com.auditpro.mobile_client.controls.BatteryIcon;
import com.auditpro.mobile_client.database.AuditDatabase;
import com.auditpro.mobile_client.database.BaseDatabase;
import com.auditpro.mobile_client.database.StoresDatabase;
import com.auditpro.mobile_client.entities.Audit;
import com.auditpro.mobile_client.entities.Notes;
import com.auditpro.mobile_client.entities.Product;
import com.auditpro.mobile_client.entities.Receipt;
import com.auditpro.mobile_client.security.Security;
import com.auditpro.mobile_client.test.R;

import java.lang.ref.WeakReference;
import java.util.Date;
import java.util.List;


/**
 * Manages the page to complete an audit.
 * @author Eric Ruck
 */
@SuppressWarnings("ConstantConditions")
public class CompletePage extends BasePage {

	/**
	 * Required empty public constructor
	 */
	public CompletePage() { }

	/**
	 * Creates a new page to complete the passed audit.
	 * @param audit Audit to complete
	 * @return A new instance of fragment CompletePage
	 */
	public static CompletePage newInstance(Audit audit) {
		CompletePage fragment = new CompletePage();
		Bundle args = new Bundle();
		args.putParcelable(ARG_AUDIT, audit);
		fragment.setArguments(args);
		return fragment;
	}

	/**
	 * Handles creation by unpacking our arguments.
	 * @param savedInstanceState Optional state
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			audit = getArguments().getParcelable(ARG_AUDIT);
		}
	}

	/**
	 * Creates our view.
	 * @param inflater Use to hydrate our layout
	 * @param container Parent view for page
	 * @param savedInstanceState Optional state
	 * @return Created view
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.fragment_complete_page, container, false);
	}

	/**
	 * Initializes our view once created.
	 * @param view View to initialize
	 * @param savedInstanceState Optional state
	 */
	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		// Initialize the controls on the view
		((TextView) view.findViewById(R.id.auditNameText)).setText(audit.toString());
		view.findViewById(R.id.printButton).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				onPrint();
			}
		});
		view.findViewById(R.id.notesButton).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onNotes();
			}
		});
		view.findViewById(R.id.closeButton).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				onClose(true, true);
			}
		});

		// Setup the printer battery level
		ImageButton printBattery = view.findViewById(R.id.printBattery);
		batteryIcon = new BatteryIcon(printBattery, getString(R.string.battery_device_printer));
		printBattery.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Toast.makeText(getContext(), batteryIcon.describeState(getContext()), Toast.LENGTH_SHORT).show();
			}
		});
	}

	/**
	 * Attempts to read the printer battery on a worker thread once the page starts.
	 */
	@Override
	public void onStart() {
		super.onStart();
		new PrinterBatteryTask(this).execute();
	}

	/**
	 * Gets the title to display for this page.
	 * @param context Application context for string resource lookup
	 * @return Complete page title
	 */
	@Override
	public String getPageName(Context context) {
		return context.getString(R.string.message_complete_title);
	}

	/**
	 * Enables geolocation updates when the page appears.
	 */
	@Override
	public void onPageAppearing() {
		// Make sure we're attempting to access the GPS for a current reading
		super.onPageAppearing();
		isGpsRequested = ((MainActivity) getActivity()).startLocationUpdates();
		try (AuditDatabase db = new AuditDatabase(getContext())) {
			// Update the notes button title text to reflect current state
			((Button) getView().findViewById(R.id.notesButton)).setText(
					db.getNotes(audit).getContents().length() > 0
					? R.string.message_complete_edit_notes_button
					: R.string.message_complete_add_notes_button);
		} catch (MobileClientException e) {
			// Exception logged, no further action necessary
		}
	}

	/**
	 * Suspends GPS request when we're disappearing.
	 */
	@Override
	public void onPageDisappearing() {
		super.onPageDisappearing();
		((MainActivity) getActivity()).endLocationUpdates();
	}

	/**
	 * Handles the request to print the reorder form.
	 */
	private void onPrint() {
		// Create the receipt to print
		Security sec = new Security(getContext().getApplicationContext());
		Receipt receipt = new Receipt(sec.getClientName(),
				audit.getStoreDescr(), BaseDatabase.readDateTime(completeTime));
		List<Product> products = null;
		try (StoresDatabase db = new StoresDatabase(getContext())) {
			products = db.getProductsForStore(audit.getStoreId());
		} catch (MobileClientException exc) {
			Toast.makeText(getContext(), exc.getMessage(), Toast.LENGTH_SHORT).show();
			return;
		}
		try (AuditDatabase db = new AuditDatabase(getContext())) {
			db.populateReceipt(receipt, audit, products);
		} catch (MobileClientException exc) {
			Toast.makeText(getContext(), exc.getMessage(), Toast.LENGTH_SHORT).show();
			return;
		}

		// Can we still print the record
		IPageParent parent = getParent();
		if (parent != null) {
			// Print the receipt
			Analytics.log("Print", audit);
			new PrintTask((MainActivity) parent, receipt).execute();
		}
	}

	/**
	 * Prints on a worker thread, with an interface back to the user interface.
	 */
	private static class PrintTask extends AsyncTask<Void, Void, Void> {

		PrintTask(MainActivity activity, Receipt receipt) {
			this.action = new PrintReceiptAction();
			this.activityRef = new WeakReference<>(activity);
			this.receipt = receipt;
		}

		@Override
		protected void onPreExecute() {
			MainActivity activity = activityRef.get();
			if (activity != null) {
				activity.setActivity(true);
			}
		}

		@Override
		protected Void doInBackground(Void... voids) {
			action.printReceipt(receipt);
			return null;
		}

		@Override
		protected void onPostExecute(Void aVoid) {
			super.onPostExecute(aVoid);

			// Are we still connected?
			final MainActivity activity = activityRef.get();
			if (activity != null) {
				// Handle action results
				activity.setActivity(false);
				String message = action.getErrorMessage(activity);
				if (message != null) {
					// Show the error
					Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
					return;
				}
				message = action.getPermissionMessage(activity);
				if (message != null) {
					// Offer permission option
					new AlertDialog.Builder(activity)
							.setTitle(R.string.print_receipt_permission_title)
							.setMessage(message)
							.setNegativeButton(R.string.button_no, new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialogInterface, int i) {
									dialogInterface.dismiss();
								}
							})
							.setPositiveButton(R.string.button_yes, new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialogInterface, int i) {
									dialogInterface.dismiss();
									action.requestBluetoothSettings(activity);
								}
							})
							.show();
					return;
				}

				// We must have succeeded
				Toast.makeText(activity, R.string.print_receipt_success, Toast.LENGTH_SHORT).show();
			}
		}

		private PrintReceiptAction action;
		private WeakReference<MainActivity> activityRef;
		private Receipt receipt;
	}

	/**
	 * Gets the printer battery status on a worker thread, with an interface back to the user interface.
	 */
	private static class PrinterBatteryTask extends AsyncTask<Void, Void, Integer> {

		PrinterBatteryTask(CompletePage page) {
			this.action = new PrintReceiptAction();
			this.pageRef = new WeakReference<>(page);
		}

		@Override
		protected Integer doInBackground(Void... voids) {
			return action.readBattery();
		}

		@Override
		protected void onPostExecute(Integer batteryStatus) {
			super.onPostExecute(batteryStatus);

			// Are we still connected?
			final CompletePage page = pageRef.get();
			if (page != null) {
				// Handle action results
				String message = action.getPermissionMessage(page.getActivity());
				if (message != null) {
					// Offer permission option
					new AlertDialog.Builder(page.getActivity())
							.setTitle(R.string.print_receipt_permission_title)
							.setMessage(message)
							.setNegativeButton(R.string.button_no, new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialogInterface, int i) {
									dialogInterface.dismiss();
								}
							})
							.setPositiveButton(R.string.button_yes, new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialogInterface, int i) {
									dialogInterface.dismiss();
									action.requestBluetoothSettings(page.getActivity());
								}
							})
							.show();
				} else if (action.getErrorMessage(page.getActivity()) == null) {
					// Update battery on success
					page.batteryIcon.setValue(batteryStatus);
				}
			}
		}

		private PrintReceiptAction action;
		private WeakReference<CompletePage> pageRef;
	}

	/**
	 * Handles request to edit the audit notes.
	 */
	private void onNotes() {
		IPageParent parent = getParent();
		if (parent != null) {
			parent.pushPage(NotesPage.newInstance(audit));
		}
	}

	/**
	 * Closes the audit and returns to the main menu (or displays a toast if there's an error).
	 * @param confirmGps Flags that we should check with the user to wait on the GPS
	 */
	private void onClose(boolean confirmNotes, boolean confirmGps) {
		// Can we close the audit now?
		if (confirmNotes && checkNotes()) {
			// Checking if the user wants to enter notes
			return;
		}
		if (confirmGps && checkGps()) {
			// Checking if the user wants tow ait for GPS
			return;
		}
		try (AuditDatabase db = new AuditDatabase(getContext())) {
			// Complete the audit
			Location lastLocation = ((MainActivity) getActivity()).getLastLocation();
			Double latitude = (lastLocation == null) ? null : lastLocation.getLatitude();
			Double longitude = (lastLocation == null) ? null : lastLocation.getLongitude();
			db.completeAudit(audit, latitude, longitude, completeTime);

			// Leave the audit user interface
			IPageParent parent = getParent();
			if (parent != null) {
				parent.popTo(MainMenuPage.class);
			}
		} catch (MobileClientException exc) {
			// Failed to complete the audit
			Toast.makeText(getContext(), exc.getMessage(), Toast.LENGTH_SHORT).show();
		}
	}

	/**
	 * Checks if the user wants to wait for GPS results.
	 * @return Need to check flag
	 */
	private boolean checkGps() {
		// Should we check with the user about the GPS?
		if (!isGpsRequested || (((MainActivity) getActivity()).getLastLocation() != null)) {
			// No need to ask, either we don't have GPS access, or we have a location
			return false;
		}

		// See if the user wants to wait
		new AlertDialog.Builder(getContext())
				.setTitle(R.string.message_complete_gps_confirm_title)
				.setMessage(R.string.message_complete_gps_confirm_message)
				.setNegativeButton(R.string.button_wait, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialogInterface, int i) {
						dialogInterface.dismiss();
					}
					})
				.setPositiveButton(R.string.button_complete_without_gps, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialogInterface, int i) {
						dialogInterface.dismiss();
						onClose(false, false);
					}
					})
				.show();
		return true;
	}

	/**
	 * Checks to see if the user needs to enter notes.
	 * @return Need to check flag
	 */
	private boolean checkNotes() {
		// Are we checking notes?
		Security sec = new Security(getContext().getApplicationContext());
		if (!sec.optSettingBool(Security.SETTING_NO_NOTES_WARNING, false)) {
			// No need to check
			return false;
		}

		// Check if there are notes
		try (AuditDatabase db = new AuditDatabase(getContext())) {
			// Get the notes from the database
			Notes notes = db.getNotes(audit);
			if (notes.getContents().length() > 0) {
				// Notes are already present
				return false;
			}
		} catch (MobileClientException e) {
			// Already logged, ignore
		}

		// Show no notes warning
		new AlertDialog.Builder(getContext())
				.setTitle(R.string.message_complete_no_notes_title)
				.setMessage(R.string.message_complete_no_notes_confirm)
				.setPositiveButton(R.string.message_complete_no_notes_enter_notes, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// Show Notes
						dialog.dismiss();
						onNotes();

					}})
				.setNegativeButton(R.string.message_complete_no_notes_close_without, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// Complete without notes
						dialog.dismiss();
						onClose(false, true);
					}})
				.show();
		return true;
	}

	/** Identifies audit argument in parameter bundle. */
	private static final String ARG_AUDIT = "auditArg";

	/** Provides the audit that we are completing. */
	private Audit audit;

	/** Marks the completion time from the initial page creation. */
	private Date completeTime = new Date();

	/** Indicates if we're able to request the GPS location. */
	private boolean isGpsRequested;

	/** Manages the printer battery display. */
	private BatteryIcon batteryIcon;
}
