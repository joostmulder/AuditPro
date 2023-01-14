/*
  AuditPRO Mobile Client Android
  Copyright 2018 AuditPRO All Rights Reserved
 */
package com.auditpro.mobile_client.pages;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.auditpro.mobile_client.Analytics;
import com.auditpro.mobile_client.MobileClientException;
import com.auditpro.mobile_client.database.AuditDatabase;
import com.auditpro.mobile_client.entities.Audit;
import com.auditpro.mobile_client.entities.Notes;
import com.auditpro.mobile_client.security.Security;
import com.auditpro.mobile_client.test.R;


/**
 * Manages the notes associated with an audit in progress.
 * @author Eric Ruck
 */
public class NotesPage extends BasePage {

	/**
	 * Required empty public constructor
	 */
	public NotesPage() { }

	/**
	 * Creates a new notes page fragment with the required audit argument.
	 * @param audit Audit to which notes are attached.
	 * @return A new instance of fragment NotesPage.
	 */
	public static NotesPage newInstance(Audit audit) {
		NotesPage fragment = new NotesPage();
		Bundle args = new Bundle();
		args.putParcelable(ARG_AUDIT, audit);
		fragment.setArguments(args);
		return fragment;
	}

	/**
	 * Handles the creation of a new instance via the fragment lifecycle.
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
	 * Creates the view for the new page fragment.
	 * @param inflater Inflater to hydrate the layout
	 * @param container View parent
	 * @param savedInstanceState Optional state
	 * @return Created view
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.fragment_notes_page, container, false);
	}

	/**
	 * Initialize the page view once the resources have been created.
	 * @param view Created view
	 * @param savedInstanceState Optional state
	 */
	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		// Analytics
		Analytics.log("Notes", "Audit Id", audit.getId().toString());

		// Get the note to edit
		try (AuditDatabase db = new AuditDatabase(getContext())) {
			// Get the notes for the audit
			notes = db.getNotes(audit);
		} catch (MobileClientException exc) {
			// Unexpected failure getting notes, handle gracefully
			Toast.makeText(getContext(), exc.getMessage(), Toast.LENGTH_SHORT).show();
			notes = new Notes(null, audit.getId(), "", "");
		}

		// Determine notes to initialize
		String notesText = (savedInstanceState == null) ? null :
				savedInstanceState.getString(STATE_NOTES);
		String storeText = (savedInstanceState == null) ? null :
				savedInstanceState.getString(STATE_STORE);
		if (notesText == null) {
			notesText = notes.getContents();
			storeText = notes.getStore();
		}

		// Complete initialization
		notesEdit = view.findViewById(R.id.notesEdit);
		storeEdit = view.findViewById(R.id.storeEdit);
		if (notesText != null) {
			// Initialize internal notes
			notesEdit.setText(notesText);
		}
		if (!hasStoreNotes()) {
			// Hide store notes and related views
			storeEdit.setVisibility(View.GONE);
			view.findViewById(R.id.notesText).setVisibility(View.GONE);
			view.findViewById(R.id.storeText).setVisibility(View.GONE);
		} else if (storeText != null) {
			// Initialize store notes
			storeEdit.setText(storeText);
		}
	}

	/**
	 * Saves our restore state.
	 */
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(STATE_NOTES, notesEdit.getText().toString());
		if (hasStoreNotes()) {
			outState.putString(STATE_STORE, storeEdit.getText().toString());
		}
	}

	/**
	 * Gets the notes display title.
	 * @param context Application context for string resource lookup
	 * @return Readable page title
	 */
	@Override
	public String getPageName(Context context) {
		return context.getString(R.string.message_notes_title);
	}

	/**
	 * Shrinks the application window when the keyboard appears.
	 * @return Shrink keyboard flag
	 */
	@Override
	public boolean shrinkForKeyboard() {
		return true;
	}

	/**
	 * Handles the page disappearing by saving.
	 */
	@Override
	public void onPageDisappearing() {
		saveNotes();
	}

	/**
	 * Saves the notes in the interface to the database.
	 */
	private void saveNotes() {
		try (AuditDatabase db = new AuditDatabase(getContext())) {
			// Save the notes
			String store = hasStoreNotes() ? storeEdit.getText().toString() : null;
			db.updateNotes(notes, notesEdit.getText().toString(), store);
		} catch (MobileClientException exc) {
			// Already logged, since this should never happen we're going
			// to absorb this error quietly
		}
	}

	/**
	 * Indicates if store notes are enabled.
	 * @return Store notes enabled flag
	 */
	private boolean hasStoreNotes() {
		Security sec = new Security(getContext().getApplicationContext());
		return sec.optSettingBool(Security.SETTING_AUDIT_STORE_NOTES, false);
	}

	private static final String ARG_AUDIT = "auditArg";
	private static final String STATE_NOTES = "notes";
	private static final String STATE_STORE = "store";
	private EditText notesEdit;
	private EditText storeEdit;
	private Audit audit;
	private Notes notes;
}
