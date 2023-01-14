//
//  NotesPage.swift
//  Mobile Client
//
//  Created by Eric Ruck on 3/25/18.
//  Copyright 2018-2019 AuditPro. All rights reserved.
//

import UIKit


/**
 * Manages the notes associated with an audit in progress.
 * @author Eric Ruck
 */
class NotesPage : BasePage {
	// Control outlets
	@IBOutlet private var notesLabel: UILabel?
	@IBOutlet private var notesEdit: UITextView?
	@IBOutlet private var storeLabel: UILabel?
	@IBOutlet private var storeEdit: UITextView?

	// Notes entities
	private var audit: Audit?
	private var notes: Notes?


	/**
	 * Initializes to edit the note for the passed audit.
	 * @param audit Audit whose note we want to edit
	 */
	convenience init(audit: Audit) {
		self.init()
		self.audit = audit
	}

	/**
	 * Initializes the loaded view.
	 */
	override func viewDidLoad() {
		super.viewDidLoad()

		// Log use of notes
		let auditId = audit?.id.uuidString ?? "?"
		Analytics.log(name: "Notes", key: "Audit Id", value: auditId)

		// Get the note to edit
		let db = AuditDatabase()
		notes = db?.getNotes(forAudit: audit!)
		if (notes == nil) {
			view.makeToast(message: "Failed to load notes for audit")
			notes = Notes(id: nil, auditId: audit!.id, contents: "", store: "")
		}

		// Complete initialization
		notesEdit?.text = notes?.contents

		// Should we show store notes?
		if (!hasStoreNotes) {
			// Hide controls related to store notes
			notesLabel?.isHidden = true
			storeLabel?.isHidden = true
			storeEdit?.isHidden = true
			storeEdit?.text = ""
			notesEdit?.translatesAutoresizingMaskIntoConstraints = true
			notesEdit?.frame = view.bounds
		} else {
			// Initialize store notes
			storeEdit?.text = notes?.store

			// Set edit borders
			notesEdit?.layer.borderColor = UIColor.black.cgColor
			notesEdit?.layer.borderWidth = 1.0
			notesEdit?.layer.cornerRadius = 5.0
			storeEdit?.layer.borderColor = UIColor.black.cgColor
			storeEdit?.layer.borderWidth = 1.0
			storeEdit?.layer.cornerRadius = 5.0
		}
	}

	/**
	 * Provides the name of the page to display in the status bar.
	 */
	override var pageName: String? {
		return "Notes"
	}

	/**
	 * Indicates that the main controller should adjust the page view so the
	 * keyboard does not cover.
	 */
	override var shrinkForKeyboard: Bool {
		return true;
	}

	/**
	 * Attempts to save notes on the way out.
	 */
	override func onBack() -> Bool {
		return !saveNotes()
	}

	/**
	 * Keep location turned on for audit store selection.
	 */
	override var isLocationNeeded: BasePage.LocationNeed {
		return  .keep
	}

	/**
	 * Saves the notes in the interface to the database.
	 * @return Success flag
	 */
	private func saveNotes() -> Bool {
		let db = AuditDatabase()
		let storeNotes = hasStoreNotes ? (storeEdit?.text ?? "") : ""
		if let errorMsg = db?.updateNotes(notes: notes!, contents: notesEdit?.text ?? "", store: storeNotes) {
			// Failed to save notes
			// TODO We probably want to take this out for the production build
			let alert = UIAlertController(title: "Save Note Error",
			message: errorMsg, preferredStyle: .alert)
			alert.addAction(UIAlertAction(title: "Discard", style: .destructive) { action in
				self.host!.popPage()
			})
			alert.addAction(UIAlertAction(title: "Retry", style: .cancel))
			present(alert, animated: true)
			return false
		}
		return true
	}

	/**
	 * Indicates if store notes are enabled.
	 * @return Store notes enabled flag
	 */
	private var hasStoreNotes: Bool {
		return Security.optSetting(name: Security.SETTING_AUDIT_STORE_NOTES, defaultValue: false)
	}
}
