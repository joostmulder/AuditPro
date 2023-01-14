/*
  AuditPRO Mobile Client Android
  Copyright 2018 AuditPRO All Rights Reserved
 */
package com.auditpro.mobile_client.pages;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.auditpro.mobile_client.Analytics;
import com.auditpro.mobile_client.MainActivity;
import com.auditpro.mobile_client.MobileClientException;
import com.auditpro.mobile_client.api.ApiClient;
import com.auditpro.mobile_client.database.AuditDatabase;
import com.auditpro.mobile_client.entities.Audit;
import com.auditpro.mobile_client.security.Security;
import com.auditpro.mobile_client.test.R;
import com.crashlytics.android.Crashlytics;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


/**
 * Manages a fragment to display closed audits for review.
 * @author Eric Ruck
 */
public class ReviewPage extends BasePage {

	/**
	 * Required empty public constructor.
	 */
	public ReviewPage() {
		// Required empty public constructor
	}

	/**
	 * Use this factory method to create a new instance of
	 * this fragment.
	 * @return A new instance of fragment ReviewPage.
	 */
	public static ReviewPage newInstance() {
		return new ReviewPage();
	}

	/**
	 * Creates our view.
	 * @param inflater Inflater to use
	 * @param container Parent container for view
	 * @param savedInstanceState Optional state
	 * @return New view for fragment
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.fragment_review_page, container, false);
	}


	/**
	 * Initializes the view after it has been created.
	 * @param view Created view
	 * @param savedInstanceState Optional state
	 */
	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		auditList = view.findViewById(R.id.auditList);

		// Connect to the audits database
		try (AuditDatabase db = new AuditDatabase(getContext())) {
			// Get all the closed audits
			Security sec = new Security(getContext().getApplicationContext());
			completedAudits = db.getCompleteAudits(sec.getUserId());
		}
		if ((completedAudits == null) || (completedAudits.size() == 0)) {
			// No audits
			showNoAudits(view);
		}

		// Display the completed audits
		auditAdapter = new ArrayAdapter<Audit>(getContext(), android.R.layout.simple_list_item_2,
				android.R.id.text1, completedAudits) {
			@NonNull @Override
			public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
				View view = super.getView(position, convertView, parent);
				Audit audit = getItem(position);
				if (audit != null) {
					DateFormat formatter = DateFormat.getDateInstance(DateFormat.SHORT);
					((TextView) view.findViewById(android.R.id.text1)).setText(audit.getStoreDescr());
					((TextView) view.findViewById(android.R.id.text2)).setText(formatter.format(audit.getAuditStartedAt()));
				}
				return view;
			}
		};
		auditList.setAdapter(auditAdapter);
		auditList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
				onSelectAudit(auditAdapter.getItem(i));
			}
		});
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
	 * Provides a title for the action bar.
	 * @param context Application context
	 * @return Action bar title
	 */
	@Override
	public String getPageName(Context context) {
		return context.getResources().getString(R.string.page_name_review);
	}

	/**
	 * Shows the no audits state.
	 * @param view Our view
	 */
	private void showNoAudits(View view) {
		if (view != null) {
			view.findViewById(R.id.noAuditsText).setVisibility(View.VISIBLE);
			auditList.setVisibility(View.GONE);
		}
		if (syncMenuItem != null) {
			syncMenuItem.setVisible(false);
		}
	}

	/**
	 * Shows generic error dialog for page.
	 * @param message Message to show
	 */
	private void showError(String message) {
		Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
	}

	/**
	 * Shows generic error dialog for page.
	 * @param message Message to show
	 */
	private void showError(int message) {
		Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
	}

	/**
	 * Handles an audit selected from the list.
	 * @param audit Selected audit
	 */
	private void onSelectAudit(final Audit audit) {
		// Validate
		if (audit == null) {
			// Nothing to do
			return;
		}

		// Format audit description
		DateFormat formatter = DateFormat.getDateInstance(DateFormat.SHORT);
		String title = String.format("%s\nStarted at %s\nEnded at %s",
				audit.getStoreDescr(),
				formatter.format(audit.getAuditStartedAt()),
				formatter.format(audit.getAuditEndedAt()));

		// Show selected audit with options
		CharSequence[] items = new CharSequence[] {
				getResources().getString(R.string.message_review_opt_sync),
				getString(R.string.message_review_opt_email),
				getString(R.string.message_review_opt_edit),
				getString(R.string.message_review_opt_remove),
				getString(R.string.button_cancel)
		};
		new AlertDialog.Builder(getContext())
				.setTitle(title)
				.setItems(items, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int i) {
						dialogInterface.dismiss();
						switch (i) {
							case 0: onSync(audit); break;
							case 1: onEmail(audit); break;
							case 2: onEdit(audit); break;
							case 3: onRemove(audit); break;
						}
					}
				})
				.show();
	}

	/**
	 * Confirms request to sync the passed audit.
	 * @param audit Audit to sync
	 */
	private void onSync(final Audit audit) {
		String message = getString(R.string.message_review_confirm_sync, audit.toString());
		new AlertDialog.Builder(getContext())
				.setMessage(message)
				.setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int i) {
						dialogInterface.dismiss();
						Analytics.log("Review", audit,"Sync", "Cancel");
					}
				})
				.setPositiveButton(R.string.button_yes, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int i) {
						dialogInterface.dismiss();
						Analytics.log("Review", audit,"Sync", "Sync");
						performSync(audit);
					}
				})
				.show();
	}

	/**
	 * Performs the sync of a single audit.
	 * @param audit Audit to sync
	 */
	private void performSync(final Audit audit) {
		syncTask = new WeakReference<>(new SyncTask(this, audit));
		syncTask.get().execute();
	}

	/**
	 * Provides reference-safe async task to login on a worker thread.
	 */
	private static class SyncTask extends AsyncTask<Void, Void, String> {
		private WeakReference<ReviewPage> host;
		private Audit audit;
		private String token;
		private ApiClient api;

		/**
		 * Initialize task to call the remote log in web service.
		 * @param host Parent host page
		 */
		SyncTask(ReviewPage host, Audit audit) {
			this.host = new WeakReference<>(host);
			this.audit = audit;
			this.token = ((MainActivity) host.getActivity()).getSessionToken();
		}

		/**
		 * Shows activity spinner while we run.
		 */
		@Override
		protected void onPreExecute() {
			ReviewPage self = host.get();
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
					// Post the current audit
					String json = db.serializeAudit(audit);
					if (!api.postPayload(json)) {
						// Failed to post the audit
						throw new MobileClientException(api.getMessage());
					}

					// Remove the posted audit from our cache
					db.deleteAudit(audit);
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
			ReviewPage self = host.get();
			IPageParent parent = (self == null) ? null : self.getParent();
			if (parent == null) {
				// The page was popped before we completed
				return;
			}

			// Update the UI for a completed audit sync
			parent.setActivity(false);
			self.auditAdapter.remove(audit);
			if (self.auditAdapter.getCount() == 0) {
				// No more audits
				self.showNoAudits(self.getView());
			}
			if (errorMessage != null) {
				// Failed to sync, show error toast
				self.showError(errorMessage);
			}
		}
	}

	/**
	 * Displays our menu when we have unscanned items.
	 * @param inflater Inflater to use on menu resource
	 * @param menu Parent receives menu items
	 * @return Handled flag
	 */
	@Override
	public boolean onCreateMenu(MenuInflater inflater, Menu menu) {
		if (completedAudits.size() > 0) {
			// Show the menu
			inflater.inflate(R.menu.menu_review, menu);
			syncMenuItem = menu.findItem(R.id.action_sync_all);
			return true;
		}

		// No audits means no menu
		return false;
	}

	/**
	 * Handles our menu items when they are selected
	 * @param item Item selected
	 * @return Handled flag
	 */
	@Override
	public boolean onMenuItem(MenuItem item) {
		// Are we interested in this item?
		int id = item.getItemId();
		if (id == R.id.action_sync_all) {
			// Offer set unscanned options
			int message = ((MainActivity) getActivity()).isWifiConnected()
					? R.string.message_review_sync_confirm
					: R.string.message_review_sync_confirm_cell;
			new AlertDialog.Builder(getContext())
					.setTitle(R.string.message_review_sync_title)
					.setMessage(message)
					.setPositiveButton(R.string.button_sync, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							Analytics.menuAction("Sync All", "sync");
							dialog.dismiss();
							IPageParent parent = getParent();
							if (parent != null) {
								parent.requestSync();
							}
						}
					})
					.setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							Analytics.menuAction("Sync All", "cancel");
							dialog.dismiss();
						}
					})
					.show();
			return true;
		}

		// Not handled
		return false;
	}

	/**
	 * Sets up an e-mail for the requested audit.
	 * @param audit Audit to e-mail
	 */
	private void onEmail(Audit audit) {
		// Access the database to serialize the audit
		byte[] payload;
		try (AuditDatabase db = new AuditDatabase(getContext())) {
			// Serialize the audit
			String json = db.serializeAudit(audit);
			payload = json.getBytes("UTF-8");
		} catch (MobileClientException exc) {
			// Failed to serialize audit
			showError(R.string.message_review_error_serialize);
			return;
		} catch (UnsupportedEncodingException exc) {
			// Failed to encode audit
			Crashlytics.log(Log.ERROR, LOG_TAG, String.format("Failed to encode audit %s: %s",
					audit.toString(), exc.getMessage()));
			showError(R.string.message_review_error_encoding);
			return;
		}

		// Prepare to write attachment to app cache
		File cacheFile = getContext().getCacheDir();

		// Cleanup old attachments
		File[] oldAttachments = cacheFile.listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				String fileName = pathname.getName();
				return fileName.startsWith("audit-") && fileName.endsWith(".json");
			}
		});
		for (File oldFile : oldAttachments) {
			if (!oldFile.delete()) {
				Log.w(LOG_TAG, String.format("Failed to delete old attachment file %s", oldFile.getAbsoluteFile()));
			}
		}

		// Write the audit attachment to shared cache
		String stamp = new SimpleDateFormat("yyMMddHHmmssZ", Locale.getDefault()).
				format(audit.getAuditStartedAt());
		File attachmentFile = new File(cacheFile, String.format(
				"audit-%s.json", stamp
		));
		try (FileOutputStream outs = new FileOutputStream(attachmentFile)) {
			outs.write(payload);
		} catch (Exception exc) {
			Log.w(LOG_TAG, "Failed to write completed audit JSON for attachment", exc);
			return;
		}

		// Setup e-mail
		Uri attachmentUri = FileProvider.getUriForFile(getContext(),
				"com.auditpro.mobile_client.fileprovider", attachmentFile);
		ArrayList<Uri> attachments = new ArrayList<>();
		attachments.add(attachmentUri);
		Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
		intent.setType("text/email");
		intent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[] { EMAIL_TO });
		intent.putExtra(android.content.Intent.EXTRA_SUBJECT,"Completed Audit");
		intent.putExtra(android.content.Intent.EXTRA_TEXT,
				String.format("Completed audit of %s attached", audit.toString()));
		intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, attachments);
		try {
			// Show the configured e-mail client
			startActivity(intent);
			Analytics.log("Review", audit,"Email", "Email");
		} catch (Exception exc) {
			// Unable to show configured e-mail client
			Log.w(LOG_TAG, "Failed to start e-mail activity", exc);
			showError(R.string.message_review_error_email);
		}
	}

	/**
	 * Reopens the passed audit for editing.
	 * @param audit Audit to edit
	 */
	private void onEdit(final Audit audit) {
		try (AuditDatabase db = new AuditDatabase(getContext())) {
			// Is there already an open audit?
			Security sec = new Security(getContext().getApplicationContext());
			if (db.resumeAudit(sec.getUserId()) != null) {
				// Yes, unable to edit now
				Toast.makeText(getContext(), R.string.message_review_error_edit_started, Toast.LENGTH_LONG).show();
				return;
			}

			// Reopen the audit
			Audit reopened = db.reopenAudit(audit);
			IPageParent parent = getParent();
			if (parent != null) {
				Analytics.log("Review", audit, "Edit", "Reopen");
				getParent().swapPage(SelectProductPage.newInstance(reopened));
			}
		} catch (MobileClientException exc) {
			// Show the error to the user, details already logged
			Toast.makeText(getContext(), exc.getMessage(), Toast.LENGTH_SHORT).show();
		}
	}

	/**
	 * Confirms request to remove the passed audit.
	 * @param audit Audit to remove
	 */
	private void onRemove(final Audit audit) {
		String message = getString(R.string.message_review_confirm_remove, audit.toString());
		new AlertDialog.Builder(getContext())
				.setMessage(message)
				.setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int i) {
						dialogInterface.dismiss();
						Analytics.log("Review", audit,"Remove", "Cancel");
					}
				})
				.setPositiveButton(R.string.button_yes, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int i) {
						dialogInterface.dismiss();
						Analytics.log("Review", audit,"Remove", "Remove");
						performRemove(audit);
					}
				})
				.show();
	}

	/**
	 * Removes the passed audit from the database.
	 * @param audit Audit to remove
	 */
	private void performRemove(final Audit audit) {
		try (AuditDatabase db = new AuditDatabase(getContext())) {
			// Remove the posted audit from our cache
			db.deleteAudit(audit);

			// Remove the audit from the list
			auditAdapter.remove(audit);
			if (auditAdapter.getCount() == 0) {
				showNoAudits(getView());
			}
		} catch (MobileClientException exc) {
			// Failed to delete audit
			showError(R.string.message_review_error_remove);
		}
	}

	/** Identifies logging source. */
	private static final String LOG_TAG = "MainMenuPage";

	/** Default e-mail recipient. */
	private static final String EMAIL_TO = "accounts@auditpro.io";

	/** References the list view. */
	private ListView auditList;

	/** References the sync menu item. */
	private MenuItem syncMenuItem;

	/* Completed audits fetched from the database. */
	List<Audit> completedAudits;

	/** Interfaces the completed audits to the list view. */
	private ArrayAdapter<Audit> auditAdapter;

	/** References the sync task in progress, if any. */
	private WeakReference<SyncTask> syncTask;
}
