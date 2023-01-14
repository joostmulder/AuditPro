//
//  ReviewPage.swift
//  Mobile Client
//
//  Created by Eric Ruck on 4/4/18.
//  Copyright 2018-2019 AuditPro. All rights reserved.
//

import UIKit
import MessageUI


/**
 * Manages a fragment to display closed audits for review.
 * @author Eric Ruck
 */
class ReviewPage : BasePage, UITableViewDelegate, UITableViewDataSource, MFMailComposeViewControllerDelegate {
	@IBOutlet var noAuditsLabel: UILabel?
	@IBOutlet var auditTable: UITableView?

	/* Completed audits fetched from the database. */
	private var completedAudits: [Audit]? = nil

	/* Sync API client. */
	private var client: ApiClient?

	/* References title bar menu. */
	private var menuView: UIView? = nil

	/** Default e-mail recipient. */
	private let EMAIL_TO = ["accounts@auditpro.io"]
	private let EMAIL_SUBJ = "Completed Audit"
	private let EMAIL_BODY = "Completed audit of %@ attached"


	/**
	 * Initializes view contents on load.
	 */
	override func viewDidLoad() {
		super.viewDidLoad()

		// Connect to the audits database
		let db = AuditDatabase()
		completedAudits = db?.getCompleteAudits(userId: Security.userId)
		if (completedAudits?.count ?? 0 == 0) {
			showNoAudits()
		}
	}

	/**
	 * Provides the display page title.
	 * @return Display title
	 */
	override var pageName: String? {
		return "Review Closed Audits"
	}

	/**
	 * Makes sure we're not in the middle of an API call when we're
	 * exiting.
	 */
	override func onPageDisappearing() {
		client?.cancel()
	}

	/**
	 * Displays our menu in the status bar.
	 * @return Our menu view
	 */
	override func onCreateMenu() -> UIView? {
		// Have we loaded our menu view?
		if (menuView == nil) {
			// No, load it now
			menuView = Bundle.main.loadNibNamed("ReviewPageMenu",
				owner: nil, options: nil)![0] as? UIView
			if let syncAllView = menuView?.viewWithTag(1) as? UIButton {
				// Update the sync all option
				if (noAuditsLabel?.isHidden ?? false) {
					// Handle tap on sync all
					syncAllView.addTarget(self, action: #selector(onSyncAll), for: .touchUpInside)
				} else {
					// Nothing to sync
					syncAllView.isHidden = true
				}
			}
		}

		// Return the menu view
		return menuView
	}

	/**
	 * Shows the no audits state.
	 * @param view Our view
	 */
	private func showNoAudits() {
		noAuditsLabel?.isHidden = false
		auditTable?.isHidden = true
		menuView?.viewWithTag(1)?.isHidden = true
	}

	/**
	 * Gets the number of rows to display in the audit table.
	 * @param tableView View requesting count
	 * @param section Section query (only one section, should always be 0)
	 */
	func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
		return completedAudits?.count ?? 0
	}

	/** Cached row height */
	private var rowHeight: CGFloat? = nil

	/**
	 * Provides the custom height for our cells.
	 * @param table Table in which cells live
	 * @param indexPath Path to row whose height we want
	 * @return Row height
	 */
	func tableView(_ tableView: UITableView, heightForRowAt indexPath: IndexPath) -> CGFloat {
		if (rowHeight == nil) {
			if let views = Bundle.main.loadNibNamed("ReviewPageTableCell", owner: nil, options: nil) {
				if let view = views[0] as? UITableViewCell {
					rowHeight = view.bounds.height
				}
			}
		}
		return rowHeight ?? 0
	}

	/**
	 * Provides a cell view for a table row.
	 * @param tableView Table who needs row view
	 * @param indexPath Path of needed row
	 * @return Row view
	 */
	func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
		// Get the cell to use
		var cell = tableView.dequeueReusableCell(withIdentifier: "audit")
		if (cell == nil) {
			cell = Bundle.main.loadNibNamed("ReviewPageTableCell", owner: nil, options: nil)![0] as? UITableViewCell
		}

		let storeLabel = cell?.viewWithTag(1) as! UILabel?
		let stampLabel = cell?.viewWithTag(2) as! UILabel?

		// Get the audit to show
		guard let audit = completedAudits?[indexPath.row] else {
			storeLabel?.text = "(Missing value)"
			stampLabel?.text = "(Missing value)"
			return cell!
		}

		// Populate the cell
		let formatter = DateFormatter()
		formatter.locale = Locale.current
		formatter.timeStyle = .none
		formatter.dateStyle = .medium
		storeLabel?.text = audit.storeDescr
		stampLabel?.text = formatter.string(from: audit.auditStartedAt)
		return cell!
	}

	/**
	 * Handles selection of a completed audit.
	 * @param tableView Table from which audit was selected
	 * @param indexPath Path of selected row
	 */
	func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
		// Get the selected audit
		guard let audit = completedAudits?[indexPath.row] else {
			// Should never happen, no audit
			NSLog("Unexpected invalid row selection")
			return
		}

		// Clear the selection
		tableView.deselectRow(at: indexPath, animated: true)

		// Format audit description
		let formatter = DateFormatter()
		formatter.locale = Locale.current
		formatter.dateStyle = .short
		formatter.timeStyle = .none
		let message = String(format: "%@\nStarted at %@\nEnded at %@",
				audit.storeDescr,
				formatter.string(from: audit.auditStartedAt),
				formatter.string(from: audit.auditEndedAt!))

		// Show selected audit with options
		let alert = UIAlertController(title: message, message: nil, preferredStyle: .actionSheet)
		alert.addAction(UIAlertAction(title: "Sync", style: .default) { action in
			self.onSync(audit: audit)
		})
		if (MFMailComposeViewController.canSendMail()) {
			alert.addAction(UIAlertAction(title: "E-mail", style: .default) { action in
				self.onEmail(audit: audit)
			})
		}
		alert.addAction(UIAlertAction(title: "Edit", style: .default) { action in
			self.onEdit(audit: audit)
		})
		alert.addAction(UIAlertAction(title: "Remove", style: .default) { action in
			self.onRemove(audit: audit)
		})
		alert.addAction(UIAlertAction(title: "Cancel", style: .cancel))
		present(alert, animated: true)
	}


	/**
	 * Sets up an e-mail for the requested audit.
	 * @param audit Audit to e-mail
	 */
	private func onEmail(audit: Audit) {
		// Serialize the audit
		let db = AuditDatabase()
		guard
			let json = db?.serialize(audit: audit),
			let attachment = json.data(using: .utf8)
		else {
			// Failed to serialize the audit
			view.makeToast(message: "Error encoding audit attachment, please contact customer support.")
			return
		}

		// Format the attachment file name
		let formatter = DateFormatter()
		formatter.dateFormat = "'audit-'yyMMddHHmmssZ'.json'"
		let fileName = formatter.string(from: audit.auditStartedAt)

		// Send the e-mail
		Analytics.log(name: "Review", audit: audit, key: "Email", value: "Email")
		let mail = MFMailComposeViewController()
		mail.mailComposeDelegate = self
		mail.setToRecipients(EMAIL_TO)
		mail.setSubject(EMAIL_SUBJ)
		mail.setMessageBody(String(format: EMAIL_BODY, audit.description), isHTML: false)
		mail.addAttachmentData(attachment, mimeType: "application/json", fileName: fileName)
		present(mail, animated: true)
	}

	/**
	 * Handles e-mail composition completion.
	 * @param controller Composer controller
	 * @param result Completion result code
	 * @param error Error description or nil on success
	 */
	func mailComposeController(_ controller: MFMailComposeViewController, didFinishWith result: MFMailComposeResult, error: Error?) {
		dismiss(animated: true)
	}

	/**
	 * Reopens the passed audit for editing.
	 * @param audit Audit to edit
	 */
	private func onEdit(audit: Audit) {
		// Attempt to reopen the audit
		guard let db = AuditDatabase() else {
			// Failed to access database
			view.makeToast(message: "Internal error, please contact customer support.")
			return
		}
		if db.resumeAudit(userId: Security.userId) != nil {
			// Another audit is currently in progress
			view.makeToast(message: "An audit is already in process. Complete the current audit from the main menu, then edit this one.")
			return
		}
		guard let reopened = db.reopenAudit(audit) else {
			// Failed to reopen this audit
			view.makeToast(message: "Unable to edit this audit at this time.")
			return
		}

		// Show the reopened audit
		Analytics.log(name: "Review", audit: audit, key: "Edit", value: "Reopen")
		host?.swapPage(SelectProductPage(audit: reopened))
	}

	/**
	 * Confirms request to remove the passed audit.
	 * @param audit Audit to remove
	 */
	private func onRemove(audit: Audit) {
		let message = String(format:
			"Are you certain you want to remove the audit %@? " +
			"It will be permanently deleted from your phone.",
			audit.description)
		let alert = UIAlertController(title: "Remove Audit",
			message: message, preferredStyle: .alert)
		alert.addAction(UIAlertAction(title: "Yes", style: .destructive) { action in
			Analytics.log(name: "Review", audit: audit, key: "Remove", value: "Remove")
			self.performRemove(audit: audit)
		})
		alert.addAction(UIAlertAction(title: "No", style: .cancel) { action in
			Analytics.log(name: "Review", audit: audit, key: "Remove", value: "Cancel")
		})
		present(alert, animated: true)
	}

	/**
	 * Removes the passed audit from the database.
	 * @param audit Audit to remove
	 */
	private func performRemove(audit: Audit) {
		// Remove the posted audit from our cache
		let db = AuditDatabase()
		guard db?.delete(audit: audit) ?? false else {
			view.makeToast(message: "Failed to remove audit, please contact customer support.")
			return
		}
		// Remove the audit from the list
		if let index = completedAudits?.firstIndex(of: audit) {
			completedAudits?.remove(at: index)
			auditTable?.reloadData()
		}
	}

	/**
	 * Confirms request to sync the passed audit.
	 * @param audit Audit to sync
	 */
	private func onSync(audit: Audit) {
		let message = String(format:
			"Are you certain you want to sync the audit %@? " +
			"On success the audit will be removed from your phone.",
			audit.description)
		let alert = UIAlertController(title: "Sync Audit",
			message: message, preferredStyle: .alert)
		alert.addAction(UIAlertAction(title: "Yes", style: .default) { action in
			Analytics.log(name: "Review", audit: audit, key: "Sync", value: "Sync")
			self.performSync(audit: audit)
		})
		alert.addAction(UIAlertAction(title: "Cancel", style: .cancel) { action in
			Analytics.log(name: "Review", audit: audit, key: "Sync", value: "Cancel")
		})
		present(alert, animated: true)
	}

	/**
	 * Performs the sync of a single audit.
	 * @param audit Audit to sync
	 */
	private func performSync(audit: Audit) {
		guard
			// Serialize the audit
			let db = AuditDatabase(),
			let json = db.serialize(audit: audit)
		else {
			// Failed to serialize the audit
			view.makeToast(message: "Failed to serialize the audit. Please contact customer support.")
			return
		}

		// Send the audit to the web service
		host?.isActivityShowing = true
		client = ApiClient(token: AppDelegate.sessionToken)
		client!.postPayload(auditJson: json, completion: { client in
			// Is the review page still current?
			self.client = nil
			if (self.host != nil) {
				DispatchQueue.main.async {
					// Update the user interface on the main thread
					self.host?.isActivityShowing = false
					if (client.message != nil) {
						self.view.makeToast(message: client.message!)
					} else {
						// Remove the posted audit from our cache
						db.delete(audit: audit)
						if let index = self.completedAudits?.firstIndex(of: audit) {
							self.completedAudits?.remove(at: index)
							self.auditTable?.reloadData()
							if (self.completedAudits?.count ?? 0) == 0 {
								self.showNoAudits()
							}
						}
					}
				}
			}
		})
	}

	/**
	 * Handles request to sync all waiting audits.
	 */
	@objc private func onSyncAll() {
		// Offer set unscanned options
		let message = AppDelegate.isWifiConnected
			? "Are you sure you want to sync all of the completed audits? Synced audits will be removed from yor phone."
			: "Are you sure you want to sync all of the completed audits? Synced audits will be removed from yor phone. Cellular data may be used."
		let alert = UIAlertController(title: "Sync All",
			message: message, preferredStyle: .alert)
		alert.addAction(UIAlertAction(title: "Yes", style: .default) { action in
			Analytics.menuAction(type: "Sync All", details: "sync")
			self.host?.requestSync()
		})
		alert.addAction(UIAlertAction(title: "Cancel", style: .cancel) { action in
			Analytics.menuAction(type: "Sync All", details: "cancel")
		})
		present(alert, animated: true)
	}
}
