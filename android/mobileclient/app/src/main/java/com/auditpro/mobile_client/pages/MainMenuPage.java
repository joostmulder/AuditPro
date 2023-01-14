/*
  AuditPRO Mobile Client Android
  Copyright 2018 AuditPRO All Rights Reserved
 */
package com.auditpro.mobile_client.pages;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.auditpro.mobile_client.Analytics;
import com.auditpro.mobile_client.MainActivity;
import com.auditpro.mobile_client.MobileClientException;
import com.auditpro.mobile_client.api.ApiClient;
import com.auditpro.mobile_client.database.AuditDatabase;
import com.auditpro.mobile_client.database.BaseDatabase;
import com.auditpro.mobile_client.database.StoresDatabase;
import com.auditpro.mobile_client.entities.Audit;
import com.auditpro.mobile_client.entities.Notes;
import com.auditpro.mobile_client.entities.Product;
import com.auditpro.mobile_client.entities.Store;
import com.auditpro.mobile_client.security.Security;
import com.auditpro.mobile_client.test.R;

import java.lang.ref.WeakReference;
import java.util.Date;
import java.util.List;


/**
 * Manages the main menu page of the application.
 */
@SuppressWarnings("ConstantConditions")
public class MainMenuPage extends BasePage {

	/**
	 * Required empty public constructor
	 */
	public MainMenuPage() { }

	/**
	 * Creates the view for this page.
	 * @param inflater Inflater used to expand layout resource.
	 * @param container Page fragment container
	 * @param savedInstanceState Optional saved state
	 * @return Created view for page
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.fragment_main_menu_page, container, false);
	}

	/**
	 * Complete view initialization.
	 * @param view Parent view
	 * @param savedInstanceState Optional saved state
	 */
	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		// Wire up control listeners
		view.findViewById(R.id.syncButton).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				onSync();
			}
		});
		view.findViewById(R.id.auditButton).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				onAudit(AuditStatus.Request);
			}
		});
		view.findViewById(R.id.logoutButton).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				onLogout();
			}
		});
		view.findViewById(R.id.reviewButton).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				onReview();
			}
		});
		view.findViewById(R.id.helpButton).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				onHelp();
			}
		});
		view.findViewById(R.id.settingsButton).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onSettings();
			}
		});

		// Make sure we have permission to access GPS
		((MainActivity) getActivity()).requestLocationPermission();

		// Can we set the version?
		IPageParent parent = getParent();
		String version = (parent == null) ? "?" : parent.getVersion();
		TextView versionView = getView().findViewById(R.id.versionText);
		if (version != null) {
			// Set the version
			versionView.setText(
					getString(R.string.app_version, version)
			);
		} else {
			// Clear the version, warning already logged
			versionView.setText("");
		}
	}

	/**
	 * Synchronizes data with the remote server.
	 */
	private void onSync() {
		// Do we have a data connection?
		if (!((MainActivity) getActivity()).isNetworkConnected()) {
			// No connection
			Toast.makeText(getContext(), R.string.message_sync_needs_inet, Toast.LENGTH_SHORT).show();
			return;
		}

		// Have we authenticated?
		if (!((MainActivity) getActivity()).isInSession()) {
			// We need to log in before we can sync
			initSync = InitSyncOption.Login;
			showPage(LoginPage.sessionInstance());
			return;
		}

		// Synchronize now
		performSync();
	}

	private enum AuditStatus { Request, Continue, EndNew, AlreadyOpen }

	/**
	 * Begins a new audit, or optionally resumes an audit in progress.
	 * @param status Current audit state
	 */
	private void onAudit(AuditStatus status) {
		// Make sure we have stores
		try (StoresDatabase db = new StoresDatabase(getContext())) {
			if (db.isEmpty()) {
				Toast.makeText(getContext(), R.string.message_main_needs_sync, Toast.LENGTH_SHORT).show();
				return;
			}
		} catch (MobileClientException exc) {
			Toast.makeText(getContext(), exc.getMessage(), Toast.LENGTH_SHORT).show();
			return;
		}

		// Is there an audit to continue?
		final Audit openAudit = getOpenAudit();

		// Do we need to confirm the request?
		if ((status == AuditStatus.Request) && (openAudit != null)) {
			// Display audit; offer continue, close and cancel options
			status = AuditStatus.AlreadyOpen;
			String message = getResources().getString(R.string.message_main_audit_confirm,
					openAudit.getStoreDescr(),
					BaseDatabase.readDateTime(openAudit.getAuditStartedAt()));
			CharSequence[] items = new CharSequence[] {
					getResources().getString(R.string.button_main_audit_confirm_continue),
					getResources().getString(R.string.button_main_audit_confirm_end_old),
					getResources().getString(R.string.button_cancel)
			};
			new AlertDialog.Builder(getContext())
					.setTitle(message)
					.setItems(items, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialogInterface, int i) {
							dialogInterface.dismiss();
							switch (i) {
								case 0: onAudit(AuditStatus.Continue); break;
								case 1: onAudit(AuditStatus.EndNew); break;
							}
						}
					})
					.show();
		} else if (status == AuditStatus.Request) {
			// Show the stores selection
			Location lastLocation = ((MainActivity) getActivity()).getLastLocation();
			Double latitude = (lastLocation == null) ? null : lastLocation.getLatitude();
			Double longitude = (lastLocation == null) ? null : lastLocation.getLongitude();
			showPage(SelectStorePage.newInstance(latitude, longitude));
		} else if (status == AuditStatus.Continue) {
			// Go straight to the products page
			showPage(SelectProductPage.newInstance(openAudit));
		} else if (status == AuditStatus.EndNew) {
			try (AuditDatabase db = new AuditDatabase(getContext())) {
				// Process request to complete open audit
				Security sec = new Security(getContext().getApplicationContext());
				if (sec.optSettingBool(Security.SETTING_NO_NOTES_WARNING, false)) {
					// Check if the user provided notes
					Notes note = db.getNotes(openAudit);
					if (note.getContents().length() == 0) {
						// Verify that the user wants to continue with no notes
						new AlertDialog.Builder(getContext())
								.setTitle(R.string.message_complete_no_notes_title)
								.setMessage(R.string.message_complete_no_notes_confirm)
								.setPositiveButton(R.string.message_complete_no_notes_continue_audit, new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int which) {
										dialog.dismiss();
										onAudit(AuditStatus.Continue);
									}
								})
								.setNeutralButton(R.string.message_complete_no_notes_close_without, new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int which) {
										dialog.dismiss();
										completeOpenAudit(openAudit);
									}
								})
								.setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int which) {
										dialog.dismiss();
									}
								})
								.show();
						return;
					}
				}
			} catch (MobileClientException exc) {
				// Logged, we're going to ignore and let the audit complete catch the issue
			}

			// Passed validation, good to complete
			completeOpenAudit(openAudit);
		}

		// Provide analytics
		Analytics.log("On Audit", "Action", status.toString());
	}

	/**
	 * Completes the open audit, and starts a new one.  Assumes that we've already checked for the
	 * "no notes" situation.
	 * @param openAudit Audit being closed
	 */
	private void completeOpenAudit(final Audit openAudit) {
		try (AuditDatabase db = new AuditDatabase(getContext())) {
			// Complete the open audit
			Location lastLocation = ((MainActivity) getActivity()).getLastLocation();
			Double latitude = (lastLocation == null) ? null : lastLocation.getLatitude();
			Double longitude = (lastLocation == null) ? null : lastLocation.getLongitude();
			db.completeAudit(openAudit, latitude, longitude, new Date());

			// Now request a new audit
			onAudit(AuditStatus.Request);
		} catch (MobileClientException exc) {
			// Failed to complete the open audit
			Toast.makeText(getContext(), exc.getMessage(), Toast.LENGTH_SHORT).show();
		}
	}

	/**
	 * Navigates to completed audit review page.
	 */
	private void onReview() {
		showPage(ReviewPage.newInstance());
	}

	/**
	 * Logs out of the application and returns to the landing page.
	 */
	private void onLogout() {
		((MainActivity) getActivity()).logout();
		IPageParent parent = getParent();
		if (parent != null) {
			parent.popPage();
		}
	}

	/**
	 * Displays the help for the main menu.
	 */
	private void onHelp() {
		showPage(HelpPage.newInstance("MainHelp.html"));
	}

	/**
	 * Displays the settings for the application.
	 */
	private void onSettings() {
		showPage(new SettingsPage());
	}

	/**
	 * Overrides default back behavior with a confirmation.
	 * @return Handled flag
	 */
	@Override
	public boolean onBack() {
		// Display the alert asynchronously
		new AlertDialog.Builder(getContext())
				.setTitle(R.string.button_main_logout)
				.setMessage(R.string.message_confirm_logout)
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
						onLogout();
					}
				})
				.setIcon(android.R.drawable.ic_dialog_alert)
				.show();

		// Indicate handled
		return true;
	}

	/**
	 * Handles attach event by determining whether we should auto-sync.
	 * @param context Application context
	 */
	@Override
	public void onAttach(Context context) {
		super.onAttach(context);

		// Do we have any Internet connection?
		Security sec = new Security(context.getApplicationContext());
		boolean autoSync = sec.optSettingBool(Security.SETTING_AUTOSYNC_WIFI, false);
		if (autoSync && ((MainActivity) context).isWifiConnected()) {
			// Once we're showing offer sync now
			initSync = InitSyncOption.Sync;
		}
	}

	/**
	 * Updates UI when the page appears.
	 */
	@Override
	public void onPageAppearing() {
		// Enable location updates
		((MainActivity) getActivity()).startLocationUpdates();

		// Is there an audit in progress?
		((Button) getView().findViewById(R.id.auditButton)).setText(
			(getOpenAudit() == null) ? R.string.button_main_audit_new : R.string.button_main_audit_continue);

		// Display audit counts to send in store button
		updateAuditCount();

		// Should we sync now?
		boolean syncNow = false;
		IPageParent parent = getParent();
		if ((parent != null) && parent.isSyncRequested() && (initSync == InitSyncOption.None)) {
			initSync = InitSyncOption.Page;
		}
		switch (initSync) {
			case Ask:
				// Ask the user if they want to sync now
				new AlertDialog.Builder(getContext())
						.setTitle(R.string.button_main_sync)
						.setMessage(R.string.message_confirm_sync)
						.setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialogInterface, int i) {
								dialogInterface.dismiss();
							}
						})
						.setPositiveButton(R.string.button_sync, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialogInterface, int i) {
								dialogInterface.dismiss();
								performSync();
							}
						})
						.setIcon(android.R.drawable.ic_dialog_alert)
						.show();
				break;
			case Login:
				// Sync now if we're logged in
				syncNow = ((MainActivity) getActivity()).isInSession();
				break;
			case Sync:
				// Always sync now
				syncNow = true;
				break;
			case Page:
				// Show regular interface as necessary
				onSync();
				syncNow = false;
				break;
		}

		// Reset sync
		initSync = InitSyncOption.None;
		if (syncNow) {
			// Perform sync
			performSync();
		}
	}

	/**
	 * Suspends GPS request when we're disappearing.
	 */
	@Override
	public void onPageDisappearing() {
		((MainActivity) getActivity()).endLocationUpdates();
	}

	/**
	 * Cancels login task in progress on stop.
	 */
	@Override
	public void onStop() {
		super.onStop();
		SyncTask inProgress = (syncTask != null) ? syncTask.get() : null;
		if (inProgress != null) {
			inProgress.cancel(true);
		}
	}

	/**
	 * Gets the current audit in process, if there is one.
	 * @return The open audit or null if none
	 */
	private Audit getOpenAudit() {
		try (AuditDatabase db = new AuditDatabase(getContext()) ){
			// Get the open audit from the database, if there is one
			Security sec = new Security(getContext().getApplicationContext());
			return db.resumeAudit(sec.getUserId());
		} catch (MobileClientException exc)  {
			// Handled failure already logged
			return null;
		} catch (Exception exc) {
			// Log unexpected error
			Log.e(LOG_TAG, "Failed to get open audit on main page", exc);
			return null;
		}
	}

	/**
	 * Updates the user interface with the current completed audit count, indicating how many audits
	 * are ready to sync.  Also updates visibility of version sync message.
	 */
	private void updateAuditCount() {
		// Get the initial button label text
		String label = getResources().getString(R.string.button_main_sync);
		Security sec = new Security(getContext().getApplicationContext());
		try (AuditDatabase db = new AuditDatabase(getContext())) {
			// Are there any completed audits to sync?
			int auditCount = db.completeCount(sec.getUserId());
			if (auditCount == 1) {
				// Update label for one audit, special case
				label = getString(R.string.button_main_sync_count_one);
			} else if (auditCount > 1) {
				// Update the label with the current count
				label = getResources().getString(R.string.button_main_sync_count, auditCount);
			}
		} catch (MobileClientException exc) {
			// Ignore, already logged
		}

		// Adjust the sync button label for the audit count
		((Button) getView().findViewById(R.id.syncButton)).setText(label);

		// Adjust visibility of the version sync message
		getView().findViewById(R.id.syncRequiredText).setVisibility(
				sec.isSyncNeeded(StoresDatabase.getVersion()) ? View.VISIBLE : View.GONE
		);
	}

	/**
	 * Executes a synchronization with the remote web services now.
	 */
	private void performSync() {
		syncTask = new WeakReference<>(new SyncTask(this));
		syncTask.get().execute();
	}

	/**
	 * Provides reference-safe async task to login on a worker thread.
	 */
	private static class SyncTask extends AsyncTask<Void, Void, String> {
		private WeakReference<MainMenuPage> host;
		private String token;
		private int userId;
		private ApiClient api;

		/**
		 * Initialize task to call the remote log in web service.
		 * @param host Parent host page
		 */
		SyncTask(MainMenuPage host) {
			Security sec = new Security(host.getContext().getApplicationContext());
			this.host = new WeakReference<>(host);
			this.token = ((MainActivity) host.getActivity()).getSessionToken();
			this.userId = sec.getUserId();
		}

		/**
		 * Shows activity spinner while we run.
		 */
		@Override
		protected void onPreExecute() {
			MainMenuPage self = host.get();
			IPageParent parent = (self == null) ? null : self.getParent();
			if (parent != null) {
				parent.setActivity(true);
			}
		}

		/**
		 * Execute remove login web service
		 * @param voids Placeholder for no parameters
		 * @return Error message or null on success
		 */
		@Override
		protected String doInBackground(Void... voids) {
			api = new ApiClient(token);
			try {
				// Send pending audits
				Context context = host.get().getContext();
				try (AuditDatabase db = new AuditDatabase(context)) {
					// Cycle through the completed audits
					List<Audit> completed = db.getCompleteAudits(userId);
					for (Audit audit : completed) {
						// Post the current audit
						String json = db.serializeAudit(audit);
						if (!api.postPayload(json)) {
							// Failed to post the audit
							throw new MobileClientException(api.getMessage());
						}

						// Remove the posted audit from our cache
						db.deleteAudit(audit);
					}
				}

				// Get new stores for this user
				List<Store> stores  = api.getStores();
				if (stores == null) {
					// Failed to get new stores
					throw new MobileClientException(api.getMessage());
				}

				// Get new products for this user
				List<Product> products = api.getProducts();
				if (products == null) {
					// Failed tp get new products
					throw new MobileClientException(api.getMessage());
				}

				// Cache the stores and products locally
				try (StoresDatabase db = new StoresDatabase(context)) {
					// Update our cache
					db.applyRefresh(stores, products);
				}

				// Indicate success
				return null;
			}
			catch (MobileClientException exc) {
				// Failed to sync
				return exc.getMessage();
			}
		}

		/**
		 * Handles the web service result in the user interface.
		 * @param errorMessage Error message on failure or null on success
		 */
		@Override
		protected void onPostExecute(String errorMessage) {
			super.onPostExecute(errorMessage);
			MainMenuPage self = host.get();
			IPageParent parent = (self == null) ? null : self.getParent();
			if (parent == null) {
				// The page was popped before we completed
				return;
			}
			parent.setActivity(false);
			if (errorMessage != null) {
				// Failed to sync, show error toast
				Toast.makeText(self.getContext(), errorMessage, Toast.LENGTH_SHORT).show();
			}

			// Complete the UI update
			Security sec = new Security(self.getContext().getApplicationContext());
			sec.setStoreVersionOnSync(StoresDatabase.getVersion());
			self.updateAuditCount();
		}
	}

	/** Safely references our sync task. */
	private WeakReference<SyncTask> syncTask;

	/** Options to sync when this page displays, forward or back navigation. */
	private enum InitSyncOption {
		None, // Don't sync
		Ask, // Ask before sync
		Sync, // Sync without asking
		Login, // Only sync if we're logged in
		Page, // Handle sync request from another page
	}

	/** Current sync on page display option. */
	private InitSyncOption initSync = InitSyncOption.None;

	/** Identifies page messages in the application log. */
	private static final String LOG_TAG = "MainMenuPage";
}
