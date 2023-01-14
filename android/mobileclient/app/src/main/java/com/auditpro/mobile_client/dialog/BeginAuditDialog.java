/*
  AuditPRO Mobile Client Android
  Copyright 2018 AuditPRO All Rights Reserved
 */
package com.auditpro.mobile_client.dialog;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.auditpro.mobile_client.api.StoreResponse;
import com.auditpro.mobile_client.entities.AuditHistory;
import com.auditpro.mobile_client.entities.Store;
import com.auditpro.mobile_client.test.R;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;


/**
 * Displays a dialog that shows details for a store about to be audited, and accepts
 * confirmation from the user.
 * @author Eric Ruck
 */
public class BeginAuditDialog extends DialogFragment implements ListAdapter {

	/** Identifies the store in our argument bundle. */
	private static final String ARG_STORE = "store";

	/** Identifies the review flag in our argument bundle. */
	private static final String ARG_REVIEW = "review";

	/** References the store that we want to audit. */
	private Store store;

	/** Flags review only mode. */
	private boolean review;

	/** Listener handles our events. */
	private Listener listener;


	/**
	 * Provides required empty public constructor.
	 */
	public BeginAuditDialog() { }

	/**
	 * Creates a new instance to collect confirmation for a store.
	 * @param store Store to confirm
	 * @return A new instance of fragment BeginAuditDialog
	 */
	public static BeginAuditDialog newInstance(Store store) {
		return newInstance(store, false);
	}

	/**
	 * Creates a new instance to collect confirmation for a store.
	 * @param store Store to confirm
	 * @param review Review only (vs begin/cancel semantics)
	 * @return A new instance of fragment BeginAuditDialog
	 */
	public static BeginAuditDialog newInstance(Store store, boolean review) {
		BeginAuditDialog fragment = new BeginAuditDialog();
		Bundle args = new Bundle();
		JSONObject storeParam = StoreResponse.toJSON(store);
		args.putString(ARG_STORE, storeParam == null ? null : storeParam.toString());
		args.putBoolean(ARG_REVIEW, review);
		fragment.setArguments(args);
		return fragment;
	}

	/**
	 * Handles creation event by restoring our arguments.
	 * @param savedInstanceState Saved state to restore or null
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle args = getArguments();
		if (args != null) {
			// Gets the store
			String parseStore = args.getString(ARG_STORE);
			if ((parseStore != null) && (parseStore.length() != 0)) {
				store = StoreResponse.fromJSON(parseStore);
			}

			// Gets the review flag
			review = args.getBoolean(ARG_REVIEW);
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
		return inflater.inflate(R.layout.dialog_begin_audit_dialog, container, false);
	}

	/**
	 * Initializes our controls once they are visible.
	 */
	@Override
	public void onStart() {
		super.onStart();

		// Validate the start state
		View view = getView();
		if (view == null) {
			return;
		}
		if (store == null) {
			Toast.makeText(getContext(), R.string.begin_audit_no_store, Toast.LENGTH_SHORT).show();
			return;
		}

		// Populate the store info
		TextView chainView = view.findViewById(R.id.chain_text);
		TextView storeView = view.findViewById(R.id.store_text);
		TextView streetView = view.findViewById(R.id.store_street_text);
		TextView cityView = view.findViewById(R.id.store_city_zip_text);
		chainView.setText(store.getChainName());
		storeView.setText(store.getDescription());
		String address = store.getStoreAddress();
		String address2 = store.getStoreAddress2();
		if ((address == null) || (address.length() == 0)) {
			address = address2;
		} else if ((address2 != null) && (address2.length() != 0)) {
			address += "\n" + address2;
		}
		if ((address == null) || (address.length() == 0)) {
			streetView.setVisibility(View.GONE);
		} else {
			streetView.setText(address);
		}
		cityView.setText(store.getCityStateZip());

		// Attach the list
		ListView historyList = view.findViewById(R.id.history_list);
		historyList.setAdapter(this);

		// Attach the buttons
		Button cancelButton = view.findViewById(R.id.cancel_button);
		Button auditButton = view.findViewById(R.id.audit_button);
		cancelButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				getDialog().dismiss();
			}
		});
		if (review) {
			// No audit option
			auditButton.setVisibility(View.GONE);
			cancelButton.setText(R.string.button_ok);
		} else {
			// Handle the audit button
			auditButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					getDialog().dismiss();
					listener.onConfirmStoreAudit(store);
				}
			});
		}
	}

	/**
	 * Handles this fragment attaching to its host context.
	 * @param context Host context
	 */
	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		if (context instanceof Listener) {
			// Keep the listener
			listener = (Listener) context;
		} else {
			// Invalid context does not implement the listener
			throw new RuntimeException(context.toString()
					+ " must implement BeginAuditDialog.Listener");
		}
	}

	/**
	 * Indicates whether all the items in this adapter are enabled. If the
	 * value returned by this method changes over time, there is no guarantee
	 * it will take effect.  If true, it means all items are selectable and
	 * clickable (there is no separator.)
	 *
	 * @return True if all items are enabled, false otherwise.
	 * @see #isEnabled(int)
	 */
	@Override
	public boolean areAllItemsEnabled() {
		return false;
	}

	/**
	 * Returns true if the item at the specified position is not a separator.
	 * (A separator is a non-selectable, non-clickable item).
	 * <p>
	 * The result is unspecified if position is invalid. An {@link ArrayIndexOutOfBoundsException}
	 * should be thrown in that case for fast failure.
	 *
	 * @param position Index of the item
	 * @return True if the item is not a separator
	 * @see #areAllItemsEnabled()
	 */
	@Override
	public boolean isEnabled(int position) {
		return false;
	}

	/**
	 * Register an observer that is called when changes happen to the data used by this adapter.
	 *
	 * @param observer the object that gets notified when the data set changes.
	 */
	@Override
	public void registerDataSetObserver(DataSetObserver observer) { }

	/**
	 * Unregister an observer that has previously been registered with this
	 * adapter via {@link #registerDataSetObserver}.
	 *
	 * @param observer the object to unregister.
	 */
	@Override
	public void unregisterDataSetObserver(DataSetObserver observer) { }

	/**
	 * How many items are in the data set represented by this Adapter.
	 *
	 * @return Count of items.
	 */
	@Override
	public int getCount() {
		return store.getHistoryCount();
	}

	/**
	 * Get the data item associated with the specified position in the data set.
	 *
	 * @param position Position of the item whose data we want within the adapter's
	 *                 data set.
	 * @return The data at the specified position.
	 */
	@Override
	public Object getItem(int position) {
		return ((position >= 0) && (position < store.getHistoryCount())) ? null : store.getHistory(position);
	}

	/**
	 * Get the row id associated with the specified position in the list.
	 *
	 * @param position The position of the item within the adapter's data set whose row id we want.
	 * @return The id of the item at the specified position.
	 */
	@Override
	public long getItemId(int position) {
		return 0;
	}

	/**
	 * Indicates whether the item ids are stable across changes to the
	 * underlying data.
	 *
	 * @return True if the same id always refers to the same object.
	 */
	@Override
	public boolean hasStableIds() {
		return false;
	}

	/**
	 * Get a View that displays the data at the specified position in the data set. You can either
	 * create a View manually or inflate it from an XML layout file. When the View is inflated, the
	 * parent View (GridView, ListView...) will apply default layout parameters unless you use
	 * {@link LayoutInflater#inflate(int, ViewGroup, boolean)}
	 * to specify a root view and to prevent attachment to the root.
	 *
	 * @param position    The position of the item within the adapter's data set of the item whose view
	 *                    we want.
	 * @param convertView The old view to reuse, if possible. Note: You should check that this view
	 *                    is non-null and of an appropriate type before using. If it is not possible to convert
	 *                    this view to display the correct data, this method can create a new view.
	 *                    Heterogeneous lists can specify their number of view types, so that this View is
	 *                    always of the right type (see {@link #getViewTypeCount()} and
	 *                    {@link #getItemViewType(int)}).
	 * @param parent      The parent that this view will eventually be attached to
	 * @return A View corresponding to the data at the specified position.
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// Validate position
		if ((position < 0) || (position >= store.getHistoryCount())) {
			// Invalid position
			return null;
		}

		// Get the view to use
		AuditHistory history = store.getHistory(position);
		ViewGroup row = (convertView != null) ? (ViewGroup) convertView :
				(ViewGroup) getLayoutInflater().inflate(R.layout.list_item_begin_audit, parent, false);

		// Determine last audit format
		String lastAudit = null;
		Date lastAuditDate = history.getLastAuditDate();
		if (lastAuditDate != null) {
			try {
				// Calculate days since date
				@SuppressLint("SimpleDateFormat")
				SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
				String formatLast = df.format(lastAuditDate);
				String formatNow = df.format(new Date());
				Date normalLast = df.parse(formatLast);
				Date normalNow = df.parse(formatNow);
				int days = (int) TimeUnit.DAYS.convert(normalNow.getTime() - normalLast.getTime(), TimeUnit.MILLISECONDS);
				lastAudit = getString(R.string.begin_audit_last, days);
			} catch (Exception exc) {
				// Never mind
			}
		}
		if ((lastAudit == null) && (history.getDaysSinceAudit() > 0)) {
			// Use date sent with history record
			lastAudit = getString(R.string.begin_audit_last, history.getDaysSinceAudit());
		}

		// Populate the row view
		TextView lastAuditedView = row.findViewById(R.id.last_audited_text);
		TextView emailView = row.findViewById(R.id.email_text);
		TextView timeView = row.findViewById(R.id.audit_time_text);
		TextView percentView = row.findViewById(R.id.percent_text);
		TextView noteView = row.findViewById(R.id.audit_note_text);
		TextView storeNoteView = row.findViewById(R.id.audit_store_note_text);
		String note = history.getAuditNote();
		String storeNote = history.getAuditStoreNote();
		emailView.setText(history.getUserEmail());
		timeView.setText(getString(R.string.begin_audit_time, history.getAuditDurationTotal()));
		percentView.setText(getString(R.string.begin_audit_percent, history.getPercentInStock()));
		if (lastAudit == null) {
			// No last audit days
			lastAuditedView.setVisibility(View.GONE);
		} else {
			// Show last audit days
			lastAuditedView.setVisibility(View.VISIBLE);
			lastAuditedView.setText(lastAudit);
		}
		if ((note == null) || (note.length() == 0)) {
			// No notes
			noteView.setVisibility(View.GONE);
		} else {
			// Show notes
			noteView.setVisibility(View.VISIBLE);
			noteView.setText(note);
		}
		if ((storeNote == null) || (storeNote.length() == 0)) {
			// No store notes
			storeNoteView.setVisibility(View.GONE);
		} else {
			storeNoteView.setVisibility(View.VISIBLE);
			storeNoteView.setText(storeNote);
		}

		// Return the populated row
		return row;
	}

	/**
	 * Get the type of View that will be created by {@link #getView} for the specified item.
	 *
	 * @param position The position of the item within the adapter's data set whose view type we
	 *                 want.
	 * @return An integer representing the type of View. Two views should share the same type if one
	 * can be converted to the other in {@link #getView}. Note: Integers must be in the
	 * range 0 to {@link #getViewTypeCount} - 1. {@link #IGNORE_ITEM_VIEW_TYPE} can
	 * also be returned.
	 * @see #IGNORE_ITEM_VIEW_TYPE
	 */
	@Override
	public int getItemViewType(int position) {
		return 0;
	}

	/**
	 * Gets the number of different types of views in the list.
	 * @return The number of types of Views that will be created by this adapter
	 */
	@Override
	public int getViewTypeCount() {
		return 1;
	}

	/**
	 * @return true if this adapter doesn't contain any data.  This is used to determine
	 * whether the empty view should be displayed.  A typical implementation will return
	 * getCount() == 0 but since getCount() includes the headers and footers, specialized
	 * adapters might want a different behavior.
	 */
	@Override
	public boolean isEmpty() {
		return store.getHistoryCount() == 0;
	}

	/**
	 * This interface must be implemented by activities that contain this
	 * fragment to allow an interaction in this fragment to be communicated
	 * to the activity and potentially other fragments contained in that
	 * activity.
	 * <p>
	 * See the Android Training lesson <a href=
	 * "http://developer.android.com/training/basics/fragments/communicating.html"
	 * >Communicating with Other Fragments</a> for more information.
	 */
	public interface Listener {
		/**
		 * Handles confirmation that the user wants to audit a store.
		 * @param store Provides the store that the user wants to audit
		 */
		void onConfirmStoreAudit(Store store);
	}
}
